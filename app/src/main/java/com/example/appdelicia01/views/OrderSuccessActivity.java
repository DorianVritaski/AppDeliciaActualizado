package com.example.appdelicia01.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdelicia01.R;

public class OrderSuccessActivity extends AppCompatActivity {

    private TextView tvOrderId;
    private Button btnBackToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        // Enlazar vistas
        tvOrderId = findViewById(R.id.tvOrderId);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        // Recibir el ID del pedido desde el Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("ORDER_ID")) {
            String orderId = intent.getStringExtra("ORDER_ID");
            tvOrderId.setText(orderId);
        }

        // Configurar el botón para volver al catálogo (o la actividad principal)
        btnBackToHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(OrderSuccessActivity.this, CatalogActivity.class);
            // Limpiar el stack de actividades para que no pueda volver al proceso de compra
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
            finish(); // Cierra esta actividad
        });
    }

    // Deshabilitar el botón de "atrás" del sistema para forzar el uso del botón en pantalla
    @Override
    public void onBackPressed() {
        // No hacer nada para evitar que el usuario vuelva al flujo de pago
    }
}
