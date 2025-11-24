package com.example.smd_project.vendor.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.adapters.CategoryAdapter;
import com.example.smd_project.models.Category;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment
        implements CategoryAdapter.OnEditClickListener,
        CategoryAdapter.OnDeleteClickListener,
        CategoryAdapter.OnCategoryClickListener {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private ExtendedFloatingActionButton fabAddCategory;

    // Stats TextViews
    private TextView tvTotalCategories;
    private TextView tvTotalProducts;

    public CategoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onEdit(Category category) {
        showEditCategoryDialog(category);
    }

    @Override
    public void onDelete(Category category) {
        showDeleteConfirmation(category);
    }

    @Override
    public void onCategoryClick(Category category) {
        showCategoryProducts(category);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_category, container, false);

        init(view);
        setupRecyclerView();
        loadCategories();
        loadTotalProducts();
        return view;
    }

    private void init(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewCategories);
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.layoutEmptyState);
        fabAddCategory = view.findViewById(R.id.fabAddCategory);

        // Initialize stats TextViews
        tvTotalCategories = view.findViewById(R.id.tvTotalCategories);
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts);

        fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void setupRecyclerView() {
        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(categoryList, this, this);
        adapter.setOnCategoryClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        FirebaseHelper.getInstance().getAllCategoriesReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        categoryList.clear();

                        for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                            Category category = categorySnapshot.getValue(Category.class);
                            if (category != null)
                                categoryList.add(category);
                        }
                        progressBar.setVisibility(View.GONE);


                        if (tvTotalCategories != null) {
                            tvTotalCategories.setText(String.valueOf(categoryList.size()));
                        }

                        if (categoryList.isEmpty()) {
                            emptyState.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyState.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadTotalProducts() {
        FirebaseHelper.getInstance().getAllProductsReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long productCount = snapshot.getChildrenCount();
                        if (tvTotalProducts != null) {
                            tvTotalProducts.setText(String.valueOf(productCount));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showCategoryProducts(Category category) {
        Bundle bundle = new Bundle();
        bundle.putString("category_name", category.getName());
        bundle.putString("category_id", category.getCategoryId());

        CategoryProductsFragment fragment = new CategoryProductsFragment();
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.vendorFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);

        // Use setView but NOT setPositive/NegativeButton (we handle them in the view)
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Make background transparent so the CardView corners show up
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Initialize Views
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etName = dialogView.findViewById(R.id.etCategoryName);
        EditText etDesc = dialogView.findViewById(R.id.etCategoryDescription);
        View btnSave = dialogView.findViewById(R.id.btnSave);
        View btnCancel = dialogView.findViewById(R.id.btnCancel);

        tvTitle.setText("Add New Category");

        // Logic
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String description = etDesc.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Category name is required");
                etName.requestFocus();
                return;
            }

            addCategory(name, description);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditCategoryDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Initialize Views
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etName = dialogView.findViewById(R.id.etCategoryName);
        EditText etDesc = dialogView.findViewById(R.id.etCategoryDescription);
        TextView btnSave = dialogView.findViewById(R.id.btnSave); // Cast to TextView/Button
        View btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Pre-fill Data
        tvTitle.setText("Edit Category");
        btnSave.setText("UPDATE");
        etName.setText(category.getName());
        etDesc.setText(category.getDescription());

        // Logic
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String description = etDesc.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Category name is required");
                etName.requestFocus();
                return;
            }

            category.setName(name);
            category.setDescription(description);
            updateCategory(category);
            dialog.dismiss();
        });

        dialog.show();
    }
    private void addCategory(String name, String description) {
        Category category = new Category(name, description);

        FirebaseHelper.getInstance().addCategory(category, new FirebaseHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Category added successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCategory(Category category) {
        FirebaseHelper.getInstance().updateCategory(category.getCategoryId(), category,
                new FirebaseHelper.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Category updated successfully!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDeleteConfirmation(Category category) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + category.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCategory(category))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCategory(Category category) {
        FirebaseHelper.getInstance().deleteCategory(category.getCategoryId(),
                new FirebaseHelper.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Category deleted successfully!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}