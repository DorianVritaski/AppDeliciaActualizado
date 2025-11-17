package com.example.appdelicia01.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Importar Glide
import com.bumptech.glide.Glide;
import com.example.appdelicia01.R;
import com.example.appdelicia01.models.Product;

import java.util.List;
import java.util.Locale;


import android.content.Context; // Necesario para el diálogo
import android.widget.NumberPicker; // Importar NumberPicker

import androidx.appcompat.app.AlertDialog;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> items;
    private Listener listener;

    public interface Listener {
        void onAdd(Product p, int quantity);
        void onShare(Product p);
    }

    public ProductAdapter(List<Product> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // La variable del producto actual se llama 'p'
        Product p = items.get(position);

        holder.txtName.setText(p.getName());
        holder.txtPrice.setText(String.format(Locale.getDefault(), "S/ %.2f", p.getPrice()));

        // --- CORRECCIÓN: Usar la variable 'p' en lugar de 'product' ---
        holder.txtDescription.setText(p.getDescription());

        // Cargar imagen desde URL con Glide
        Glide.with(holder.itemView.getContext())
                .load(p.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.imgProduct);

        holder.btnAdd.setOnClickListener(v -> {
            showQuantityDialog(v.getContext(), p);
        });

        holder.btnShare.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShare(p);
            }
        });
    }

    private void showQuantityDialog(Context context, Product product) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_quantity_picker, null);

        final TextView tvDialogProductName = dialogView.findViewById(R.id.tvDialogProductName);
        final NumberPicker numberPicker = dialogView.findViewById(R.id.npQuantity);

        tvDialogProductName.setText(product.getName());
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(20);
        numberPicker.setValue(1);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setTitle("Agregar al Carrito");

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            int quantity = numberPicker.getValue();
            if (listener != null) {
                listener.onAdd(product, quantity);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.items.clear();
        this.items.addAll(newProducts);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtPrice, txtDescription; // Asegúrate de que txtDescription esté declarado aquí
        Button btnAdd;
        Button btnShare;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            // --- CORRECCIÓN 2: Inicializar el TextView de la descripción ---
            txtDescription = itemView.findViewById(R.id.txtDescription);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}
