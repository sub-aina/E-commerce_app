package com.example.smd_project.models;

public class CartItem {
    private String productId;
    private String productName;
    private String productImage;
    private double productPrice;
    private int quantity;
    private String vendorId;

    public CartItem() {
        // Required empty constructor for Firebase
    }

    public CartItem(String productId, String productName, String productImage,
                    double productPrice, int quantity, String vendorId) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.vendorId = vendorId;
    }

    // Getters
    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getVendorId() {
        return vendorId;
    }

    // Setters
    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    // Utility method
    public double getTotalPrice() {
        return productPrice * quantity;
    }
}