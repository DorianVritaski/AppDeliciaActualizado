package com.example.appdelicia01.controllers;

import android.util.Log;
import android.util.Patterns;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterController {

    private final RegisterView view;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private static final String TAG = "RegisterController";

    public RegisterController(RegisterView view) {
        this.view = view;
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public void performRegister(String fullName, String email, String password, String confirmPassword) {
        view.clearValidationErrors();

        if (!validateInput(fullName, email, password, confirmPassword)) {
            return; // La validación falló
        }

        view.showLoading(true);

        // 1. Crear usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // 2. Guardar información adicional en Firestore
                            saveUserDataInFirestore(user, fullName, email);
                        } else {
                            view.showLoading(false);
                            view.onRegisterError("Error al obtener usuario después del registro.");
                        }
                    } else {
                        view.showLoading(false);
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        String errorMessage = "Fallo el registro.";
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {
                            errorMessage = "Este correo electrónico ya está registrado. Intenta iniciar sesión.";
                        } catch (Exception e) {
                            if (e.getMessage() != null) {
                                errorMessage = e.getMessage();
                            }
                        }
                        view.onRegisterError(errorMessage);
                    }
                });
    }

    private boolean validateInput(String fullName, String email, String password, String confirmPassword) {
        boolean isValid = true;

        if (fullName.isEmpty()) {
            view.onValidationError("fullName", "El nombre completo es requerido");
            isValid = false;
        }

        if (email.isEmpty()) {
            view.onValidationError("email", "El correo es requerido");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.onValidationError("email", "Ingrese un correo válido");
            isValid = false;
        }

        if (password.isEmpty()) {
            view.onValidationError("password", "La contraseña es requerida");
            isValid = false;
        } else if (password.length() < 6) {
            view.onValidationError("password", "La contraseña debe tener al menos 6 caracteres");
            isValid = false;
        }

        if (confirmPassword.isEmpty()) {
            view.onValidationError("confirmPassword", "Confirme la contraseña");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            view.onValidationError("confirmPassword", "Las contraseñas no coinciden");
            isValid = false;
        }
        return isValid;
    }

    private void saveUserDataInFirestore(FirebaseUser firebaseUser, String fullName, String email) {
        String userId = firebaseUser.getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("role", "customer"); // Rol por defecto
        userData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Datos de usuario guardados en Firestore.");
                    // 3. Actualizar el perfil de Firebase Auth
                    updateFirebaseUserProfile(firebaseUser, fullName, email);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al guardar datos en Firestore", e);
                    view.showLoading(false);
                    view.onRegisterError("Error al guardar los datos del usuario.");
                });
    }

    private void updateFirebaseUserProfile(FirebaseUser firebaseUser, String fullName, String email) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();

        firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    view.showLoading(false); // El proceso termina aquí, con o sin éxito en este paso final
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Perfil de Firebase Auth actualizado.");
                    } else {
                        Log.w(TAG, "Error al actualizar perfil de Auth.", task.getException());
                        // Opcional: podrías notificar a la vista, pero no es crítico.
                    }
                    // 4. Notificar a la vista que todo el proceso fue exitoso
                    view.onRegisterSuccess(email);
                });
    }
}
