package com.example.mavtrade.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.mavtrade.Chat;
import com.example.mavtrade.ChatAdapter;
import com.example.mavtrade.Inbox;
import com.example.mavtrade.Post;
import com.example.mavtrade.R;
import com.example.mavtrade.UserInfo;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatFragment extends Fragment {

    public static final String TAG = "ChatFragment";
    public static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;

    protected TextView tvChatTitle;
    protected ImageView ivChatUser;
    protected ImageView ivChatPost;

    protected RecyclerView rvChat;
    protected ChatAdapter adapter;
    protected List<Chat> messages;
    protected ParseUser currentUser;
    protected ParseUser otherUser;
    protected ParseUser seller;
    protected ParseUser querier;
    protected Post post;
    protected Chat message;
    protected Inbox conversation;
    protected String data;

    private SwipeRefreshLayout swipeContainer;
    private EditText etMessage;
    private ImageButton btnSend;
    private boolean initialLoad;
    private boolean foundConversation;

    public ChatFragment(Post post, ParseUser currentUser, ParseUser otherUser) {
        this.post = post;
        this.currentUser = currentUser;
        this.otherUser = otherUser;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvChatTitle = getView().findViewById(R.id.tvChatTitle);
        ivChatPost = getView().findViewById(R.id.ivChatPost);
        ivChatUser = getView().findViewById(R.id.ivChatUser);

        swipeContainer = getView().findViewById(R.id.scMessages);
        rvChat = getView().findViewById(R.id.rvChat);

        etMessage = getView().findViewById(R.id.etMessage);
        btnSend = getView().findViewById(R.id.btnSend);
        initialLoad = true;

        swipeContainer.setOnRefreshListener(() -> {
            Log.i(TAG, "Fetching new data!");
            refreshMessages();
        });

        messages = new ArrayList<>();
        adapter = new ChatAdapter(getContext(), post, messages);
        rvChat.setAdapter(adapter);

        queryConversation();

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        // linearLayoutManager.setReverseLayout(true) // Makes messages appear at bottom
        rvChat.setLayoutManager(linearLayoutManager);

        btnSend.setOnClickListener(v -> {
            data = etMessage.getText().toString();

            if(!data.trim().isEmpty()) {
                saveConversation();
            }
        });
    }

    private void queryConversation() {

        if (post.getUser().getObjectId().equals(currentUser.getObjectId())) { // Post author is message sender

            seller = currentUser;
            querier = otherUser;

        } else { // Post author is message receiver

            seller = otherUser;
            querier = currentUser;

        }

        // Set the Chat information header
        tvChatTitle.setText(post.getTitle());

        ParseFile postImage = post.getImage();
        if (postImage != null) {
            Glide.with(getContext()).load(postImage.getUrl())
                    .circleCrop()
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .into(ivChatPost);
        }

        queryProfileImage();

        // Query to check if user has already has an existing conversation
        ParseQuery<Inbox> findConversationQuery = ParseQuery.getQuery(Inbox.class);
        findConversationQuery.include(Inbox.KEY_POST);
        findConversationQuery.include(Inbox.KEY_SELLER);
        findConversationQuery.include(Inbox.KEY_QUERIER);
        findConversationQuery.setLimit(1);

        findConversationQuery.whereEqualTo(Inbox.KEY_POST, post);
        findConversationQuery.whereEqualTo(Inbox.KEY_SELLER, seller);
        findConversationQuery.whereEqualTo(Inbox.KEY_QUERIER, querier);

        // Check if conversation already exists
        findConversationQuery.findInBackground((queriedConversation, e) -> {
            if (queriedConversation.size() == 1) {
                // Conversation already exists
                conversation = queriedConversation.get(0);
                foundConversation = true;
                queryMessages();

            } else {
                // Conversation does not exist, so create new conversation
                if (queriedConversation.size() == 0) {
                    foundConversation = false;

                } else {
                    Log.e(TAG, "Issue with searching Inbox object: ", e);
                }
            }
        });
    }

    private void queryMessages() {
        ParseQuery<Chat> findMessagesQuery = ParseQuery.getQuery(Chat.class);
        findMessagesQuery.include(Chat.KEY_FROM_USER);
        findMessagesQuery.include(Chat.KEY_TO_USER);
        findMessagesQuery.include(Chat.KEY_CONVERSATION);

        findMessagesQuery.whereEqualTo(Chat.KEY_CONVERSATION, conversation);
        findMessagesQuery.addAscendingOrder(Chat.KEY_CREATED_AT);
        findMessagesQuery.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);

        findMessagesQuery.findInBackground(new FindCallback<Chat>() {
            @Override
            public void done(List<Chat> queriedMessages, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting chat messages ", e);
                    return;
                }

                for (Chat message : queriedMessages) {
                    Log.i(TAG, "Message: " + message.getBody() + ", From: " + message.getFromUser().getUsername());
                }

                adapter.clear();
                adapter.addAll(queriedMessages);
                // Now we call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }
        });
    }

    public void queryProfileImage() {
        ParseQuery<UserInfo> queryProfileImage = ParseQuery.getQuery(UserInfo.class);
        queryProfileImage.include(UserInfo.KEY_USER);
        queryProfileImage.whereEqualTo(UserInfo.KEY_USER, otherUser);

        queryProfileImage.getFirstInBackground(new GetCallback<UserInfo>() {
            @Override
            public void done(UserInfo userProfile, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with profile image of other user " + otherUser.getObjectId(), e);
                }

                ParseFile profileImage = userProfile.getProfileImage();
                if (profileImage != null) {
                    Glide.with(getContext()).load(profileImage.getUrl())
                            .circleCrop()
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .into(ivChatUser);
                }
            }
        });
    }

    private void saveConversation() {
        // New Inbox item
        if (!foundConversation) {

            conversation = new Inbox();

            conversation.setPost(post);
            conversation.setSeller(seller);
            conversation.setQuerier(querier);

            conversation.saveInBackground(e -> {
                if (e != null) {
                    Log.e(TAG, "Create error in saveConversation function, Error: " + e.getCode(), e);
                }

                Log.i(TAG, "Added conversation in Inbox database");
            });
        }

        saveChat();
    }

    private void saveChat() {

        //Save message in Chat class
        message = new Chat();

        message.setFromUser(currentUser);
        message.setToUser(otherUser);
        message.setBody(data);
        message.setConversation(conversation);

        message.saveInBackground(ex -> {
            if (ex != null) {
                Log.e(TAG, "Save error in saveChat function, Code: " + ex.getCode(), ex);
            }

            etMessage.setText("");
            Log.i(TAG, "Added message to Chat database");
            refreshMessages();
        });
    }

    // Query messages from Parse so we can load them into the chat adapter
    private void refreshMessages() {
        ParseQuery<Chat> query = ParseQuery.getQuery(Chat.class);
        query.include(Chat.KEY_FROM_USER);
        query.include(Chat.KEY_TO_USER);
        query.include(Chat.KEY_CONVERSATION);

        query.whereEqualTo(Chat.KEY_CONVERSATION, conversation);
        query.addAscendingOrder(Chat.KEY_CREATED_AT);
        query.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);

        // Execute query to fetch all messages from Parse asynchronously
        // This is equivalent to a SELECT query with SQL
        query.findInBackground((refreshMessages, e) -> {
            if (e != null) {
                Log.e(TAG, "Error Loading Messages: ", e);
                return;
            }

            adapter.clear();
            adapter.addAll(refreshMessages);
            // Now we call setRefreshing(false) to signal refresh has finished
            swipeContainer.setRefreshing(false);

            // Scroll to the bottom of the list on initial load
            if (initialLoad) {
                rvChat.scrollToPosition(0);
                initialLoad = false;
            }
        });
    }
}