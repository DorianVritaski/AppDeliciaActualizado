package com.example.appdelicia01.views;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appdelicia01.R;
import com.example.appdelicia01.controllers.AddProductController;
import com.example.appdelicia01.controllers.AddProductView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddProductActivity extends AppCompatActivity implements AddProductView {

    // --- VISTAS ---
    private TextInputLayout tilProductName, tilProductPrice, tilProductDescription, tilProductImageUrl;
    private TextInputEditText etProductName, etProductPrice, etProductDescription, etProductImageUrl;
    private Button btnSaveProduct;
    private ProgressBar progressBarAddProduct;

    // --- CONTROLADOR ---
    private AddProductController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Se instancia el Controlador
        controller = new AddProductController(this);

        // Configurar la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarAddProduct);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Agregar Producto");
        }

        // Inicializar vistas
        tilProductName = findViewById(R.id.tilProductName);
        etProductName = findViewById(R.id.etProductName);
        tilProductPrice = findViewById(R.id.tilProductPrice);
        etProductPrice = findViewById(R.id.etProductPrice);
        tilProductDescription = findViewById(R.id.tilProductDescription);
        etProductDescription = findViewById(R.id.etProductDescription);
        tilProductImageUrl = findViewById(R.id.tilProductImageUrl);
        etProductImageUrl = findViewById(R.id.etProductImageUrl);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
        progressBarAddProduct = findViewById(R.id.progressBarAddProduct);

        // La Vista captura el evento y lo delega al Controlador
        btnSaveProduct.setOnClickListener(v -> {
            String name = etProductName.getText().toString().trim();
            String priceStr = etProductPrice.getText().toString().trim();
            String description = etProductDescription.getText().toString().trim();
            String imageUrl = etProductImageUrl.getText().toString().trim();
            controller.saveProduct(name, priceStr, description, imageUrl);
        });
    }

    // ===================================================================
    // --- IMPLEMENTACIÓN DE LOS MÉTODOS DE LA INTERFAZ AddProductView ---
    // ===================================================================

    @Override
    public void showLoading(boolean isLoading) {
        if (isLoading) {
            btnSaveProduct.setEnabled(false);
            btnSaveProduct.setAlpha(0.5f);
            progressBarAddProduct.setVisibility(View.VISIBLE);
        } else {
            btnSaveProduct.setEnabled(true);
            btnSaveProduct.setAlpha(1.0f);
            progressBarAddProduct.setVisibility(View.GONE);
        }
    }

    @Override
    public void onProductSavedSuccess() {
        Toast.makeText(this, "Producto guardado exitosamente", Toast.LENGTH_SHORT).show();
        finish(); // Cierra la actividad y vuelve al catálogo
    }

    @Override
    public void onProductSaveError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showValidationError(String field, String message) {
        switch (field) {
            case "name":
                tilProductName.setError(message);
                break;
            case "price":
                tilProductPrice.setError(message);
                break;
            case "imageUrl":
                tilProductImageUrl.setError(message);
                break;
        }
    }

    @Override
    public void clearAllErrors() {
        tilProductName.setError(null);
        tilProductPrice.setError(null);
        tilProductDescription.setError(null); // Aunque no se valida, es bueno limpiarlo
        tilProductImageUrl.setError(null);
    }

    // Para manejar el clic en el botón de "atrás" de la Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

