package com.example.appdelicia01.controllers;

import android.util.Log;
import android.util.Patterns;
import com.example.appdelicia01.models.UserManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginController {

    private final LoginView view;
    private final FirebaseAuth mAuth;
    private static final String TAG = "LoginController";

    public LoginController(LoginView view) {
        this.view = view;
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void performLogin(String email, String password) {
        if (view == null) return;

        view.clearValidationErrors();

        boolean isValid = true;
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
        }

        if (!isValid) {
            return;
        }

        view.showLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (view == null) return; // Comprobar si la vista todavía existe

                    if (task.isSuccessful()) {
                        // --- CAMBIO CLAVE ---
                        // Ya no llamamos a ningún método aquí.
                        // El AuthStateListener de UserManager se activará automáticamente,
                        // cargará los datos del usuario y notificará a LoginActivity,
                        // que se encargará de la navegación.
                        Log.d(TAG, "signInWithEmail: success. UserManager se encargará del resto.");
                        // El `showLoading(false)` ahora será manejado por la LoginActivity
                        // cuando reciba el evento onUserDataChanged.
                    } else {
                        view.showLoading(false);
                        String errorMessage = "Error de autenticación.";
                        try {
                            // Lanza la excepción para poder capturarla y determinar el tipo de error.
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            errorMessage = "No existe una cuenta con este correo electrónico.";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            errorMessage = "La contraseña es incorrecta.";
                        } catch (Exception e) {
                            Log.e(TAG, "Error en el inicio de sesión: ", e);
                            if (e.getMessage() != null) {
                                errorMessage = e.getMessage();
                            }
                        }
                        view.onLoginError(errorMessage);
                    }
                });
    }

    // --- CAMBIO CLAVE 2: Eliminar el método obsoleto ---
    /*
    private void loadUserDataAndNotifyView() {
        // ESTE MÉTODO YA NO ES NECESARIO Y SE HA ELIMINADO.
    }
    */
}
