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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smd_project.FirebaseHelper;
import com.example.smd_project.R;
import com.example.smd_project.customer.CustomerActivity;
import com.example.smd_project.models.User;
import com.example.smd_project.vendor.activities.VendorActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ForgotPasswordOTPActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordOTP";

    private EditText[] otpFields;
    private MaterialButton btnConfirm;
    private TextView tvResend, tvEmail, tvBack;
    private String email;
    private String userId;
    private CountDownTimer resendTimer;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_otp);

        auth = FirebaseAuth.getInstance();
        email = getIntent().getStringExtra("email");

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Email not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        init();
        setupOtpFieldsAutoFocus();

        tvEmail.setText("Enter the OTP sent to " + email);

        btnConfirm.setOnClickListener(v -> {
            String otpCode = collectCode();

            if (otpCode.length() != 6) {
                Toast.makeText(this, "Please enter the complete 6-digit code", Toast.LENGTH_SHORT).show();
                return;
            }

            btnConfirm.setEnabled(false);
            btnConfirm.setText("Verifying...");
            verifyOtpAndLogin(email, otpCode);
        });

        tvResend.setOnClickListener(v -> {
            if (tvResend.isEnabled()) {
                resendOtp();
            }
        });

        tvBack.setOnClickListener(v -> finish());

        startResendTimer();
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
        tvEmail = findViewById(R.id.tvEmail);
        tvBack = findViewById(R.id.tvBack);
    }

    private void setupOtpFieldsAutoFocus() {
        for (int i = 0; i < otpFields.length - 1; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1) {
                        otpFields[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        for (int i = 1; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0) {
                        otpFields[index - 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void startResendTimer() {
        resendTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvResend.setText("Resend in 00:" + String.format("%02d", millisUntilFinished / 1000));
                tvResend.setEnabled(false);
                tvResend.setTextColor(ContextCompat.getColor(ForgotPasswordOTPActivity.this, R.color.gray));
            }

            public void onFinish() {
                tvResend.setText("Resend code");
                tvResend.setEnabled(true);
                tvResend.setTextColor(ContextCompat.getColor(ForgotPasswordOTPActivity.this, R.color.purple_500));
            }
        }.start();
    }

    private void resendOtp() {
        String safeEmail = email.replace(".", "_");
        int otp = (int) (Math.random() * 900000) + 100000;
        long validTill = System.currentTimeMillis() + (5 * 60 * 1000);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("forgot_password_otp").child(safeEmail);

        HashMap<String, Object> otpData = new HashMap<>();
        otpData.put("otp", otp);
        otpData.put("validTill", validTill);
        otpData.put("email", email);

        ref.setValue(otpData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "OTP saved successfully: " + otp);

                EmailSender.sendOTPEmail(email, String.valueOf(otp), new EmailSender.EmailCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(ForgotPasswordOTPActivity.this, "OTP resent to your email", Toast.LENGTH_SHORT).show();
                            startResendTimer();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(ForgotPasswordOTPActivity.this, "Failed to send email: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } else {
                Toast.makeText(this, "Failed to resend OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String collectCode() {
        StringBuilder otp = new StringBuilder();
        for (EditText field : otpFields) {
            String text = field.getText() != null ? field.getText().toString().trim() : "";
            otp.append(text);
        }
        return otp.toString();
    }

    private void verifyOtpAndLogin(String email, String token) {
        Log.d(TAG, "Verifying OTP for email: " + email);

        String safeEmail = email.replace(".", "_");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("forgot_password_otp").child(safeEmail);

        ref.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Failed to read OTP", task.getException());
                resetButton();
                Toast.makeText(this, "Error verifying OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!task.getResult().exists()) {
                Log.e(TAG, "No OTP found");
                resetButton();
                Toast.makeText(this, "Invalid or expired OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            Long otpCodeValue = task.getResult().child("otp").getValue(Long.class);
            Long validTillValue = task.getResult().child("validTill").getValue(Long.class);

            if (otpCodeValue == null || validTillValue == null) {
                resetButton();
                Toast.makeText(this, "Invalid OTP data", Toast.LENGTH_SHORT).show();
                return;
            }

            String otpCodeStr = String.valueOf(otpCodeValue);
            long now = System.currentTimeMillis();

            if (otpCodeStr.equals(token) && now < validTillValue) {
                // OTP is valid - find user and log them in
                loginUserByEmail(email);

                // Delete OTP after successful verification
                ref.removeValue();
            } else {
                resetButton();
                Toast.makeText(this, "Wrong or expired OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUserByEmail(String email) {
        // Find user by email in database
        FirebaseHelper.getInstance().getAllUsersReference()
                .orderByChild("email")
                .equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                User user = userSnapshot.getValue(User.class);
                                if (user != null) {
                                    userId = userSnapshot.getKey();
                                    Toast.makeText(ForgotPasswordOTPActivity.this,
                                            "Login successful! Welcome " + user.getName(),
                                            Toast.LENGTH_SHORT).show();
                                    navigateBasedOnRole(user);
                                    return;
                                }
                            }
                        }
                        resetButton();
                        Toast.makeText(ForgotPasswordOTPActivity.this,
                                "User not found", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        resetButton();
                        Toast.makeText(ForgotPasswordOTPActivity.this,
                                "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateBasedOnRole(User user) {
        Intent intent;

        if ("admin".equalsIgnoreCase(user.getRole())) {
            intent = new Intent(this, VendorActivity.class);
        } else {
            intent = new Intent(this, CustomerActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void resetButton() {
        btnConfirm.setEnabled(true);
        btnConfirm.setText("Confirm");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}