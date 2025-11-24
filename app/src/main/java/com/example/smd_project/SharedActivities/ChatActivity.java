package com.example.smd_project.SharedActivities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smd_project.R;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "GeminiChat";
    private static final String API_KEY = "AIzaSyAPlEvE6UU4ZLGLztgQOc3wJ9LHjb7t_0w";

    private TextView chatHistoryTextView;
    private EditText messageInput;
    private Button sendButton;
    private ImageButton btnBack;

    private GenerativeModelFutures model;
    private ChatFutures chat;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        initializeGemini();
        setupClickListeners();
    }

    private void initViews() {
        chatHistoryTextView = findViewById(R.id.chatHistoryTextView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        btnBack = findViewById(R.id.btnBack);

        executor = Executors.newSingleThreadExecutor();
    }

    private void initializeGemini() {
        try {
            GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", API_KEY);
            model = GenerativeModelFutures.from(gm);
            chat = model.startChat();

            chatHistoryTextView.append("AI Assistant ready! How can I help you?\n\n");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Gemini", e);
            Toast.makeText(this, "Failed to initialize: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        if (chat == null) {
            Toast.makeText(this, "Chat not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show user message
        chatHistoryTextView.append("You: " + message + "\n\n");
        messageInput.setText("");
        sendButton.setEnabled(false);
        sendButton.setText("Thinking...");

        // Create content from user message
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
                        chatHistoryTextView.append("AI: " + responseText + "\n\n");
                    } else {
                        chatHistoryTextView.append("AI: (No response)\n\n");
                    }
                    sendButton.setEnabled(true);
                    sendButton.setText("Send");

                    // Auto-scroll to bottom
                    scrollToBottom();
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "API call failed", t);

                runOnUiThread(() -> {
                    chatHistoryTextView.append("Error: " + t.getMessage() + "\n\n");
                    sendButton.setEnabled(true);
                    sendButton.setText("Send");
                    Toast.makeText(ChatActivity.this, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }, executor);
    }

    private void scrollToBottom() {
        chatHistoryTextView.post(() -> {
            int scrollAmount = chatHistoryTextView.getLayout().getLineTop(chatHistoryTextView.getLineCount())
                    - chatHistoryTextView.getHeight();
            if (scrollAmount > 0) {
                chatHistoryTextView.scrollTo(0, scrollAmount);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}