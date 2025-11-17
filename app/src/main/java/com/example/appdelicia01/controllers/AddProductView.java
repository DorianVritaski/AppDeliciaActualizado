package com.example.appdelicia01.controllers;

public interface AddProductView {
    void showLoading(boolean isLoading);
    void onProductSavedSuccess();
    void onProductSaveError(String message);
    void showValidationError(String field, String message);
    void clearAllErrors();
}

