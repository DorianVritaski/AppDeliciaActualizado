package com.example.appdelicia01.controllers;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class UserController {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    // Interfaz para comunicar el resultado de la actualización
    public interface UserUpdateListener {
        void onUpdateSuccess();
        void onUpdateFailure(String error);
    }

    public void updateUserProfile(String newFullName, UserUpdateListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onUpdateFailure("No hay un usuario autenticado.");
            return;
        }

        // Solo actualizaremos el nombre, no el email ni otros datos sensibles
        DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newFullName); // Asegúrate que el campo se llama 'fullName'

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> listener.onUpdateSuccess())
                .addOnFailureListener(e -> listener.onUpdateFailure(e.getMessage()));
    }
}
