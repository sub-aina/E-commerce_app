package com.example.smd_project.models;

import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    private String productId;
    private String name;
    private String description;
    private double price;
    private List<String> images;
    private String category;
    private List<String> sizes;
    private List<String> colors;
    private int stock;
    private double rating;
    private boolean featured;
    private long createdAt;

    public Product() {
        // Default constructor required for Firebase
    }

    public Product(String name, String description, double price, int stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.rating = 0.0;
        this.featured = false;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getId()
    {
        return productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getSizes() {
        return sizes;
    }

    public void setSizes(List<String> sizes) {
        this.sizes = sizes;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isInStock() {
        return stock > 0;
    }

    public void decreaseStock(int quantity) {
        if (stock >= quantity) {
            stock -= quantity;
        }
    }

    public void increaseStock(int quantity) {
        stock += quantity;
    }
}