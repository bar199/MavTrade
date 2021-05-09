package com.example.mavtrade.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.target.Target;
import com.example.mavtrade.Following;
import com.example.mavtrade.FollowingAdapter;
import com.example.mavtrade.ItemClickSupport;
import com.example.mavtrade.Post;
import com.example.mavtrade.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class FollowingFragment extends Fragment {

    public static final String TAG = "FollowingAdapter";
    protected RecyclerView rvFollowing;
    protected FollowingAdapter adapter;
    protected List<Following> followedPosts;

    public FollowingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_following, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFollowing = view.findViewById(R.id.rvFollowing);

        followedPosts = new ArrayList<>();
        adapter = new FollowingAdapter(getContext(), followedPosts);
        rvFollowing.setAdapter(adapter);
        rvFollowing.setLayoutManager(new LinearLayoutManager(getContext()));

        queryFollowing();

        // Leveraging ItemClickSupport decorator to handle clicks on items in our recyclerView
        ItemClickSupport.addTo(rvFollowing).setOnItemClickListener((recyclerView, position, v) -> {
            Following followedPost = followedPosts.get(position);

            Fragment fragment = null;
            fragment = new DetailsFragment(followedPost.getPost().getObjectId());

            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.flContainer, fragment)
                    .addToBackStack("Open Detail Fragment").commit();
        });
    }

    private void queryFollowing() {
        ParseQuery<Following> query = ParseQuery.getQuery(Following.class);
        query.include(Following.KEY_POST);
        query.include(Following.KEY_USER);

        query.whereEqualTo(Following.KEY_USER, ParseUser.getCurrentUser());
        query.addDescendingOrder(Post.KEY_CREATED_AT);

        query.findInBackground(new FindCallback<Following>() {
            @Override
            public void done(List<Following> followings, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "queryFollowing couldn't get followed posts", e);
                    return;
                }

                for (Following following : followings) {
                    Log.i(TAG, "Post: " + following.getPost().getTitle());
                }

                adapter.clear();
                adapter.addAll(followings);
            }
        });
    }
}