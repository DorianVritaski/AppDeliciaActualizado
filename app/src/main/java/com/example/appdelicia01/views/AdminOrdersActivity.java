package com.example.appdelicia01.views;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdelicia01.R;
import com.example.appdelicia01.controllers.OrderController;
import com.example.appdelicia01.models.Order;
import com.example.appdelicia01.views.adapters.AdminOrdersAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminOrdersActivity extends AppCompatActivity {

    // Vistas existentes
    private RecyclerView rvAdminOrders;
    private ProgressBar progressBarAdmin;
    private AdminOrdersAdapter adapter;

    // Vistas nuevas para los filtros
    private TextView tvNoResults, tvSelectedFilter;
    private EditText etFilterName;
    private Button btnFilterDate, btnClearFilters;

    // Controlador y variables de estado para los filtros
    private OrderController orderController;
    private Calendar selectedDate = null;
    private String nameFilter = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        // --- Configuración de la Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbarAdmin);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // --- Inicialización de Vistas ---
        rvAdminOrders = findViewById(R.id.rvAdminOrders);
        progressBarAdmin = findViewById(R.id.progressBarAdmin);
        tvNoResults = findViewById(R.id.tvNoResults);
        etFilterName = findViewById(R.id.etFilterName);
        btnFilterDate = findViewById(R.id.btnFilterDate);
        btnClearFilters = findViewById(R.id.btnClearFilters);
        tvSelectedFilter = findViewById(R.id.tvSelectedFilter);

        rvAdminOrders.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar controlador
        orderController = new OrderController();

        // Configurar los listeners para los filtros
        setupFilterListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cargar los pedidos aplicando los filtros actuales (o ninguno si están vacíos)
        applyFilters();
    }

    private void setupFilterListeners() {
        // Listener para el filtro de nombre: se activa mientras el usuario escribe.
        etFilterName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                nameFilter = s.toString();
                applyFilters(); // Aplica el filtro en tiempo real
            }
        });

        // Listener para el botón de fecha: muestra un selector de fecha.
        btnFilterDate.setOnClickListener(v -> showDatePickerDialog());

        // Listener para el botón de limpiar filtros: resetea todo a su estado inicial.
        btnClearFilters.setOnClickListener(v -> {
            etFilterName.setText(""); // Limpia el campo de texto
            nameFilter = "";
            selectedDate = null;
            applyFilters(); // Vuelve a cargar todos los pedidos
        });
    }

    private void showDatePickerDialog() {
        final Calendar currentDate = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            applyFilters(); // Aplica el filtro con la nueva fecha seleccionada
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void applyFilters() {
        progressBarAdmin.setVisibility(View.VISIBLE);
        tvNoResults.setVisibility(View.GONE);
        rvAdminOrders.setVisibility(View.GONE);

        // Llamamos al controlador con los filtros actuales
        orderController.loadFilteredOrders(nameFilter, selectedDate, new OrderController.OrdersLoadListener() {
            @Override
            public void onOrdersLoaded(List<Order> orders) {
                progressBarAdmin.setVisibility(View.GONE);

                if (orders.isEmpty()) {
                    tvNoResults.setVisibility(View.VISIBLE);
                    rvAdminOrders.setVisibility(View.GONE);
                } else {
                    tvNoResults.setVisibility(View.GONE);
                    rvAdminOrders.setVisibility(View.VISIBLE);
                }

                // Actualizamos el adaptador con los nuevos pedidos filtrados
                if (adapter == null) {
                    adapter = new AdminOrdersAdapter(orders, AdminOrdersActivity.this);
                    rvAdminOrders.setAdapter(adapter);
                } else {
                    adapter.updateOrders(orders);
                }
            }

            @Override
            public void onDataLoadFailed(String error) {
                progressBarAdmin.setVisibility(View.GONE);
                Toast.makeText(AdminOrdersActivity.this, "Error al cargar pedidos: " + error, Toast.LENGTH_LONG).show();
            }
        });

        // Actualizamos el texto que muestra los filtros activos
        updateFilterUI();
    }

    private void updateFilterUI() {
        boolean hasNameFilter = !nameFilter.isEmpty();
        boolean hasDateFilter = selectedDate != null;

        if (!hasNameFilter && !hasDateFilter) {
            tvSelectedFilter.setVisibility(View.GONE);
            btnClearFilters.setVisibility(View.GONE);
            return;
        }

        StringBuilder filterText = new StringBuilder("Filtrando por: ");
        if (hasNameFilter) {
            filterText.append("'").append(nameFilter).append("'");
        }
        if (hasNameFilter && hasDateFilter) {
            filterText.append(" y ");
        }
        if (hasDateFilter) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            filterText.append("fecha '").append(sdf.format(selectedDate.getTime())).append("'");
        }

        tvSelectedFilter.setText(filterText.toString());
        tvSelectedFilter.setVisibility(View.VISIBLE);
        btnClearFilters.setVisibility(View.VISIBLE);
    }

    // Para manejar el clic en el botón de atrás de la Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
