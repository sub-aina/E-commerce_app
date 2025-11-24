package com.example.smd_project.vendor.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.adapters.ProductAdapter;
import com.example.smd_project.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class VendorProductsFragment extends Fragment
        implements AddProductFragment.OnProductAddedListener, ProductAdapter.onProductActionListener {

    RecyclerView recyclerView;
    Button addProducts;
    ArrayList<Product> productList=new ArrayList<>();
    ProductAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_vendor_products, container, false);

        recyclerView = view.findViewById(R.id.recyclerProducts);
        addProducts = view.findViewById(R.id.btnAddProduct);

        adapter = new ProductAdapter(productList, (ProductAdapter.onProductActionListener) this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Load products from Firebase
         FirebaseHelper.getInstance().getAllProductsReference().addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot snapshot) {
             productList.clear();
             for (DataSnapshot snap : snapshot.getChildren()) {
                 Product product = snap.getValue(Product.class);
                 if (product != null) productList.add(product);
             }
             adapter.notifyDataSetChanged();
         }

         @Override
         public void onCancelled(@NonNull DatabaseError error) {
             Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
         }
     });

        addProducts.setOnClickListener(v -> {
            AddProductFragment addProductFragment = new AddProductFragment();
            addProductFragment.setListener(this);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.vendorFragmentContainer, addProductFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    @Override
    public void onProductAdded(Product product) {
        if (product.getProductId() != null && !product.getProductId().isEmpty()) {
            FirebaseHelper.getInstance().updateProduct(product.getProductId(), product, new FirebaseHelper.OnCompleteListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Product updated successfully!", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(String error) {
                    Toast.makeText(getContext(), "Failed to update product: " + error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            FirebaseHelper.getInstance().addProduct(product, new FirebaseHelper.OnCompleteListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Product added successfully!", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(String error) {
                    Toast.makeText(getContext(), "Failed to add product: " + error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void onDelete(Product product) {
        if (product.getProductId() == null) {
            Toast.makeText(getContext(), "Error: Product ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseHelper.getInstance().deleteProduct(product.getProductId(), new FirebaseHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), product.getName() + " deleted.", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Failed to delete: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onEdit(Product product) {

        AddProductFragment editFragment = new AddProductFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable("product_to_edit", product);
        editFragment.setArguments(bundle);

        editFragment.setListener(this);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.vendorFragmentContainer, editFragment)
                .addToBackStack(null)
                .commit();

        Toast.makeText(getContext(), "Opening edit form for " + product.getName(), Toast.LENGTH_SHORT).show();
    }


    public void onStockChanged(Product product, int newStock) {
        if (product.getProductId() == null) {
            Toast.makeText(getContext(), "Error: Product ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseHelper.getInstance().updateProductStock(product.getProductId(), newStock, new FirebaseHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), product.getName() + " stock updated to " + newStock + ".", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Failed to update stock: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

}
