package com.example.appdelicia01.controllers;

public interface RegisterView {
    void showLoading(boolean isLoading);
    void onRegisterSuccess(String email);
    void onRegisterError(String message);
    void onValidationError(String field, String message);
    void clearValidationErrors();
}
