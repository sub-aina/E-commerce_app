package com.example.smd_project.vendor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.adapters.ProductAdapter;
import com.example.smd_project.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CategoryProductsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private ArrayList<Product> productList;
    private TextView tvProductCount;
    private LinearLayout layoutEmpty;

    private ProgressBar progressBar;
    private TextView tvCategoryTitle, tvEmptyState;

    private String categoryName;
    private String categoryId;

    public static CategoryProductsFragment newInstance(String categoryId, String name) {
        CategoryProductsFragment fragment = new CategoryProductsFragment();
        Bundle args = new Bundle();
        args.putString("category_id", categoryId);
        args.putString("category_name", name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_products, container, false);

        if (getArguments() != null) {
            categoryName = getArguments().getString("category_name");
            categoryId = getArguments().getString("category_id");
        }

        initViews(view);
        setupRecyclerView();
        loadCategoryProducts();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewProducts);
        progressBar = view.findViewById(R.id.progressBar);
        tvCategoryTitle = view.findViewById(R.id.tvCategoryTitle);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvProductCount = view.findViewById(R.id.tvProductCount);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        View btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {

                requireActivity().onBackPressed();
            }
        });

        if (categoryName != null) {
            tvCategoryTitle.setText(categoryName);
        }
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();

        adapter = new ProductAdapter(productList, new ProductAdapter.onProductActionListener() {
            @Override
            public void onEdit(Product product) {
                Toast.makeText(getContext(), "Edit: " + product.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(Product product) {
                deleteProduct(product);
            }

            @Override
            public void onStockChanged(Product product, int newStock) {
                updateProductStock(product, newStock);
            }
        });

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);
    }

    private void loadCategoryProducts() {
        progressBar.setVisibility(View.VISIBLE);
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);

        FirebaseHelper.getInstance().getAllProductsReference()
                .orderByChild("category")
                .equalTo(categoryName)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();

                        for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                            Product product = productSnapshot.getValue(Product.class);
                            if (product != null) {
                                product.setProductId(productSnapshot.getKey());
                                productList.add(product);
                            }
                        }

                        progressBar.setVisibility(View.GONE);


                        if (tvProductCount != null) {
                            tvProductCount.setText(productList.size() + " Products");
                        }

                        if (productList.isEmpty()) {

                            if (layoutEmpty != null) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                            } else if (tvEmptyState != null) {
                                tvEmptyState.setVisibility(View.VISIBLE);
                                tvEmptyState.setText("No products in this category yet");
                            }
                            recyclerView.setVisibility(View.GONE);
                        } else {

                            if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
                            if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error loading products: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteProduct(Product product) {
        if (product.getProductId() == null) return;

        FirebaseHelper.getInstance().getAllProductsReference()
                .child(product.getProductId())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Product deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProductStock(Product product, int newStock) {
        if (product.getProductId() == null) return;

        FirebaseHelper.getInstance().getAllProductsReference()
                .child(product.getProductId())
                .child("stock")
                .setValue(newStock)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update stock", Toast.LENGTH_SHORT).show();
                });
    }
}