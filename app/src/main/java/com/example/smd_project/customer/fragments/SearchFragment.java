package com.example.smd_project.customer.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements CustomerProductAdapter.OnProductClickListener {

    private AutoCompleteTextView searchInput;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoResults;
    private ChipGroup chipGroupFilters;

    private CustomerProductAdapter productAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();

    private String currentQuery = "";
    private String currentCategory = "All";
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        initViews(view);
        setupSearchInput();
        setupRecyclerView();
        loadCategories();
        loadAllProducts();

        return view;
    }

    private void initViews(View view) {
        searchInput = view.findViewById(R.id.searchInput);
        recyclerView = view.findViewById(R.id.recyclerSearchResults);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoResults = view.findViewById(R.id.tvNoResults);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
    }

    private void setupSearchInput() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentQuery = s.toString().trim();

                searchRunnable = () -> performSearch();
                searchHandler.postDelayed(searchRunnable, 500);

                if (currentQuery.length() >= 2) {
                    loadSearchSuggestions(currentQuery);
                }
            }
        });

        searchInput.setOnItemClickListener((parent, view, position, id) -> {
            String suggestion = (String) parent.getItemAtPosition(position);
            currentQuery = suggestion;
            performSearch();
        });
    }

    private void loadCategories() {
        FirebaseHelper.getInstance().getAllCategoriesReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        categoryList.clear();
                        chipGroupFilters.removeAllViews();

                        // Add "All" chip first
                        addCategoryChip("All", true);

                        // Add chips for each category from Firebase
                        for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                            Category category = categorySnapshot.getValue(Category.class);
                            if (category != null) {
                                categoryList.add(category);
                                addCategoryChip(category.getName(), false);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void addCategoryChip(String categoryName, boolean isSelected) {
        Chip chip = new Chip(getContext(), null, R.color.chip_background_selector);

        // Apply our custom style
        chip.setChipBackgroundColorResource(R.color.chip_background_selector);
        chip.setChipStrokeColorResource(R.color.chip_stroke_selector);
        chip.setTextColor(getContext().getColorStateList(R.color.chip_text_selector));

        chip.setChipCornerRadius(18 * getResources().getDisplayMetrics().density);
        chip.setChipMinHeight(40 * getResources().getDisplayMetrics().density);
        chip.setChipStrokeWidth(1.5f * getResources().getDisplayMetrics().density);
        chip.setEnsureMinTouchTargetSize(false);
        chip.setCheckedIconVisible(false);

        int padding = (int) (18 * getResources().getDisplayMetrics().density);
        chip.setChipStartPadding(padding);
        chip.setChipEndPadding(padding);

        chip.setText(categoryName);
        chip.setCheckable(true);
        chip.setChecked(isSelected);

        chip.setOnClickListener(v -> {
            for (int i = 0; i < chipGroupFilters.getChildCount(); i++) {
                Chip otherChip = (Chip) chipGroupFilters.getChildAt(i);
                otherChip.setChecked(otherChip == chip);
            }
            filterByCategory(categoryName);
        });

        chipGroupFilters.addView(chip);
    }

    private void setupRecyclerView() {
        productAdapter = new CustomerProductAdapter(getContext(), filteredProducts, this);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(productAdapter);
    }

    private void loadAllProducts() {
        showLoading(true);

        FirebaseHelper.getInstance().getAllProductsReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allProducts.clear();

                        for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                            Product product = productSnapshot.getValue(Product.class);
                            if (product != null && product.getStock() > 0) {
                                product.setProductId(productSnapshot.getKey());
                                allProducts.add(product);
                            }
                        }

                        showLoading(false);
                        applyFilters();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showLoading(false);
                        Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void performSearch() {
        if (currentQuery.isEmpty()) {
            applyFilters();
            return;
        }

        showLoading(true);

        FirebaseHelper.getInstance().searchProducts(currentQuery,
                new FirebaseHelper.OnProductSearchListener() {
                    @Override
                    public void onSuccess(List<Product> products) {
                        showLoading(false);
                        allProducts.clear();
                        allProducts.addAll(products);
                        applyFilters();
                    }

                    @Override
                    public void onFailure(String error) {
                        showLoading(false);
                        Toast.makeText(getContext(), "Search failed: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadSearchSuggestions(String query) {
        FirebaseHelper.getInstance().getSearchSuggestions(query,
                new FirebaseHelper.OnSearchSuggestionsListener() {
                    @Override
                    public void onSuccess(List<String> suggestions) {
                        if (getContext() != null) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    getContext(),
                                    android.R.layout.simple_dropdown_item_1line,
                                    suggestions
                            );
                            searchInput.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        // Silently fail for suggestions
                    }
                });
    }

    private void filterByCategory(String category) {
        currentCategory = category;
        applyFilters();
    }

    private void applyFilters() {
        filteredProducts.clear();

        for (Product product : allProducts) {
            boolean matchesCategory = currentCategory.equals("All") ||
                    (product.getCategory() != null &&
                            product.getCategory().equalsIgnoreCase(currentCategory));

            boolean matchesSearch = currentQuery.isEmpty() ||
                    (product.getName() != null && product.getName().toLowerCase().contains(currentQuery.toLowerCase())) ||
                    (product.getDescription() != null && product.getDescription().toLowerCase().contains(currentQuery.toLowerCase()));

            if (matchesCategory && matchesSearch) {
                filteredProducts.add(product);
            }
        }

        productAdapter.notifyDataSetChanged();
        updateResultsUI();
    }

    private void updateResultsUI() {
        if (filteredProducts.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            if (currentQuery.isEmpty()) {
                tvNoResults.setText("No products available");
            } else {
                tvNoResults.setText("No results found for \"" + currentQuery + "\"");
            }
        } else {
            tvNoResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onProductClick(Product product) {
        if (product != null && product.getProductId() != null) {
            ProductDetailFragment detailFragment = ProductDetailFragment.newInstance(product.getProductId());

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.customerFragmentContainer, detailFragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            Toast.makeText(getContext(), "Could not open product details.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}