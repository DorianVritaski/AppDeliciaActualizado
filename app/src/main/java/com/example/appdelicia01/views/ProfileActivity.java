package com.example.appdelicia01.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appdelicia01.R;
import com.example.appdelicia01.controllers.ProfileController;
import com.example.appdelicia01.controllers.ProfileView;

public class ProfileActivity extends AppCompatActivity implements ProfileView {

    // --- VISTAS (Views) ---
    private ImageView ivProfileUserAvatar;
    private TextView tvProfileFullName, tvProfileEmail;
    private Button btnProfileLogout;
    private Button btnEditProfile; // <-- NUEVO: Declarar el botón de editar
    private ProgressBar progressBarProfile;

    // --- CONTROLADOR (Controller) ---
    private ProfileController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Se instancia el Controlador
        controller = new ProfileController(this);

        // --- Configurar la Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Perfil");
        }

        // Inicializar vistas
        ivProfileUserAvatar = findViewById(R.id.ivProfileUserAvatar);
        tvProfileFullName = findViewById(R.id.tvProfileFullName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        btnProfileLogout = findViewById(R.id.btnProfileLogout);
        progressBarProfile = findViewById(R.id.progressBarProfile);
        btnEditProfile = findViewById(R.id.btnEditProfile); // <-- NUEVO: Enlazar el botón desde el XML

        // La Vista delega la acción de logout al controlador
        btnProfileLogout.setOnClickListener(v -> controller.performLogout());

        // <-- NUEVO: Configurar el listener para el botón de editar perfil
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    // <-- NUEVO: Se sobrescribe onResume para recargar los datos
    @Override
    protected void onResume() {
        super.onResume();
        // La Vista le pide al controlador que cargue los datos cada vez que la pantalla es visible.
        // Esto asegura que si el usuario edita su perfil, los cambios se reflejen al volver.
        controller.loadUserProfile();
    }

    // ================================================================
    // --- IMPLEMENTACIÓN DE LOS MÉTODOS DE LA INTERFAZ ProfileView ---
    // ================================================================

    @Override
    public void showLoading(boolean isLoading) {
        if (progressBarProfile != null) {
            progressBarProfile.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayUserProfile(String fullName, String email) {
        tvProfileFullName.setText(fullName);
        tvProfileEmail.setText(email);
        ivProfileUserAvatar.setImageResource(R.drawable.user_image); // Placeholder
    }

    @Override
    public void displayError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void navigateToLogin() {
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Manejar el clic en el botón de "atrás" de la Toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Finaliza esta actividad y vuelve a la anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
