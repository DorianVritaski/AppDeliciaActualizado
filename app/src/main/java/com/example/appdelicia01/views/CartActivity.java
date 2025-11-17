package com.example.appdelicia01.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appdelicia01.R;
import com.example.appdelicia01.controllers.CartController;
import com.example.appdelicia01.controllers.CartView;
import com.example.appdelicia01.models.Product;

import java.util.Locale;
import java.util.Map;

public class CartActivity extends AppCompatActivity implements CartView {

    // --- AÑADE ESTA LÍNEA AQUÍ ---
    public static final String EXTRA_SUBTOTAL = "SUBTOTAL";
    // -----------------------------


    // --- VISTAS ---
    private LinearLayout container;
    private TextView txtTotal;

    // --- CONTROLADOR ---
    private CartController controller;

    private static final String TAG = "CartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Se instancia el Controlador
        controller = new CartController(this);

        // Inicializar Vistas
        container = findViewById(R.id.container);
        txtTotal = findViewById(R.id.txtTotal);

        // Configurar Listeners que delegan al controlador
        findViewById(R.id.btnClear).setOnClickListener(v -> controller.onClearCartClicked());
        findViewById(R.id.btnCheckout).setOnClickListener(v -> controller.onCheckoutClicked());
        findViewById(R.id.btnBackToCatalog).setOnClickListener(v -> finish());

        // Cargar el estado inicial del carrito
        controller.loadCart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CartController.LOGIN_REQUEST_CODE) {
            controller.onLoginResult(resultCode == RESULT_OK);
        }
    }

    // =============================================================
    // --- IMPLEMENTACIÓN DE LOS MÉTODOS DE LA INTERFAZ CartView ---
    // =============================================================

    @Override
    public void displayCartItems(Map<Product, Integer> cartItems, double totalPrice) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (cartItems.isEmpty()) {
            // Opcional: Mostrar un mensaje de carrito vacío
            TextView emptyView = new TextView(this);
            emptyView.setText("Tu carrito está vacío");
            emptyView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            emptyView.setPadding(0, 64, 0, 64);
            container.addView(emptyView);
        } else {
            for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
                Product p = entry.getKey();
                int quantity = entry.getValue();
                View row = inflater.inflate(R.layout.item_cart_product, container, false); // Usar un layout personalizado

                TextView tvProductName = row.findViewById(R.id.tvCartProductName);
                TextView tvProductDetails = row.findViewById(R.id.tvCartProductDetails);

                tvProductName.setText(p.getName());
                tvProductDetails.setText(String.format(Locale.getDefault(), "S/ %.2f x %d", p.getPrice(), quantity));

                row.setOnClickListener(v -> controller.onProductClicked(p));
                container.addView(row);
            }
        }

        txtTotal.setText(String.format(Locale.getDefault(), "Total: S/ %.2f", totalPrice));
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToLogin(int requestCode) {
        Log.d(TAG, "Navegando a LoginActivity esperando resultado...");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void navigateToCheckout(String userEmail) {
        // Aquí iría la navegación a la actividad de Checkout real
        Toast.makeText(this, "Usuario logueado: " + userEmail + ". Ir a Checkout (Pantalla pendiente).", Toast.LENGTH_LONG).show();
        // Intent intent = new Intent(this, CheckoutActivity.class);
        // startActivity(intent);
    }

    @Override
    public void navigateToDeliveryOptions(double subtotal) {
        Intent intent = new Intent(this, DeliveryOptionsActivity.class);
        intent.putExtra(EXTRA_SUBTOTAL, subtotal); // Ahora EXTRA_SUBTOTAL existe
        startActivity(intent);
    }
}
