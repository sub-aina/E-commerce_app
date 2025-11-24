package com.example.smd_project.SharedActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.smd_project.SharedActivities.EmailSender;
import com.example.smd_project.customer.CustomerActivity;
import com.example.smd_project.models.User;
import com.example.smd_project.vendor.activities.VendorActivity;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    EditText fullNameInput, emailInput, phoneInput, passwordInput, confirmPasswordInput;
    Button signUpButton;
    TextView loginText;
    ImageView googleSignUp;

    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

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

    private void setupClickListeners() {
        signUpButton.setOnClickListener(v -> registerUser());

        // Google Sign-Up Click Listener
        googleSignUp.setOnClickListener(v -> signUpWithGoogle());

        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(SignUp.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

    private void signUpWithGoogle() {
        Log.d(TAG, "Starting Google Sign-Up");

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
                            Toast.makeText(this, "Signed up as " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
                            checkAndCreateUser(firebaseUser);
                        }
                    } else {
                        Log.e(TAG, "Firebase authentication with Google failed", task.getException());
                        Toast.makeText(this, "Authentication failed: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkAndCreateUser(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();

        FirebaseHelper.getInstance().getUser(userId, new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                // User already exists, navigate to appropriate screen
                Log.d(TAG, "User already exists in database");
                navigateBasedOnRole(user);
            }

            @Override
            public void onFailure(String error) {
                // User doesn't exist, create new user
                Log.d(TAG, "Creating new user in database");
                createGoogleUser(firebaseUser);
            }
        });
    }

    private void createGoogleUser(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();
        String email = firebaseUser.getEmail();
        String name = firebaseUser.getDisplayName();
        String photoUrl = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null;

        // If name is null, extract from email
        if (name == null || name.isEmpty()) {
            name = email != null ? email.substring(0, email.indexOf("@")) : "User";
        }

        Log.d(TAG, "Creating user in database - Name: " + name + ", Email: " + email);

        // Create new user with customer role
        User user = new User(userId, name, email, "", "customer");
        user.setCreatedAt(System.currentTimeMillis());
        user.setWalletBalance(0.0);


        FirebaseHelper.getInstance().createUser(userId, user, new FirebaseHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "User created successfully in database");
                Toast.makeText(SignUp.this, "Account created! Welcome!", Toast.LENGTH_SHORT).show();
                navigateBasedOnRole(user);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to create user: " + error);
                Toast.makeText(SignUp.this, "Error creating profile: " + error, Toast.LENGTH_LONG).show();

                // Delete auth user if database save fails
                if (auth.getCurrentUser() != null) {
                    auth.getCurrentUser().delete();
                }
            }
        });
    }

    private void navigateBasedOnRole(User user) {
        if (user != null) {
            Intent intent;

            if ("admin".equalsIgnoreCase(user.getRole())) {
                Log.d(TAG, "Navigating to Admin Dashboard");
                intent = new Intent(SignUp.this, VendorActivity.class);
                Toast.makeText(SignUp.this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Navigating to Customer Main");
                intent = new Intent(SignUp.this, CustomerActivity.class);
                Toast.makeText(SignUp.this, "Welcome " + user.getName() + "!", Toast.LENGTH_SHORT).show();
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(SignUp.this, "User data not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {
        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        // phoneInput = findViewById(R.id.phoneInput); // Commented out as it doesn't exist in layout
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        signUpButton = findViewById(R.id.signUpButton);
        loginText = findViewById(R.id.loginText);
        googleSignUp = findViewById(R.id.googleSignup); // Use the correct ID from your layout
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLongToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
//
//    private void registerUser() {
//        String fullName = fullNameInput.getText().toString().trim();
//        String email = emailInput.getText().toString().trim();
//        String phone = " ";
//        String password = passwordInput.getText().toString().trim();
//        String confirmPassword = confirmPasswordInput.getText().toString().trim();
//
//        Log.d(TAG, "Starting registration for: " + email);
//
//        // Validation
//        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() ||
//                password.isEmpty() || confirmPassword.isEmpty()) {
//            showToast("Please fill all fields");
//            return;
//        }
//
//        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            showToast("Please enter a valid email");
//            return;
//        }
//
//        if (!password.equals(confirmPassword)) {
//            showToast("Passwords do not match");
//            return;
//        }
//
//        if (password.length() < 6) {
//            showToast("Password must be at least 6 characters");
//            return;
//        }
//
//        // Disable button to prevent multiple clicks
//        signUpButton.setEnabled(false);
//        signUpButton.setText("Creating account...");
//        sendCustomOtp(email, password, fullName);
//        showToast("Creating your account...");
//
//        // Create user in Firebase Auth
//        auth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Log.d(TAG, "Firebase Auth account created successfully");
//                        showToast("Account created! Setting up your profile...");
//                        String userId = auth.getCurrentUser().getUid();
//                        Log.d(TAG, "User ID: " + userId);
//                        saveUserToDatabase(userId, fullName, email, phone);
//                    } else {
//                        Log.e(TAG, "Firebase Auth failed", task.getException());
//                        signUpButton.setEnabled(true);
//                        signUpButton.setText("Sign Up");
//                        showLongToast("Registration failed: " + task.getException().getMessage());
//                    }
//                });
//    }

    private void registerUser() {
        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = " ";
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        Log.d(TAG, "Starting registration for: " + email);

        // Validation
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Please fill all fields");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return;
        }

        if (password.length() < 6) {
            showToast("Password must be at least 6 characters");
            return;
        }

        // Disable button to prevent multiple clicks
        signUpButton.setEnabled(false);
        signUpButton.setText("Sending OTP...");

        // ✅ ONLY SEND OTP - Don't create user yet
        sendCustomOtp(email, password, fullName);
    }
    private void saveUserToDatabase(String userId, String fullName, String email, String phone) {
        // Create User object with "customer" role by default
        User user = new User(userId, fullName, email, phone, "customer");
        user.setCreatedAt(System.currentTimeMillis());
        user.setWalletBalance(0.0);

        // Save to Users collection in Realtime Database
        FirebaseHelper.getInstance().createUser(userId, user, new FirebaseHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                signUpButton.setEnabled(true);
                signUpButton.setText("Sign Up");
                Toast.makeText(SignUp.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                // Send email verification
                auth.getCurrentUser().sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUp.this,
                                        "Verification email sent. Please verify your email.",
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                navigateToLogin();
            }

            @Override
            public void onFailure(String error) {
                signUpButton.setEnabled(true);
                signUpButton.setText("Sign Up");

                // If database save fails, delete the auth user to maintain consistency
                if (auth.getCurrentUser() != null) {
                    auth.getCurrentUser().delete();
                }

                Toast.makeText(SignUp.this, "Error saving user: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SignUp.this, Login.class);
        startActivity(intent);
        finish();
    }
//    public void sendCustomOtp(String userEmail, String password, String name) {
//        // email of user
//        String safeEmail = userEmail.replace(".", "_");
//        // Generate 6-digit OTP
//        int otp = (int) (Math.random() * 900000) + 100000;
//        // Valid for 5 minutes
//        long validTill = System.currentTimeMillis() + (5 * 60 * 1000);
//        // Firebase reference
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("otp").child(safeEmail);
//        // Make object to store
//        HashMap<String, Object> otpData = new HashMap<>();
//        otpData.put("otp", otp);
//        otpData.put("validTill", validTill);
//        otpData.put("email", userEmail);
//        // Write to Firebase
//        ref.setValue(otpData).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                Log.d("OTP", "OTP saved successfully");
//            } else {
//                Log.e("OTP", "Failed to save OTP: " + task.getException());
//            }
//        });
//        try {
//            EmailSender.sendOTPEmail(userEmail, String.valueOf(otp));
//            Intent intent = new Intent(SignUp.this, OTPActivity.class);
//            intent.putExtra("email", userEmail);
//            intent.putExtra("password", password);
//            intent.putExtra("name", name);
//            startActivity(intent);
//        } catch (JSONException e) {
//            Log.e("SEND_OTP", "Error sending OTP email", e);
//            Toast.makeText(this, "Failed to send OTP email", Toast.LENGTH_SHORT).show();
//        }
//    }
// In SignUp.java - Update the sendCustomOtp method:

    public void sendCustomOtp(String userEmail, String password, String name) {
        String safeEmail = userEmail.replace(".", "_");
        int otp = (int) (Math.random() * 900000) + 100000;
        long validTill = System.currentTimeMillis() + (5 * 60 * 1000);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("otp").child(safeEmail);

        HashMap<String, Object> otpData = new HashMap<>();
        otpData.put("otp", otp);
        otpData.put("validTill", validTill);
        otpData.put("email", userEmail);

        ref.setValue(otpData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("OTP", "OTP saved to Firebase: " + otp);

                // ✅ Use callback to check if email was sent
                EmailSender.sendOTPEmail(userEmail, String.valueOf(otp), new EmailSender.EmailCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(SignUp.this, "OTP sent to your email!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(SignUp.this, OTPActivity.class);
                            intent.putExtra("email", userEmail);
                            intent.putExtra("password", password);
                            intent.putExtra("name", name);
                            startActivity(intent);

                            signUpButton.setEnabled(true);
                            signUpButton.setText("Sign Up");
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Log.e("OTP_EMAIL", "Failed: " + error);
                            Toast.makeText(SignUp.this, "Failed to send email: " + error, Toast.LENGTH_LONG).show();
                            signUpButton.setEnabled(true);
                            signUpButton.setText("Sign Up");
                        });
                    }
                });

            } else {
                Log.e("OTP", "Failed to save OTP: " + task.getException());
                Toast.makeText(SignUp.this, "Failed to generate OTP", Toast.LENGTH_SHORT).show();
                signUpButton.setEnabled(true);
                signUpButton.setText("Sign Up");
            }
        });
    }
}
