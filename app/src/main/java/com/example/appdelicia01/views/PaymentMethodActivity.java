package com.example.appdelicia01.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdelicia01.R;
import com.example.appdelicia01.controllers.OrderController;

import java.util.Locale;

public class PaymentMethodActivity extends AppCompatActivity {

    // Vistas
    private TextView tvTotalAmount;
    private RadioGroup rgPaymentOptions;
    private RadioButton rbPayOnPickup, rbPayOnDelivery, rbPayWithCard;
    private Button btnConfirmOrder;

    private OrderController orderController;

    // Datos recibidos del Intent
    private String deliveryMethod;
    private String address;
    private double subtotal;
    private double deliveryFee = 5.0; // Costo de envío fijo, puedes cambiarlo
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        orderController = new OrderController();

        // Enlazar vistas
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        rgPaymentOptions = findViewById(R.id.rgPaymentOptions);
        rbPayOnPickup = findViewById(R.id.rbPayOnPickup);
        rbPayOnDelivery = findViewById(R.id.rbPayOnDelivery);
        rbPayWithCard = findViewById(R.id.rbPayWithCard);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);

        // Recibir datos de DeliveryOptionsActivity
        Intent intent = getIntent();
        deliveryMethod = intent.getStringExtra(DeliveryOptionsActivity.EXTRA_DELIVERY_METHOD);
        address = intent.getStringExtra(DeliveryOptionsActivity.EXTRA_ADDRESS);
        subtotal = intent.getDoubleExtra(CartActivity.EXTRA_SUBTOTAL, 0.0);

        // Configurar la UI basado en los datos recibidos
        setupUI();

        // Configurar el listener del botón de confirmar
        btnConfirmOrder.setOnClickListener(v -> handleConfirmOrder());
    }

    private void setupUI() {
        if ("pickup".equals(deliveryMethod)) {
            // Si es recojo en tienda
            rbPayOnPickup.setVisibility(View.VISIBLE); // Mostrar "Pagar al Recoger"
            rbPayOnDelivery.setVisibility(View.GONE);
            totalAmount = subtotal;
        } else if ("delivery".equals(deliveryMethod)) {
            // Si es delivery
            rbPayOnPickup.setVisibility(View.GONE);
            rbPayOnDelivery.setVisibility(View.VISIBLE); // Mostrar "Pago Contraentrega"
            totalAmount = subtotal + deliveryFee;
        } else {
            // Caso por defecto o error
            totalAmount = subtotal;
        }

        // Actualizar el texto del total a pagar
        tvTotalAmount.setText(String.format(Locale.US, "Total a Pagar: S/ %.2f", totalAmount));
    }

    private void handleConfirmOrder() {
        int selectedPaymentId = rgPaymentOptions.getCheckedRadioButtonId();

        if (selectedPaymentId == -1) {
            Toast.makeText(this, "Por favor, elige un método de pago", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod;
        if (selectedPaymentId == R.id.rbPayOnPickup) {
            paymentMethod = "Pagar al Recoger";
        } else if (selectedPaymentId == R.id.rbPayOnDelivery) {
            paymentMethod = "Pago Contraentrega";
        } else if (selectedPaymentId == R.id.rbPayWithCard) {
            paymentMethod = "Pagado con Tarjeta";
            // TODO: Aquí iniciarías la pasarela de pago (Stripe, etc.)
            // Por ahora, solo lo registraremos.
        } else {
            paymentMethod = "Desconocido";
        }

        // Por ahora, solo mostraremos un Toast con el resumen final.
        // El siguiente paso será guardar esta información en Firestore.
        String summary = "Pedido Confirmado:\n" +
                "Método de Entrega: " + deliveryMethod + "\n" +
                "Dirección: " + address + "\n" +
                "Método de Pago: " + paymentMethod + "\n" +
                "Total: S/ " + String.format(Locale.US, "%.2f", totalAmount);

        Toast.makeText(this, summary, Toast.LENGTH_LONG).show();

        // TODO: Crear un controlador y un método en el modelo para guardar el pedido en Firestore.
        // TODO: Navegar a una pantalla de "Pedido Exitoso" y limpiar el carrito.

        orderController.createOrder(
                deliveryMethod,
                address,
                paymentMethod,
                subtotal,
                "delivery".equals(deliveryMethod) ? deliveryFee : 0.0, // Pasar 0 si es pickup
                totalAmount,
                new OrderController.OrderCreationListener() {
                    @Override
                    public void onOrderCreated(String orderId) {
                        //Toast.makeText(PaymentMethodActivity.this, "¡Pedido #" + orderId + " creado con éxito!", Toast.LENGTH_LONG).show();

                        // TODO: Navegar a una pantalla de éxito (OrderSuccessActivity)
                        Intent successIntent = new Intent(PaymentMethodActivity.this, OrderSuccessActivity.class);
                        successIntent.putExtra("ORDER_ID", orderId);
                        // Limpiar el stack de actividades para que el usuario no pueda volver atrás
                        successIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(successIntent);
                        finish();
                    }

                    @Override
                    public void onOrderCreationFailed(String error) {
                        Toast.makeText(PaymentMethodActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        btnConfirmOrder.setEnabled(true); // Rehabilitar botón si falla
                    }
                }
        );
    }
}
