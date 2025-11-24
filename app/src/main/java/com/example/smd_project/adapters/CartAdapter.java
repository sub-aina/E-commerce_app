package com.example.smd_project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smd_project.R;
import com.example.smd_project.customer.fragments.CartFragment.CartItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private OnQuantityChangeListener quantityChangeListener;
    private OnItemRemoveListener removeListener;

    public interface OnQuantityChangeListener {
        void onQuantityChanged(CartItem item, int newQuantity);
    }

    public interface OnItemRemoveListener {
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItems,
                       OnQuantityChangeListener quantityChangeListener,
                       OnItemRemoveListener removeListener) {
        this.context = context;
        this.cartItems = cartItems;
        this.quantityChangeListener = quantityChangeListener;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvProductName, tvProductPrice, tvQuantity, tvSize, tvColor;
        ImageButton btnDecrease, btnIncrease, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvColor = itemView.findViewById(R.id.tvColor);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(CartItem item) {
            tvProductName.setText(item.getProduct().getName());


            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));
            double totalPrice = item.getProduct().getPrice() * item.getQuantity();
            tvProductPrice.setText(formatter.format(totalPrice));

            tvQuantity.setText(String.valueOf(item.getQuantity()));

            if (item.getSize() != null && !item.getSize().isEmpty()) {
                tvSize.setText("Size: " + item.getSize());
                tvSize.setVisibility(View.VISIBLE);
            } else {
                tvSize.setVisibility(View.GONE);
            }

            if (item.getColor() != null && !item.getColor().isEmpty()) {
                tvColor.setText("Color: " + item.getColor());
                tvColor.setVisibility(View.VISIBLE);
            } else {
                tvColor.setVisibility(View.GONE);
            }


            if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                Glide.with(context)
                        .load(item.getProduct().getImages().get(0))
                        .placeholder(R.drawable.placeholder_product)
                        .into(ivProduct);
            } else {
                ivProduct.setImageResource(R.drawable.placeholder_product);
            }


            btnDecrease.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() - 1;
                if (newQuantity > 0) {
                    quantityChangeListener.onQuantityChanged(item, newQuantity);
                } else {

                    removeListener.onItemRemoved(item);
                }
            });


            btnIncrease.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() + 1;
                if (newQuantity <= item.getProduct().getStock()) {
                    quantityChangeListener.onQuantityChanged(item, newQuantity);
                } else {

                }
            });

            btnRemove.setOnClickListener(v -> removeListener.onItemRemoved(item));
        }
    }
}