package com.agriconnect.agri_connect.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.ChatApi;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.Message;
import com.agriconnect.agri_connect.api.model.SendMessageRequest;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_OTHER_USER_NAME = "other_user_name";
    public static final String EXTRA_RECIPIENT_ID = "recipient_id";

    private ImageView btnBack, btnSend;
    private CircleImageView ivAvatar;
    private TextView tvUserName;
    private RecyclerView rvMessages;
    private TextInputEditText etMessage;
    private ProgressBar progressBar;

    private ChatApi chatApi;
    private String conversationId;
    private String otherUserName;
    private String recipientId;
    private String currentUserId;

    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        otherUserName = getIntent().getStringExtra(EXTRA_OTHER_USER_NAME);
        recipientId = getIntent().getStringExtra(EXTRA_RECIPIENT_ID);

        // Get current user ID from TokenManager
        currentUserId = com.agriconnect.agri_connect.api.TokenManager.getInstance(this).getUserId();
        Log.d("ChatActivity", "Current user ID: " + currentUserId);

        chatApi = ApiClient.getInstance(this).getChatApi();

        initViews();
        setupRecyclerView();
        setupListeners();

        if (conversationId != null) {
            loadMessages();
            markAsRead();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        progressBar = findViewById(R.id.progressBar);

        tvUserName.setText(otherUserName != null ? otherUserName : "Trò chuyện");
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(messages, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        progressBar.setVisibility(View.VISIBLE);
        chatApi.getMessages(conversationId).enqueue(new Callback<ApiResponse<List<Message>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Message>>> call,
                    Response<ApiResponse<List<Message>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    messages.clear();
                    messages.addAll(response.body().getData());
                    messageAdapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) {
                        rvMessages.scrollToPosition(messages.size() - 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Message>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ChatActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty())
            return;

        etMessage.setText("");
        SendMessageRequest request = new SendMessageRequest(conversationId, recipientId, content);

        chatApi.sendMessage(request).enqueue(new Callback<ApiResponse<Message>>() {
            @Override
            public void onResponse(Call<ApiResponse<Message>> call, Response<ApiResponse<Message>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Message sentMessage = response.body().getData();
                    if (conversationId == null) {
                        conversationId = sentMessage.getConversationId();
                    }
                    messages.add(sentMessage);
                    messageAdapter.notifyItemInserted(messages.size() - 1);
                    rvMessages.scrollToPosition(messages.size() - 1);
                } else {
                    Toast.makeText(ChatActivity.this, "Không thể gửi tin nhắn", Toast.LENGTH_SHORT).show();
                    etMessage.setText(content); // Restore text on failure
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Message>> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                etMessage.setText(content); // Restore text on failure
            }
        });
    }

    private void markAsRead() {
        if (conversationId == null)
            return;
        chatApi.markAsRead(conversationId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
            }
        });
    }
}
