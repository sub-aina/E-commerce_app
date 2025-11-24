package com.example.smd_project.customer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.smd_project.R;
import com.example.smd_project.customer.fragments.*;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerActivity extends AppCompatActivity {

    private static final String TAG = "CustomerChat";
    private static final String API_KEY = "AIzaSyAPlEvE6UU4ZLGLztgQOc3wJ9LHjb7t_0w";

    // UI Components
    BottomNavigationView bottomNav;
    FloatingActionButton fabChat;
    CardView chatPanel;
    ImageButton btnCloseChat, sendButton;
    EditText messageInput;
    TextView chatHistoryTextView;
    ScrollView chatScrollView;

    // Gemini AI Components
    private GenerativeModelFutures model;
    private ChatFutures chat;
    private ExecutorService executor;
    private StringBuilder chatHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        // Initialize views
        bottomNav = findViewById(R.id.bottomNavCustomer);
        fabChat = findViewById(R.id.fab_chat);
        chatPanel = findViewById(R.id.chatPanel);
        btnCloseChat = findViewById(R.id.btn_close_chat);
        messageInput = findViewById(R.id.messageInput);
        chatHistoryTextView = findViewById(R.id.chatHistoryTextView);
        chatScrollView = findViewById(R.id.chatScrollView);
        sendButton = findViewById(R.id.sendButton);

        chatHistory = new StringBuilder();
        executor = Executors.newSingleThreadExecutor();

        // Initialize Gemini AI
        initializeGemini();

        // Load initial fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Bottom Navigation
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (itemId == R.id.nav_categories) {
                selected = new SearchFragment();
            } else if (itemId == R.id.nav_cart) {
                selected = new CartFragment();
            } else if (itemId == R.id.nav_profile) {
                selected = new CustomerProfileFragment();
            }

            if (selected != null) {
                loadFragment(selected);
                return true;
            }
            return false;
        });


        fabChat.setOnClickListener(v -> {
            chatPanel.setVisibility(View.VISIBLE);
            fabChat.setVisibility(View.GONE);
        });


        btnCloseChat.setOnClickListener(v -> {
            chatPanel.setVisibility(View.GONE);
            fabChat.setVisibility(View.VISIBLE);
        });


        sendButton.setOnClickListener(v -> sendMessage());

       messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });

        // Initial greeting
        addBotMessage("👋 Hello! I'm your AI Assistant. How can I help you today?");
    }

    private void initializeGemini() {
        try {
            GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", API_KEY);
            model = GenerativeModelFutures.from(gm);
            chat = model.startChat();
            Log.d(TAG, "Gemini AI initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Gemini", e);
            Toast.makeText(this, "Failed to initialize AI: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();

        if (chat == null) {
            Toast.makeText(this, "AI not ready. Please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        addUserMessage(message);

        messageInput.setText("");
        sendButton.setEnabled(false);

        Content userContent = new Content.Builder()
                .addText(message)
                .build();

        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userContent);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String responseText = result.getText();

                runOnUiThread(() -> {
                    if (responseText != null && !responseText.isEmpty()) {
                        addBotMessage(responseText);
                    } else {
                        addBotMessage("I couldn't generate a response. Please try again.");
                    }
                    sendButton.setEnabled(true);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "API call failed", t);

                runOnUiThread(() -> {
                    String errorMsg = "Sorry, I encountered an error: " + t.getMessage();
                    addBotMessage(errorMsg);
                    sendButton.setEnabled(true);
                });
            }
        }, executor);
    }

    private void addUserMessage(String message) {
        chatHistory.append("You: ").append(message).append("\n\n");
        updateChatDisplay();
    }

    private void addBotMessage(String message) {
        chatHistory.append("Assistant: ").append(message).append("\n\n");
        updateChatDisplay();
    }

    private void updateChatDisplay() {
        chatHistoryTextView.setText(chatHistory.toString());
        chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.customerFragmentContainer, fragment)
                .commit();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}