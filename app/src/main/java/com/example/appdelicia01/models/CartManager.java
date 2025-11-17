package com.example.appdelicia01.models;

import java.util.HashMap;

import java.util.Map;



public class CartManager {
    private static CartManager instance; // Nombre de instancia cambiado de INSTANCE a instance
    private final Map<Product, Integer> cart = new HashMap<>(); // Product como clave, Integer como cantidad

    private CartManager() {
        // Constructor privado para el patrón Singleton
    }

    // Método get() renombrado a getInstance() y no es synchronized por defecto
    // Si la concurrencia es una preocupación importante, podrías necesitar
    // volver a hacerlo synchronized o usar double-checked locking.
    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // Método add() renombrado a addToCart() y ahora toma Product y quantity
    public void addToCart(Product p, int quantity) {
        // Asegúrate que la clase Product tenga implementados hashCode() y equals()
        // para que funcione correctamente como clave en un HashMap.
        int currentQuantity = cart.getOrDefault(p, 0);
        cart.put(p, currentQuantity + quantity);
    }

    // Nuevo método para obtener todos los items del carrito
    public Map<Product, Integer> getCartItems() {
        return cart; // Devuelve una referencia directa al mapa, considera devolver una copia si la inmutabilidad es importante
    }

    // Método clear() renombrado a clearCart()
    public void clearCart() {
        cart.clear();
    }



    // Ejemplo de cómo podría ser getTotalPrice con la nueva estructura:
    public double getTotalPrice() {
        double sum = 0;
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            sum += entry.getKey().getPrice() * entry.getValue();
        }
        return sum;
    }

    // Ejemplo de cómo podría ser getQuantityOfProduct:
    public int getTotalItemCount() {
        int totalItems = 0;
        if (cart == null || cart.isEmpty()) {
            return 0;
        }
        for (Integer quantity : cart.values()) {
            if (quantity != null) {
                totalItems += quantity;
            }
        }
        return totalItems;
    }
}
