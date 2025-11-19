package com.example.appdelicia01.controllers;

import android.util.Log;

import com.example.appdelicia01.models.CartManager;
import com.example.appdelicia01.models.Order;
import com.example.appdelicia01.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderController {
    private static final String TAG = "OrderController";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // --- INTERFACES (SIN CAMBIOS) ---
    public interface OrderCreationListener {
        void onOrderCreated(String orderId);
        void onOrderCreationFailed(String error);
    }

    public interface OrdersLoadListener {
        void onOrdersLoaded(List<Order> orders);
        void onDataLoadFailed(String error);
    }

    public interface OrderDetailsListener {
        void onOrderLoaded(Order order);
        void onDataLoadFailed(String error);
    }

    public interface OrderUpdateListener {
        void onOrderUpdated();
        void onUpdateFailed(String error);
    }

    // --- CONSTRUCTOR Y MÉTODOS EXISTENTES (SIN CAMBIOS) ---
    public OrderController() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void getOrderById(String orderId, OrderDetailsListener listener) {
        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Order order = documentSnapshot.toObject(Order.class);
                        if (order != null) {
                            order.setOrderId(documentSnapshot.getId());
                            listener.onOrderLoaded(order);
                        }
                    } else {
                        listener.onDataLoadFailed("No se encontró el pedido.");
                    }
                })
                .addOnFailureListener(e -> listener.onDataLoadFailed(e.getMessage()));
    }

    public void updateOrderStatus(String orderId, String newStatus, OrderUpdateListener listener) {
        db.collection("orders").document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> listener.onOrderUpdated())
                .addOnFailureListener(e -> listener.onUpdateFailed(e.getMessage()));
    }

    public void loadAllOrders(OrdersLoadListener listener) {
        // Mantenemos esta llamada para compatibilidad, invoca al método de filtros sin filtros.
        loadFilteredOrders(null, null, listener);
    }

    // =========================================================================
    // ============== MÉTODO DE FILTRADO CON LÓGICA ALTERNATIVA ================
    // =========================================================================
    public void loadFilteredOrders(String nameFilter, Calendar dateFilter, OrdersLoadListener listener) {
        // Si el filtro de nombre está vacío, procedemos con la lógica simple.
        if (nameFilter == null || nameFilter.trim().isEmpty()) {
            loadOrdersSimple(null, dateFilter, listener);
            return;
        }

        // --- PASO 1: OBTENER TODOS LOS USUARIOS (para filtrar en el código) ---
        // Esto es factible para cientos o pocos miles de usuarios, pero no para millones.
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        Log.e(TAG, "Error buscando todos los usuarios", task.getException());
                        listener.onDataLoadFailed("Error al buscar clientes.");
                        return;
                    }

                    // --- PASO 2: FILTRAR MANUALMENTE EN EL CÓDIGO (Case-Insensitive) ---
                    String nameFilterLower = nameFilter.toLowerCase();
                    List<String> userIds = new ArrayList<>();

                    // Recorremos la lista de usuarios obtenida y filtramos en la memoria de la app.
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String fullName = document.getString("fullName");
                        if (fullName != null && fullName.toLowerCase().contains(nameFilterLower)) {
                            // Si el nombre completo del usuario (en minúsculas) CONTIENE
                            // el texto del filtro (en minúsculas), lo añadimos a la lista.
                            userIds.add(document.getId());
                        }
                    }

                    // Si después de filtrar no queda ningún usuario, devolvemos una lista vacía.
                    if (userIds.isEmpty()) {
                        listener.onOrdersLoaded(new ArrayList<>());
                        return;
                    }

                    // --- PASO 3: BUSCAR ÓRDENES CON LOS IDs ENCONTRADOS ---
                    // Esta parte no cambia.
                    loadOrdersSimple(userIds, dateFilter, listener);
                });
    }


    /**
     * Método auxiliar que busca órdenes filtrando por una lista de User IDs y/o fecha.
     * (ESTE MÉTODO NO TIENE CAMBIOS RESPECTO A LA ÚLTIMA VERSIÓN FUNCIONAL)
     */
    private void loadOrdersSimple(List<String> userIds, Calendar dateFilter, OrdersLoadListener listener) {
        Query query = db.collection("orders");

        // Aplicamos el filtro de IDs de usuario si existe.
        if (userIds != null && !userIds.isEmpty()) {
            if (userIds.size() <= 30) {
                query = query.whereIn("userId", userIds);
            } else {
                List<String> sublist = userIds.subList(0, 30);
                query = query.whereIn("userId", sublist);
                Log.w(TAG, "La búsqueda por nombre excedió los 30 resultados, mostrando solo los primeros 30.");
            }
        }

        // Aplicamos el filtro de fecha si existe.
        if (dateFilter != null) {
            Calendar startOfDay = (Calendar) dateFilter.clone();
            startOfDay.set(Calendar.HOUR_OF_DAY, 0); startOfDay.set(Calendar.MINUTE, 0); startOfDay.set(Calendar.SECOND, 0);

            Calendar endOfDay = (Calendar) dateFilter.clone();
            endOfDay.set(Calendar.HOUR_OF_DAY, 23); endOfDay.set(Calendar.MINUTE, 59); endOfDay.set(Calendar.SECOND, 59);

            Timestamp startTimestamp = new Timestamp(startOfDay.getTime());
            Timestamp endTimestamp = new Timestamp(endOfDay.getTime());

            query = query.whereGreaterThanOrEqualTo("timestamp", startTimestamp)
                    .whereLessThanOrEqualTo("timestamp", endTimestamp);
        }

        // El índice (userId ASC, timestamp DESC) permite esta consulta y ordenamiento.
        query = query.orderBy("timestamp", Query.Direction.DESCENDING);

        // Ejecutamos la consulta final
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Order> orders = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Order order = document.toObject(Order.class);
                    if (order != null) {
                        order.setOrderId(document.getId());
                        orders.add(order);
                    }
                }
                listener.onOrdersLoaded(orders);
            } else {
                Log.e(TAG, "Error final al cargar órdenes.", task.getException());
                if (task.getException() != null) {
                    listener.onDataLoadFailed(task.getException().getMessage());
                } else {
                    listener.onDataLoadFailed("No se encontraron pedidos con esos filtros.");
                }
            }
        });
    }


    public void loadOrdersForCurrentUser(OrdersLoadListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onDataLoadFailed("Usuario no autenticado.");
            return;
        }

        db.collection("orders")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        order.setOrderId(document.getId());
                        orders.add(order);
                    }
                    listener.onOrdersLoaded(orders);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando pedidos del usuario", e);
                    listener.onDataLoadFailed(e.getMessage());
                });
    }

    public void createOrder(String deliveryMethod, String address, String paymentMethod, double subtotal, double deliveryFee, double totalAmount, OrderCreationListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onOrderCreationFailed("Usuario no autenticado.");
            return;
        }

        Map<String, Object> cartItemsForFirestore = new HashMap<>();
        for (Map.Entry<Product, Integer> entry : CartManager.getInstance().getCartItems().entrySet()) {
            Product p = entry.getKey();
            Map<String, Object> productDetails = new HashMap<>();
            productDetails.put("name", p.getName());
            productDetails.put("price", p.getPrice());
            productDetails.put("quantity", entry.getValue());
            cartItemsForFirestore.put(p.getName(), productDetails);
        }

        Order order = new Order(
                currentUser.getUid(),
                currentUser.getEmail(),
                cartItemsForFirestore,
                deliveryMethod,
                address,
                paymentMethod,
                subtotal,
                deliveryFee,
                totalAmount
        );

        db.collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    String orderId = documentReference.getId();
                    Log.d(TAG, "Pedido creado con ID: " + orderId);
                    CartManager.getInstance().clearCart();
                    listener.onOrderCreated(orderId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear el pedido", e);
                    listener.onOrderCreationFailed(e.getMessage());
                });
    }
}
