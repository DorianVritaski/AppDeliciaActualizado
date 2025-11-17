package com.example.appdelicia01.controllers;

import com.example.appdelicia01.models.Product;
import java.util.List;

public interface CatalogView {
    void showLoading(boolean isLoading);
    void displayProducts(List<Product> products);
    void displayError(String message);
    void updateCartBadge(int itemCount);
    void showAdminOptions(boolean isAdmin);
    void navigateToLogin();
    void navigateToProfile();
    void navigateToCart();
    void navigateToAddProduct();
    void showProductAddedMessage(String productName, int quantity);
    void shareProduct(Product product);
}

