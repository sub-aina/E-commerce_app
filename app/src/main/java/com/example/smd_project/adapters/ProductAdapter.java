package com.example.smd_project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smd_project.R;
import com.example.smd_project.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    ArrayList<Product> productList;
    private onProductActionListener listener;



    public interface onProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
        void onStockChanged(Product product, int newStock);
    }

    public ProductAdapter(ArrayList<Product> productList, onProductActionListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    public ProductAdapter(List<Product> productList, Context context) {
       productList = productList;
        context = context;
        this.listener = null;
    }

    @NonNull
    @Override
    public ProductAdapter.ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.ProductViewHolder holder, int position) {
        Product p = productList.get(position);

        if (holder.name != null) {
            holder.name.setText(p.getName());
        }

        if (holder.price != null) {
            holder.price.setText("Rs" + String.format("%.2f", p.getPrice()));
        }

        if (holder.quantity != null) {
            holder.quantity.setText(String.valueOf(p.getStock()) + " units");
        }

        // Load image
        if (holder.productImage != null) {
            if (p.getImages() != null && !p.getImages().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(p.getImages().get(0))
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .centerCrop()
                        .into(holder.productImage);
            } else {
                holder.productImage.setImageResource(R.drawable.placeholder_product);
            }
        }

        // Stock increase button
        if (holder.btnIncreaseStock != null) {
            holder.btnIncreaseStock.setOnClickListener(v -> {
                p.increaseStock(1);
                if (holder.quantity != null) {
                    holder.quantity.setText(String.valueOf(p.getStock()) + " units");
                }
                if (listener != null) {
                    listener.onStockChanged(p, p.getStock());
                }
            });
        }

        // Stock decrease button
        if (holder.btnDecreaseStock != null) {
            holder.btnDecreaseStock.setOnClickListener(v -> {
                if (p.getStock() > 0) {
                    p.decreaseStock(1);
                    if (holder.quantity != null) {
                        holder.quantity.setText(String.valueOf(p.getStock()) + " units");
                    }
                    if (listener != null) {
                        listener.onStockChanged(p, p.getStock());
                    }
                }
            });
        }

        // Edit button
        if (holder.btnEdit != null) {
            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(p);
                }
            });
        }

        // Delete button
        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(p);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView name, price, quantity;
        ImageButton btnIncreaseStock, btnDecreaseStock;
        Button btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);


            productImage = itemView.findViewById(R.id.ivProductImage);
            name = itemView.findViewById(R.id.txtName);
            price = itemView.findViewById(R.id.txtPrice);
            quantity = itemView.findViewById(R.id.txtQuantity);

            btnIncreaseStock = itemView.findViewById(R.id.btnIncreaseStock);
            btnDecreaseStock = itemView.findViewById(R.id.btnDecreaseStock);

            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}