package com.example.appdelicia01.controllers;

import android.util.Log;
import com.example.appdelicia01.models.CartManager;
import com.example.appdelicia01.models.Product;
import com.example.appdelicia01.models.UserManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CatalogController {

    private final CatalogView view;
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private final CartManager cartManager;
    private final UserManager userManager;
    private ListenerRegistration productsListener;
    private static final String TAG = "CatalogController";

    public CatalogController(CatalogView view) {
        this.view = view;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.cartManager = CartManager.getInstance();
        this.userManager = UserManager.getInstance();
    }

    public void loadProducts() {
        view.showLoading(true);
        productsListener = db.collection("products")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    view.showLoading(false);
                    if (error != null) {
                        Log.e(TAG, "Error al escuchar cambios en productos", error);
                        view.displayError("Error al cargar productos.");
                        return;
                    }

                    if (value != null) {
                        List<Product> productList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Product product = doc.toObject(Product.class);
                            product.setId(doc.getId());
                            productList.add(product);
                        }
                        Log.d(TAG, "Productos cargados: " + productList.size());
                        view.displayProducts(productList);
                    }
                });
    }

    public void onAddToCart(Product product, int quantity) {
        cartManager.addToCart(product, quantity);
        view.showProductAddedMessage(product.getName(), quantity);
        updateCartBadge();
    }

    public void onShareProduct(Product product) {
        view.shareProduct(product);
    }

    public void updateCartBadge() {
        view.updateCartBadge(cartManager.getTotalItemCount());
    }

    public void checkUserRole() {
        view.showAdminOptions(userManager.isAdmin());
    }

    public void detachListeners() {
        if (productsListener != null) {
            productsListener.remove();
            Log.d(TAG, "Firestore listener detached.");
        }
    }

    public void onProfileClicked() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            view.navigateToProfile();
        } else {
            view.navigateToLogin();
        }
    }

    // Métodos para manejar los clics del menú de opciones
    public void onCartOptionClicked() {
        view.navigateToCart();
    }

    public void onAddProductOptionClicked() {
        if (userManager.isAdmin()) {
            view.navigateToAddProduct();
        } else {
            Log.w(TAG, "Intento no autorizado de acceder a AddProduct.");
            view.displayError("No tienes permiso para realizar esta acción.");
        }
    }
}