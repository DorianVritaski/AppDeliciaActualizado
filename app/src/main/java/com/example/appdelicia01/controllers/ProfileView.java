package com.example.appdelicia01.controllers;

public interface ProfileView {
    void showLoading(boolean isLoading);
    void displayUserProfile(String fullName, String email);
    void displayError(String message);
    void navigateToLogin();
}
