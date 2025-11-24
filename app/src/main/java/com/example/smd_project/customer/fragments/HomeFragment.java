package com.example.smd_project.customer.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.adapters.CustomerProductAdapter;
import com.example.smd_project.models.Category;
import com.example.smd_project.models.Product;
import com.example.smd_project.models.User;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvEmptyProducts, tvSeeMoreCategories, tvSeeMoreProducts;
    private ChipGroup chipGroupCategories;
    private RecyclerView recyclerViewProducts;
    private ProgressBar progressBar;
    private EditText etSearch;
    private ImageView btnNotification, btnMenu;

    private CustomerProductAdapter productAdapter;
    private List<Product> productList;
    private List<Product> filteredProductList;
    private List<Category> categoryList;

    private String selectedCategory = "All";
    private String currentSearchText = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupClickListeners();
        setupSearchListener();
        loadUserInfo();
        loadCategories();
        loadProducts();

        return view;
    }

    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tvGreeting);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyProducts = view.findViewById(R.id.tvEmptyProducts);
        etSearch = view.findViewById(R.id.etSearch);


        tvSeeMoreProducts = view.findViewById(R.id.tvSeeMoreProducts);



        // Setup RecyclerView
        productList = new ArrayList<>();
        filteredProductList = new ArrayList<>();
        productAdapter = new CustomerProductAdapter(getContext(), filteredProductList, this::onProductClick);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerViewProducts.setLayoutManager(gridLayoutManager);
        recyclerViewProducts.setAdapter(productAdapter);

        // Disable nested scrolling for RecyclerView to work smoothly inside NestedScrollView
        recyclerViewProducts.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {

        tvSeeMoreProducts.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.customerFragmentContainer, new AllProductsFragment())
                    .addToBackStack(null)
                    .commit();
        });


    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString().toLowerCase().trim();
                filterProducts();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUserInfo() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseHelper.getInstance().getUser(userId, new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                String firstName = user.getName().split(" ")[0];
                tvGreeting.setText(" Hello, " + firstName);
            }

            @Override
            public void onFailure(String error) {
                tvGreeting.setText(" Hello, User");
            }
        });
    }

    private void loadCategories() {
        categoryList = new ArrayList<>();
        FirebaseHelper.getInstance().getAllCategoriesReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        categoryList.clear();
                        chipGroupCategories.removeAllViews();

                        addCategoryChip("All", true);

                        for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                            Category category = categorySnapshot.getValue(Category.class);
                            if (category != null) {
                                categoryList.add(category);
                                addCategoryChip(category.getName(), false);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void addCategoryChip(String categoryName, boolean isSelected) {
        Chip chip = new Chip(getContext());
        chip.setText(categoryName);
        chip.setCheckable(true);
        chip.setChecked(isSelected);
        chip.setChipBackgroundColorResource(isSelected ? R.color.primary : R.color.chip_background);
        chip.setTextColor(getResources().getColor(isSelected ? android.R.color.white : R.color.chip_text));

        chip.setOnClickListener(v -> {
            // Visual Update
            for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
                Chip otherChip = (Chip) chipGroupCategories.getChildAt(i);
                otherChip.setChecked(false);
                otherChip.setChipBackgroundColorResource(R.color.chip_background);
                otherChip.setTextColor(getResources().getColor(R.color.chip_text));
            }
            chip.setChecked(true);
            chip.setChipBackgroundColorResource(R.color.primary);
            chip.setTextColor(getResources().getColor(android.R.color.white));

            // Logic Update
            selectedCategory = categoryName;
            filterProducts();
        });

        chipGroupCategories.addView(chip);
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.getInstance().getAllProductsReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                            Product product = productSnapshot.getValue(Product.class);
                            if (product != null && product.getStock() > 0) {
                                product.setProductId(productSnapshot.getKey()); // Ensure ID is set
                                productList.add(product);
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                        filterProducts();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void filterProducts() {
        filteredProductList.clear();

        int limit = 4;

        for (Product product : productList) {
            boolean matchesCategory = selectedCategory.equals("All") ||
                    (product.getCategory() != null && product.getCategory().equals(selectedCategory));

            boolean matchesSearch = currentSearchText.isEmpty() ||
                    product.getName().toLowerCase().contains(currentSearchText);

            if (matchesCategory && matchesSearch) {
                filteredProductList.add(product);

                if (filteredProductList.size() >= limit) {
                    break;
                }
            }
        }

        if (filteredProductList.isEmpty()) {
            tvEmptyProducts.setVisibility(View.VISIBLE);
            recyclerViewProducts.setVisibility(View.GONE);
        } else {
            tvEmptyProducts.setVisibility(View.GONE);
            recyclerViewProducts.setVisibility(View.VISIBLE);
            productAdapter.notifyDataSetChanged();
        }
    }

    private void onProductClick(Product product) {
        ProductDetailFragment fragment = ProductDetailFragment.newInstance(product.getProductId());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.customerFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}