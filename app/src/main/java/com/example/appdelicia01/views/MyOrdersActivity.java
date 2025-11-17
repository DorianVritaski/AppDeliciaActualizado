package com.example.appdelicia01.views;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdelicia01.R;
import com.example.appdelicia01.controllers.OrderController;
import com.example.appdelicia01.models.Order;
import com.example.appdelicia01.views.adapters.MyOrdersAdapter;
import java.util.List;

public class MyOrdersActivity extends AppCompatActivity {

    private RecyclerView rvMyOrders;
    private ProgressBar progressBarMyOrders;
    private TextView tvNoOrders;
    private OrderController orderController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        rvMyOrders = findViewById(R.id.rvMyOrders);
        progressBarMyOrders = findViewById(R.id.progressBarMyOrders);
        tvNoOrders = findViewById(R.id.tvNoOrders);
        rvMyOrders.setLayoutManager(new LinearLayoutManager(this));

        orderController = new OrderController();
        loadUserOrders();
    }

    private void loadUserOrders() {
        progressBarMyOrders.setVisibility(View.VISIBLE);
        tvNoOrders.setVisibility(View.GONE);
        rvMyOrders.setVisibility(View.GONE);

        orderController.loadOrdersForCurrentUser(new OrderController.OrdersLoadListener() {
            @Override
            public void onOrdersLoaded(List<Order> orders) {
                progressBarMyOrders.setVisibility(View.GONE);
                if (orders.isEmpty()) {
                    tvNoOrders.setVisibility(View.VISIBLE);
                } else {
                    rvMyOrders.setVisibility(View.VISIBLE);
                    MyOrdersAdapter adapter = new MyOrdersAdapter(orders, MyOrdersActivity.this);
                    rvMyOrders.setAdapter(adapter);
                }
            }

            @Override
            public void onDataLoadFailed(String error) {
                progressBarMyOrders.setVisibility(View.GONE);
                Toast.makeText(MyOrdersActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
