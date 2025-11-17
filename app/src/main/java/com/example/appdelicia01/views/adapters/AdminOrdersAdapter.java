package com.example.appdelicia01.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdelicia01.R;
import com.example.appdelicia01.models.Order;
import com.example.appdelicia01.views.AdminOrderDetailActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;

    public AdminOrdersAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        // 1. Declarar el nuevo TextView
        TextView tvOrderId, tvOrderDate, tvOrderTotal, tvOrderStatus, tvCustomerName;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvAdminOrderId);
            // 2. Enlazar el nuevo TextView
            tvCustomerName = itemView.findViewById(R.id.tvAdminCustomerName);
            tvOrderDate = itemView.findViewById(R.id.tvAdminOrderDate);
            tvOrderTotal = itemView.findViewById(R.id.tvAdminOrderTotal);
            tvOrderStatus = itemView.findViewById(R.id.tvAdminOrderStatus);
        }

        void bind(final Order order) {
            // Asigna los datos que ya tienes
            tvOrderId.setText("Pedido: " + order.getOrderId());
            tvOrderTotal.setText(String.format(Locale.US, "Total: S/ %.2f", order.getTotalAmount()));
            tvOrderStatus.setText("Estado: " + order.getStatus());

            if (order.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvOrderDate.setText("Fecha: " + sdf.format(order.getTimestamp()));
            }

            // --- INICIO DE LA CORRECCIÓN ---
            // Lógica actualizada para obtener y mostrar el nombre del cliente desde el campo "fullname"
            if (order.getUserId() != null && !order.getUserId().isEmpty()) {
                // Muestra un texto temporal mientras carga
                tvCustomerName.setText("Cargando cliente...");

                FirebaseFirestore.getInstance().collection("users").document(order.getUserId()).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // Obtiene el nombre completo del campo "fullname"
                                String fullName = documentSnapshot.getString("fullName");

                                // Comprueba que el nombre no esté vacío antes de mostrarlo
                                if (fullName != null && !fullName.trim().isEmpty()) {
                                    tvCustomerName.setText("Cliente: " + fullName);
                                } else {
                                    tvCustomerName.setText("Cliente: (Sin nombre)");
                                }
                            } else {
                                tvCustomerName.setText("Cliente: No encontrado");
                            }
                        })
                        .addOnFailureListener(e -> {
                            tvCustomerName.setText("Cliente: Error al cargar");
                        });
            } else {
                tvCustomerName.setText("Cliente: Desconocido");
            }
            // --- FIN DE LA CORRECCIÓN ---

            // Configura el click para ir al detalle
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, AdminOrderDetailActivity.class);
                intent.putExtra("ORDER_ID", order.getOrderId());
                context.startActivity(intent);
            });
        }
    }
}
