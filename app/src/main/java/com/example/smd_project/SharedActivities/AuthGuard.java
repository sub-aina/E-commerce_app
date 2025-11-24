package com.example.smd_project.SharedActivities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.smd_project.SharedActivities.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Utility class to handle authentication checks across the app
 */
public class AuthGuard {

    /**
     * Check if user is authenticated
     * @return FirebaseUser if authenticated, null otherwise
     */
    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * Check if user is authenticated, if not redirect to login
     * @param fragment Fragment to check
     * @return true if authenticated, false if redirected to login
     */
    public static boolean requireAuth(Fragment fragment) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectToLogin(fragment.requireActivity(), "Please login to continue");
            return false;
        }
        return true;
    }

    /**
     * Check if user is authenticated, if not redirect to login
     * @param activity Activity to check
     * @return true if authenticated, false if redirected to login
     */
    public static boolean requireAuth(Activity activity) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectToLogin(activity, "Please login to continue");
            return false;
        }
        return true;
    }

    /**
     * Get current user ID safely
     * @return User ID or null if not authenticated
     */
    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Redirect to login screen
     */
    public static void redirectToLogin(Activity activity, String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        }

        Intent intent = new Intent(activity, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    /**
     * Check authentication and show toast without redirecting
     */
    public static boolean checkAuthWithToast(Context context, String message) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}