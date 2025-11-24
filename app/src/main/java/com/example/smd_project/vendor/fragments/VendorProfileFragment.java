package com.example.smd_project.vendor.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.SharedActivities.Login;
import com.example.smd_project.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;

public class VendorProfileFragment extends Fragment {


    private TextView adminNameText, adminRoleText, adminEmailText;

    private TextView totalUsersText, totalProductsText, totalOrdersText;
    private TextView totalRevenueText, totalCategoriesText, pendingOrdersText;

    private LinearLayout statTotalUsers, statTotalProducts, statTotalOrders;
    private LinearLayout statTotalRevenue, statTotalCategories, statPendingOrders;

    private LinearLayout editProfileLayout, changePasswordLayout;
    private LinearLayout aboutAppLayout, helpSupportLayout, privacyPolicyLayout, termsConditionsLayout;
    private SwitchMaterial notificationSwitch, emailNotificationSwitch;
    private MaterialButton logoutBtn;

    private FirebaseHelper firebaseHelper;
    private FirebaseAuth auth;
    private String currentUserId;
    private User currentUser;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_profile, container, false);

        initializeViews(view);

        setupClickListeners();
        loadUserData();
        loadStatistics();

        return view;
    }

    private void initializeViews(View view) {
        firebaseHelper = FirebaseHelper.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        currentUserId = firebaseUser != null ? firebaseUser.getUid() : null;


        adminNameText = view.findViewById(R.id.adminNameText);
        adminRoleText = view.findViewById(R.id.adminRoleText);
        adminEmailText = view.findViewById(R.id.adminEmailText);

        statTotalUsers = view.findViewById(R.id.statTotalUsers);
        statTotalProducts = view.findViewById(R.id.statTotalProducts);
        statTotalOrders = view.findViewById(R.id.statTotalOrders);
        statTotalRevenue = view.findViewById(R.id.statTotalRevenue);
        statTotalCategories = view.findViewById(R.id.statTotalCategories);
        statPendingOrders = view.findViewById(R.id.statPendingOrders);


        final int statValueId = R.id.statValue;
        final int statLabelId = R.id.statLabel;

        totalUsersText = statTotalUsers.findViewById(statValueId);
        totalProductsText = statTotalProducts.findViewById(statValueId);
        totalOrdersText = statTotalOrders.findViewById(statValueId);
        totalRevenueText = statTotalRevenue.findViewById(statValueId);
        totalCategoriesText = statTotalCategories.findViewById(statValueId);
        pendingOrdersText = statPendingOrders.findViewById(statValueId);

        ((TextView) statTotalUsers.findViewById(statLabelId)).setText("Total Users");
        ((TextView) statTotalProducts.findViewById(statLabelId)).setText("Total Products");
        ((TextView) statTotalOrders.findViewById(statLabelId)).setText("Total Orders");
        ((TextView) statTotalRevenue.findViewById(statLabelId)).setText("Total Revenue");
        ((TextView) statTotalCategories.findViewById(statLabelId)).setText("Total Categories");
        ((TextView) statPendingOrders.findViewById(statLabelId)).setText("Pending Orders");

        editProfileLayout = view.findViewById(R.id.editProfileLayout);
        notificationSwitch = view.findViewById(R.id.notificationSwitch);
        emailNotificationSwitch = view.findViewById(R.id.emailNotificationSwitch);

        aboutAppLayout = view.findViewById(R.id.aboutAppLayout);
        helpSupportLayout = view.findViewById(R.id.helpSupportLayout);
        privacyPolicyLayout = view.findViewById(R.id.privacyPolicyLayout);
        termsConditionsLayout = view.findViewById(R.id.termsConditionsLayout);

        logoutBtn = view.findViewById(R.id.logoutBtn);
    }



    private void setupClickListeners() {

        editProfileLayout.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.vendorFragmentContainer, new EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(getContext(), "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        emailNotificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(getContext(), "Email notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        aboutAppLayout.setOnClickListener(v -> showAboutDialog());

        helpSupportLayout.setOnClickListener(v -> showHelpDialog());

        privacyPolicyLayout.setOnClickListener(v -> showPrivacyPolicyDialog());

        termsConditionsLayout.setOnClickListener(v -> showTermsDialog());

        logoutBtn.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadUserData() {
        if (currentUserId == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseHelper.getUser(currentUserId, new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                updateUIWithUserData();
            }

            @Override
            public void onFailure(String error) {
                // If user doesn't exist in database, create from Firebase Auth
                FirebaseUser firebaseUser = auth.getCurrentUser();
                if (firebaseUser != null) {
                    currentUser = new User(
                            firebaseUser.getUid(),
                            firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Admin",
                            firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "",
                            "",
                            "admin"
                    );
                    updateUIWithUserData();

                    firebaseHelper.createUser(currentUserId, currentUser, new FirebaseHelper.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            Log.d("VendorProfile", "Admin user created in database");
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e("VendorProfile", "Failed to create admin user: " + error);
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUIWithUserData() {
        if (currentUser == null) return;

        adminNameText.setText(currentUser.getName());
        adminRoleText.setText("System Administrator");
        adminEmailText.setText(currentUser.getEmail());

    }

    private void loadStatistics() {
        firebaseHelper.getDashboardStats(new FirebaseHelper.OnDashboardStatsListener() {
            @Override
            public void onSuccess(FirebaseHelper.DashboardStats stats) {
                totalUsersText.setText(String.valueOf(stats.totalUsers));
                totalProductsText.setText(String.valueOf(stats.totalProducts));
                totalOrdersText.setText(String.valueOf(stats.totalOrders));
                totalRevenueText.setText(String.format(Locale.US, "$%.2f", stats.totalRevenue));
                totalCategoriesText.setText(String.valueOf(stats.totalCategories));
                pendingOrdersText.setText(String.valueOf(stats.pendingOrders));
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Failed to load statistics: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }





    private void showAboutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("About App")
                .setMessage("E-Commerce Admin Panel\n\nVersion: 1.0.0\n\nManage your online store with ease. Track products, orders, customers, and revenue all in one place.\n\n© 2025 Your Company")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Help & Support")
                .setMessage("Need help?\n\nEmail: support@example.com\nPhone: +1 234 567 8900\n\nBusiness Hours:\nMon-Fri: 9:00 AM - 6:00 PM\n\nFor technical issues, please contact our support team with detailed information about the problem.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showPrivacyPolicyDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Privacy Policy")
                .setMessage("We respect your privacy and are committed to protecting your personal data.\n\nData Collection:\n• Personal information\n• Usage data\n• Device information\n\nData Usage:\n• Service improvement\n• Customer support\n• Analytics\n\nFor full privacy policy, visit our website.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showTermsDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Terms & Conditions")
                .setMessage("By using this app, you agree to:\n\n1. Use the service responsibly\n2. Maintain account security\n3. Comply with applicable laws\n4. Respect intellectual property\n\nThe service is provided 'as is' without warranties.\n\nFor complete terms, visit our website.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    auth.signOut();
                    Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

                    requireActivity().finish();
                    startActivity(new Intent(getContext(), Login.class));
                })
                .setNegativeButton("No", null)
                .show();

    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
    }
}