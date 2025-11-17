package com.example.appdelicia01.controllers;

import com.example.appdelicia01.models.Product;
import java.util.Map;

public interface CartView {
    void displayCartItems(Map<Product, Integer> cartItems, double totalPrice);
    void showMessage(String message);
    void navigateToLogin(int requestCode);
    void navigateToCheckout(String userEmail);
    void navigateToDeliveryOptions(double subtotal);
}
