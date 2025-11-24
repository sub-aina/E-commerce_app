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
import com.example.smd_project.customer.fragments.CartFragment;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.ViewHolder> {

    private Context context;
    private List<CartFragment.CartItem> cartItems;
    private NumberFormat formatter;

    public CheckoutAdapter(Context context, List<CartFragment.CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
        this.formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartFragment.CartItem item = cartItems.get(position);

        holder.tvProductName.setText(item.getProduct().getName());
        holder.tvProductPrice.setText(formatter.format(item.getProduct().getPrice()));
        holder.tvQuantity.setText("Qty: " + item.getQuantity());


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


        double itemSubtotal = item.getProduct().getPrice() * item.getQuantity();
        holder.tvItemTotal.setText(formatter.format(itemSubtotal));

        // Load product image
        if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
            Glide.with(context)
                    .load(item.getProduct().getImages().get(0))
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .centerCrop()
                    .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice, tvQuantity, tvSize, tvColor, tvItemTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvColor = itemView.findViewById(R.id.tvColor);
            tvItemTotal = itemView.findViewById(R.id.tvItemTotal);
        }
    }
}