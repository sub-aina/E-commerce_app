package com.example.smd_project.customer.fragments;

import android.os.Bundle;
import android.util.Log;
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
import androidx.viewpager2.widget.ViewPager2;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.adapters.ProductImageAdapter;
import com.example.smd_project.models.Product;
import com.example.smd_project.SharedActivities.AuthGuard;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductDetailFragment extends Fragment {

    private static final String TAG = "ProductDetailFragment";
    private static final String ARG_PRODUCT_ID = "product_id";

    private ViewPager2 viewPagerImages;
    private TextView tvProductName, tvProductPrice, tvProductDescription, tvStock;
    private ChipGroup chipGroupSizes, chipGroupColors;
    private Button btnAddToCart;
    private ImageButton btnBack, btnWishlist;
    private ProgressBar progressBar;
    private View indicatorContainer;

    private Product product;
    private String selectedSize = "";
    private String selectedColor = "";
    private boolean isInWishlist = false;

    public static ProductDetailFragment newInstance(String productId) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_detail, container, false);

        initViews(view);
        loadProductDetails();

        return view;
    }

    private void initViews(View view) {
        viewPagerImages = view.findViewById(R.id.viewPagerImages);
        tvProductName = view.findViewById(R.id.tvProductName);
        tvProductPrice = view.findViewById(R.id.tvProductPrice);
        tvProductDescription = view.findViewById(R.id.tvProductDescription);
        tvStock = view.findViewById(R.id.tvStock);
        chipGroupSizes = view.findViewById(R.id.chipGroupSizes);
        chipGroupColors = view.findViewById(R.id.chipGroupColors);
        btnAddToCart = view.findViewById(R.id.btnAddToCart);
        btnBack = view.findViewById(R.id.btnBack);
        btnWishlist = view.findViewById(R.id.btnWishlist);
        progressBar = view.findViewById(R.id.progressBar);
        indicatorContainer = view.findViewById(R.id.indicatorContainer);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnWishlist.setOnClickListener(v -> toggleWishlist());
        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void loadProductDetails() {
        if (getArguments() == null) return;

        String productId = getArguments().getString(ARG_PRODUCT_ID);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseHelper.getInstance().getProductReference(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            product = snapshot.getValue(Product.class);
                            if (product != null) {
                                product.setProductId(productId);
                                displayProductDetails();
                                checkWishlistStatus();
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error loading product", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayProductDetails() {
        Log.d(TAG, "=== displayProductDetails called ===");

        // Product name
        tvProductName.setText(product.getName());

        // Product price
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));
        tvProductPrice.setText(formatter.format(product.getPrice()));

        // Product description
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            tvProductDescription.setText(product.getDescription());
            tvProductDescription.setVisibility(View.VISIBLE);
        } else {
            tvProductDescription.setVisibility(View.GONE);
        }

        // Stock
        if (product.getStock() > 0) {
            tvStock.setText("In Stock: " + product.getStock());
            tvStock.setTextColor(getResources().getColor(R.color.success));
        } else {
            tvStock.setText("Out of Stock");
            tvStock.setTextColor(getResources().getColor(R.color.error));
            btnAddToCart.setEnabled(false);
        }

        // Images
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            setupImageSlider(product.getImages());
        }

        // Sizes
        if (product.getSizes() != null && !product.getSizes().isEmpty()) {
            setupSizes(product.getSizes());
            chipGroupSizes.setVisibility(View.VISIBLE);
        } else {
            chipGroupSizes.setVisibility(View.GONE);
        }

        // Colors
        if (product.getColors() != null && !product.getColors().isEmpty()) {
            setupColors(product.getColors());
            chipGroupColors.setVisibility(View.VISIBLE);
        } else {
            chipGroupColors.setVisibility(View.GONE);
        }
    }

    private void setupColors(List<String> colors) {
        chipGroupColors.removeAllViews();

        for (int i = 0; i < colors.size(); i++) {
            String color = colors.get(i);

            if (color == null || color.trim().isEmpty()) {
                continue;
            }

            color = color.trim();

            Chip chip = new Chip(getContext());
            chip.setText(color);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_background);
            chip.setTextColor(getResources().getColor(R.color.chip_text));

            final String finalColor = color;
            chip.setOnClickListener(v -> {
                for (int j = 0; j < chipGroupColors.getChildCount(); j++) {
                    Chip otherChip = (Chip) chipGroupColors.getChildAt(j);
                    otherChip.setChecked(false);
                    otherChip.setChipBackgroundColorResource(R.color.chip_background);
                    otherChip.setTextColor(getResources().getColor(R.color.chip_text));
                }
                chip.setChecked(true);
                chip.setChipBackgroundColorResource(R.color.primary);
                chip.setTextColor(getResources().getColor(android.R.color.white));
                selectedColor = finalColor;
            });

            chipGroupColors.addView(chip);

            // Select first color by default
            if (i == 0) {
                chip.setChecked(true);
                chip.setChipBackgroundColorResource(R.color.primary);
                chip.setTextColor(getResources().getColor(android.R.color.white));
                selectedColor = color;
            }
        }
    }

    private void setupSizes(List<String> sizes) {
        chipGroupSizes.removeAllViews();

        for (int i = 0; i < sizes.size(); i++) {
            String size = sizes.get(i);

            if (size == null || size.trim().isEmpty()) {
                continue;
            }

            size = size.trim();

            Chip chip = new Chip(getContext());
            chip.setText(size);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_background);
            chip.setTextColor(getResources().getColor(R.color.chip_text));

            final String finalSize = size;
            chip.setOnClickListener(v -> {
                for (int j = 0; j < chipGroupSizes.getChildCount(); j++) {
                    Chip otherChip = (Chip) chipGroupSizes.getChildAt(j);
                    otherChip.setChecked(false);
                    otherChip.setChipBackgroundColorResource(R.color.chip_background);
                    otherChip.setTextColor(getResources().getColor(R.color.chip_text));
                }
                chip.setChecked(true);
                chip.setChipBackgroundColorResource(R.color.primary);
                chip.setTextColor(getResources().getColor(android.R.color.white));
                selectedSize = finalSize;
            });

            chipGroupSizes.addView(chip);

            // Select first size by default
            if (i == 0) {
                chip.setChecked(true);
                chip.setChipBackgroundColorResource(R.color.primary);
                chip.setTextColor(getResources().getColor(android.R.color.white));
                selectedSize = size;
            }
        }
    }

    private void setupImageSlider(List<String> images) {
        ProductImageAdapter adapter = new ProductImageAdapter(getContext(), images);
        viewPagerImages.setAdapter(adapter);

        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
            }
        });
    }

    private void setupIndicators(int count) {
        // Placeholder for indicator setup
    }

    private void updateIndicators(int position) {
        // Placeholder for indicator update
    }

    private void checkWishlistStatus() {

        String userId = AuthGuard.getCurrentUserId();
        if (userId == null || product == null || product.getProductId() == null) {
            btnWishlist.setImageResource(R.drawable.ic_heart_outline);
            return;
        }

        FirebaseHelper.getInstance().getWishlist(userId)
                .child(product.getProductId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    isInWishlist = snapshot.exists();
                    updateWishlistIcon();
                })
                .addOnFailureListener(e -> {
                    btnWishlist.setImageResource(R.drawable.ic_heart_outline);
                });
    }

    private void toggleWishlist() {

        if (!AuthGuard.checkAuthWithToast(getContext(), "Please login to add to wishlist")) {
            return;
        }

        String userId = AuthGuard.getCurrentUserId();
        if (userId == null || product == null || product.getProductId() == null) {
            Toast.makeText(getContext(), "Unable to update wishlist", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isInWishlist) {
            // Remove from wishlist
            FirebaseHelper.getInstance().removeFromWishlist(userId, product.getProductId(),
                    new FirebaseHelper.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            if (isAdded()) {
                                isInWishlist = false;
                                updateWishlistIcon();
                                Toast.makeText(getContext(), "Removed from wishlist", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            if (isAdded()) {
                                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            // Add to wishlist
            FirebaseHelper.getInstance().addToWishlist(userId, product.getProductId(),
                    new FirebaseHelper.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            if (isAdded()) {
                                isInWishlist = true;
                                updateWishlistIcon();
                                Toast.makeText(getContext(), "Added to wishlist", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            if (isAdded()) {
                                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void updateWishlistIcon() {
        if (btnWishlist != null) {
            btnWishlist.setImageResource(isInWishlist ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        }
    }

    private void addToCart() {
        if (product == null || product.getStock() <= 0) {
            Toast.makeText(getContext(), "Product out of stock", Toast.LENGTH_SHORT).show();
            return;
        }

        //  Check authentication
        if (!AuthGuard.checkAuthWithToast(getContext(), "Please login to add to cart")) {
            return;
        }

        String userId = AuthGuard.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "Please login to add to cart", Toast.LENGTH_SHORT).show();
            return;
        }

        if (product.getProductId() == null) {
            Toast.makeText(getContext(), "Invalid product", Toast.LENGTH_SHORT).show();
            return;
        }

        String itemKey = FirebaseHelper.getInstance().getCartItems(userId).push().getKey();

        if (itemKey == null) {
            Toast.makeText(getContext(), "Error adding to cart", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create cart item
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("productId", product.getProductId());
        cartItem.put("quantity", 1);
        cartItem.put("size", selectedSize != null ? selectedSize : "");
        cartItem.put("color", selectedColor != null ? selectedColor : "");

        btnAddToCart.setEnabled(false);
        btnAddToCart.setText("Adding...");

        FirebaseHelper.getInstance().addToCart(userId, itemKey, cartItem,
                new FirebaseHelper.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        if (isAdded() && btnAddToCart != null) {
                            btnAddToCart.setEnabled(true);
                            btnAddToCart.setText("Add To Cart");
                            Toast.makeText(getContext(), "Added to cart!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        if (isAdded() && btnAddToCart != null) {
                            btnAddToCart.setEnabled(true);
                            btnAddToCart.setText("Add To Cart");
                            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}