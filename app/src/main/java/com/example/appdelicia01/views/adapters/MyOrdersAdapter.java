package com.example.appdelicia01.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdelicia01.R;
import com.example.appdelicia01.models.Order;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
// 1. Importar Map para manejar los items del carrito
import java.util.Map;

public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersAdapter.MyOrderViewHolder> {

    private List<Order> orderList;
    private Context context;

    public MyOrdersAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_order, parent, false);
        return new MyOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyOrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class MyOrderViewHolder extends RecyclerView.ViewHolder {
        // 2. Declarar la variable para el nuevo TextView de items
        TextView tvMyOrderDate, tvMyOrderTotal, tvMyOrderStatus, tvMyOrderItems;

        public MyOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMyOrderDate = itemView.findViewById(R.id.tvMyOrderDate);
            tvMyOrderTotal = itemView.findViewById(R.id.tvMyOrderTotal);
            tvMyOrderStatus = itemView.findViewById(R.id.tvMyOrderStatus);
            // 3. Enlazar el nuevo TextView
            tvMyOrderItems = itemView.findViewById(R.id.tvMyOrderItems);
        }

        void bind(final Order order) {
            // Asignar fecha, total y estado (código existente)
            if (order.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvMyOrderDate.setText("Pedido del " + sdf.format(order.getTimestamp()));
            } else {
                tvMyOrderDate.setText("Pedido sin fecha");
            }
            tvMyOrderTotal.setText(String.format(Locale.US, "S/ %.2f", order.getTotalAmount()));
            tvMyOrderStatus.setText("Estado: " + order.getStatus());

            // --- 4. INICIO DE LA LÓGICA PARA MOSTRAR LOS PRODUCTOS ---
            if (order.getCartItems() != null && !order.getCartItems().isEmpty()) {
                StringBuilder itemsBuilder = new StringBuilder();
                // Iterar sobre el mapa de productos del pedido
                for (Map.Entry<String, Object> entry : order.getCartItems().entrySet()) {
                    // Firestore devuelve los valores internos como un Map
                    Map<String, Object> productDetails = (Map<String, Object>) entry.getValue();
                    String name = (String) productDetails.get("name");
                    // Firestore puede devolver números como Long o Double, así que lo manejamos con cuidado
                    Object quantityObj = productDetails.get("quantity");
                    long quantity = 0;
                    if (quantityObj instanceof Long) {
                        quantity = (Long) quantityObj;
                    } else if (quantityObj instanceof Double) {
                        quantity = ((Double) quantityObj).longValue();
                    }

                    // Añadir cada producto al texto
                    itemsBuilder.append("- ")
                            .append(name)
                            .append(" x ")
                            .append(quantity)
                            .append("\n");
                }
                // Quitar el último salto de línea para un formato más limpio
                if (itemsBuilder.length() > 0) {
                    itemsBuilder.setLength(itemsBuilder.length() - 1);
                }
                tvMyOrderItems.setText(itemsBuilder.toString());
                tvMyOrderItems.setVisibility(View.VISIBLE);
            } else {
                // Si por alguna razón no hay items, ocultamos la caja de detalles
                tvMyOrderItems.setVisibility(View.GONE);
            }
            // --- FIN DE LA LÓGICA ---
        }
    }
}
