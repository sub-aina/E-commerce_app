package com.example.smd_project;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.smd_project.models.User;
import com.example.smd_project.models.Product;
import com.google.firebase.database.ValueEventListener;
import com.example.smd_project.models.Category;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FirebaseHelper {
    private static FirebaseHelper instance;
    private DatabaseReference database;

    private FirebaseHelper() {
        database = FirebaseDatabase.getInstance().getReference();
    }

    public static FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    //USERS

    public void createUser(String userId, User user, OnCompleteListener listener) {
        Log.d("FirebaseHelper", "Creating user with ID: " + userId);
        Log.d("FirebaseHelper", "Database reference: " + database.child("Users").child(userId).toString());

        database.child("Users").child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", " User created successfully in database");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", " Failed to create user: " + e.getMessage(), e);
                    listener.onFailure(e.getMessage());
                });
    }

    public void updateUser(String userId, User user, OnCompleteListener listener) {
        database.child("Users").child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public DatabaseReference getUserReference(String userId) {
        return database.child("Users").child(userId);
    }

    public DatabaseReference getAllUsersReference() {
        return database.child("Users");
    }

    public void getUser(String userId, OnUserFetchListener listener) {
        database.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    listener.onSuccess(user);
                } else {
                    listener.onFailure("User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void deleteUser(String userId, OnCompleteListener listener) {
        database.child("Users").child(userId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    //  PRODUCTS

    public void addProduct(Product product, OnCompleteListener listener) {
        String productId = database.child("Products").push().getKey();
        if (productId != null) {
            product.setProductId(productId);
            database.child("Products").child(productId).setValue(product)
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
        }
    }

    public void updateProduct(String productId, Product product, OnCompleteListener listener) {
        database.child("Products").child(productId).setValue(product)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public DatabaseReference getProductReference(String productId) {
        return database.child("Products").child(productId);
    }

    public DatabaseReference getAllProductsReference() {
        return database.child("Products");
    }

    public DatabaseReference getProductsByCategory(String category) {
        return database.child("Products").orderByChild("category").equalTo(category).getRef();
    }

    public DatabaseReference getFeaturedProducts() {
        return database.child("Products").orderByChild("featured").equalTo(true).getRef();
    }

    public void updateProductStock(String productId, int newStock, OnCompleteListener listener) {
        database.child("Products").child(productId).child("stock").setValue(newStock)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void deleteProduct(String productId, OnCompleteListener listener) {
        database.child("Products").child(productId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // CATEGORIES

    public void addCategory(Category category, OnCompleteListener listener) {
        String categoryId = database.child("Categories").push().getKey();
        if (categoryId != null) {
            category.setCategoryId(categoryId);
            category.setCreatedAt(System.currentTimeMillis());
            database.child("Categories").child(categoryId).setValue(category)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirebaseHelper", " Category added successfully");
                        listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseHelper", " Failed to add category: " + e.getMessage());
                        listener.onFailure(e.getMessage());
                    });
        } else {
            listener.onFailure("Failed to generate category ID");
        }
    }

    public void updateCategory(String categoryId, Category category, OnCompleteListener listener) {
        database.child("Categories").child(categoryId).setValue(category)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", " Category updated successfully");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", " Failed to update category: " + e.getMessage());
                    listener.onFailure(e.getMessage());
                });
    }

    public DatabaseReference getAllCategoriesReference() {
        return database.child("Categories");
    }

    public void getCategory(String categoryId, OnCategoryFetchListener listener) {
        database.child("Categories").child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Category category = snapshot.getValue(Category.class);
                    listener.onSuccess(category);
                } else {
                    listener.onFailure("Category not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void deleteCategory(String categoryId, OnCompleteListener listener) {
        database.child("Categories").child(categoryId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", " Category deleted successfully");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "✗ Failed to delete category: " + e.getMessage());
                    listener.onFailure(e.getMessage());
                });
    }

    //  ORDERS

    public void createOrder(String orderId, Object order, OnCompleteListener listener) {
        database.child("Orders").child(orderId).setValue(order)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public DatabaseReference getAllOrdersReference() {
        return database.child("Orders");
    }

    public DatabaseReference getOrdersByCustomer(String customerId) {
        return database.child("Orders").orderByChild("customerId").equalTo(customerId).getRef();
    }

    public void updateOrderStatus(String orderId, String status, OnCompleteListener listener) {
        database.child("Orders").child(orderId).child("status").setValue(status)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    //  CART

    public void addToCart(String userId, String itemKey, Object item, OnCompleteListener listener) {
        database.child("Cart").child(userId).child("items").child(itemKey).setValue(item)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public DatabaseReference getCartItems(String userId) {
        return database.child("Cart").child(userId).child("items");
    }

    public void removeFromCart(String userId, String itemKey, OnCompleteListener listener) {
        database.child("Cart").child(userId).child("items").child(itemKey).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void clearCart(String userId, OnCompleteListener listener) {
        database.child("Cart").child(userId).child("items").removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // WISHLIST

    public void addToWishlist(String userId, String productId, OnCompleteListener listener) {
        database.child("Wishlist").child(userId).child("productIds").child(productId).setValue(true)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void removeFromWishlist(String userId, String productId, OnCompleteListener listener) {
        database.child("Wishlist").child(userId).child("productIds").child(productId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public DatabaseReference getWishlist(String userId) {
        return database.child("Wishlist").child(userId).child("productIds");
    }

    // REVIEWS

    public void addReview(String reviewId, Object review, OnCompleteListener listener) {
        database.child("Reviews").child(reviewId).setValue(review)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public DatabaseReference getReviewsByProduct(String productId) {
        return database.child("Reviews").orderByChild("productId").equalTo(productId).getRef();
    }

    // DASHBOARD STATISTICS

    public void getDashboardStats(OnDashboardStatsListener listener) {
        DashboardStats stats = new DashboardStats();

        // Get total users
        database.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stats.totalUsers = (int) snapshot.getChildrenCount();
                checkStatsComplete(stats, listener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });

        // Get total products
        database.child("Products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stats.totalProducts = (int) snapshot.getChildrenCount();
                checkStatsComplete(stats, listener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });

        // Get total orders and calculate revenue
        database.child("Orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stats.totalOrders = (int) snapshot.getChildrenCount();
                double revenue = 0;
                int pending = 0, shipped = 0, delivered = 0;

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Double amount = orderSnapshot.child("totalAmount").getValue(Double.class);
                    if (amount != null) {
                        revenue += amount;
                    }

                    String status = orderSnapshot.child("status").getValue(String.class);
                    if (status != null) {
                        switch (status.toLowerCase()) {
                            case "pending":
                                pending++;
                                break;
                            case "shipped":
                                shipped++;
                                break;
                            case "delivered":
                                delivered++;
                                break;
                        }
                    }
                }

                stats.totalRevenue = revenue;
                stats.pendingOrders = pending;
                stats.shippedOrders = shipped;
                stats.deliveredOrders = delivered;
                stats.ordersComplete = true;
                checkStatsComplete(stats, listener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });

        // Get total categories
        database.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stats.totalCategories = (int) snapshot.getChildrenCount();
                checkStatsComplete(stats, listener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    private void checkStatsComplete(DashboardStats stats, OnDashboardStatsListener listener) {
        if (stats.totalUsers >= 0 && stats.totalProducts >= 0 &&
                stats.totalOrders >= 0 && stats.totalCategories >= 0 && stats.ordersComplete) {
            listener.onSuccess(stats);
        }
    }

    public static class DashboardStats {
        public int totalUsers = -1;
        public int totalProducts = -1;
        public int totalOrders = -1;
        public int totalCategories = -1;
        public double totalRevenue = 0;
        public int pendingOrders = 0;
        public int shippedOrders = 0;
        public int deliveredOrders = 0;
        public boolean ordersComplete = false;
    }

//  SEARCH


    public void searchProducts(String query, OnProductSearchListener listener) {
        if (query == null || query.trim().isEmpty()) {
            listener.onSuccess(new ArrayList<>());
            return;
        }

        String searchQuery = query.toLowerCase().trim();

        database.child("Products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> results = new ArrayList<>();

                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);

                    if (product != null && isProductMatchingQuery(product, searchQuery)) {
                        results.add(product);
                    }
                }

                listener.onSuccess(results);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }


    private boolean isProductMatchingQuery(Product product, String query) {
        // Search in product name
        if (product.getName() != null &&
                product.getName().toLowerCase().contains(query)) {
            return true;
        }

        // Search in product description
        if (product.getDescription() != null &&
                product.getDescription().toLowerCase().contains(query)) {
            return true;
        }

        // Search in product category
        if (product.getCategory() != null &&
                product.getCategory().toLowerCase().contains(query)) {
            return true;
        }

        return false;
    }

    public void searchProductsByPriceRange(double minPrice, double maxPrice,
                                           OnProductSearchListener listener) {
        database.child("Products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> results = new ArrayList<>();

                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);

                    if (product != null && product.getPrice() >= minPrice &&
                            product.getPrice() <= maxPrice) {
                        results.add(product);
                    }
                }

                listener.onSuccess(results);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }


    public void getSearchSuggestions(String partialQuery, OnSearchSuggestionsListener listener) {
        if (partialQuery == null || partialQuery.trim().isEmpty()) {
            listener.onSuccess(new ArrayList<>());
            return;
        }

        String query = partialQuery.toLowerCase().trim();

        database.child("Products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> suggestions = new HashSet<>();

                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);

                    if (product != null) {
                        // Add product name if it matches
                        if (product.getName() != null &&
                                product.getName().toLowerCase().startsWith(query)) {
                            suggestions.add(product.getName());
                        }

                        // Add category if it matches
                        if (product.getCategory() != null &&
                                product.getCategory().toLowerCase().startsWith(query)) {
                            suggestions.add(product.getCategory());
                        }
                    }

                    // Limit suggestions to 10
                    if (suggestions.size() >= 10) break;
                }

                listener.onSuccess(new ArrayList<>(suggestions));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

//  SEARCH CALLBACK INTERFACES

    public interface OnProductSearchListener {
        void onSuccess(List<Product> products);
        void onFailure(String error);
    }

    public interface OnSearchSuggestionsListener {
        void onSuccess(List<String> suggestions);
        void onFailure(String error);
    }
    // CALLBACK INTERFACES

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }


    public interface OnUserFetchListener {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface OnDashboardStatsListener {
        void onSuccess(DashboardStats stats);
        void onFailure(String error);
    }


    public interface OnCategoryFetchListener {
        void onSuccess(Category category);
        void onFailure(String error);
    }
}