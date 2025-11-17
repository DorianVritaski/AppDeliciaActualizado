package com.example.appdelicia01.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.Map;

public class Order {

    // --- Estados del Pedido ---
    public static final String STATUS_PENDING = "Pendiente";
    public static final String STATUS_CONFIRMED = "Confirmado";
    public static final String STATUS_READY_FOR_PICKUP = "Listo para recoger";
    public static final String STATUS_OUT_FOR_DELIVERY = "En camino";
    public static final String STATUS_COMPLETED = "Completado";
    public static final String STATUS_CANCELLED = "Cancelado";

    // --- Atributos del Pedido ---
    private String orderId;
    private String userId;
    private String userEmail;
    private Map<String, Object> cartItems;
    private String deliveryMethod;
    private String deliveryAddress;
    private String paymentMethod;
    private double subtotal;
    private double deliveryFee;
    private double totalAmount;
    private String status;
    @ServerTimestamp
    private Date timestamp;
    @Exclude
    private String userName;

    // Constructor vacío requerido por Firestore
    public Order() {}

    // Constructor principal
    public Order(String userId, String userEmail, Map<String, Object> cartItems, String deliveryMethod, String deliveryAddress, String paymentMethod, double subtotal, double deliveryFee, double totalAmount) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.cartItems = cartItems;
        this.deliveryMethod = deliveryMethod;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.totalAmount = totalAmount;
        this.status = STATUS_PENDING;
    }

    // --- Getters y Setters ---

    @Exclude
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Map<String, Object> getCartItems() {
        return cartItems;
    }

    public void setCartItems(Map<String, Object> cartItems) {
        this.cartItems = cartItems;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
