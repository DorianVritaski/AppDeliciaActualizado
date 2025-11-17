package com.example.appdelicia01.controllers;

import android.util.Log;

import com.example.appdelicia01.models.CartManager;
import com.example.appdelicia01.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CartController {

    private final CartView view;
    private final CartManager cartManager;
    private final FirebaseAuth mAuth;
    private static final String TAG = "CartController";
    public static final int LOGIN_REQUEST_CODE = 1001;

    public CartController(CartView view) {
        this.view = view;
        this.cartManager = CartManager.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void loadCart() {
        view.displayCartItems(cartManager.getCartItems(), cartManager.getTotalPrice());
    }

    public void onClearCartClicked() {
        cartManager.clearCart();
        loadCart(); // Recargar la vista vacía
        view.showMessage("Carrito vaciado");
    }

    public void onProductClicked(Product product) {
        // Este método no debería existir aquí, la lógica de añadir al carrito
        // pertenece a la vista del catálogo, no del carrito.
        // Lo mantengo si lo necesitas, pero es una observación de diseño.
    }

    public void onCheckoutClicked() {
        // Validación: No proceder si el carrito está vacío.
        if (cartManager.getCartItems().isEmpty()) {
            view.showMessage("Tu carrito está vacío");
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // --- INICIO DE LA CORRECCIÓN ---
            Log.d(TAG, "Usuario logueado: " + currentUser.getEmail() + ". Procediendo a opciones de entrega.");

            // 1. Calcular el subtotal usando el método correcto.
            double subtotal = cartManager.getTotalPrice();

            // 2. Navegar a la pantalla de opciones de entrega.
            view.navigateToDeliveryOptions(subtotal);

            // La línea view.navigateToCheckout(currentUser.getEmail()); se puede eliminar
            // ya que ahora la navegación continúa desde DeliveryOptionsActivity.
            // --- FIN DE LA CORRECCIÓN ---

        } else {
            Log.d(TAG, "Usuario no logueado. Redirigiendo a LoginActivity.");
            // Le decimos a la vista que navegue a Login esperando un resultado
            view.navigateToLogin(LOGIN_REQUEST_CODE);
        }
    }

    public void onLoginResult(boolean success) {
        if (success) {
            view.showMessage("Inicio de sesión exitoso. Por favor, presiona 'Pagar' de nuevo.");
            // Es mejor que el usuario confirme su intención de nuevo.
            // Si quisieras proceder automáticamente, llamarías a onCheckoutClicked() aquí.
            // onCheckoutClicked();
        } else {
            view.showMessage("Inicio de sesión cancelado o fallido.");
        }
    }
}
