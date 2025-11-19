package com.example.appdelicia01.controllers;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
// --- PASO 1: IMPORTA LA CLASE 'Source' ---
import com.google.firebase.firestore.Source;

public class ProfileController {

    private final ProfileView view;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private static final String TAG = "ProfileController";

    public ProfileController(ProfileView view) {
        this.view = view;
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public void loadUserProfile() {
        view.showLoading(true);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Log.e(TAG, "Error: No se encontró usuario actual. Navegando a Login.");
            view.displayError("Sesión no encontrada. Por favor, inicie sesión de nuevo.");
            view.navigateToLogin();
            return;
        }

        // Al recargar el perfil, el `displayName` del objeto `currentUser` puede estar desactualizado.
        // Por eso, es más seguro ir directamente a Firestore para obtener los datos frescos.
        final String email = currentUser.getEmail() != null ? currentUser.getEmail() : "Email no disponible";
        Log.d(TAG, "Cargando perfil desde Firestore para asegurar datos actualizados...");
        loadNameFromFirestore(currentUser.getUid(), email);
    }

    private void loadNameFromFirestore(String userId, final String email) {
        DocumentReference userDocRef = db.collection("users").document(userId);

        // --- PASO 2: AÑADE .get(Source.SERVER) PARA IGNORAR EL CACHÉ ---
        userDocRef.get(Source.SERVER).addOnCompleteListener(task -> { // <-- CAMBIO CLAVE
            view.showLoading(false);
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String fullName = document.getString("fullName");
                    if (fullName != null && !fullName.isEmpty()) {
                        Log.d(TAG, "Nombre cargado desde el SERVIDOR de Firestore: " + fullName);
                        view.displayUserProfile(fullName, email);
                    } else {
                        Log.w(TAG, "El campo 'fullName' no existe o está vacío en Firestore para UID: " + userId);
                        view.displayUserProfile("Nombre no registrado", email);
                    }
                } else {
                    Log.w(TAG, "No se encontró documento en Firestore para UID: " + userId);
                    view.displayUserProfile("Datos de perfil no encontrados", email);
                }
            } else {
                Log.e(TAG, "Error al obtener datos de Firestore: ", task.getException());
                view.displayError("Error al cargar nombre del perfil.");
                view.displayUserProfile("Error al cargar nombre", email);
            }
        });
    }

    public void performLogout() {
        mAuth.signOut();
        Log.d(TAG, "Usuario cerró sesión.");
        view.navigateToLogin();
    }
}
