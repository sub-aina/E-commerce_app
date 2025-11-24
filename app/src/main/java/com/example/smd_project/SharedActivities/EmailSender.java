package com.example.smd_project.SharedActivities;

import android.util.Log;
import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EmailSender {
    private static final String TAG = "EmailSender";

    private static final String SENDER_EMAIL = "subaina12345@gmail.com";
    private static final String SENDER_NAME = "E-Commerce App";
    private static final String API_KEY = System.getenv("SENDINBLUE_API_KEY");

    private static final String URL = "https://api.brevo.com/v3/smtp/email";
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public interface EmailCallback {
        void onSuccess();
        void onFailure(String error);
    }

    /**
     * Send OTP verification email
     */
    public static void sendOTPEmail(String toEmail, String otp, EmailCallback callback) {
        String subject = "Your OTP Verification Code";
        String textContent = "Your OTP code is: " + otp + "\n\nThis code is valid for 5 minutes.\n\nIf you didn't request this code, please ignore this email.";
        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>" +
                "  <div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>" +
                "    <h2 style='color: #333; text-align: center;'>Email Verification</h2>" +
                "    <p style='color: #666; font-size: 16px;'>Your OTP verification code is:</p>" +
                "    <div style='background-color: #f0f0f0; padding: 20px; text-align: center; border-radius: 5px; margin: 20px 0;'>" +
                "      <p style='font-size: 32px; font-weight: bold; color: #5B8DEE; margin: 0; letter-spacing: 5px;'>" + otp + "</p>" +
                "    </div>" +
                "    <p style='color: #666; font-size: 14px;'>This code is valid for <strong>5 minutes</strong>.</p>" +
                "    <p style='color: #999; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px;'>If you didn't request this code, please ignore this email.</p>" +
                "  </div>" +
                "</body>" +
                "</html>";

        sendEmail(toEmail, subject, textContent, htmlContent, callback);
    }

    /**
     * Send order status notification email
     */
    public static void sendOrderStatusEmail(String toEmail, String subject, String htmlContent, EmailCallback callback) {
        // Create plain text version from subject
        String textContent = "Your order status has been updated. Please check your account for details.";
        sendEmail(toEmail, subject, textContent, htmlContent, callback);
    }

    /**
     * Generic email sending method
     */
    public static void sendEmail(String toEmail, String subject, String textContent, String htmlContent, EmailCallback callback) {
        try {
            JSONObject json = new JSONObject();

            // Set recipient
            JSONObject toObj = new JSONObject();
            toObj.put("email", toEmail);
            org.json.JSONArray toArray = new org.json.JSONArray();
            toArray.put(toObj);
            json.put("to", toArray);

            // Set sender
            JSONObject senderObj = new JSONObject();
            senderObj.put("email", SENDER_EMAIL);
            senderObj.put("name", SENDER_NAME);
            json.put("sender", senderObj);

            // Set content
            json.put("subject", subject);
            json.put("textContent", textContent);
            json.put("htmlContent", htmlContent);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(URL)
                    .post(body)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .addHeader("api-key", API_KEY)
                    .build();

            Log.d(TAG, "Sending email to: " + toEmail);
            Log.d(TAG, "Subject: " + subject);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Network failure sending email", e);
                    if (callback != null) {
                        callback.onFailure("Network error: " + e.getMessage());
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String resBody = response.body() != null ? response.body().string() : "No response body";

                    if (response.isSuccessful()) {
                        Log.d(TAG, " Email sent successfully! Response: " + resBody);
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else {
                        Log.e(TAG, "❌ Failed to send email. Status: " + response.code());
                        Log.e(TAG, "Response body: " + resBody);

                        String errorMsg = "Email sending failed (Code: " + response.code() + ")";
                        try {
                            JSONObject errorJson = new JSONObject(resBody);
                            if (errorJson.has("message")) {
                                errorMsg = errorJson.getString("message");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Could not parse error response", e);
                        }

                        if (callback != null) {
                            callback.onFailure(errorMsg);
                        }
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON payload", e);
            if (callback != null) {
                callback.onFailure("JSON error: " + e.getMessage());
            }
        }
    }

    // Backward compatibility
    public static void sendOTPEmail(String toEmail, String otp) throws JSONException {
        sendOTPEmail(toEmail, otp, null);
    }
}