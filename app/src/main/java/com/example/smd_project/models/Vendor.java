package com.example.smd_project.models;

public class Vendor {
    private String vendorId;
    private String userId;
    private String shopName;
    private String shopDescription;
    private String shopLogo;
    private boolean isApproved;
    private double rating;
    private int totalSales;

    public Vendor() {}

    public Vendor(String vendorId, String userId, String shopName, String shopDescription) {
        this.vendorId = vendorId;
        this.userId = userId;
        this.shopName = shopName;
        this.shopDescription = shopDescription;
        this.isApproved = false;
        this.rating = 0.0;
        this.totalSales = 0;
    }

    // Getters and Setters
    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopDescription() {
        return shopDescription;
    }

    public void setShopDescription(String shopDescription) {
        this.shopDescription = shopDescription;
    }

    public String getShopLogo() {
        return shopLogo;
    }

    public void setShopLogo(String shopLogo) {
        this.shopLogo = shopLogo;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(int totalSales) {
        this.totalSales = totalSales;
    }
}