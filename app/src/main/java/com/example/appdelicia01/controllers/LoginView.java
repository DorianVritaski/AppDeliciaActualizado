package com.example.appdelicia01.controllers;

public interface LoginView {
    void showLoading(boolean isLoading);
    void onLoginSuccess();
    void onLoginError(String message);
    void onValidationError(String field, String message);
    void clearValidationErrors();
}
