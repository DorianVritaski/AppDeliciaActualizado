package com.example.appdelicia01.controllers;

import android.util.Log;

import com.example.appdelicia01.models.CartManager;
import com.example.appdelicia01.models.Order;
import com.example.appdelicia01.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderController {
    private static final String TAG = "OrderController";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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

    public void getOrderById(String orderId, OrderDetailsListener listener) {
        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Order order = documentSnapshot.toObject(Order.class);
                        order.setOrderId(documentSnapshot.getId());
                        listener.onOrderLoaded(order);
                    } else {
                        listener.onDataLoadFailed("No se encontró el pedido.");
                    }
                })
                .addOnFailureListener(e -> listener.onDataLoadFailed(e.getMessage()));
    }

    public interface OrderUpdateListener {
        void onOrderUpdated();
        void onUpdateFailed(String error);
    }

    public void updateOrderStatus(String orderId, String newStatus, OrderUpdateListener listener) {
        db.collection("orders").document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> listener.onOrderUpdated())
                .addOnFailureListener(e -> listener.onUpdateFailed(e.getMessage()));
    }


    public void loadAllOrders(OrdersLoadListener listener) {
        db.collection("orders")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING) // Los más nuevos primero
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        order.setOrderId(document.getId()); // Guardar el ID del documento en el objeto
                        orders.add(order);
                    }
                    listener.onOrdersLoaded(orders);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando pedidos", e);
                    listener.onDataLoadFailed(e.getMessage());
                });
    }

    public void loadOrdersForCurrentUser(OrdersLoadListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onDataLoadFailed("Usuario no autenticado.");
            return;
        }

        db.collection("orders")
                .whereEqualTo("userId", currentUser.getUid()) // <-- LA CLAVE ES ESTE FILTRO
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
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

    public OrderController() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void createOrder(String deliveryMethod, String address, String paymentMethod, double subtotal, double deliveryFee, double totalAmount, OrderCreationListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onOrderCreationFailed("Usuario no autenticado.");
            return;
        }

        // Convertir el carrito a un formato compatible con Firestore
        Map<String, Object> cartItemsForFirestore = new HashMap<>();
        for (Map.Entry<Product, Integer> entry : CartManager.getInstance().getCartItems().entrySet()) {
            Product p = entry.getKey();
            Map<String, Object> productDetails = new HashMap<>();
            productDetails.put("name", p.getName());
            productDetails.put("price", p.getPrice());
            productDetails.put("quantity", entry.getValue());
            // Usamos el ID del producto como clave si lo tienes, sino el nombre
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

        // Guardar en Firestore
        db.collection("orders")
                .add(order) // .add() genera un ID automático
                .addOnSuccessListener(documentReference -> {
                    String orderId = documentReference.getId();
                    Log.d(TAG, "Pedido creado con ID: " + orderId);
                    // Limpiar el carrito después de crear el pedido
                    CartManager.getInstance().clearCart();
                    listener.onOrderCreated(orderId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear el pedido", e);
                    listener.onOrderCreationFailed(e.getMessage());
                });
    }
}

