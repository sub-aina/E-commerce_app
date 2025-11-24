package com.example.smd_project.SharedActivities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.SharedActivities.EmailSender;
import com.example.smd_project.customer.CustomerActivity;
import com.example.smd_project.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;

import java.util.HashMap;

public class OTPActivity extends AppCompatActivity {

    private EditText[] otpFields;
    private MaterialButton btnConfirm;
    private TextView tvResend;
    private String email, password, username;
    private CountDownTimer resendTimer;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        auth = FirebaseAuth.getInstance();

        // Get data from previous screen
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        username = getIntent().getStringExtra("name");

        Log.d("OTP_ACTIVITY", "Email: " + email + ", Username: " + username);

        // UI init
        init();

        // Auto move focus between OTP fields
        setupOtpFieldsAutoFocus();

        // Confirm OTP button click
        btnConfirm.setOnClickListener(v -> {
            String otpCode = collectCode();

            if (otpCode.length() != 6) {
                Toast.makeText(this, "Please enter the complete 6-digit code", Toast.LENGTH_SHORT).show();
                return;
            }

            btnConfirm.setEnabled(false);
            btnConfirm.setText("Verifying...");

            verifyOtpCode(email, otpCode);
        });

        // Resend OTP button click
        tvResend.setOnClickListener(v -> {
            if (tvResend.isEnabled()) {
                resendOtp();
            }
        });

        // Start countdown timer for resend
        startResendTimer();
    }

    private void setupOtpFieldsAutoFocus() {
        for (int i = 0; i < otpFields.length - 1; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1) {
                        otpFields[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        // Handle backspace to move to previous field
        for (int i = 1; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0) {
                        otpFields[index - 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void startResendTimer() {
        resendTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvResend.setText("Resend in 00:" + String.format("%02d", millisUntilFinished / 1000));
                tvResend.setEnabled(false);
                tvResend.setTextColor(ContextCompat.getColor(OTPActivity.this, R.color.gray));
            }

            public void onFinish() {
                tvResend.setText("Resend code");
                tvResend.setEnabled(true);
                tvResend.setTextColor(ContextCompat.getColor(OTPActivity.this, R.color.purple_500));
            }
        }.start();
    }

    private void resendOtp() {
        Log.d("OTP_RESEND", "Resending OTP to: " + email);
        String safeEmail = email.replace(".", "_");

        // Generate 6-digit OTP
        int otp = (int) (Math.random() * 900000) + 100000;

        // Valid for 5 minutes
        long validTill = System.currentTimeMillis() + (5 * 60 * 1000);

        // Firebase reference
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("otp").child(safeEmail);

        // Make object to store
        HashMap<String, Object> otpData = new HashMap<>();
        otpData.put("otp", otp);
        otpData.put("validTill", validTill);
        otpData.put("email", email);

        // Write to Firebase
        ref.setValue(otpData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("OTP", "OTP saved successfully");
                Toast.makeText(this, "OTP resent to your email", Toast.LENGTH_SHORT).show();
                startResendTimer();
            } else {
                Log.e("OTP", "Failed to save OTP: " + task.getException());
                Toast.makeText(this, "Failed to resend OTP", Toast.LENGTH_SHORT).show();
            }
        });

        try {
            EmailSender.sendOTPEmail(email, String.valueOf(otp));
        } catch (JSONException e) {
            Log.e("OTP_RESEND", "Error sending email", e);
            Toast.makeText(this, "Failed to send email", Toast.LENGTH_SHORT).show();
        }
    }

    private String collectCode() {
        StringBuilder otp = new StringBuilder();
        for (EditText field : otpFields) {
            String text = field.getText() != null ? field.getText().toString().trim() : "";
            otp.append(text);
        }
        return otp.toString();
    }

    public void verifyOtpCode(String email, String token) {
        Log.d("OTP_VERIFY", "Verifying OTP for email: " + email + ", token: " + token);

        String safeEmail = email.replace(".", "_");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("otp").child(safeEmail);

        ref.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("OTP_VERIFY", "Failed to read OTP node", task.getException());
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Confirm");
                Toast.makeText(this, "Error verifying OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!task.getResult().exists()) {
                Log.e("OTP_VERIFY", "No OTP record found for this email");
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Confirm");
                Toast.makeText(this, "Invalid or expired OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            // Read as numbers
            Long otpCodeValue = task.getResult().child("otp").getValue(Long.class);
            Long validTillValue = task.getResult().child("validTill").getValue(Long.class);

            Log.d("OTP_VERIFY", "DB OTP: " + otpCodeValue + ", validTill: " + validTillValue);

            if (otpCodeValue == null || validTillValue == null) {
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Confirm");
                Toast.makeText(this, "Invalid or expired OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            String otpCodeStr = String.valueOf(otpCodeValue);
            long now = System.currentTimeMillis();

            if (otpCodeStr.equals(token) && now < validTillValue) {
                // OTP is valid - create user
                createUserAccount();
            } else {
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Confirm");
                Toast.makeText(this, "Wrong or expired OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUserAccount() {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        String userId = firebaseUser.getUid();

                        // Create User object
                        User user = new User(userId, username, email, "", "customer");
                        user.setCreatedAt(System.currentTimeMillis());
                        user.setWalletBalance(0.0);

                        // Save to database
                        FirebaseHelper.getInstance().createUser(userId, user, new FirebaseHelper.OnCompleteListener() {
                            @Override
                            public void onSuccess() {
                                // Delete OTP after successful signup
                                String safeEmail = email.replace(".", "_");
                                FirebaseDatabase.getInstance().getReference("otp")
                                        .child(safeEmail).removeValue();

                                Toast.makeText(OTPActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                updateUI(firebaseUser);
                            }

                            @Override
                            public void onFailure(String error) {
                                btnConfirm.setEnabled(true);
                                btnConfirm.setText("Confirm");
                                Log.e("OTP_VERIFY", "Failed to save user: " + error);
                                Toast.makeText(OTPActivity.this, "Error creating profile: " + error, Toast.LENGTH_SHORT).show();

                                // Delete auth user if database save fails
                                if (auth.getCurrentUser() != null) {
                                    auth.getCurrentUser().delete();
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("Confirm");
                    Log.e("OTP_VERIFY", "Failed to create user", e);
                    Toast.makeText(this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void updateUI(@Nullable FirebaseUser currentUser) {
        if (currentUser != null) {
            Intent intent = new Intent(this, CustomerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void init() {
        otpFields = new EditText[]{
                findViewById(R.id.etOtp1),
                findViewById(R.id.etOtp2),
                findViewById(R.id.etOtp3),
                findViewById(R.id.etOtp4),
                findViewById(R.id.etOtp5),
                findViewById(R.id.etOtp6)
        };
        btnConfirm = findViewById(R.id.btnConfirm);
        tvResend = findViewById(R.id.tvResend);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}