package com.example.smd_project.customer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.adapters.CustomerCategoryAdapter;
import com.example.smd_project.models.Category;
import com.example.smd_project.vendor.fragments.CategoryProductsFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AllCategoriesFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvTitle, tvEmptyCategories;
    private ProgressBar progressBar;
    private List<Category> categoryList;
    private CustomerCategoryAdapter categoryAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_products, container, false);

        initViews(view);
        setupRecyclerView();
        loadCategories();

        return view;
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tvCategoryTitle);
        ImageView btnBack = view.findViewById(R.id.btnBack);
        recyclerView = view.findViewById(R.id.recyclerViewProducts);


        View cardProductCount = view.findViewById(R.id.cardProductCount);
        if (cardProductCount != null) {
            cardProductCount.setVisibility(View.GONE);
        }

        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyCategories = view.findViewById(R.id.tvEmptyProducts);

        tvTitle.setText("Browse Categories");

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void setupRecyclerView() {
        categoryList = new ArrayList<>();
        categoryAdapter = new CustomerCategoryAdapter(getContext(), categoryList, this::onCategoryClick);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        FirebaseHelper.getInstance().getAllCategoriesReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        categoryList.clear();

                        for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                            Category category = categorySnapshot.getValue(Category.class);
                            if (category != null) {
                                category.setCategoryId(categorySnapshot.getKey());
                                categoryList.add(category);
                            }
                        }

                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                        updateUI();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        Toast.makeText(getContext(), "Failed to load categories: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        if (categoryList.isEmpty()) {
            if (tvEmptyCategories != null) {
                tvEmptyCategories.setVisibility(View.VISIBLE);
                tvEmptyCategories.setText("No categories available");
            }
            recyclerView.setVisibility(View.GONE);
        } else {
            if (tvEmptyCategories != null) {
                tvEmptyCategories.setVisibility(View.GONE);
            }
            recyclerView.setVisibility(View.VISIBLE);
            categoryAdapter.notifyDataSetChanged();
        }
    }

    private void onCategoryClick(Category category) {

        CategoryProductsFragment fragment = CategoryProductsFragment.newInstance(
                category.getCategoryId(),
                category.getName()
        );

        getParentFragmentManager().beginTransaction()
                .replace(R.id.customerFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}