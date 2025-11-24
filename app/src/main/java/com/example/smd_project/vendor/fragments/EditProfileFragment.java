package com.example.smd_project.vendor.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.models.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";

    private ImageButton btnBack;
    private EditText etName, etEmail, etPhone, etAddress;
    private LinearLayout btnChangePassword;
    private Button btnSaveProfile;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_vendor_profile, container, false);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        initViews(view);
        loadUserData();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        progressBar = view.findViewById(R.id.progressBar);

        // Email is not editable
        etEmail.setEnabled(false);
        etEmail.setAlpha(0.6f);
    }

    private void loadUserData() {
        if (firebaseUser == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        FirebaseHelper.getInstance().getUser(firebaseUser.getUid(), new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                progressBar.setVisibility(View.GONE);
                currentUser = user;
                populateFields();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Failed to load user: " + error);

                // Use Firebase Auth data as fallback
                etName.setText(firebaseUser.getDisplayName());
                etEmail.setText(firebaseUser.getEmail());
            }
        });
    }

    private void populateFields() {
        if (currentUser == null) return;

        etName.setText(currentUser.getName());
        etEmail.setText(currentUser.getEmail());
        etPhone.setText(currentUser.getPhone());

        if (currentUser.getAddress() != null) {
            etAddress.setText(currentUser.getAddress());
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSaveProfile.setEnabled(false);

        updateUserData(name, phone, address);
    }

    private void updateUserData(String name, String phone, String address) {
        if (currentUser == null) {
            currentUser = new User();
            currentUser.setUserId(firebaseUser.getUid());
            currentUser.setEmail(firebaseUser.getEmail());
            currentUser.setRole("vendor");
        }

        currentUser.setName(name);
        currentUser.setPhone(phone);
        currentUser.setAddress(address);

        FirebaseHelper.getInstance().updateUser(firebaseUser.getUid(), currentUser, new FirebaseHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                btnSaveProfile.setEnabled(true);
                Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                btnSaveProfile.setEnabled(true);
                Toast.makeText(getContext(), "Failed to update: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Change Password");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        final EditText currentPasswordInput = new EditText(getContext());
        currentPasswordInput.setHint("Current Password");
        currentPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(currentPasswordInput);

        final EditText newPasswordInput = new EditText(getContext());
        newPasswordInput.setHint("New Password (min 6 characters)");
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 20;
        newPasswordInput.setLayoutParams(params);
        layout.addView(newPasswordInput);

        final EditText confirmPasswordInput = new EditText(getContext());
        confirmPasswordInput.setHint("Confirm New Password");
        confirmPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmPasswordInput.setLayoutParams(params);
        layout.addView(confirmPasswordInput);

        builder.setView(layout);
        builder.setPositiveButton("Change", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String currentPassword = currentPasswordInput.getText().toString().trim();
                String newPassword = newPasswordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();

                if (currentPassword.isEmpty()) {
                    currentPasswordInput.setError("Required");
                    return;
                }
                if (newPassword.isEmpty()) {
                    newPasswordInput.setError("Required");
                    return;
                }
                if (newPassword.length() < 6) {
                    newPasswordInput.setError("Minimum 6 characters");
                    return;
                }
                if (!newPassword.equals(confirmPassword)) {
                    confirmPasswordInput.setError("Passwords don't match");
                    return;
                }

                changePassword(currentPassword, newPassword, dialog);
            });
        });

        dialog.show();
    }

    private void changePassword(String currentPassword, String newPassword, AlertDialog dialog) {
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);

        firebaseUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    firebaseUser.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid1 -> {
                                progressBar.setVisibility(View.GONE);
                                dialog.dismiss();
                                Toast.makeText(getContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Failed to change password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
                });
    }
}