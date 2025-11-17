package com.example.appdelicia01.models;

public class Product {
    private String id; // ID del documento de Firestore
    private String name;
    private double price;
    private String description;
    private String imageUrl; // URL de la imagen del producto
    // El campo `image` (int) ya no es necesario si todos los productos vienen de Firestore

    // Constructor vacío requerido por Firestore para la deserialización automática
    public Product() {}

    // Constructor que usaremos en la app
    public Product(String id, String name, double price, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }

    // Setters (también requeridos por Firestore)
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Métodos equals() y hashCode() para que el CartManager funcione correctamente.
    // Es crucial que la igualdad se base en el ID único del producto.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        // Si el ID es nulo en ambos casos, podrían no ser iguales a menos que otros campos lo sean.
        // Pero para productos de Firestore, el ID debe ser el diferenciador clave.
        return id != null ? id.equals(product.id) : product.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
