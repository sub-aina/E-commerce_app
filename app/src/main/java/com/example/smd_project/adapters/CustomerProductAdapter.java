package com.example.smd_project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CustomerProductAdapter extends RecyclerView.Adapter<CustomerProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnProductClickListener clickListener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public CustomerProductAdapter(Context context, List<Product> productList, OnProductClickListener clickListener) {
        this.context = context;
        this.productList = productList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_grid, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice;
        ImageButton btnWishlist;
        CardView cardProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnWishlist = itemView.findViewById(R.id.btnWishlist);
        }

        public void bind(Product product) {
            tvProductName.setText(product.getName());

            // Format price
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));
            tvProductPrice.setText(formatter.format(product.getPrice()));

            // Load image with Glide
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                Glide.with(context)
                        .load(product.getImages().get(0))
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .centerCrop()
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.placeholder_product);
            }


            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                checkWishlistStatus(product.getProductId());
                btnWishlist.setOnClickListener(v -> toggleWishlist(product));
            } else {

                btnWishlist.setImageResource(R.drawable.ic_heart_outline);
                btnWishlist.setOnClickListener(v -> {
                    Toast.makeText(context, "Please login to add to wishlist", Toast.LENGTH_SHORT).show();
                });
            }


            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onProductClick(product);
                }
            });
        }

        private void checkWishlistStatus(String productId) {

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                btnWishlist.setImageResource(R.drawable.ic_heart_outline);
                return;
            }

            String userId = currentUser.getUid();
            FirebaseHelper.getInstance().getWishlist(userId)
                    .child(productId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            btnWishlist.setImageResource(R.drawable.ic_heart_filled);
                        } else {
                            btnWishlist.setImageResource(R.drawable.ic_heart_outline);
                        }
                    })
                    .addOnFailureListener(e -> {

                        btnWishlist.setImageResource(R.drawable.ic_heart_outline);
                    });
        }

        private void toggleWishlist(Product product) {

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(context, "Please login to add to wishlist", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = currentUser.getUid();
            String productId = product.getProductId();

            if (productId == null || productId.isEmpty()) {
                Toast.makeText(context, "Invalid product", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseHelper.getInstance().getWishlist(userId)
                    .child(productId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            // Remove from wishlist
                            FirebaseHelper.getInstance().removeFromWishlist(userId, productId,
                                    new FirebaseHelper.OnCompleteListener() {
                                        @Override
                                        public void onSuccess() {
                                            btnWishlist.setImageResource(R.drawable.ic_heart_outline);
                                            Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            Toast.makeText(context, "Failed to remove: " + error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Add to wishlist
                            FirebaseHelper.getInstance().addToWishlist(userId, productId,
                                    new FirebaseHelper.OnCompleteListener() {
                                        @Override
                                        public void onSuccess() {
                                            btnWishlist.setImageResource(R.drawable.ic_heart_filled);
                                            Toast.makeText(context, "Added to wishlist", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            Toast.makeText(context, "Failed to add: " + error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}