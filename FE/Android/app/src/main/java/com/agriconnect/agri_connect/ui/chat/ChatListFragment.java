package com.agriconnect.agri_connect.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.ChatApi;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.Conversation;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListFragment extends Fragment {

    private RecyclerView rvChats;
    private LinearLayout layoutEmpty;
    private ChatAdapter chatAdapter;
    private ChatApi chatApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API
        if (getContext() != null) {
            chatApi = ApiClient.getInstance(getContext()).getChatApi();
        }

        initViews(view);
        setupRecyclerView();
        loadChats();
    }

    private void initViews(View view) {
        rvChats = view.findViewById(R.id.rvChats);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        chatAdapter.setOnChatClickListener(chat -> {
            // Navigate to ChatActivity
            android.content.Intent intent = new android.content.Intent(getContext(), ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, chat.id);
            intent.putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, chat.userName);
            // Note: recipientId would need to be stored in ChatItem or derived from
            // conversation participants
            startActivity(intent);
        });
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChats.setAdapter(chatAdapter);
    }

    private void loadChats() {
        chatApi.getConversations().enqueue(new Callback<ApiResponse<List<Conversation>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Conversation>>> call,
                    Response<ApiResponse<List<Conversation>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Conversation> conversations = response.body().getData();

                    if (conversations != null && !conversations.isEmpty()) {
                        List<ChatItem> chatItems = new ArrayList<>();
                        for (Conversation conv : conversations) {
                            chatItems.add(new ChatItem(
                                    conv.getId(),
                                    conv.getParticipantName() != null ? conv.getParticipantName() : "Người dùng",
                                    conv.getLastMessageText(),
                                    formatTime(conv.getLastMessageTime()),
                                    conv.getUnreadCount()));
                        }
                        chatAdapter.setData(chatItems);
                        showList();
                    } else {
                        showEmpty();
                    }
                } else {
                    showEmpty();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Conversation>>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải tin nhắn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
                showEmpty();
            }
        });
    }

    private String formatTime(String time) {
        if (time == null)
            return "";
        // Simple format - in real app, use proper date formatting
        if (time.length() > 10) {
            return time.substring(11, 16); // HH:mm
        }
        return time;
    }

    private void showList() {
        layoutEmpty.setVisibility(View.GONE);
        rvChats.setVisibility(View.VISIBLE);
    }

    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvChats.setVisibility(View.GONE);
    }

    public static class ChatItem {
        public String id;
        public String userName;
        public String lastMessage;
        public String time;
        public int unreadCount;

        public ChatItem(String id, String userName, String lastMessage, String time, int unreadCount) {
            this.id = id;
            this.userName = userName;
            this.lastMessage = lastMessage;
            this.time = time;
            this.unreadCount = unreadCount;
        }
    }
}
