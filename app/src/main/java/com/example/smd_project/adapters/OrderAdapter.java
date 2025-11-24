package com.example.smd_project.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smd_project.R;
import com.example.smd_project.models.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private static final String TAG = "OrderAdapter";
    private final List<Order> orderList;
    private final OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onStatusChange(String orderId, String newStatus);
        void onViewDetails(Order order);
    }

    public OrderAdapter(List<Order> orderList, OnOrderActionListener listener) {
        this.orderList = orderList;
        this.listener = listener;
        Log.d(TAG, "OrderAdapter created with " + orderList.size() + " orders, listener: " + (listener != null));
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order, parent, false);
        Log.d(TAG, "ViewHolder created");
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        Log.d(TAG, "Binding order at position " + position + ": " + order.getOrderId());


        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));
        String total = formatter.format(order.getTotalAmount());

        holder.tvOrderId.setText("ID: " + order.getOrderId());
        holder.tvCustomerInfo.setText("Customer: " + (order.getCustomerId() != null ? order.getCustomerId() : "N/A"));
        holder.tvOrderTotal.setText(total);
        holder.btnStatus.setText(order.getStatus());


        holder.tvDate.setText(formatDate(order.getTimestamp()));


        holder.btnStatus.setOnClickListener(v -> {
            Log.d(TAG, "Status button clicked for order: " + order.getOrderId());
            showStatusPopupMenu(v, order);
        });

        // ⭐ CRITICAL FIX: Handle View Details Click
        holder.btnViewDetails.setOnClickListener(v -> {
            Log.d(TAG, "View Details button clicked for order: " + order.getOrderId());
            if (listener != null) {
                Log.d(TAG, "Calling listener.onViewDetails()");
                listener.onViewDetails(order);
            } else {
                Log.e(TAG, "ERROR: Listener is NULL!");
            }
        });


        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Item view clicked for order: " + order.getOrderId());
            if (listener != null) {
                listener.onViewDetails(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = orderList.size();
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }


    private String formatDate(long timestamp) {
        if (timestamp == 0) return "N/A";

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void showStatusPopupMenu(View v, Order order) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.getMenu().add("Pending");
        popup.getMenu().add("Shipped");
        popup.getMenu().add("Delivered");

        popup.setOnMenuItemClickListener(item -> {
            String newStatus = item.getTitle().toString();
            if (!newStatus.equalsIgnoreCase(order.getStatus())) {
                listener.onStatusChange(order.getOrderId(), newStatus);
            } else {
                Toast.makeText(v.getContext(), "Status is already " + newStatus, Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        final TextView tvOrderId, tvCustomerInfo, tvOrderTotal, tvDate;
        final Button btnStatus, btnViewDetails;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomerInfo = itemView.findViewById(R.id.tvCustomerInfo);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            btnStatus = itemView.findViewById(R.id.btnChangeStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);

            Log.d("OrderViewHolder", "All views initialized: " +
                    (tvOrderId != null) + ", " +
                    (tvCustomerInfo != null) + ", " +
                    (btnViewDetails != null));
        }
    }
}