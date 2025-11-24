package com.example.smd_project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smd_project.R;
import com.example.smd_project.models.OrderItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private Context context;
    private List<OrderItem> orderItems;

    public OrderItemAdapter(Context context, List<OrderItem> orderItems) {
        this.context = context;
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);

        holder.tvProductName.setText(item.getProductName());
        holder.tvQuantity.setText("Qty: " + item.getQuantity());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));
        holder.tvPrice.setText(formatter.format(item.getPrice()));
        holder.tvTotal.setText(formatter.format(item.getPrice() * item.getQuantity()));

        if (item.getSize() != null && !item.getSize().isEmpty()) {
            holder.tvSize.setText("Size: " + item.getSize());
            holder.tvSize.setVisibility(View.VISIBLE);
        } else {
            holder.tvSize.setVisibility(View.GONE);
        }

        if (item.getColor() != null && !item.getColor().isEmpty()) {
            holder.tvColor.setText("Color: " + item.getColor());
            holder.tvColor.setVisibility(View.VISIBLE);
        } else {
            holder.tvColor.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvQuantity, tvPrice, tvTotal, tvSize, tvColor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvColor = itemView.findViewById(R.id.tvColor);
        }
    }
}