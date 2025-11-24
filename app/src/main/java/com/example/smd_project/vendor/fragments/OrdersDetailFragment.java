package com.example.smd_project.vendor.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.SharedActivities.EmailSender;
import com.example.smd_project.adapters.OrderItemAdapter;
import com.example.smd_project.models.Order;
import com.example.smd_project.models.OrderItem;
import com.example.smd_project.models.User;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class OrdersDetailFragment extends Fragment {

    private static final String ARG_ORDER = "order";

    private TextView tvOrderId, tvOrderDate, tvCustomerName, tvShippingAddress;
    private TextView tvPaymentMethod, tvOrderStatus, tvTotalAmount;
    private RecyclerView rvOrderItems;
    private Button btnApprove, btnShip, btnDeliver, btnCancel;
    private ImageButton btnBack;

    private Order order;
    private OrderItemAdapter adapter;
    private List<OrderItem> itemsList;

    public static OrdersDetailFragment newInstance(Order order) {
        OrdersDetailFragment fragment = new OrdersDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDER, order);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            order = (Order) getArguments().getSerializable(ARG_ORDER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders_detail, container, false);

        initViews(view);
        displayOrderDetails();
        setupButtons();

        return view;
    }

    private void initViews(View view) {
        tvOrderId = view.findViewById(R.id.tvOrderId);
        tvOrderDate = view.findViewById(R.id.tvOrderDate);
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        tvShippingAddress = view.findViewById(R.id.tvShippingAddress);
        tvPaymentMethod = view.findViewById(R.id.tvPaymentMethod);
        tvOrderStatus = view.findViewById(R.id.tvOrderStatus);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);

        rvOrderItems = view.findViewById(R.id.rvOrderItems);

        btnApprove = view.findViewById(R.id.btnApprove);
        btnShip = view.findViewById(R.id.btnShip);
        btnDeliver = view.findViewById(R.id.btnDeliver);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnBack = view.findViewById(R.id.btnBack);

        itemsList = new ArrayList<>();
        adapter = new OrderItemAdapter(getContext(), itemsList);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderItems.setAdapter(adapter);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getFragmentManager() != null) {
                    getFragmentManager().popBackStack();
                }
            });
        }
    }

    private void displayOrderDetails() {
        if (order == null) return;

        tvOrderId.setText("Order #" + order.getOrderId());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        String dateStr = sdf.format(new Date(order.getTimestamp()));
        tvOrderDate.setText(dateStr);

        tvCustomerName.setText(order.getCustomerName());
        tvShippingAddress.setText(order.getShippingAddress());
        tvPaymentMethod.setText(order.getPaymentMethod());

        tvOrderStatus.setText(order.getStatus());
        updateStatusColor(order.getStatus());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));
        tvTotalAmount.setText(formatter.format(order.getTotalAmount()));

        loadOrderItems();
    }

    private void loadOrderItems() {
        itemsList.clear();

        if (order.getItems() != null) {
            HashMap<String, OrderItem> items = order.getItems();
            for (OrderItem item : items.values()) {
                itemsList.add(item);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void setupButtons() {
        if (order == null) return;

        String status = order.getStatus();
        if (status == null) status = "Unknown";

        btnApprove.setVisibility(status.equalsIgnoreCase("Pending") ? View.VISIBLE : View.GONE);
        btnShip.setVisibility(status.equalsIgnoreCase("Approved") ? View.VISIBLE : View.GONE);
        btnDeliver.setVisibility(status.equalsIgnoreCase("Shipped") ? View.VISIBLE : View.GONE);

        boolean canCancel = !(status.equalsIgnoreCase("Delivered") || status.equalsIgnoreCase("Cancelled"));
        btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);

        if (btnApprove != null) {
            btnApprove.setOnClickListener(v -> showStatusChangeConfirmation("Approved"));
        }
        if (btnShip != null) {
            btnShip.setOnClickListener(v -> showStatusChangeConfirmation("Shipped"));
        }
        if (btnDeliver != null) {
            btnDeliver.setOnClickListener(v -> showStatusChangeConfirmation("Delivered"));
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> showStatusChangeConfirmation("Cancelled"));
        }
    }

    private void showStatusChangeConfirmation(String newStatus) {
        if (getContext() == null) return;

        String message = "Are you sure you want to change order status to " + newStatus + "?";

        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Status Change")
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> updateOrderStatus(newStatus))
                .setNegativeButton("No", null)
                .show();
    }

    private void updateOrderStatus(String newStatus) {
        if (order == null) return;

        FirebaseHelper.getInstance().updateOrderStatus(order.getOrderId(), newStatus,
                new FirebaseHelper.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        order.setStatus(newStatus);
                        if (tvOrderStatus != null) {
                            tvOrderStatus.setText(newStatus);
                            updateStatusColor(newStatus);
                        }
                        setupButtons();

                        if (getContext() != null) {
                            Toast.makeText(getContext(),
                                    " Order status updated to " + newStatus,
                                    Toast.LENGTH_SHORT).show();
                        }

                        // Send email notification for specific status changes
                        sendOrderStatusEmail(newStatus);
                    }

                    @Override
                    public void onFailure(String error) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(),
                                    "Failed to update status: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // Replace the sendOrderStatusEmail method with this debug version:

    private void sendOrderStatusEmail(String newStatus) {
        Log.d("OrderEmail", "=== sendOrderStatusEmail called ===");

        if (order == null) {
            Log.e("OrderEmail", "Order is NULL");
            return;
        }

        String customerEmail = order.getCustomerEmail();
        Log.d("OrderEmail", "Customer Email from order: " + customerEmail);

        // If email exists in order, send directly
        if (customerEmail != null && !customerEmail.isEmpty()) {
            sendEmailNotification(customerEmail, newStatus);
        } else {
            // Fetch email from Users table using customerId
            Log.d("OrderEmail", "Email not in order, fetching from Users table...");
            String customerId = order.getCustomerId();

            if (customerId == null || customerId.isEmpty()) {
                Log.e("OrderEmail", "No customerId found");
                return;
            }

            FirebaseHelper.getInstance().getUser(customerId, new FirebaseHelper.OnUserFetchListener() {
                @Override
                public void onSuccess(User user) {
                    if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                        Log.d("OrderEmail", "Got email from Users: " + user.getEmail());
                        sendEmailNotification(user.getEmail(), newStatus);
                    } else {
                        Log.e("OrderEmail", "User found but no email");
                        showToast("Customer email not found");
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e("OrderEmail", "Failed to fetch user: " + error);
                    showToast("Could not fetch customer email");
                }
            });
        }
    }

    private void sendEmailNotification(String customerEmail, String newStatus) {
        String subject;
        String htmlContent;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));
        String formattedAmount = formatter.format(order.getTotalAmount());

        switch (newStatus) {
            case "Approved":
                subject = "Your Order Has Been Confirmed! - Order #" + getShortOrderId();
                htmlContent = buildOrderConfirmedEmail(formattedAmount);
                break;
            case "Shipped":
                subject = "Your Order Has Been Shipped! - Order #" + getShortOrderId();
                htmlContent = buildOrderShippedEmail(formattedAmount);
                break;
            case "Delivered":
                subject = "Your Order Has Been Delivered! - Order #" + getShortOrderId();
                htmlContent = buildOrderDeliveredEmail(formattedAmount);
                break;
            case "Cancelled":
                subject = "Your Order Has Been Cancelled - Order #" + getShortOrderId();
                htmlContent = buildOrderCancelledEmail(formattedAmount);
                break;
            default:
                return;
        }

        Log.d("OrderEmail", "Sending to: " + customerEmail);

        EmailSender.sendOrderStatusEmail(customerEmail, subject, htmlContent, new EmailSender.EmailCallback() {
            @Override
            public void onSuccess() {
                Log.d("OrderEmail", " Email sent!");
                showToast("Email sent to customer");
            }

            @Override
            public void onFailure(String error) {
                Log.e("OrderEmail", "❌ Failed: " + error);
                showToast("Email failed: " + error);
            }
        });
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getShortOrderId() {
        String orderId = order.getOrderId();
        if (orderId != null && orderId.length() > 8) {
            return orderId.substring(orderId.length() - 8);
        }
        return orderId;
    }

    private String buildOrderConfirmedEmail(String amount) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>" +
                "  <div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px;'>" +
                "    <div style='text-align: center; margin-bottom: 30px;'>" +
                "      <h1 style='color: #4CAF50; margin: 0;'> Order Confirmed!</h1>" +
                "    </div>" +
                "    <p style='color: #333; font-size: 16px;'>Dear " + order.getCustomerName() + ",</p>" +
                "    <p style='color: #666;'>Great news! Your order has been confirmed and is being prepared.</p>" +
                "    <div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                "      <p style='margin: 5px 0;'><strong>Order ID:</strong> #" + getShortOrderId() + "</p>" +
                "      <p style='margin: 5px 0;'><strong>Total Amount:</strong> " + amount + "</p>" +
                "      <p style='margin: 5px 0;'><strong>Shipping Address:</strong> " + order.getShippingAddress() + "</p>" +
                "    </div>" +
                "    <p style='color: #666;'>We'll notify you once your order has been shipped.</p>" +
                "    <p style='color: #999; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px;'>Thank you for shopping with us!</p>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    private String buildOrderShippedEmail(String amount) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>" +
                "  <div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px;'>" +
                "    <div style='text-align: center; margin-bottom: 30px;'>" +
                "      <h1 style='color: #2196F3; margin: 0;'>📦 Order Shipped!</h1>" +
                "    </div>" +
                "    <p style='color: #333; font-size: 16px;'>Dear " + order.getCustomerName() + ",</p>" +
                "    <p style='color: #666;'>Your order is on its way! It has been shipped and will arrive soon.</p>" +
                "    <div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                "      <p style='margin: 5px 0;'><strong>Order ID:</strong> #" + getShortOrderId() + "</p>" +
                "      <p style='margin: 5px 0;'><strong>Total Amount:</strong> " + amount + "</p>" +
                "      <p style='margin: 5px 0;'><strong>Delivery Address:</strong> " + order.getShippingAddress() + "</p>" +
                "    </div>" +
                "    <p style='color: #666;'>Please ensure someone is available to receive the package.</p>" +
                "    <p style='color: #999; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px;'>Thank you for shopping with us!</p>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    private String buildOrderDeliveredEmail(String amount) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>" +
                "  <div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px;'>" +
                "    <div style='text-align: center; margin-bottom: 30px;'>" +
                "      <h1 style='color: #4CAF50; margin: 0;'>🎉 Order Delivered!</h1>" +
                "    </div>" +
                "    <p style='color: #333; font-size: 16px;'>Dear " + order.getCustomerName() + ",</p>" +
                "    <p style='color: #666;'>Your order has been successfully delivered. We hope you enjoy your purchase!</p>" +
                "    <div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                "      <p style='margin: 5px 0;'><strong>Order ID:</strong> #" + getShortOrderId() + "</p>" +
                "      <p style='margin: 5px 0;'><strong>Total Amount:</strong> " + amount + "</p>" +
                "    </div>" +
                "    <p style='color: #666;'>If you have any questions about your order, please contact us.</p>" +
                "    <p style='color: #999; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px;'>Thank you for shopping with us!</p>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    private String buildOrderCancelledEmail(String amount) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>" +
                "  <div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px;'>" +
                "    <div style='text-align: center; margin-bottom: 30px;'>" +
                "      <h1 style='color: #F44336; margin: 0;'>Order Cancelled</h1>" +
                "    </div>" +
                "    <p style='color: #333; font-size: 16px;'>Dear " + order.getCustomerName() + ",</p>" +
                "    <p style='color: #666;'>We regret to inform you that your order has been cancelled.</p>" +
                "    <div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                "      <p style='margin: 5px 0;'><strong>Order ID:</strong> #" + getShortOrderId() + "</p>" +
                "      <p style='margin: 5px 0;'><strong>Amount:</strong> " + amount + "</p>" +
                "    </div>" +
                "    <p style='color: #666;'>If you have any questions or need assistance, please contact our support team.</p>" +
                "    <p style='color: #999; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px;'>We apologize for any inconvenience.</p>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    private void updateStatusColor(String status) {
        if (status == null || tvOrderStatus == null) return;

        int color;
        switch (status) {
            case "Pending":
                color = 0xFFFFA726;
                break;
            case "Approved":
                color = 0xFF42A5F5;
                break;
            case "Shipped":
                color = 0xFF9C27B0;
                break;
            case "Delivered":
                color = 0xFF4CAF50;
                break;
            case "Cancelled":
                color = 0xFFF44336;
                break;
            default:
                color = 0xFF757575;
        }
        tvOrderStatus.setTextColor(color);
    }
}