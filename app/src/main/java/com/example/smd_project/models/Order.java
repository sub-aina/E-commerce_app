package com.example.smd_project.models;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

// Implementing Serializable is highly recommended for passing the object between fragments (e.g., to OrderDetailsFragment)
public class Order implements Serializable {
    private String orderId;
    private String customerId;
    private String customerName;
    private String shippingAddress;  // Single string instead of Map
    private String paymentMethod;    // Add this field
    private double totalAmount;
    private String status;
    private long timestamp;
    private HashMap<String, OrderItem> items;
    // In Order.java
    private String customerEmail;

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    // Default constructor required for Firebase DataSnapshot.getValue(Order.class)
    public Order() {
    }

    // Constructor for creating a new order (excluding orderId and status which are set later)
    public Order(String customerId, String customerName, String shippingAddress, String paymentMethod, double totalAmount, HashMap<String, OrderItem> items) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;  // Add this line
        this.totalAmount = totalAmount;
        this.items = items;
        this.status = "Pending"; // Default status
        this.timestamp = System.currentTimeMillis();
    }

    // --- GETTERS AND SETTERS ---

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    // Add getter and setter for paymentMethod
    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public HashMap<String, OrderItem> getItems() {
        return items;
    }

    public void setItems(HashMap<String, OrderItem> items) {
        this.items = items;
    }

    // Helper method to get item count
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }
}