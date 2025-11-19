package com.example.appdelicia01.views.adapters;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdelicia01.R;
import com.example.appdelicia01.models.Product;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> items;
    private Listener listener;
    private boolean isAdmin;

    public interface Listener {
        void onAdd(Product p, int quantity);
        void onShare(Product p);
    }

    public ProductAdapter(List<Product> items, boolean isAdmin, Listener listener) {
        this.items = items;
        this.isAdmin = isAdmin;
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
        Product p = items.get(position);

        holder.txtName.setText(p.getName());
        holder.txtPrice.setText(String.format(Locale.getDefault(), "S/ %.2f", p.getPrice()));
        holder.txtDescription.setText(p.getDescription());

        Glide.with(holder.itemView.getContext())
                .load(p.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.imgProduct);

        holder.bind(p, isAdmin);

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

    /**
     * Devuelve la lista actual de productos que maneja el adaptador.
     * Este método es necesario para que CatalogActivity pueda reconstruir el
     * adaptador cuando el rol del usuario cambia sin perder los productos actuales.
     * @return La lista de productos.
     */
    public List<Product> getProducts() {
        return this.items;
    }

    /**
     * Devuelve el producto en una posición específica de la lista.
     * Utilizado para saber qué producto se seleccionó en el menú contextual.
     * @param position La posición del item.
     * @return El objeto Product en esa posición.
     */
    public Product getProductAt(int position) {
        return items.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        ImageView imgProduct;
        TextView txtName, txtPrice, txtDescription;
        Button btnAdd;
        Button btnShare;
        private boolean isAdmin = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            btnShare = itemView.findViewById(R.id.btnShare);

            itemView.setOnCreateContextMenuListener(this);
        }

        void bind(Product product, boolean isAdmin) {
            this.isAdmin = isAdmin;
            if (isAdmin) {
                btnAdd.setVisibility(View.GONE);
                btnShare.setVisibility(View.GONE);
            } else {
                btnAdd.setVisibility(View.VISIBLE);
                btnShare.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            if (isAdmin) {
                menu.setHeaderTitle("Acciones de Producto");
                menu.add(this.getAdapterPosition(), R.id.menu_edit_product, 0, "Editar Producto");
            }
        }
    }
}
