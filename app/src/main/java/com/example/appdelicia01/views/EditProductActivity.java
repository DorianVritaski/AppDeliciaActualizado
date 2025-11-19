package com.example.appdelicia01.views;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.appdelicia01.R;
import com.example.appdelicia01.controllers.EditProductController;
import com.example.appdelicia01.controllers.EditProductView;
import com.example.appdelicia01.models.Product;

import java.util.Locale;

public class EditProductActivity extends AppCompatActivity implements EditProductView {

    // CAMBIO: Añadir el EditText para la URL
    private EditText etProductName, etProductDescription, etProductPrice, etProductImageUrl;
    private Button btnSaveChanges;
    private ImageView ivProductImage;
    private ProgressBar progressBar;

    private EditProductController controller;
    private String productId;
    private static final String TAG = "EditProductActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // --- Configuración de la Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbarEditProduct);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Editar Producto");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // --- Inicialización completa de las vistas ---
        etProductName = findViewById(R.id.etProductName);
        etProductDescription = findViewById(R.id.etProductDescription);
        etProductPrice = findViewById(R.id.etProductPrice);
        etProductImageUrl = findViewById(R.id.etProductImageUrl); // <-- CAMBIO: Inicializar el nuevo campo
        ivProductImage = findViewById(R.id.ivProductImage);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        progressBar = findViewById(R.id.progressBarEditProduct);

        // --- Recuperar el ID del producto ---
        productId = getIntent().getStringExtra("PRODUCT_ID");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Error: ID de producto no encontrado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Inicializar el controlador
        controller = new EditProductController(this);

        // Pedir al controlador que cargue los datos del producto
        controller.loadProductDetails(productId);

        // --- Configuración del botón de guardar ---
        btnSaveChanges.setOnClickListener(v -> {
            try {
                String newName = etProductName.getText().toString().trim();
                String newDesc = etProductDescription.getText().toString().trim();
                String newImageUrl = etProductImageUrl.getText().toString().trim(); // <-- CAMBIO: Obtener la nueva URL
                double newPrice = Double.parseDouble(etProductPrice.getText().toString());

                // CAMBIO: Delegar la acción de guardar al controlador con la nueva URL
                controller.saveChanges(productId, newName, newDesc, newPrice, newImageUrl);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Por favor, introduce un precio válido.", Toast.LENGTH_SHORT).show();
            }
        });

        // CAMBIO OPCIONAL: Previsualizar la imagen mientras se escribe la URL
        etProductImageUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Glide.with(EditProductActivity.this)
                        .load(s.toString())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(ivProductImage);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void displayProductDetails(Product product) {
        etProductName.setText(product.getName());
        etProductDescription.setText(product.getDescription());
        etProductPrice.setText(String.format(Locale.US, "%.2f", product.getPrice()));
        etProductImageUrl.setText(product.getImageUrl()); // <-- CAMBIO: Mostrar la URL actual

        // Cargar imagen con Glide desde la URL
        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(ivProductImage);
    }

    @Override
    public void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void displayError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSaveChangesSuccess() {
        Toast.makeText(this, "Producto actualizado con éxito", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
