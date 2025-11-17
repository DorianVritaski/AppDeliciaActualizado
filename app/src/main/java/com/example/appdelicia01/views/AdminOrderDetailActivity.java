package com.example.appdelicia01.views;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appdelicia01.R;
import com.example.appdelicia01.controllers.OrderController;
import com.example.appdelicia01.models.Order;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminOrderDetailActivity extends AppCompatActivity {

    private TextView tvDetailOrderId, tvDetailUser, tvDetailTotal, tvDetailItems, tvDetailDeliveryInfo;
    private Spinner spinnerStatus;
    private Button btnUpdateStatus;
    private ProgressBar progressBarDetail;

    private OrderController orderController;
    private String orderId;
    private Order currentOrder;
    private List<String> statusOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);

        // Inicializar vistas
        initializeViews();

        orderController = new OrderController();
        orderId = getIntent().getStringExtra("ORDER_ID");

        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Error: ID de pedido no encontrado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadOrderDetails();

        btnUpdateStatus.setOnClickListener(v -> updateOrderStatus());
    }

    private void initializeViews() {
        tvDetailOrderId = findViewById(R.id.tvDetailOrderId);
        tvDetailUser = findViewById(R.id.tvDetailUser);
        tvDetailTotal = findViewById(R.id.tvDetailTotal);
        tvDetailItems = findViewById(R.id.tvDetailItems);
        tvDetailDeliveryInfo = findViewById(R.id.tvDetailDeliveryInfo);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        progressBarDetail = findViewById(R.id.progressBarDetail);
    }

    private void loadOrderDetails() {
        progressBarDetail.setVisibility(View.VISIBLE);
        orderController.getOrderById(orderId, new OrderController.OrderDetailsListener() {
            @Override
            public void onOrderLoaded(Order order) {
                currentOrder = order;
                displayOrderDetails();
                setupStatusSpinner();
                progressBarDetail.setVisibility(View.GONE);
            }

            @Override
            public void onDataLoadFailed(String error) {
                progressBarDetail.setVisibility(View.GONE);
                Toast.makeText(AdminOrderDetailActivity.this, "Error al cargar detalles: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayOrderDetails() {
        tvDetailOrderId.setText("ID: " + currentOrder.getOrderId());
        tvDetailUser.setText("Cliente: " + currentOrder.getUserEmail());
        tvDetailTotal.setText(String.format(Locale.US, "Total: S/ %.2f", currentOrder.getTotalAmount()));

        // Mostrar items
        StringBuilder itemsText = new StringBuilder("Items del Pedido:\n");
        for (Map.Entry<String, Object> entry : currentOrder.getCartItems().entrySet()) {
            Map<String, Object> productDetails = (Map<String, Object>) entry.getValue();
            itemsText.append("- ").append(productDetails.get("name"))
                    .append(" x ").append(productDetails.get("quantity")).append("\n");
        }
        tvDetailItems.setText(itemsText.toString());

        // Mostrar info de entrega
        String deliveryInfo = "Método de Entrega: " + currentOrder.getDeliveryMethod() + "\n" +
                "Dirección: " + currentOrder.getDeliveryAddress() + "\n" +
                "Pago: " + currentOrder.getPaymentMethod();
        tvDetailDeliveryInfo.setText(deliveryInfo);
    }

    private void setupStatusSpinner() {
        // Opciones de estado que el admin puede seleccionar
        statusOptions = Arrays.asList(
                Order.STATUS_PENDING, Order.STATUS_CONFIRMED,
                Order.STATUS_READY_FOR_PICKUP, Order.STATUS_OUT_FOR_DELIVERY,
                Order.STATUS_COMPLETED, Order.STATUS_CANCELLED);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // Seleccionar el estado actual del pedido en el spinner
        int currentStatusPosition = statusOptions.indexOf(currentOrder.getStatus());
        if (currentStatusPosition >= 0) {
            spinnerStatus.setSelection(currentStatusPosition);
        }
    }

    private void updateOrderStatus() {
        String newStatus = spinnerStatus.getSelectedItem().toString();
        if (newStatus.equals(currentOrder.getStatus())) {
            Toast.makeText(this, "El estado ya es el actual.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUpdateStatus.setEnabled(false);
        progressBarDetail.setVisibility(View.VISIBLE);

        orderController.updateOrderStatus(orderId, newStatus, new OrderController.OrderUpdateListener() {
            @Override
            public void onOrderUpdated() {
                Toast.makeText(AdminOrderDetailActivity.this, "Estado actualizado a: " + newStatus, Toast.LENGTH_SHORT).show();
                btnUpdateStatus.setEnabled(true);
                progressBarDetail.setVisibility(View.GONE);
                // Actualizamos el estado local para reflejar el cambio
                currentOrder.setStatus(newStatus);
            }

            @Override
            public void onUpdateFailed(String error) {
                Toast.makeText(AdminOrderDetailActivity.this, "Error al actualizar: " + error, Toast.LENGTH_LONG).show();
                btnUpdateStatus.setEnabled(true);
                progressBarDetail.setVisibility(View.GONE);
            }
        });
    }
}
