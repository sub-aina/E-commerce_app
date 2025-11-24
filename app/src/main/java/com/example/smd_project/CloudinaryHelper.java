package com.example.smd_project;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudinaryHelper {
    private static final String TAG = "CloudinaryHelper";
    private static CloudinaryHelper instance;
    private Cloudinary cloudinary;

    private CloudinaryHelper() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dpvuowuz1");
        config.put("api_key", "512215983315828");
        config.put("api_secret", "xMAjEkzsj3hYTpNIAqoijybzm1U");

        config.put("secure", "true");

        cloudinary = new Cloudinary(config);
        Log.d(TAG, "Cloudinary initialized with cloud_name: dpvuowuz1");
    }

    public static CloudinaryHelper getInstance() {
        if (instance == null) {
            instance = new CloudinaryHelper();
        }
        return instance;
    }

    public interface OnImageUploadListener {
        void onSuccess(List<String> imageUrls);
        void onProgress(int uploaded, int total);
        void onFailure(String error);
    }

    public void uploadProductImages(Context context, List<Uri> imageUris, OnImageUploadListener listener) {
        List<String> uploadedUrls = new ArrayList<>();
        final int[] uploadCount = {0};

        new Thread(() -> {
            try {
                for (int i = 0; i < imageUris.size(); i++) {
                    Uri imageUri = imageUris.get(i);
                    Log.d(TAG, "Uploading image " + (i + 1) + " of " + imageUris.size());

                    InputStream inputStream = context.getContentResolver().openInputStream(imageUri);

                    if (inputStream == null) {
                        Log.e(TAG, "Failed to open input stream for URI: " + imageUri);
                        // Run on main thread
                        ((android.app.Activity) context).runOnUiThread(() ->
                                listener.onFailure("Could not read image file"));
                        return;
                    }

                    // Upload with proper parameters
                    Map<String, Object> uploadParams = ObjectUtils.asMap(
                            "folder", "products",
                            "resource_type", "image",
                            "use_filename", true,
                            "unique_filename", true,
                            "overwrite", false
                    );

                    Map uploadResult = cloudinary.uploader().upload(inputStream, uploadParams);

                    String url = uploadResult.get("secure_url").toString();
                    uploadedUrls.add(url);
                    uploadCount[0]++;

                    Log.d(TAG, "Image uploaded successfully: " + url);

                    final int currentCount = uploadCount[0];
                    final int total = imageUris.size();

                    // Run progress on main thread
                    ((android.app.Activity) context).runOnUiThread(() ->
                            listener.onProgress(currentCount, total));

                    inputStream.close();

                    if (uploadCount[0] == imageUris.size()) {
                        Log.d(TAG, "All images uploaded successfully");
                        final List<String> finalUrls = new ArrayList<>(uploadedUrls);
                        // Run success on main thread
                        ((android.app.Activity) context).runOnUiThread(() ->
                                listener.onSuccess(finalUrls));
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Upload failed: " + e.getMessage(), e);
                final String errorMsg = "Upload failed: " + e.getMessage();
                // Run failure on main thread
                ((android.app.Activity) context).runOnUiThread(() ->
                        listener.onFailure(errorMsg));
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error: " + e.getMessage(), e);
                final String errorMsg = "Unexpected error: " + e.getMessage();
                // Run failure on main thread
                ((android.app.Activity) context).runOnUiThread(() ->
                        listener.onFailure(errorMsg));
            }
        }).start();
    }

    public void deleteImage(String imageUrl, OnCompleteListener listener) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Attempting to delete image: " + imageUrl);

                // Extract public_id from URL
                String publicId = extractPublicId(imageUrl);

                if (publicId.isEmpty()) {
                    Log.e(TAG, "Could not extract public_id from URL: " + imageUrl);
                    listener.onFailure("Invalid image URL");
                    return;
                }

                Log.d(TAG, "Extracted public_id: " + publicId);

                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

                String resultStr = result.get("result").toString();
                if ("ok".equals(resultStr)) {
                    Log.d(TAG, "Image deleted successfully");
                    listener.onSuccess();
                } else {
                    Log.w(TAG, "Delete result: " + resultStr);
                    listener.onSuccess(); // Still consider it success
                }

            } catch (IOException e) {
                Log.e(TAG, "Delete failed: " + e.getMessage(), e);
                listener.onFailure(e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error during delete: " + e.getMessage(), e);
                listener.onFailure(e.getMessage());
            }
        }).start();
    }

    private String extractPublicId(String url) {
        try {
            // Example URL: https://res.cloudinary.com/dpvuowuz1/image/upload/v1234567890/products/filename.jpg
            // Should return: products/filename

            String[] parts = url.split("/");
            int uploadIndex = -1;

            // Find the "upload" part in URL
            for (int i = 0; i < parts.length; i++) {
                if ("upload".equals(parts[i])) {
                    uploadIndex = i;
                    break;
                }
            }

            if (uploadIndex >= 0 && uploadIndex + 2 < parts.length) {
                // Skip version (v1234567890) if present
                int startIndex = uploadIndex + 1;
                if (parts[startIndex].startsWith("v") && parts[startIndex].length() > 1) {
                    startIndex++; // Skip version
                }

                // Build public_id from remaining parts
                StringBuilder publicId = new StringBuilder();
                for (int i = startIndex; i < parts.length; i++) {
                    if (i > startIndex) {
                        publicId.append("/");
                    }

                    String part = parts[i];
                    // Remove file extension from last part
                    if (i == parts.length - 1 && part.contains(".")) {
                        part = part.substring(0, part.lastIndexOf('.'));
                    }
                    publicId.append(part);
                }

                return publicId.toString();
            }

            Log.e(TAG, "Could not parse URL structure: " + url);
            return "";

        } catch (Exception e) {
            Log.e(TAG, "Error extracting public_id: " + e.getMessage(), e);
            return "";
        }
    }

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }
}