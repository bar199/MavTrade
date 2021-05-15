package com.example.mavtrade.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mavtrade.Chat;
import com.example.mavtrade.Inbox;
import com.example.mavtrade.InboxAdapter;
import com.example.mavtrade.ItemClickSupport;
import com.example.mavtrade.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InboxFragment extends Fragment {

    public static final String TAG = "InboxFragment";
    protected ParseUser currentUser = ParseUser.getCurrentUser();
    protected RecyclerView rvChats;
    protected TextView tvEmptyView;
    protected InboxAdapter adapter;
    protected List<Inbox> allConversations;
    SwipeRefreshLayout swipeContainer;

    public InboxFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inbox, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvChats = view.findViewById(R.id.rvChats);
        tvEmptyView = view.findViewById(R.id.tvEmptyView);
        swipeContainer = view.findViewById(R.id.scChats);

        swipeContainer.setOnRefreshListener(() -> {
            Log.i(TAG, "Fetching new data!");
            refreshInbox();
        });

        allConversations = new ArrayList<>();
        adapter = new InboxAdapter(getContext(), allConversations);
        rvChats.setAdapter(adapter);
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));

        queryChats();

        // Leveraging ItemClickSupport decorator to handle clicks on items in our recyclerView
        ItemClickSupport.addTo(rvChats).setOnItemClickListener((recyclerView, position, v) -> {
            Inbox conversation = allConversations.get(position);

            ParseUser otherUser;
            if (conversation.getSeller().getObjectId().equals(currentUser.getObjectId())) { // Current user is the seller
                otherUser = conversation.getQuerier();
            } else { // Other user is the seller
                otherUser = conversation.getSeller();
            }

            Fragment fragment = null;
            fragment = new ChatFragment(conversation.getPost(), currentUser, otherUser);

            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.flContainer, fragment)
                                .addToBackStack("Open Chat Fragment from Inbox").commit();
        });
    }

    protected void queryChats() {
        ParseQuery<Inbox> checkSellerQuery = ParseQuery.getQuery(Inbox.class);
        checkSellerQuery.whereEqualTo(Inbox.KEY_SELLER, currentUser);

        ParseQuery<Inbox> checkQuerierQuery = ParseQuery.getQuery(Inbox.class);
        checkQuerierQuery.whereEqualTo(Inbox.KEY_QUERIER, currentUser);

        List<ParseQuery<Inbox>> queries = new ArrayList<>();
        queries.add(checkSellerQuery);
        queries.add(checkQuerierQuery);

        //Find all conversations that involve the current user
        ParseQuery<Inbox> mainQuery = ParseQuery.or(queries);
        mainQuery.include(Inbox.KEY_POST);
        mainQuery.include(Inbox.KEY_SELLER);
        mainQuery.include(Inbox.KEY_QUERIER);

        mainQuery.findInBackground((conversations, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting inbox conversations", e);
                return;
            }

            for (Inbox conversation : conversations) {
                Log.d(TAG, "Post: " + conversation.getPost().getTitle() + ", Seller: "
                                        + conversation.getSeller().getUsername() + ", Querier: "
                                        + conversation.getQuerier().getUsername());
            }

            if (conversations.isEmpty()) {
                rvChats.setVisibility(View.GONE);
                tvEmptyView.setVisibility(View.VISIBLE);
            }
            else {
                rvChats.setVisibility(View.VISIBLE);
                tvEmptyView.setVisibility(View.GONE);
            }

            adapter.clear();
            adapter.addAll(conversations);
            // Now we call setRefreshing(false) to signal refresh has finished
            swipeContainer.setRefreshing(false);
        });
    }

    private void refreshInbox() {
        ParseQuery<Inbox> checkSellerQuery = ParseQuery.getQuery(Inbox.class);
        checkSellerQuery.whereEqualTo(Inbox.KEY_SELLER, currentUser);

        ParseQuery<Inbox> checkQuerierQuery = ParseQuery.getQuery(Inbox.class);
        checkQuerierQuery.whereEqualTo(Inbox.KEY_QUERIER, currentUser);

        List<ParseQuery<Inbox>> queries = new ArrayList<>();
        queries.add(checkSellerQuery);
        queries.add(checkQuerierQuery);

        //Find all conversations that involve the current user
        ParseQuery<Inbox> mainQuery = ParseQuery.or(queries);
        mainQuery.include(Inbox.KEY_POST);
        mainQuery.include(Inbox.KEY_SELLER);
        mainQuery.include(Inbox.KEY_QUERIER);

        mainQuery.findInBackground((conversations, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting inbox conversations", e);
                return;
            }

            Log.d(TAG, "Inbox refresh!");
            for (Inbox conversation : conversations) {
                Log.d(TAG, "Post: " + conversation.getPost().getTitle() + ", Seller: "
                        + conversation.getSeller().getUsername() + ", Querier: "
                        + conversation.getQuerier().getUsername());
            }

            if (conversations.size() == 0) {
                tvEmptyView.setVisibility(View.VISIBLE);
                rvChats.setVisibility(View.GONE);
            }
            else {
                tvEmptyView.setVisibility(View.GONE);
                rvChats.setVisibility(View.VISIBLE);
            }

            adapter.clear();
            adapter.addAll(conversations);
            // Now we call setRefreshing(false) to signal refresh has finished
            swipeContainer.setRefreshing(false);
        });
    }
}