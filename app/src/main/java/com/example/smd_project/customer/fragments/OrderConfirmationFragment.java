package com.example.smd_project.customer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smd_project.R;
import com.example.smd_project.vendor.fragments.OrderFragment;

public class OrderConfirmationFragment extends Fragment {

    private static final String ARG_ORDER_ID = "order_id";

    private TextView tvOrderId, tvMessage;
    private ImageView ivSuccess;
    private Button btnViewOrders, btnContinueShopping;

    private String orderId;

    public static OrderConfirmationFragment newInstance(String orderId) {
        OrderConfirmationFragment fragment = new OrderConfirmationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString(ARG_ORDER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_confirmation, container, false);

        initViews(view);
        displayOrderInfo();

        return view;
    }

    private void initViews(View view) {
        tvOrderId = view.findViewById(R.id.tvOrderId);
        tvMessage = view.findViewById(R.id.tvMessage);
        ivSuccess = view.findViewById(R.id.ivSuccess);
        btnViewOrders = view.findViewById(R.id.btnViewOrders);
        btnContinueShopping = view.findViewById(R.id.btnContinueShopping);

        btnViewOrders.setOnClickListener(v -> navigateToOrders());
        btnContinueShopping.setOnClickListener(v -> navigateToHome());
    }

    private void displayOrderInfo() {
        tvOrderId.setText("Order ID: " + orderId);
        tvMessage.setText("Your order has been placed successfully!\n\nYou will receive a confirmation email shortly.");
    }

    private void navigateToOrders() {
        OrderFragment ordersFragment = new OrderFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.customerFragmentContainer, ordersFragment)
                .commit();
    }

    private void navigateToHome() {
        HomeFragment homeFragment = new HomeFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.customerFragmentContainer, homeFragment)
                .commit();
    }
}