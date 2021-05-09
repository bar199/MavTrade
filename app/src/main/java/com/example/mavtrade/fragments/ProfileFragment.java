package com.example.mavtrade.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.mavtrade.Post;
import com.example.mavtrade.ProfileAdapter;
import com.example.mavtrade.R;
import com.example.mavtrade.UserInfo;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";
    private RecyclerView rvProfile;
    private ProfileAdapter adapter;
    private List<Post> allPosts;

    private ImageView ivProfile;
    private TextView tvProfile;
    private TextView tvNumPosts;
    private Button btnCompose;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvProfile = view.findViewById(R.id.rvProfile);
        ivProfile = view.findViewById(R.id.ivProfile);
        tvProfile = view.findViewById(R.id.tvProfile);
        tvNumPosts = view.findViewById(R.id.tvNumPosts);
        btnCompose = view.findViewById(R.id.btnCompose);

        // Steps to use recycler view:
        // 0. Create layout for one row in the list
        // 1. Create the adapter
        allPosts = new ArrayList<>();
        adapter = new ProfileAdapter(getContext(), allPosts);
        // 2. Create the data source
        // 3. Set the adapter on the recycler view
        rvProfile.setAdapter(adapter);
        // 4. Set the layout manager on the recycler view
        rvProfile.setLayoutManager(new GridLayoutManager(getContext(), 3));

        tvProfile.setText(ParseUser.getCurrentUser().getUsername());

        queryUserImage();
        queryPosts();

        btnCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = null;
                fragment = new ComposeFragment();

                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.flContainer, fragment)
                        .addToBackStack("Open Compose Fragment").commit();
            }
        });
    }

    public void queryUserImage() {
        ParseQuery<UserInfo> query = ParseQuery.getQuery(UserInfo.class);
        query.include(UserInfo.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());

        query.getFirstInBackground(new GetCallback<UserInfo>() {
            @Override
            public void done(UserInfo userProfile, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with profile image", e);
                    return;
                }

                ParseFile profileImage = userProfile.getProfileImage();
                if (profileImage != null) {
                    Glide.with(getContext()).load(profileImage.getUrl())
                            .circleCrop()
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .into(ivProfile);
                }
            }
        });
    }

    public void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        query.setLimit(20);
        query.addDescendingOrder(Post.KEY_CREATED_AT);

        query.findInBackground((posts, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting posts", e);
                return;
            }

            for (Post post : posts) {
                Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getUser().getUsername());
            }

            adapter.clear();
            adapter.addAll(posts);

            tvNumPosts.setText(adapter.getItemCount() + " Posts");
            adapter.notifyDataSetChanged();
        });
    }
}