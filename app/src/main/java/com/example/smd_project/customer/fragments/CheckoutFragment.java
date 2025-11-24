package com.example.smd_project.customer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.adapters.CheckoutAdapter;
import com.example.smd_project.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutFragment extends Fragment {

    private RecyclerView rvOrderSummary;
    private EditText etFullName, etPhoneNumber, etAddress, etCity, etPostalCode;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCashOnDelivery, rbWallet;
    private TextView tvSubtotal, tvShipping, tvTotal;
    private Button btnPlaceOrder;
    private ProgressBar progressBar;

    private CheckoutAdapter adapter;
    private List<CartFragment.CartItem> cartItems;
    private double subtotal = 0;
    private double shippingFee = 150;
    private double total = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        initViews(view);
        loadCartItems();
        setupPaymentMethod();

        return view;
    }

    private void initViews(View view) {
        rvOrderSummary = view.findViewById(R.id.rvOrderSummary);
        etFullName = view.findViewById(R.id.etFullName);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        etAddress = view.findViewById(R.id.etAddress);
        etCity = view.findViewById(R.id.etCity);
        etPostalCode = view.findViewById(R.id.etPostalCode);
        rgPaymentMethod = view.findViewById(R.id.rgPaymentMethod);
        rbCashOnDelivery = view.findViewById(R.id.rbCashOnDelivery);
        tvSubtotal = view.findViewById(R.id.tvSubtotal);
        tvShipping = view.findViewById(R.id.tvShipping);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);
        progressBar = view.findViewById(R.id.progressBar);

        cartItems = new ArrayList<>();
        adapter = new CheckoutAdapter(getContext(), cartItems);
        rvOrderSummary.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderSummary.setAdapter(adapter);

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void setupPaymentMethod() {
        rbCashOnDelivery.setChecked(true); // Default to Cash on Delivery
    }

    private void loadCartItems() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseHelper.getInstance().getCartItems(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cartItems.clear();
                        subtotal = 0;

                        if (!snapshot.exists()) {
                            Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
                            goBackToCart();
                            return;
                        }

                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            String productId = itemSnapshot.child("productId").getValue(String.class);
                            Integer quantity = itemSnapshot.child("quantity").getValue(Integer.class);
                            String size = itemSnapshot.child("size").getValue(String.class);
                            String color = itemSnapshot.child("color").getValue(String.class);

                            if (productId != null && quantity != null) {
                                loadProductForCheckout(productId, quantity, size, color, itemSnapshot.getKey());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error loading cart", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadProductForCheckout(String productId, int quantity, String size, String color, String itemKey) {
        FirebaseHelper.getInstance().getProductReference(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            com.example.smd_project.models.Product product =
                                    snapshot.getValue(com.example.smd_project.models.Product.class);

                            if (product != null) {
                                CartFragment.CartItem cartItem =
                                        new CartFragment.CartItem(product, quantity, size, color, itemKey);
                                cartItems.add(cartItem);

                                subtotal += product.getPrice() * quantity;

                                adapter.notifyDataSetChanged();
                                updateTotals();
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void updateTotals() {
        total = subtotal + shippingFee;

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));
        tvSubtotal.setText(formatter.format(subtotal));
        tvShipping.setText(formatter.format(shippingFee));
        tvTotal.setText(formatter.format(total));
    }

    private void placeOrder() {
        // Validate inputs
        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String postalCode = etPostalCode.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (phoneNumber.isEmpty()) {
            etPhoneNumber.setError("Phone number is required");
            etPhoneNumber.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return;
        }

        if (city.isEmpty()) {
            etCity.setError("City is required");
            etCity.requestFocus();
            return;
        }

        if (postalCode.isEmpty()) {
            etPostalCode.setError("Postal code is required");
            etPostalCode.requestFocus();
            return;
        }

        // Get payment method
        String paymentMethod;
        if (rbCashOnDelivery.isChecked()) {
            paymentMethod = "Cash on Delivery";
        } else if (rbWallet.isChecked()) {
            paymentMethod = "Wallet";
        } else {
            Toast.makeText(getContext(), "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create order
        progressBar.setVisibility(View.VISIBLE);
        btnPlaceOrder.setEnabled(false);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String orderId = FirebaseHelper.getInstance().getAllOrdersReference().push().getKey();


        String shippingAddressStr = fullName + "\n" +
                phoneNumber + "\n" +
                address + ", " + city + " - " + postalCode;

        HashMap<String, com.example.smd_project.models.OrderItem> orderItems = new HashMap<>();
        for (CartFragment.CartItem item : cartItems) {
            String itemKey = item.getProduct().getProductId();

            com.example.smd_project.models.OrderItem orderItem =
                    new com.example.smd_project.models.OrderItem(
                            item.getProduct().getProductId(),
                            item.getProduct().getName(),
                            item.getProduct().getPrice(),
                            item.getQuantity(),
                            item.getSize(),
                            item.getColor(),
                            item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()
                                    ? item.getProduct().getImages().get(0) : ""
                    );

            orderItems.put(itemKey, orderItem);
        }


        Order order = new Order(
                userId,
                fullName,
                shippingAddressStr,
                paymentMethod,
                total,
                orderItems
        );
        order.setOrderId(orderId);
        order.setStatus("Pending");
        order.setTimestamp(System.currentTimeMillis());

        // Save order to Firebase
        FirebaseHelper.getInstance().createOrder(orderId, order,
                new FirebaseHelper.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        // Clear cart after successful order
                        FirebaseHelper.getInstance().clearCart(userId,
                                new FirebaseHelper.OnCompleteListener() {
                                    @Override
                                    public void onSuccess() {

                                        if (isAdded()) {
                                            progressBar.setVisibility(View.GONE);
                                            btnPlaceOrder.setEnabled(true);


                                            Toast.makeText(getContext(),
                                                    " Order placed successfully!",
                                                    Toast.LENGTH_LONG).show();


                                            showOrderConfirmation(orderId);
                                        }
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        // ⭐ FIX 2: Check if the Fragment is still attached (Safe Context)
                                        if (isAdded()) {
                                            progressBar.setVisibility(View.GONE);
                                            btnPlaceOrder.setEnabled(true);
                                            Toast.makeText(getContext(),
                                                    "Order placed but cart clear failed: " + error,
                                                    Toast.LENGTH_SHORT).show();
                                            // Still navigate, as the order itself succeeded
                                            showOrderConfirmation(orderId);
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onFailure(String error) {
                        if (isAdded()) {
                            progressBar.setVisibility(View.GONE);
                            btnPlaceOrder.setEnabled(true);
                            Toast.makeText(getContext(),
                                    "Failed to place order: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showOrderConfirmation(String orderId) {
        // Navigate to OrdersFragment or show confirmation dialog
        OrderConfirmationFragment confirmationFragment = OrderConfirmationFragment.newInstance(orderId);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.customerFragmentContainer, confirmationFragment)
                .commit();
    }

    private void goBackToCart() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}