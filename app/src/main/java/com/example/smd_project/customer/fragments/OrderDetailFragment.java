package com.example.smd_project.customer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.example.smd_project.adapters.OrderItemAdapter;
import com.example.smd_project.models.Order;
import com.example.smd_project.models.OrderItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetailFragment extends Fragment {

    private static final String ARG_ORDER_ID = "order_id";

    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvShippingAddress;
    private TextView tvSubtotal, tvShipping, tvTotal;
    private RecyclerView rvOrderItems;
    private ProgressBar progressBar;
    private ImageButton btnBack;
    private Button btnTrackOrder, btnCancelOrder;

    private Order order;
    private OrderItemAdapter itemAdapter;
    private List<OrderItem> orderItems;

    public static OrderDetailFragment newInstance(String orderId) {
        OrderDetailFragment fragment = new OrderDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_detail, container, false);

        initViews(view);
        setupRecyclerView();
        loadOrderDetails();

        return view;
    }

    private void initViews(View view) {
        tvOrderId = view.findViewById(R.id.tvOrderId);
        tvOrderDate = view.findViewById(R.id.tvOrderDate);
        tvOrderStatus = view.findViewById(R.id.tvOrderStatus);
        tvShippingAddress = view.findViewById(R.id.tvShippingAddress);
        tvSubtotal = view.findViewById(R.id.tvSubtotal);
        tvShipping = view.findViewById(R.id.tvShipping);
        tvTotal = view.findViewById(R.id.tvTotal);
        rvOrderItems = view.findViewById(R.id.rvOrderItems);
        progressBar = view.findViewById(R.id.progressBar);
        btnBack = view.findViewById(R.id.btnBack);
        btnTrackOrder = view.findViewById(R.id.btnTrackOrder);
        btnCancelOrder = view.findViewById(R.id.btnCancelOrder);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnTrackOrder.setOnClickListener(v -> trackOrder());
        btnCancelOrder.setOnClickListener(v -> cancelOrder());
    }

    private void setupRecyclerView() {
        orderItems = new ArrayList<>();
        itemAdapter = new OrderItemAdapter(getContext(), orderItems);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderItems.setAdapter(itemAdapter);
    }

    private void loadOrderDetails() {
        if (getArguments() == null) return;

        String orderId = getArguments().getString(ARG_ORDER_ID);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseHelper.getInstance().getAllOrdersReference()
                .child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            order = snapshot.getValue(Order.class);
                            if (order != null) {
                                displayOrderDetails();
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error loading order", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayOrderDetails() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));

        // Order ID
        tvOrderId.setText("Order #" + order.getOrderId().substring(0, 8));

        // Order Date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        tvOrderDate.setText(sdf.format(new Date(order.getTimestamp())));

        // Order Status
        tvOrderStatus.setText(order.getStatus());
        setStatusColor(order.getStatus());

        // Shipping Address
        if (order.getShippingAddress() != null) {
            tvShippingAddress.setText(order.getShippingAddress());
        } else {
            tvShippingAddress.setText("No address provided");
        }

        // Order Items
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            orderItems.clear();
            orderItems.addAll(order.getItems().values());
            itemAdapter.notifyDataSetChanged();
        }

        // Pricing
        double subtotal = order.getTotalAmount() - 150; // Assuming 150 shipping
        tvSubtotal.setText(formatter.format(subtotal));
        tvShipping.setText(formatter.format(150));
        tvTotal.setText(formatter.format(order.getTotalAmount()));

        // Button visibility based on status
        if (order.getStatus().equalsIgnoreCase("delivered") ||
                order.getStatus().equalsIgnoreCase("cancelled")) {
            btnCancelOrder.setVisibility(View.GONE);
        }
    }

    private void setStatusColor(String status) {
        int statusColor;
        switch (status.toLowerCase()) {
            case "delivered":
                statusColor = getResources().getColor(R.color.green);
                break;
            case "shipped":
                statusColor = getResources().getColor(R.color.blue);
                break;
            case "pending":
                statusColor = getResources().getColor(R.color.orange);
                break;
            case "cancelled":
                statusColor = getResources().getColor(R.color.red);
                break;
            default:
                statusColor = getResources().getColor(R.color.gray);
        }
        tvOrderStatus.setTextColor(statusColor);
    }

    private void trackOrder() {
        Toast.makeText(getContext(), "Track order: " + order.getOrderId(), Toast.LENGTH_SHORT).show();
        // TODO: Implement order tracking
    }

    private void cancelOrder() {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel this order?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);

                    FirebaseHelper.getInstance().updateOrderStatus(order.getOrderId(), "Cancelled",
                            new FirebaseHelper.OnCompleteListener() {
                                @Override
                                public void onSuccess() {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Order cancelled", Toast.LENGTH_SHORT).show();
                                    getParentFragmentManager().popBackStack();
                                }

                                @Override
                                public void onFailure(String error) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }
}