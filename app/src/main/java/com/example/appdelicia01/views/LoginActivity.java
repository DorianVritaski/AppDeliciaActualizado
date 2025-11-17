package com.example.appdelicia01.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdelicia01.R;
import com.example.appdelicia01.controllers.LoginController;
import com.example.appdelicia01.controllers.LoginView;
import com.example.appdelicia01.models.User;
import com.example.appdelicia01.models.UserManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// --- CAMBIO 1: La Activity ahora implementa UserDataChangeListener ---
public class LoginActivity extends AppCompatActivity implements LoginView, UserManager.UserDataChangeListener {

    // --- VISTAS (Views) ---
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBarLogin;

    // --- CONTROLADOR (Controller) ---
    private LoginController controller;

    // --- OTROS ---
    private static final int REGISTER_REQUEST_CODE = 1002;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        controller = new LoginController(this);

        // --- CAMBIO 2: Registrar esta Activity como listener ---
        UserManager.getInstance().addUserDataChangeListener(this);

        // Inicializar vistas
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBarLogin = findViewById(R.id.progressBarLogin);

        // La Vista captura el evento y lo delega al Controlador
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim().toLowerCase();
            String password = etPassword.getText().toString().trim();
            controller.performLogin(email, password);
        });

        // La lógica de navegación a otras vistas (como Register) se queda en la Activity
        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                Log.d(TAG, "tvRegister clicked, navigating to RegisterActivity.");
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, REGISTER_REQUEST_CODE);
            });
        }

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Toast.makeText(LoginActivity.this, "Funcionalidad pendiente", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // --- CAMBIO 3: Simplificar la lógica de onStart ---
        // Ya no llamamos a loadUserData. El AuthStateListener de UserManager lo hará automáticamente.
        // Simplemente verificamos si hay un usuario en Auth. Si lo hay, el listener
        // (onUserDataChanged) se encargará de la navegación cuando los datos estén listos.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "onStart: Usuario ya logueado en Firebase Auth (" + currentUser.getEmail() + "). Esperando a UserManager...");
            showLoading(true); // Mostramos un loading mientras UserManager trabaja en segundo plano.
        } else {
            Log.d(TAG, "onStart: Ningún usuario logueado.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // --- CAMBIO 4: Desregistrar el listener para evitar memory leaks ---
        UserManager.getInstance().removeUserDataChangeListener(this);
    }


    // --- CAMBIO 5: Implementar el método de la interfaz ---
    @Override
    public void onUserDataChanged() {
        // Este método es llamado por UserManager cuando los datos del usuario están listos.
        User userData = UserManager.getInstance().getCurrentUserData();

        // Solo navegamos si estamos en esta activity y si los datos del usuario se han cargado.
        if (userData != null) {
            Log.d(TAG, "onUserDataChanged: Datos de usuario listos. Navegando al catálogo.");
            // Usamos runOnUiThread para asegurarnos de que la UI se actualice en el hilo principal
            runOnUiThread(() -> {
                showLoading(false);
                onLoginSuccess(); // Reutilizamos el método para la navegación
            });
        } else {
            // Si los datos del usuario son nulos, significa que no hay sesión o se cerró.
            // Nos aseguramos de que no haya un loading spinner activo.
            runOnUiThread(() -> showLoading(false));
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Manejar el resultado de la pantalla de registro
        if (requestCode == REGISTER_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Registro exitoso. Por favor, inicia sesión.", Toast.LENGTH_LONG).show();
            if (data != null && data.hasExtra("REGISTERED_EMAIL")) {
                etEmail.setText(data.getStringExtra("REGISTERED_EMAIL"));
                etPassword.requestFocus();
            }
        }
    }

    // =====================================================================================
    // --- IMPLEMENTACIÓN DE LOS MÉTODOS DE LA INTERFAZ LoginView ---
    // =====================================================================================

    @Override
    public void showLoading(boolean isLoading) {
        if (isLoading) {
            btnLogin.setEnabled(false);
            btnLogin.setAlpha(0.5f);
            progressBarLogin.setVisibility(View.VISIBLE);
        } else {
            btnLogin.setEnabled(true);
            btnLogin.setAlpha(1.0f);
            progressBarLogin.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoginSuccess() {
        // No mostramos el Toast aquí si es un auto-login, pero la navegación es la misma.
        // Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, CatalogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onLoginError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onValidationError(String field, String message) {
        if ("email".equals(field)) {
            tilEmail.setError(message);
        } else if ("password".equals(field)) {
            tilPassword.setError(message);
        }
    }

    @Override
    public void clearValidationErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }
}
