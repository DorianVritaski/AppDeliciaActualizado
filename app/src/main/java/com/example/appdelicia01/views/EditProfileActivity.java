package com.example.appdelicia01.views;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdelicia01.R;
import com.example.appdelicia01.controllers.UserController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private TextView tvProfileEmail;
    private EditText etProfileFullName;
    private Button btnSaveChanges;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Inicializar vistas
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        etProfileFullName = findViewById(R.id.etProfileFullName);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        progressBar = findViewById(R.id.progressBarEditProfile);

        // Inicializar Firebase y Controller
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userController = new UserController();

        loadUserData();

        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void loadUserData() {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            // Mostrar email (no editable)
            tvProfileEmail.setText(auth.getCurrentUser().getEmail());

            // Cargar nombre desde Firestore
            setLoading(true);
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        setLoading(false);
                        if (documentSnapshot.exists()) {
                            String currentName = documentSnapshot.getString("fullName");
                            etProfileFullName.setText(currentName);
                        } else {
                            Toast.makeText(this, "No se encontró el perfil de usuario.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        Toast.makeText(this, "Error al cargar el perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveChanges() {
        String newFullName = etProfileFullName.getText().toString().trim();

        if (newFullName.isEmpty()) {
            etProfileFullName.setError("El nombre no puede estar vacío");
            etProfileFullName.requestFocus();
            return;
        }

        setLoading(true);
        userController.updateUserProfile(newFullName, new UserController.UserUpdateListener() {
            @Override
            public void onUpdateSuccess() {
                setLoading(false);
                Toast.makeText(EditProfileActivity.this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
                finish(); // Cierra la actividad y regresa a la pantalla anterior
            }

            @Override
            public void onUpdateFailure(String error) {
                setLoading(false);
                Toast.makeText(EditProfileActivity.this, "Error al actualizar: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSaveChanges.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSaveChanges.setEnabled(true);
        }
    }
}
