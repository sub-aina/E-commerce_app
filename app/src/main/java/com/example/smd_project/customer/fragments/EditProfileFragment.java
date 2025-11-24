package com.example.smd_project.customer.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.models.User;
import com.example.smd_project.SharedActivities.AuthGuard;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileFragment extends Fragment {

    private EditText etName, etPhone, etEmail;
    private Button btnSave, btnChangePhoto;
    private ProgressBar progressBar;

    private User currentUser;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // ✅ Check authentication first
        if (!AuthGuard.requireAuth(this)) {
            return view;
        }

        initViews(view);
        setupImagePicker();
        loadUserData();

        return view;
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.etName);
        etPhone = view.findViewById(R.id.etPhone);
        etEmail = view.findViewById(R.id.etEmail);
        btnSave = view.findViewById(R.id.btnSave);
        progressBar = view.findViewById(R.id.progressBar);

        etEmail.setEnabled(false); // Email cannot be changed

        // Setup click listeners
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void setupImagePicker() {
        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Display selected image if you have an ImageView
                            // Glide.with(this).load(selectedImageUri).into(ivProfilePicture);
                            Toast.makeText(getContext(), "Image selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void loadUserData() {
        String userId = AuthGuard.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            AuthGuard.redirectToLogin(requireActivity(), null);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        FirebaseHelper.getInstance().getUser(userId, new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                if (!isAdded()) return; // Check if fragment is still attached

                currentUser = user;
                if (user != null) {
                    etName.setText(user.getName() != null ? user.getName() : "");
                    etPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                    etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String error) {
                if (!isAdded()) return;

                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error loading profile: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String userId = AuthGuard.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name required");
            etName.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        if (currentUser == null) {
            currentUser = new User();
            currentUser.setUserId(userId);
        }

        currentUser.setName(name);
        currentUser.setPhone(phone);
        // If image is selected, upload it first then update profile
        if (selectedImageUri != null) {
            uploadImageAndUpdateProfile(userId);
        } else {
            updateUserProfile(userId);
        }
    }

    private void uploadImageAndUpdateProfile(String userId) {
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("profile_images")
                .child(userId + ".jpg");

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        if (!isAdded()) return;

                        currentUser.setProfileImage(uri.toString());
                        updateUserProfile(userId);
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;

                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserProfile(String userId) {
        FirebaseHelper.getInstance().updateUser(userId, currentUser,
                new FirebaseHelper.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        if (!isAdded()) return;

                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                        // Go back to profile screen
                        getParentFragmentManager().popBackStack();
                    }

                    @Override
                    public void onFailure(String error) {
                        if (!isAdded()) return;

                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(getContext(), "Error updating profile: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}