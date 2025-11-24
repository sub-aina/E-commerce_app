package com.example.smd_project.customer.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.SharedActivities.Login;
import com.example.smd_project.adapters.CustomerOrderAdapter;
import com.example.smd_project.adapters.WishlistAdapter;
import com.example.smd_project.models.Order;
import com.example.smd_project.models.Product;
import com.example.smd_project.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // User Info Views
    private ImageView ivProfilePicture;
    private TextView tvUserName, tvUserEmail;

    // Stats
    private TextView tvOrderCount, tvWishlistCount;

    // Wishlist Section
    private RecyclerView rvWishlist;
    private TextView tvWishlistEmpty, tvViewAllWishlist;
    private WishlistAdapter wishlistAdapter;
    private List<Product> wishlistProducts = new ArrayList<>();

    // Orders Section
    private RecyclerView rvOrders;
    private TextView tvOrdersEmpty, tvViewAllOrders;
    private CustomerOrderAdapter orderAdapter;
    private List<Order> orderList = new ArrayList<>();

    // Menu Options
    private LinearLayout btnEditProfile, btnMyAddresses, btnPaymentMethods, btnNotifications, btnHelp, btnLogout;

    private FirebaseUser currentUser;
    private String currentAddress = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_profile, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return view;
        }

        initViews(view);
        setupRecyclerViews();
        loadUserData();
        loadWishlist();
        loadOrders();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        // User info
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);

        // Wishlist
        rvWishlist = view.findViewById(R.id.rvWishlist);
        tvWishlistEmpty = view.findViewById(R.id.tvWishlistEmpty);
        tvViewAllWishlist = view.findViewById(R.id.tvViewAllWishlist);

        rvOrders = view.findViewById(R.id.rvOrders);
        tvOrdersEmpty = view.findViewById(R.id.tvOrdersEmpty);
        tvViewAllOrders = view.findViewById(R.id.tvViewAllOrders);
        tvViewAllOrders = view.findViewById(R.id.tvViewAllOrders);

        // Menu options
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnMyAddresses = view.findViewById(R.id.btnMyAddresses);

        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void setupRecyclerViews() {
        // Wishlist
        wishlistAdapter = new WishlistAdapter(getContext(), wishlistProducts, product -> {
            openProductDetail(product.getProductId());
        });
        rvWishlist.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvWishlist.setAdapter(wishlistAdapter);


    }

    private void loadUserData() {
        FirebaseHelper.getInstance().getUser(currentUser.getUid(), new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                if (user != null && isAdded()) {
                    tvUserName.setText(user.getName() != null ? user.getName() : "User");
                    tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : currentUser.getEmail());

                    currentAddress = user.getAddress() != null ? user.getAddress() : "";

                    if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                        Glide.with(requireContext())
                                .load(user.getProfileImage())
                                .placeholder(R.drawable.ic_profile) // Make sure you have this drawable
                                .circleCrop()
                                .into(ivProfilePicture);
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                tvUserName.setText("User");
            }
        });
    }


    private void loadOrders() {
        FirebaseDatabase.getInstance().getReference("Orders")
                .orderByChild("userId")
                .equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderList.clear();
                        long count = snapshot.getChildrenCount();

                        if (count == 0) {
                            tvOrdersEmpty.setVisibility(View.VISIBLE);
                            rvOrders.setVisibility(View.GONE);
                            return;
                        }

                        tvOrdersEmpty.setVisibility(View.GONE);
                        rvOrders.setVisibility(View.VISIBLE);


                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            Order order = orderSnapshot.getValue(Order.class);
                            if (order != null) {
                                orderList.add(order);
                            }
                        }


                        Collections.reverse(orderList);


                        if(orderList.size() > 3) {
                            orderList = orderList.subList(0, 3);
                        }

                        orderAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load orders: " + error.getMessage());
                    }
                });
    }

    private void loadWishlist() {
        String userId = currentUser.getUid();

        FirebaseHelper.getInstance().getWishlist(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        wishlistProducts.clear();
                        long wishlistCount = snapshot.getChildrenCount();

                        if (wishlistCount == 0) {
                            tvWishlistEmpty.setVisibility(View.VISIBLE);
                            rvWishlist.setVisibility(View.GONE);
                            wishlistAdapter.notifyDataSetChanged(); // Notify empty state
                            return;
                        }

                        tvWishlistEmpty.setVisibility(View.GONE);
                        rvWishlist.setVisibility(View.VISIBLE);

                        // Only load first 4 items for the profile preview
                        int limit = 0;
                        for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                            if (limit >= 4) break;

                            String productId = productSnapshot.getKey();
                            loadProductForWishlist(productId);
                            limit++;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load wishlist");
                    }
                });
    }

    private void loadProductForWishlist(String productId) {
        FirebaseHelper.getInstance().getProductReference(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Product product = snapshot.getValue(Product.class);
                        if (product != null && isAdded()) {
                            product.setProductId(snapshot.getKey());
                            wishlistProducts.add(product);
                            wishlistAdapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setupClickListeners() {
        // Navigation listeners
        tvViewAllWishlist.setOnClickListener(v -> navigateToFragment(new WishlistFragment()));
        tvViewAllOrders.setOnClickListener(v -> navigateToFragment(new OrdersFragment()));
        btnEditProfile.setOnClickListener(v -> navigateToFragment(new EditProfileFragment()));

        // --- NEW DIALOGS ---
        btnMyAddresses.setOnClickListener(v -> showAddressDialog());


        btnLogout.setOnClickListener(v -> logout());
    }

    private void showAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_address, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Transparent background for CardView corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText etAddress = dialogView.findViewById(R.id.etAddress);
        View btnSave = dialogView.findViewById(R.id.btnSaveAddress);
        View btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Pre-fill existing address
        etAddress.setText(currentAddress);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newAddress = etAddress.getText().toString().trim();
            if (newAddress.isEmpty()) {
                etAddress.setError("Address cannot be empty");
                return;
            }

            // Update Firebase
            FirebaseHelper.getInstance().getUserReference(currentUser.getUid())
                    .child(currentUser.getUid())
                    .child("address")
                    .setValue(newAddress)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Address updated", Toast.LENGTH_SHORT).show();
                        currentAddress = newAddress; // Update local variable
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    private void navigateToFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.customerFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void openProductDetail(String productId) {
        ProductDetailFragment fragment = ProductDetailFragment.newInstance(productId);
        navigateToFragment(fragment);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}