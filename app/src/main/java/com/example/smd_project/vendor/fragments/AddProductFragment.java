package com.example.smd_project.vendor.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smd_project.CloudinaryHelper;
import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.models.Category;
import com.example.smd_project.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddProductFragment extends Fragment {

    private EditText etName, etDescription, etPrice, etQuantity, etSizes, etColors;
    private Spinner spinnerCategory;
    private Button btnSaveProduct, btnSelectImages;
    private RecyclerView rvSelectedImages;
    private ProgressBar progressBar;

    private List<Uri> selectedImageUris = new ArrayList<>();
    private ImagePreviewAdapter imageAdapter;

    private List<Category> categoryList = new ArrayList<>();
    private List<String> categoryNames = new ArrayList<>();
    private Map<String, String> categoryNameToId = new HashMap<>();
    private ArrayAdapter<String> categoryAdapter;

    private Product productToEdit = null;
    private OnProductAddedListener listener;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public interface OnProductAddedListener {
        void onProductAdded(Product product);
    }

    public void setListener(OnProductAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        selectedImageUris.clear();

                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count && i < 5; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                selectedImageUris.add(imageUri);
                            }
                        } else if (data.getData() != null) {
                            selectedImageUris.add(data.getData());
                        }

                        imageAdapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), selectedImageUris.size() + " image(s) selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product, container, false);

        initViews(view);
        setupImageRecyclerView();
        loadCategories();

        // Check if editing existing product
        if (getArguments() != null) {
            productToEdit = (Product) getArguments().getSerializable("product_to_edit");
            if (productToEdit != null) {
                populateFields(productToEdit);
            }
        }

        btnSelectImages.setOnClickListener(v -> openImagePicker());
        btnSaveProduct.setOnClickListener(v -> saveProduct());

        return view;
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.etName);
        etDescription = view.findViewById(R.id.etDescription);
        etPrice = view.findViewById(R.id.etPrice);
        etQuantity = view.findViewById(R.id.etQuantity);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        btnSaveProduct = view.findViewById(R.id.btnSaveProduct);
        btnSelectImages = view.findViewById(R.id.btnSelectImages);
        rvSelectedImages = view.findViewById(R.id.rvSelectedImages);
        progressBar = view.findViewById(R.id.progressBar);

        etSizes = view.findViewById(R.id.etSizes);
        etColors = view.findViewById(R.id.etColors);

        // Setup category spinner
        categoryNames.add("Select Category");
        categoryAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        FirebaseHelper.getInstance().getAllCategoriesReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        categoryList.clear();
                        categoryNames.clear();
                        categoryNameToId.clear();

                        categoryNames.add("Select Category");

                        for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                            Category category = categorySnapshot.getValue(Category.class);
                            if (category != null) {
                                categoryList.add(category);
                                categoryNames.add(category.getName());
                                categoryNameToId.put(category.getName(), category.getCategoryId());
                            }
                        }

                        categoryAdapter.notifyDataSetChanged();

                        // If editing, select the current category
                        if (productToEdit != null && productToEdit.getCategory() != null) {
                            selectCategory(productToEdit.getCategory());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void selectCategory(String categoryName) {
        int position = categoryNames.indexOf(categoryName);
        if (position >= 0) {
            spinnerCategory.setSelection(position);
        }
    }

    private void setupImageRecyclerView() {
        imageAdapter = new ImagePreviewAdapter(selectedImageUris, position -> {
            selectedImageUris.remove(position);
            imageAdapter.notifyItemRemoved(position);
            Toast.makeText(getContext(), "Image removed", Toast.LENGTH_SHORT).show();
        });

        rvSelectedImages.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvSelectedImages.setAdapter(imageAdapter);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Product Images"));
    }

    private void populateFields(Product product) {
        etName.setText(product.getName());
        etDescription.setText(product.getDescription());
        etPrice.setText(String.valueOf(product.getPrice()));
        etQuantity.setText(String.valueOf(product.getStock()));

        if (product.getSizes() != null && !product.getSizes().isEmpty()) {
            etSizes.setText(String.join(", ", product.getSizes()));
        }

        // Populate colors
        if (product.getColors() != null && !product.getColors().isEmpty()) {
            etColors.setText(String.join(", ", product.getColors()));
        }
        // Category will be selected after categories are loaded
    }

    private void saveProduct() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();

        // Get selected category
        int selectedPosition = spinnerCategory.getSelectedItemPosition();
        if (selectedPosition == 0) {
            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        String selectedCategoryName = categoryNames.get(selectedPosition);

        // Validation
        if (name.isEmpty()) {
            etName.setError("Product name required");
            etName.requestFocus();
            return;
        }
        if (description.isEmpty()) {
            etDescription.setError("Description required");
            etDescription.requestFocus();
            return;
        }
        if (priceStr.isEmpty()) {
            etPrice.setError("Price required");
            etPrice.requestFocus();
            return;
        }
        if (quantityStr.isEmpty()) {
            etQuantity.setError("Quantity required");
            etQuantity.requestFocus();
            return;
        }

        double price = Double.parseDouble(priceStr);
        int quantity = Integer.parseInt(quantityStr);

        // Convert Sizes
        List<String> sizes = new ArrayList<>();
        if (!etSizes.getText().toString().trim().isEmpty()) {
            sizes = Arrays.asList(etSizes.getText().toString().trim().split(","));
        }

// Convert Colors
        List<String> colors = new ArrayList<>();
        if (!etColors.getText().toString().trim().isEmpty()) {
            colors = Arrays.asList(etColors.getText().toString().trim().split(","));
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnSaveProduct.setEnabled(false);
        btnSelectImages.setEnabled(false);

        // If editing and no new images selected, just update product data
        if (productToEdit != null && selectedImageUris.isEmpty()) {
            updateProductData(productToEdit, name, description, price, quantity,
                    selectedCategoryName, productToEdit.getImages());
            return;
        }

        // Upload images first if new images are selected
        if (!selectedImageUris.isEmpty()) {
            CloudinaryHelper.getInstance().uploadProductImages(
                    getContext(),
                    selectedImageUris,
                    new CloudinaryHelper.OnImageUploadListener() {
                        @Override
                        public void onSuccess(List<String> imageUrls) {
                            if (productToEdit != null) {
                                updateProductData(productToEdit, name, description, price,
                                        quantity, selectedCategoryName, imageUrls);
                            } else {
                                createNewProduct(name, description, price, quantity,
                                        selectedCategoryName, imageUrls);
                            }
                        }

                        @Override
                        public void onProgress(int uploaded, int total) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(),
                                        "Uploading " + uploaded + "/" + total,
                                        Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            requireActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                btnSaveProduct.setEnabled(true);
                                btnSelectImages.setEnabled(true);
                                Toast.makeText(getContext(),
                                        "Upload failed: " + error,
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    }
            );
        } else {
            // No images selected for new product
            Toast.makeText(getContext(), "Please select at least one image", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnSaveProduct.setEnabled(true);
            btnSelectImages.setEnabled(true);
        }
    }

    private void createNewProduct(String name, String description, double price,
                                  int quantity, String category, List<String> imageUrls) {
        Product product = new Product(name, description, price, quantity);
        product.setCategory(category);
        product.setImages(imageUrls);
        product.setCreatedAt(System.currentTimeMillis());

        String sizesStr = etSizes.getText().toString().trim();
        String colorsStr = etColors.getText().toString().trim();

        if (!sizesStr.isEmpty()) {
            List<String> sizes = Arrays.asList(sizesStr.split("\\s*,\\s*"));
            product.setSizes(sizes);
        }

        if (!colorsStr.isEmpty()) {
            List<String> colors = Arrays.asList(colorsStr.split("\\s*,\\s*"));
            product.setColors(colors);
        }

        FirebaseHelper.getInstance().addProduct(product, new FirebaseHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                requireActivity().runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onProductAdded(product);
                    }

                    progressBar.setVisibility(View.GONE);
                    btnSaveProduct.setEnabled(true);
                    btnSelectImages.setEnabled(true);

                    Toast.makeText(getContext(), " Product added successfully!", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
            }

            @Override
            public void onFailure(String error) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSaveProduct.setEnabled(true);
                    btnSelectImages.setEnabled(true);
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateProductData(Product product, String name, String description,
                                   double price, int quantity, String category,
                                   List<String> newImageUrls) {

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(quantity);
        product.setCategory(category);

        String sizesStr = etSizes.getText().toString().trim();
        if (!sizesStr.isEmpty()) {
            List<String> sizes = Arrays.asList(sizesStr.split("\\s*,\\s*"));
            product.setSizes(sizes);
        }

        String colorsStr = etColors.getText().toString().trim();
        if (!colorsStr.isEmpty()) {
            List<String> colors = Arrays.asList(colorsStr.split("\\s*,\\s*"));
            product.setColors(colors);
        }

        if (newImageUrls != null && !newImageUrls.isEmpty()) {
            List<String> oldImageUrls = product.getImages();
            if (oldImageUrls != null && !oldImageUrls.isEmpty()) {
                for (String oldUrl : oldImageUrls) {
                    CloudinaryHelper.getInstance().deleteImage(oldUrl, new CloudinaryHelper.OnCompleteListener() {
                        @Override
                        public void onSuccess() {}
                        @Override
                        public void onFailure(String error) {}
                    });
                }
            }
            product.setImages(newImageUrls);
        }

        FirebaseHelper.getInstance().updateProduct(product.getProductId(), product,
                new FirebaseHelper.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        requireActivity().runOnUiThread(() -> {
                            if (listener != null) {
                                listener.onProductAdded(product);
                            }

                            progressBar.setVisibility(View.GONE);
                            btnSaveProduct.setEnabled(true);
                            btnSelectImages.setEnabled(true);

                            Toast.makeText(getContext(), " Product updated successfully!", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        requireActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnSaveProduct.setEnabled(true);
                            btnSelectImages.setEnabled(true);
                            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }
    private static class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder> {
        private List<Uri> imageUris;
        private OnImageRemoveListener removeListener;

        interface OnImageRemoveListener {
            void onRemove(int position);
        }

        public ImagePreviewAdapter(List<Uri> imageUris, OnImageRemoveListener listener) {
            this.imageUris = imageUris;
            this.removeListener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product_db, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.imageView.setImageURI(imageUris.get(position));
            holder.btnRemove.setOnClickListener(v -> {
                if (removeListener != null) {
                    removeListener.onRemove(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return imageUris.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ImageView btnRemove;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.ivPreview);
                btnRemove = itemView.findViewById(R.id.btnRemove);
            }
        }
    }
}