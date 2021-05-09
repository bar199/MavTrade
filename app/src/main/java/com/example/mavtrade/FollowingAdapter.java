package com.example.mavtrade;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.target.Target;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import java.util.List;

public class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.ViewHolder> {

    public static final String TAG = "FollowingAdapter";
    private Context context;
    private List<Following> followedPosts;

    public FollowingAdapter(Context context, List<Following> posts) {
        this.context = context;
        this.followedPosts = posts;
    }

    @NonNull
    @Override
    public FollowingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.following_post, parent, false);
        return new FollowingAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowingAdapter.ViewHolder holder, int position) {
        Following followedPost = followedPosts.get(position);
        holder.bind(followedPost);
    }

    @Override
    public int getItemCount() {
        return followedPosts.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        followedPosts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Following> followedPostList) {
        followedPosts.addAll(followedPostList);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private Post post;
        private ImageView ivFollowingPost;
        private TextView tvFollowingTitle;
        private TextView tvFollowingUser;
        private TextView tvFollowingDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivFollowingPost = itemView.findViewById(R.id.ivFollowingPost);
            tvFollowingTitle = itemView.findViewById(R.id.tvFollowingTitle);
            tvFollowingUser = itemView.findViewById(R.id.tvFollowingUser);
            tvFollowingDescription = itemView.findViewById(R.id.tvFollowingDescription);
        }

        public void bind(Following followedPost) {
            post = followedPost.getPost();

            ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
            query.include(Post.KEY_USER);

            query.getInBackground(post.getObjectId(), new GetCallback<Post>() {
                @Override
                public void done(Post object, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "queryPost couldn't find post");
                    }

                    tvFollowingTitle.setText(object.getTitle());
                    tvFollowingUser.setText(object.getUser().getUsername());
                    tvFollowingDescription.setText(object.getDescription());

                    ParseFile followingImage = object.getImage();
                    if (followingImage != null) {
                        Glide.with(context).load(followingImage.getUrl())
                                .transform(new RoundedCorners(30))
                                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                .into(ivFollowingPost);
                    }
                }
            });
        }

    }
}
