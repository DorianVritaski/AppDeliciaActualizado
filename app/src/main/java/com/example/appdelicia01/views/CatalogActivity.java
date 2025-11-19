package com.example.appdelicia01.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdelicia01.R;
import com.example.appdelicia01.controllers.CatalogController;
import com.example.appdelicia01.controllers.CatalogView;
import com.example.appdelicia01.models.Product;
import com.example.appdelicia01.models.UserManager;
import com.example.appdelicia01.views.adapters.ProductAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CatalogActivity extends AppCompatActivity implements CatalogView, ProductAdapter.Listener, UserManager.UserDataChangeListener {

    private ProductAdapter adapter;
    private FloatingActionButton fabCart;
    private ProgressBar progressBarCatalog;
    private RecyclerView rvProducts;
    private Menu optionsMenu;
    private CatalogController controller;
    private static final String TAG = "CatalogActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        controller = new CatalogController(this);
        setupUI();
        UserManager.getInstance().addUserDataChangeListener(this);
        controller.loadProducts();
        controller.checkUserRole();

        // <-- CAMBIO: Registrar el RecyclerView para el menú contextual.
        // Esto es crucial para que onContextItemSelected funcione.
        registerForContextMenu(rvProducts);
    }

    private void setupUI() {
        Toolbar toolbar = findViewById(R.id.toolbarCatalog);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Catálogo");
        }

        progressBarCatalog = findViewById(R.id.progressBarCatalog);
        rvProducts = findViewById(R.id.rvProducts);

        int numberOfColumns = 2;
        rvProducts.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        // <-- CAMBIO: La inicialización del adaptador ahora está vacía.
        // Se creará uno nuevo en displayProducts o showAdminOptions.
        // Esto asegura que el rol de admin se asigne correctamente.

        fabCart = findViewById(R.id.fabCart);
        fabCart.setOnClickListener(v -> controller.onCartOptionClicked());
    }

    // <-- CAMBIO: Método para manejar la selección del menú contextual (al mantener presionado).
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        // Obtenemos la posición del producto desde el menú que creamos en el Adapter.
        int position = item.getGroupId();

        // Verificamos qué opción se seleccionó.
        if (item.getItemId() == R.id.menu_edit_product) {
            // Obtenemos el producto seleccionado usando la posición.
            Product selectedProduct = adapter.getProductAt(position);
            // Delegamos la lógica de navegación al controlador.
            controller.onEditProductClicked(selectedProduct);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        controller.updateCartBadge();
        // Es buena práctica verificar el rol también al volver a la actividad.
        controller.checkUserRole();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.detachListeners();
        UserManager.getInstance().removeUserDataChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        Log.d(TAG, "onCreateOptionsMenu: Menú inflado.");
        controller.checkUserRole();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_profile) {
            controller.onProfileClicked();
            return true;
        } else if (itemId == R.id.action_add_product) {
            controller.onAddProductOptionClicked();
            return true;
        } else if (itemId == R.id.action_admin_panel) {
            startActivity(new Intent(this, AdminOrdersActivity.class));
            return true;
        } else if (itemId == R.id.action_my_orders) {
            startActivity(new Intent(this, MyOrdersActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Implementación de CatalogView ---

    @Override
    public void showLoading(boolean isLoading) {
        progressBarCatalog.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        rvProducts.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    @Override
    public void displayProducts(List<Product> products) {
        // <-- CAMBIO: Se instancia el adaptador aquí para asegurar que tiene el rol correcto.
        boolean isAdmin = UserManager.getInstance().isAdmin();
        adapter = new ProductAdapter(products, isAdmin, this);
        rvProducts.setAdapter(adapter);
    }

    @Override
    public void displayError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateCartBadge(int itemCount) {
        if (itemCount > 0) {
            fabCart.show();
        } else {
            fabCart.hide();
        }
    }

    @Override
    public void showAdminOptions(boolean isAdmin) {
        // <-- CAMBIO: Actualizamos el adaptador existente si el rol cambia dinámicamente.
        if (adapter != null) {
            boolean currentIsAdmin = UserManager.getInstance().isAdmin();
            // Recreamos el adaptador con la nueva lista de productos y el rol actualizado.
            List<Product> currentProducts = adapter.getProducts(); // Necesitarás añadir este método al adapter.
            adapter = new ProductAdapter(currentProducts, currentIsAdmin, this);
            rvProducts.setAdapter(adapter);
        }

        if (optionsMenu == null) {
            Log.w(TAG, "showAdminOptions: Se intentó actualizar la visibilidad, pero optionsMenu es nulo.");
            return;
        }

        boolean isLoggedIn = UserManager.getInstance().getCurrentUserData() != null;
        MenuItem addProductItem = optionsMenu.findItem(R.id.action_add_product);
        MenuItem adminPanelItem = optionsMenu.findItem(R.id.action_admin_panel);
        MenuItem myOrdersItem = optionsMenu.findItem(R.id.action_my_orders);

        if (addProductItem != null) addProductItem.setVisible(isAdmin);
        if (adminPanelItem != null) adminPanelItem.setVisible(isAdmin);
        if (myOrdersItem != null) myOrdersItem.setVisible(isLoggedIn);

        Log.d(TAG, "Visibilidad de menú actualizada. Admin: " + isAdmin + ", Logueado: " + isLoggedIn);
    }

    // <-- CAMBIO: Nuevo método de la interfaz CatalogView para manejar la navegación.
    @Override
    public void navigateToEditProduct(String productId) {
        Intent intent = new Intent(this, EditProductActivity.class);
        intent.putExtra("PRODUCT_ID", productId);
        startActivity(intent);
    }

    @Override
    public void navigateToLogin() {
        Toast.makeText(this, "Por favor, inicia sesión para ver tu perfil.", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public void navigateToProfile() {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    @Override
    public void navigateToCart() {
        startActivity(new Intent(this, CartActivity.class));
    }

    @Override
    public void navigateToAddProduct() {
        startActivity(new Intent(this, AddProductActivity.class));
    }

    @Override
    public void showProductAddedMessage(String productName, int quantity) {
        String message = productName + " x" + quantity + " agregado al carrito";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void shareProduct(Product product) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareBody = product.getName() + " - S/ " + String.format("%.2f", product.getPrice());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent, "Compartir producto"));
    }

    @Override
    public void onAdd(Product p, int quantity) {
        controller.onAddToCart(p, quantity);
    }

    @Override
    public void onShare(Product p) {
        controller.onShareProduct(p);
    }

    @Override
    public void onUserDataChanged() {
        Log.d(TAG, "onUserDataChanged: Notificación recibida. Verificando rol...");
        runOnUiThread(() -> controller.checkUserRole());
    }
}
