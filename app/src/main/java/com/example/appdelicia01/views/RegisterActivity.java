package com.example.appdelicia01.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdelicia01.R;
import com.example.appdelicia01.controllers.RegisterController;
import com.example.appdelicia01.controllers.RegisterView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity implements RegisterView {

    // --- VISTAS (Views) ---
    private TextInputLayout tilFullName, tilEmailRegister, tilPasswordRegister, tilConfirmPassword;
    private TextInputEditText etFullName, etEmailRegister, etPasswordRegister, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginHere;
    private ProgressBar progressBarRegister;

    // --- CONTROLADOR (Controller) ---
    private RegisterController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Se instancia el Controlador
        controller = new RegisterController(this);

        // Inicializar vistas
        tilFullName = findViewById(R.id.tilFullName);
        etFullName = findViewById(R.id.etFullName);
        tilEmailRegister = findViewById(R.id.tilEmailRegister);
        etEmailRegister = findViewById(R.id.etEmailRegister);
        tilPasswordRegister = findViewById(R.id.tilPasswordRegister);
        etPasswordRegister = findViewById(R.id.etPasswordRegister);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginHere = findViewById(R.id.tvLoginHere);
        progressBarRegister = findViewById(R.id.progressBarRegister);

        // La Vista captura el evento y lo delega al Controlador
        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmailRegister.getText().toString().trim().toLowerCase();
            String password = etPasswordRegister.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            controller.performRegister(fullName, email, password, confirmPassword);
        });

        // La navegación es responsabilidad de la Vista
        tvLoginHere.setOnClickListener(v -> finish());
    }

    // ====================================================================
    // --- IMPLEMENTACIÓN DE LOS MÉTODOS DE LA INTERFAZ RegisterView ---
    // ====================================================================

    @Override
    public void showLoading(boolean isLoading) {
        if (isLoading) {
            btnRegister.setEnabled(false);
            btnRegister.setAlpha(0.5f);
            progressBarRegister.setVisibility(View.VISIBLE);
        } else {
            btnRegister.setEnabled(true);
            btnRegister.setAlpha(1.0f);
            progressBarRegister.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRegisterSuccess(String email) {
        Toast.makeText(this, "Registro exitoso. Ahora puedes iniciar sesión.", Toast.LENGTH_LONG).show();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("REGISTERED_EMAIL", email);
        setResult(RESULT_OK, resultIntent);
        finish(); // Cierra RegisterActivity y vuelve a LoginActivity
    }

    @Override
    public void onRegisterError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onValidationError(String field, String message) {
        switch (field) {
            case "fullName":
                tilFullName.setError(message);
                break;
            case "email":
                tilEmailRegister.setError(message);
                break;
            case "password":
                tilPasswordRegister.setError(message);
                break;
            case "confirmPassword":
                tilConfirmPassword.setError(message);
                break;
        }
    }

    @Override
    public void clearValidationErrors() {
        tilFullName.setError(null);
        tilEmailRegister.setError(null);
        tilPasswordRegister.setError(null);
        tilConfirmPassword.setError(null);
    }
}
