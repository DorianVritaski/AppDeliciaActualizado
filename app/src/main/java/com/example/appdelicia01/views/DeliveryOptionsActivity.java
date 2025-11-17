package com.example.appdelicia01.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdelicia01.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

public class DeliveryOptionsActivity extends AppCompatActivity {

    // Vistas principales
    private RadioGroup rgDeliveryOptions;
    private MaterialCardView cardDeliveryAddress;
    private EditText etAddress;
    private MaterialButton btnContinueToPayment;

    // Subtotal recibido del carrito
    private double subtotal = 0.0;

    // Claves para los extras
    public static final String EXTRA_DELIVERY_METHOD = "DELIVERY_METHOD";
    public static final String EXTRA_ADDRESS = "ADDRESS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_options);

        // =============== RECIBIR SUBTOTAL DESDE CART ======================
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(CartActivity.EXTRA_SUBTOTAL)) {
            subtotal = intent.getDoubleExtra(CartActivity.EXTRA_SUBTOTAL, 0.0);
        }

        // Mensaje de depuración (puedes quitarlo luego)
        Toast.makeText(this,
                "Subtotal: S/ " + String.format(Locale.US, "%.2f", subtotal),
                Toast.LENGTH_SHORT).show();

        // =============== ENLAZAR VISTAS ======================
        rgDeliveryOptions = findViewById(R.id.rgDeliveryOptions);
        cardDeliveryAddress = findViewById(R.id.cardDeliveryAddress); // <-- NUEVO ID
        etAddress = findViewById(R.id.etAddress);
        btnContinueToPayment = findViewById(R.id.btnContinueToPayment);

        // =============== LÓGICA DE OPCIONES DE ENTREGA ======================
        rgDeliveryOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPickup) {
                cardDeliveryAddress.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbDelivery) {
                cardDeliveryAddress.setVisibility(View.VISIBLE);
            }
        });

        // =============== BOTÓN PARA CONTINUAR ======================
        btnContinueToPayment.setOnClickListener(v -> handleContinueToPayment());
    }

    private void handleContinueToPayment() {

        int selectedOptionId = rgDeliveryOptions.getCheckedRadioButtonId();

        // Validación: debe seleccionar una opción
        if (selectedOptionId == -1) {
            Toast.makeText(this, "Por favor, elige una opción de entrega", Toast.LENGTH_SHORT).show();
            return;
        }

        String deliveryMethod;
        String address = "N/A";

        if (selectedOptionId == R.id.rbDelivery) {
            deliveryMethod = "delivery";

            String addressInput = etAddress.getText().toString().trim();

            // Validación de dirección
            if (addressInput.isEmpty()) {
                etAddress.setError("La dirección es obligatoria");
                Toast.makeText(this, "Por favor, ingresa tu dirección", Toast.LENGTH_SHORT).show();
                return;
            }

            address = addressInput;

        } else {
            deliveryMethod = "Recojo en tienda";
        }

        // Debug Toast (útil para pruebas)
        Toast.makeText(this,
                "Método: " + deliveryMethod + "\nDirección: " + address,
                Toast.LENGTH_LONG).show();

        // =============== NAVEGAR A PaymentMethodActivity ======================
        Intent paymentIntent = new Intent(DeliveryOptionsActivity.this, PaymentMethodActivity.class);
        paymentIntent.putExtra(EXTRA_DELIVERY_METHOD, deliveryMethod);
        paymentIntent.putExtra(EXTRA_ADDRESS, address);
        paymentIntent.putExtra(CartActivity.EXTRA_SUBTOTAL, subtotal);

        startActivity(paymentIntent);
    }
}
