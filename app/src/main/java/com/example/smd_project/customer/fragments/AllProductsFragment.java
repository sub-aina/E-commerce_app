package com.example.smd_project.customer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R; // Ensure correct R import
import com.example.smd_project.adapters.CustomerProductAdapter;
import com.example.smd_project.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AllProductsFragment extends Fragment {

    private RecyclerView recyclerView;
    private CustomerProductAdapter adapter;
    private List<Product> productList;
    private ImageView btnBack;
    private TextView tvTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_category_products, container, false);

        btnBack = view.findViewById(R.id.btnBack);
        tvTitle = view.findViewById(R.id.tvCategoryTitle);
        recyclerView = view.findViewById(R.id.recyclerViewProducts);


        if(view.findViewById(R.id.cardProductCount) != null)
            view.findViewById(R.id.cardProductCount).setVisibility(View.GONE);

        tvTitle.setText("All Products");

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        productList = new ArrayList<>();
        adapter = new CustomerProductAdapter(getContext(), productList, product -> {
            ProductDetailFragment fragment = ProductDetailFragment.newInstance(product.getProductId());
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.customerFragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);

        loadAllProducts();

        return view;
    }

    private void loadAllProducts() {
        FirebaseHelper.getInstance().getAllProductsReference()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Product p = ds.getValue(Product.class);
                            if (p != null && p.getStock() > 0) {
                                p.setProductId(ds.getKey());
                                productList.add(p);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}