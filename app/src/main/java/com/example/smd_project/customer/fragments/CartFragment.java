package com.example.smd_project.customer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.SharedActivities.Login;
import com.example.smd_project.adapters.CartAdapter;
import com.example.smd_project.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartFragment extends Fragment {

    private RecyclerView recyclerViewCart;
    private TextView tvSubtotal, tvTotal;
    private Button btnCheckout;
    private LinearLayout tvEmptyCart;
    private ProgressBar progressBar;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private Map<String, Product> productMap;
    private View layoutTotal;

    private double subtotal = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {

            Toast.makeText(getContext(), "Please login to view cart", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return view;
        }

        initViews(view);
        loadCartItems();

        return view;
    }

    private void initViews(View view) {
        recyclerViewCart = view.findViewById(R.id.recyclerViewCart);
        tvSubtotal = view.findViewById(R.id.tvSubtotal);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        progressBar = view.findViewById(R.id.progressBar);

        cartItems = new ArrayList<>();
        productMap = new HashMap<>();
        cartAdapter = new CartAdapter(getContext(), cartItems, this::onQuantityChanged, this::onItemRemoved);

        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCart.setAdapter(cartAdapter);

        btnCheckout.setOnClickListener(v -> proceedToCheckout());
    }

    private void loadCartItems() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String userId = currentUser.getUid();

        FirebaseHelper.getInstance().getCartItems(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cartItems.clear();
                        productMap.clear();

                        if (!snapshot.exists() || !snapshot.hasChildren()) {
                            showEmptyCart();
                            return;
                        }

                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            String productId = itemSnapshot.child("productId").getValue(String.class);
                            Integer quantity = itemSnapshot.child("quantity").getValue(Integer.class);
                            String size = itemSnapshot.child("size").getValue(String.class);
                            String color = itemSnapshot.child("color").getValue(String.class);

                            if (productId != null && quantity != null) {
                                loadProductDetails(productId, quantity, size, color, itemSnapshot.getKey());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error loading cart: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadProductDetails(String productId, int quantity, String size, String color, String itemKey) {
        FirebaseHelper.getInstance().getProductReference(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Product product = snapshot.getValue(Product.class);
                            if (product != null) {
                                product.setProductId(productId);
                                productMap.put(productId, product);
                                CartItem cartItem = new CartItem(product, quantity, size, color, itemKey);
                                cartItems.add(cartItem);

                                cartAdapter.notifyDataSetChanged();
                                calculateTotal();
                                showCartContent();
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error loading product: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onQuantityChanged(CartItem item, int newQuantity) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        FirebaseHelper.getInstance().getCartItems(userId)
                .child(item.getItemKey())
                .child("quantity")
                .setValue(newQuantity)
                .addOnSuccessListener(aVoid -> {
                    item.setQuantity(newQuantity);
                    calculateTotal();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error updating quantity: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void onItemRemoved(CartItem item) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        FirebaseHelper.getInstance().removeFromCart(userId, item.getItemKey(),
                new FirebaseHelper.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Item removed from cart", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void calculateTotal() {
        subtotal = 0;
        for (CartItem item : cartItems) {
            if (item.getProduct() != null) {
                subtotal += item.getProduct().getPrice() * item.getQuantity();
            }
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));
        tvSubtotal.setText(formatter.format(subtotal));
        tvTotal.setText(formatter.format(subtotal));
    }

    private void showEmptyCart() {
        progressBar.setVisibility(View.GONE);
        recyclerViewCart.setVisibility(View.GONE);
        btnCheckout.setVisibility(View.GONE);

        // Show empty cart message
        Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
    }

    private void showCartContent() {
        recyclerViewCart.setVisibility(View.VISIBLE);
        btnCheckout.setVisibility(View.VISIBLE);
    }

    private void proceedToCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please login to checkout", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Navigate to Checkout Fragment
        CheckoutFragment checkoutFragment = new CheckoutFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.customerFragmentContainer, checkoutFragment)
                .addToBackStack(null)
                .commit();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(getActivity(), Login.class);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    public static class CartItem {
        private Product product;
        private int quantity;
        private String size;
        private String color;
        private String itemKey;

        public CartItem(Product product, int quantity, String size, String color, String itemKey) {
            this.product = product;
            this.quantity = quantity;
            this.size = size;
            this.color = color;
            this.itemKey = itemKey;
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getSize() { return size; }
        public String getColor() { return color; }
        public String getItemKey() { return itemKey; }
    }
}