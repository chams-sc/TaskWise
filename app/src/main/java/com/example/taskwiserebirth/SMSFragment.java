package com.example.taskwiserebirth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.chatlogs.ChatAdapter;
import com.example.taskwiserebirth.chatlogs.ChatMessage;
import com.example.taskwiserebirth.database.ConversationDbManager;
import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.database.UserDatabaseManager;

import java.util.ArrayList;
import java.util.List;

import io.realm.mongodb.App;
import io.realm.mongodb.User;

public class SMSFragment extends Fragment {
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private RecyclerView recyclerView;
    private ConversationDbManager conversationDbManager;
    private String aiName;
    private UserDatabaseManager userDatabaseManager;
    private TextView chatAiDisplay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chatlogs, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(chatAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    // Scrolling down
                    ((MainActivity) requireActivity()).toggleNavBarVisibility(false, true);
                } else if (dy < 0) {
                    // Scrolling up
                    ((MainActivity) requireActivity()).toggleNavBarVisibility(true, true);
                }
            }
        });

        App app = MongoDbRealmHelper.initializeRealmApp();
        User user = app.currentUser();
        conversationDbManager = new ConversationDbManager(user);
        userDatabaseManager = new UserDatabaseManager(user, requireContext());
        chatAiDisplay = view.findViewById(R.id.chatAiName);

        userDatabaseManager.getUserData(userModel -> {
            aiName = userModel.getAiName();
            chatAiDisplay.setText(aiName);
        });

        loadChatMessages();

        return view;
    }

    private void loadChatMessages() {
        conversationDbManager.getAllUserConversation(new ConversationDbManager.GetAllConversationsCallback() {
            @Override
            public void onSuccess(List<ChatMessage> messages) {
                chatMessages.clear();
                chatMessages.addAll(messages);
                chatAdapter.notifyDataSetChanged();

                if (!chatMessages.isEmpty()) {
                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("ChatActivity", "Failed to load conversations", e);
            }
        });
    }
}
