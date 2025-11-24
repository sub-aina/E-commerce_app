package com.example.smd_project.vendor.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smd_project.R;
import com.example.smd_project.vendor.fragments.CategoryFragment;
import com.example.smd_project.vendor.fragments.VendorDashboardFragment;
//import com.example.smd_project.vendor.fragments.OrderFragment;
import com.example.smd_project.vendor.fragments.VendorProductsFragment;
import com.example.smd_project.vendor.fragments.VendorProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class VendorActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    private final int ID_DASHBOARD = View.generateViewId();
    private final int ID_PRODUCTS = View.generateViewId();
    private final int ID_ORDERS = View.generateViewId();
    private final int ID_CATEGORY = View.generateViewId();
    private final int ID_PROFILE = View.generateViewId();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vendor);


        bottomNav = findViewById(R.id.vendorBottomNav);

        //  Add items
        bottomNav.getMenu().add(0, ID_DASHBOARD, 0, "Dashboard").setIcon(R.drawable.ic_home);
        bottomNav.getMenu().add(0, ID_PRODUCTS, 1, "Products").setIcon(R.drawable.ic_add_products);
        bottomNav.getMenu().add(0, ID_ORDERS, 2, "Orders").setIcon(R.drawable.ic_orders);
        bottomNav.getMenu().add(0,ID_CATEGORY,3,"Category").setIcon(R.drawable.ic_category);
        bottomNav.getMenu().add(0, ID_PROFILE, 4, "Profile").setIcon(R.drawable.ic_profile);

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new VendorDashboardFragment());
        }


        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == ID_DASHBOARD) {
                    selectedFragment = new VendorDashboardFragment();
                } else if (itemId == ID_PRODUCTS) {
                    selectedFragment = new VendorProductsFragment();
                } else if (itemId == ID_ORDERS) {
                    selectedFragment = new com.example.smd_project.vendor.fragments.OrderFragment();
                } else if (itemId == ID_PROFILE) {
                    selectedFragment = new VendorProfileFragment();
                } else if (itemId== ID_CATEGORY) {
                    selectedFragment = new CategoryFragment();
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }
                return false;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.vendorFragmentContainer, fragment)
                .commit();
    }
}
