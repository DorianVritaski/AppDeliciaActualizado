package com.example.appdelicia01.controllers;

import android.util.Log;
import android.util.Patterns;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddProductController {

    private final AddProductView view;
    private final FirebaseFirestore db;
    private static final String TAG = "AddProductController";

    public AddProductController(AddProductView view) {
        this.view = view;
        this.db = FirebaseFirestore.getInstance();
    }

    public void saveProduct(String name, String priceStr, String description, String imageUrl) {
        view.clearAllErrors();

        if (!validateInput(name, priceStr, imageUrl)) {
            return; // La validación falló
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            view.showValidationError("price", "Ingrese un precio válido");
            return;
        }

        view.showLoading(true);

        // Crear mapa de datos del producto
        Map<String, Object> product = new HashMap<>();
        product.put("name", name);
        product.put("price", price);
        product.put("description", description);
        product.put("imageUrl", imageUrl);
        product.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        // Añadir a la colección "products"
        db.collection("products")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Producto guardado con ID: " + documentReference.getId());
                    view.showLoading(false);
                    view.onProductSavedSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar producto", e);
                    view.showLoading(false);
                    view.onProductSaveError("Error al guardar producto: " + e.getMessage());
                });
    }

    private boolean validateInput(String name, String priceStr, String imageUrl) {
        boolean isValid = true;
        if (name.isEmpty()) {
            view.showValidationError("name", "El nombre es requerido");
            isValid = false;
        }

        if (priceStr.isEmpty()) {
            view.showValidationError("price", "El precio es requerido");
            isValid = false;
        }

        if (imageUrl.isEmpty()) {
            view.showValidationError("imageUrl", "La URL de la imagen es requerida");
            isValid = false;
        } else if (!Patterns.WEB_URL.matcher(imageUrl).matches()) {
            view.showValidationError("imageUrl", "Ingrese una URL válida");
            isValid = false;
        }

        return isValid;
    }
}

