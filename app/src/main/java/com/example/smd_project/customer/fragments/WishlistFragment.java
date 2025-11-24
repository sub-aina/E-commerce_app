package com.example.smd_project.customer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.SharedActivities.AuthGuard;
import com.example.smd_project.adapters.CustomerProductAdapter;
import com.example.smd_project.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WishlistFragment extends Fragment {

    private RecyclerView recyclerView;
    private CustomerProductAdapter adapter;
    private List<Product> wishlistProducts;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        if (!AuthGuard.requireAuth(this)) {
            return view;
        }

        initViews(view);
        setupRecyclerView();
        loadWishlist();
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
    }

    private void setupRecyclerView() {
        wishlistProducts = new ArrayList<>();
        adapter = new CustomerProductAdapter(getContext(), wishlistProducts, product -> {
            ProductDetailFragment fragment = ProductDetailFragment.newInstance(product.getProductId());
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.customerFragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);
    }

    private void loadWishlist() {
        String userId = AuthGuard.getCurrentUserId();
        if (userId == null) {
            progressBar.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        FirebaseHelper.getInstance().getWishlist(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        wishlistProducts.clear();

                        if (snapshot.getChildrenCount() == 0) {
                            progressBar.setVisibility(View.GONE);
                            tvEmptyState.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            return;
                        }

                        tvEmptyState.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                        for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                            String productId = productSnapshot.getKey();
                            loadProduct(productId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void loadProduct(String productId) {
        FirebaseHelper.getInstance().getProductReference(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Product product = snapshot.getValue(Product.class);
                        if (product != null) {
                            wishlistProducts.add(product);
                            adapter.notifyDataSetChanged();
                        }
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}