package com.example.mavtrade.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mavtrade.Post;
import com.example.mavtrade.PostsAdapter;
import com.example.mavtrade.ItemClickSupport;
import com.example.mavtrade.R;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    protected RecyclerView rvPosts;
    protected PostsAdapter adapter;
    protected List<Post> allPosts;
    SwipeRefreshLayout swipeContainer;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvPosts = view.findViewById(R.id.rvPosts);
        swipeContainer = view.findViewById(R.id.scPosts);

        swipeContainer.setOnRefreshListener(() -> {
            Log.i(TAG, "Fetching new data!");
            queryPosts();
        });

        // Steps to use recycler view:
        // 0. Create layout for one row in the list
        // 1. Create the adapter
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts);
        // 2. Create the data source
        // 3. Set the adapter on the recycler view
        rvPosts.setAdapter(adapter);
        // 4. Set the layout manager on the recycler view
        rvPosts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        queryPosts();

        // Leveraging ItemClickSupport decorator to handle clicks on items in our recyclerView
        ItemClickSupport.addTo(rvPosts).setOnItemClickListener((recyclerView, position, v) -> {
            Post post = allPosts.get(position);

            Fragment fragment = null;
            fragment = new DetailsFragment(post.getObjectId());

            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.flContainer, fragment)
                                .addToBackStack("Open Detail Fragment").commit();
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            adapter.notifyDataSetChanged();
        }
    }

    protected void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.setLimit(20);
        query.addDescendingOrder(Post.KEY_CREATED_AT);

        query.findInBackground((List<Post> posts, ParseException e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting posts", e);
                return;
            }

            for (Post post : posts) {
                String username = post.getUser().getUsername();
                Log.i(TAG, "ObjectId: " + post.getObjectId() + ", Post: " + post.getTitle() + ", username: " + post.getUser().getUsername());
            }

            adapter.clear();
            adapter.addAll(posts);
            // Now we call setRefreshing(false) to signal refresh has finished
            swipeContainer.setRefreshing(false);
        });
    }
}