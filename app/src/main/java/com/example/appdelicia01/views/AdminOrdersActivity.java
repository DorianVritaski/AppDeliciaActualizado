package com.example.appdelicia01.views;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdelicia01.R;
import com.example.appdelicia01.controllers.OrderController;
import com.example.appdelicia01.models.Order;
import com.example.appdelicia01.views.adapters.AdminOrdersAdapter;

import java.util.List;

public class AdminOrdersActivity extends AppCompatActivity {

    private RecyclerView rvAdminOrders;
    private ProgressBar progressBarAdmin;
    private OrderController orderController;
    private AdminOrdersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        rvAdminOrders = findViewById(R.id.rvAdminOrders);
        progressBarAdmin = findViewById(R.id.progressBarAdmin);
        rvAdminOrders.setLayoutManager(new LinearLayoutManager(this));

        orderController = new OrderController();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders(); // Cargar o recargar los pedidos cada vez que se muestre la pantalla
    }

    private void loadOrders() {
        progressBarAdmin.setVisibility(View.VISIBLE);
        orderController.loadAllOrders(new OrderController.OrdersLoadListener() {
            @Override
            public void onOrdersLoaded(List<Order> orders) {
                progressBarAdmin.setVisibility(View.GONE);
                adapter = new AdminOrdersAdapter(orders, AdminOrdersActivity.this);
                rvAdminOrders.setAdapter(adapter);
            }

            @Override
            public void onDataLoadFailed(String error) {
                progressBarAdmin.setVisibility(View.GONE);
                Toast.makeText(AdminOrdersActivity.this, "Error al cargar pedidos: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
