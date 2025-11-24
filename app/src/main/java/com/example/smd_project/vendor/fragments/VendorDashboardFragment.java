package com.example.smd_project.vendor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.util.Locale;

public class VendorDashboardFragment extends Fragment {

    private TextView tvTotalUsers, tvTotalProducts, tvTotalOrders, tvTotalRevenue;
    private TextView tvPendingOrders, tvShippedOrders, tvDeliveredOrders, tvTotalCategories;
    private TextView tvWelcomeAdmin;
    private ProgressBar progressBar;
    private CardView cardUsers, cardProducts, cardOrders, cardRevenue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_dashboard, container, false);

        initViews(view);
        loadDashboardData();

        return view;
    }

    private void initViews(View view) {
        // Welcome message
        tvWelcomeAdmin = view.findViewById(R.id.tvWelcomeAdmin);

        // Summary cards
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts);
        tvTotalOrders = view.findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);

        // stats
        tvPendingOrders = view.findViewById(R.id.tvPendingOrders);
        tvShippedOrders = view.findViewById(R.id.tvShippedOrders);
        tvDeliveredOrders = view.findViewById(R.id.tvDeliveredOrders);
        tvTotalCategories = view.findViewById(R.id.tvTotalCategories);

        progressBar = view.findViewById(R.id.progressBar);

        // Cards for click actions
        cardUsers = view.findViewById(R.id.cardUsers);
        cardProducts = view.findViewById(R.id.cardProducts);
        cardOrders = view.findViewById(R.id.cardOrders);
        cardRevenue = view.findViewById(R.id.cardRevenue);

        // Set welcome message
        String adminName = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : "Admin";
        tvWelcomeAdmin.setText("Welcome, " + adminName.split("@")[0] + "!");
    }

    private void loadDashboardData() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseHelper.getInstance().getDashboardStats(new FirebaseHelper.OnDashboardStatsListener() {
            @Override
            public void onSuccess(FirebaseHelper.DashboardStats stats) {
                progressBar.setVisibility(View.GONE);
                updateUI(stats);
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error loading dashboard: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(FirebaseHelper.DashboardStats stats) {
        // Update main cards
        tvTotalUsers.setText(String.valueOf(stats.totalUsers));
        tvTotalProducts.setText(String.valueOf(stats.totalProducts));
        tvTotalOrders.setText(String.valueOf(stats.totalOrders));

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));
        tvTotalRevenue.setText(formatter.format(stats.totalRevenue));

        // Update additional stats
        tvPendingOrders.setText(String.valueOf(stats.pendingOrders));
        tvShippedOrders.setText(String.valueOf(stats.shippedOrders));
        tvDeliveredOrders.setText(String.valueOf(stats.deliveredOrders));
        tvTotalCategories.setText(String.valueOf(stats.totalCategories));

        // Add click listeners for navigation
        cardUsers.setOnClickListener(v -> navigateToManageUsers());
        cardProducts.setOnClickListener(v -> navigateToManageProducts());
        cardOrders.setOnClickListener(v -> navigateToManageOrders());
    }

    private void navigateToManageUsers() {
        // TODO: Navigate to Manage Users Fragment
        Toast.makeText(getContext(), "Navigate to Manage Users", Toast.LENGTH_SHORT).show();
    }

    private void navigateToManageProducts() {
        // TODO: Navigate to Manage Products Fragment
        Toast.makeText(getContext(), "Navigate to Manage Products", Toast.LENGTH_SHORT).show();
    }

    private void navigateToManageOrders() {
        // TODO: Navigate to Manage Orders Fragment
        Toast.makeText(getContext(), "Navigate to Manage Orders", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }
}