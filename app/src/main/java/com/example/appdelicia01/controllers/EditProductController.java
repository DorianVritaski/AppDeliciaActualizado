package com.example.appdelicia01.controllers;

import android.util.Log;

import com.example.appdelicia01.models.Product;
import com.example.appdelicia01.controllers.EditProductView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProductController {

    private final EditProductView view;
    private final FirebaseFirestore db;
    private static final String TAG = "EditProductController";

    public EditProductController(EditProductView view) {
        this.view = view;
        this.db = FirebaseFirestore.getInstance();
    }

    public void loadProductDetails(String productId) {
        // ... (Este método no cambia, está perfecto)
        view.showLoading(true);
        db.collection("products").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    view.showLoading(false);
                    if (documentSnapshot.exists()) {
                        Product product = documentSnapshot.toObject(Product.class);
                        if (product != null) {
                            product.setId(documentSnapshot.getId());
                            view.displayProductDetails(product);
                        } else {
                            view.displayError("No se pudieron leer los datos del producto.");
                        }
                    } else {
                        view.displayError("El producto que intentas editar ya no existe.");
                    }
                })
                .addOnFailureListener(e -> {
                    view.showLoading(false);
                    Log.e(TAG, "Error al cargar los detalles del producto.", e);
                    view.displayError("Error de red al cargar el producto.");
                });
    }


    // <-- CAMBIO: El 5to argumento ahora es un String para la URL
    public void saveChanges(String productId, String name, String description, double price, String imageUrl) {
        view.showLoading(true);

        DocumentReference productRef = db.collection("products").document(productId);

        // Usamos un mapa para actualizar los campos
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("description", description);
        updates.put("price", price);
        updates.put("imageUrl", imageUrl); // Guardamos la nueva URL como texto

        productRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Producto actualizado con éxito en Firestore.");
                    view.showLoading(false);
                    view.onSaveChangesSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar el producto en Firestore", e);
                    view.showLoading(false);
                    view.displayError("No se pudieron guardar los cambios.");
                });
    }
}
