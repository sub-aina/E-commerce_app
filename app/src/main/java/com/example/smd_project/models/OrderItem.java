package com.example.smd_project.models;

import java.io.Serializable;

public class OrderItem implements Serializable {

    private String productId;
    private String productName;
    private double price;
    private int quantity;
    private String size;
    private String color;
    private String imageUrl;

    // Default constructor required for Firebase
    public OrderItem() {
    }

    public OrderItem(String productId, String productName, double price, int quantity,
                     String size, String color, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.size = size;
        this.color = color;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getItemTotal() {
        return price * quantity;
    }
}