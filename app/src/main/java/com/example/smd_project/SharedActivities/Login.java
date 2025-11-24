package com.example.smd_project.SharedActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.customer.CustomerActivity;
import com.example.smd_project.vendor.activities.VendorActivity;
import com.example.smd_project.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    EditText usernameInput;
    EditText passwordInput;
    TextView forgotPassword;
    TextView signUpText;
    Button loginButton;
    ImageView googleLogin;

    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        init();
        setupGoogleSignIn();
        setupClickListeners();
    }

    private void setupGoogleSignIn() {
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Setup Activity Result Launcher for Google Sign-In
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleGoogleSignInResult(task);
                    } else {
                        Log.e(TAG, "Google Sign-In cancelled");
                        Toast.makeText(this, "Sign-in cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // 1. UPDATE THIS METHOD
    private void setupClickListeners() {
        loginButton.setOnClickListener(view -> {
            String email = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        googleLogin.setOnClickListener(view -> signInWithGoogle());

        signUpText.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, SignUp.class);
            startActivity(intent);
        });

        // UPDATED: Now calls the dialog method
        forgotPassword.setOnClickListener(view -> {
            showForgotPasswordDialog();
        });
    }

    // Update the showForgotPasswordDialog method in Login.java:

    private void showForgotPasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etEmail = dialogView.findViewById(R.id.etResetEmail);
        View btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSend = dialogView.findViewById(R.id.btnSend);

        // Pre-fill email if available
        String currentEmail = usernameInput.getText().toString().trim();
        if (!currentEmail.isEmpty()) {
            etEmail.setText(currentEmail);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSend.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                etEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Invalid email format");
                etEmail.requestFocus();
                return;
            }

            btnSend.setEnabled(false);
            btnSend.setText("Sending...");

            // Check if user exists in database
            checkUserAndSendOTP(email, dialog, btnSend);
        });

        dialog.show();
    }

    private void checkUserAndSendOTP(String email, android.app.AlertDialog dialog, Button btnSend) {
        FirebaseHelper.getInstance().getAllUsersReference()
                .orderByChild("email")
                .equalTo(email)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // User found, send OTP
                            sendForgotPasswordOTP(email, dialog);
                        } else {
                            btnSend.setEnabled(true);
                            btnSend.setText("SEND OTP");
                            Toast.makeText(Login.this, "No account found with this email", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                        btnSend.setEnabled(true);
                        btnSend.setText("SEND OTP");
                        Toast.makeText(Login.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendForgotPasswordOTP(String email, android.app.AlertDialog dialog) {
        String safeEmail = email.replace(".", "_");
        int otp = (int) (Math.random() * 900000) + 100000;
        long validTill = System.currentTimeMillis() + (5 * 60 * 1000);

        com.google.firebase.database.DatabaseReference ref = com.google.firebase.database.FirebaseDatabase
                .getInstance().getReference("forgot_password_otp").child(safeEmail);

        java.util.HashMap<String, Object> otpData = new java.util.HashMap<>();
        otpData.put("otp", otp);
        otpData.put("validTill", validTill);
        otpData.put("email", email);

        ref.setValue(otpData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "OTP saved: " + otp);

                EmailSender.sendOTPEmail(email, String.valueOf(otp), new EmailSender.EmailCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(Login.this, "OTP sent to your email!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                            // Navigate to OTP verification screen
                            Intent intent = new Intent(Login.this, ForgotPasswordOTPActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(Login.this, "Failed to send email: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            } else {
                Toast.makeText(Login.this, "Failed to generate OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void signInWithGoogle() {
        Log.d(TAG, "Starting Google Sign-In");

        // Sign out first to show account picker every time
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Google Sign-In successful: " + account.getEmail());
            Toast.makeText(this, "Google Sign-In successful", Toast.LENGTH_SHORT).show();
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.e(TAG, "Google Sign-In failed. Error code: " + e.getStatusCode(), e);
            String errorMessage = "Google Sign-In failed";

            // Provide more specific error messages
            switch (e.getStatusCode()) {
                case 12501:
                    errorMessage = "Sign-in cancelled";
                    break;
                case 10:
                    errorMessage = "Developer error. Please check SHA-1 configuration";
                    break;
                default:
                    errorMessage = "Sign-in failed: " + e.getStatusCode();
            }

            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d(TAG, "Authenticating with Firebase using Google token");

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase authentication with Google successful");
                        FirebaseUser firebaseUser = auth.getCurrentUser();

                        if (firebaseUser != null) {
                            Toast.makeText(this, "Signed in as " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
                            checkAndSyncUser(firebaseUser.getUid(), firebaseUser);
                        }
                    } else {
                        Log.e(TAG, "Firebase authentication with Google failed", task.getException());
                        Toast.makeText(this, "Authentication failed: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loginUser(String email, String password) {
        Log.d(TAG, "Attempting login for: " + email);

        // Disable button to prevent multiple clicks
        loginButton.setEnabled(false);
        loginButton.setText("Logging in");

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase Auth login successful");
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            Log.d(TAG, "User ID: " + userId);
                            checkAndSyncUser(userId, firebaseUser);
                        }
                    } else {
                        Log.e(TAG, "Login failed", task.getException());
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Login failed";
                        Toast.makeText(Login.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkAndSyncUser(String userId, FirebaseUser firebaseUser) {
        Log.d(TAG, "Checking if user exists in database...");

        FirebaseHelper.getInstance().getUser(userId, new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "User found in database");
                navigateBasedOnRole(user);
            }

            @Override
            public void onFailure(String error) {
                Log.w(TAG, "User not found in database, creating...");
                Toast.makeText(Login.this, "Setting up your profile...", Toast.LENGTH_SHORT).show();
                syncUserToDatabase(userId, firebaseUser);
            }
        });
    }

    private void syncUserToDatabase(String userId, FirebaseUser firebaseUser) {
        String email = firebaseUser.getEmail();
        String name = firebaseUser.getDisplayName();
        String photoUrl = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null;

        // If name is null, extract from email
        if (name == null || name.isEmpty()) {
            name = email != null ? email.substring(0, email.indexOf("@")) : "User";
        }

        Log.d(TAG, "Creating user in database - Name: " + name + ", Email: " + email);

        // Create new user with customer role (admin needs to be set manually)
        User user = new User(userId, name, email, "", "customer");
        user.setCreatedAt(System.currentTimeMillis());
        user.setWalletBalance(0.0);



        FirebaseHelper.getInstance().createUser(userId, user, new FirebaseHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "User synced to database successfully");
                Toast.makeText(Login.this, "Profile created! Logging in...", Toast.LENGTH_SHORT).show();
                fetchUserData(userId);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to sync user: " + error);
                loginButton.setEnabled(true);
                loginButton.setText("Login");
                Toast.makeText(Login.this, "Error creating profile: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchUserData(String userId) {
        FirebaseHelper.getInstance().getUser(userId, new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                navigateBasedOnRole(user);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error fetching user: " + error);
                loginButton.setEnabled(true);
                loginButton.setText("Login");
                Toast.makeText(Login.this, "Error fetching user data: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateBasedOnRole(User user) {
        loginButton.setEnabled(true);
        loginButton.setText("Login");

        if (user != null) {
            Intent intent;

            if ("admin".equalsIgnoreCase(user.getRole())) {
                Log.d(TAG, "Navigating to Admin Dashboard");
                intent = new Intent(Login.this, VendorActivity.class);
                Toast.makeText(Login.this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Navigating to Customer Main");
                intent = new Intent(Login.this, CustomerActivity.class);
                Toast.makeText(Login.this, "Welcome " + user.getName() + "!", Toast.LENGTH_SHORT).show();
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(Login.this, "User data not found", Toast.LENGTH_SHORT).show();
        }
    }

    public void init() {
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        forgotPassword = findViewById(R.id.forgotPassword);
        signUpText = findViewById(R.id.signUpText);
        loginButton = findViewById(R.id.loginButton);
        googleLogin = findViewById(R.id.googleLogin); // Add this line
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Only auto-login if coming from a fresh start, not after manual logout
        // You can add a SharedPreferences flag if you want to prevent auto-login after logout
    }
}