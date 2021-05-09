package com.example.mavtrade.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.example.mavtrade.Following;
import com.example.mavtrade.Post;
import com.example.mavtrade.R;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DetailsFragment extends Fragment {

    public static final String TAG = "DetailsFragment";
    public static final String DATE_FORMAT_1 = "dd-MMM-yy";
    public static final String DATE_FORMAT_2 = "h:mm a";

    private Post post;
    private String objectId;
    private Following postFollowing;
    private TextView tvDetailsTitle;
    private TextView tvPrice;
    private TextView tvAuthor;
    private TextView tvDate;
    private TextView tvDetailsDescription;
    private ImageView ivDetailsImage;
    private ToggleButton tbtnFollow;
    private Button btnMessage;

    public DetailsFragment(String objectId) {
        this.objectId = objectId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDetailsTitle = getView().findViewById(R.id.tvDetailsTitle);
        tvPrice = getView().findViewById(R.id.tvPrice);
        tvAuthor = getView().findViewById(R.id.tvAuthor);
        tvDate = getView().findViewById(R.id.tvDate);
        tvDetailsDescription = getView().findViewById(R.id.tvDetailsDescription);
        ivDetailsImage = getView().findViewById(R.id.ivDetailsImage);
        tbtnFollow = getView().findViewById(R.id.tbtnFollow);
        btnMessage = getView().findViewById(R.id.btnMessage);

        queryPost();

        // Set FOLLOW/UNFOLLOW button functionality
        tbtnFollow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    saveFollowing();
                } else {
                    deleteFollowing();
                }
            }
        });


        // Set MESSAGE button functionality
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = null;
                fragment = new ChatFragment(post, ParseUser.getCurrentUser(), post.getUser());

                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.flContainer, fragment)
                        .addToBackStack("Open Chat Fragment from Inbox").commit();
            }
        });
    }

    protected void queryPost() {
        ParseQuery<Post> query = ParseQuery.getQuery("Post");
        query.include(Post.KEY_USER);

        query.getInBackground(objectId, (object, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting post with objectId " + objectId, e);
                return;
            }

            post = object;
            tvDetailsTitle.setText(object.getTitle());

            // Format price
            int dollars = (int) object.getPrice() / 100;
            int cents = (int) object.getPrice() % 100;

            if (cents < 10) {
                tvPrice.setText("$" + dollars + ".0" + cents);
            } else {
                tvPrice.setText("$" + dollars + "." + cents);
            }

            tvAuthor.setText(object.getUser().getUsername());
            tvDate.setText(Html.fromHtml(getFormattedDate(object)));

            ParseFile image = object.getImage();
            if (image != null) {
                try {
                    Glide.with(requireContext()).load(image.getUrl()).into(ivDetailsImage);
                } catch (Exception ex) {
                    Log.e(TAG, "Issue with loading post image: ", ex);
                }
            }

            tvDetailsDescription.setText(object.getDescription());
            setToggleFollow();
        });
    }

    protected void setToggleFollow() {
        ParseQuery<Following> query = ParseQuery.getQuery(Following.class);
        query.include(Following.KEY_USER);
        query.include(Following.KEY_POST);

        query.whereEqualTo(Following.KEY_USER, ParseUser.getCurrentUser());
        query.whereEqualTo(Following.KEY_POST, post);

        query.findInBackground((following, e) -> {
            if (following.size() == 0) {
                tbtnFollow.setChecked(false);

            } else if (following.size() == 1) { // User already following post
                tbtnFollow.setChecked(true);
            }
            else {
                Log.e(TAG, "setToggleFollow couldn't initialize the toggle button", e);
            }
        });
    }

    protected void saveFollowing() {
        ParseQuery<Post> query = ParseQuery.getQuery("Post");
        query.include(Post.KEY_USER);

        query.getInBackground(objectId, new GetCallback<Post>() {
            @Override
            public void done(Post object, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "queryFollowing couldn't get Following post");
                }

                postFollowing = new Following();
                postFollowing.setPost(object);
                postFollowing.setUser(ParseUser.getCurrentUser());

                postFollowing.saveInBackground(ex -> {

                    if (ex != null) {
                        Log.e(TAG, "Function saveFollowing could not save following", ex);
                    } else {
                        Log.i(TAG, "Following object successfully saved!");
                    }
                });
            }
        });
    }

    protected void deleteFollowing() {
        ParseQuery<Following> query = ParseQuery.getQuery(Following.class);
        query.include(Following.KEY_USER);
        query.include(Following.KEY_POST);

        query.whereEqualTo(Following.KEY_USER, ParseUser.getCurrentUser());
        query.whereEqualTo(Following.KEY_POST, post);

        query.findInBackground((following, e) -> {
            if (following.size() == 1) {
                // Delete the following
                postFollowing = following.get(0);
                postFollowing.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException ex) {
                        if (ex != null) {
                            Log.e(TAG, "Issue with deleting Following object: ", ex);
                        } else {
                            Log.i(TAG, "Following successfully deleted!");
                        }
                    }
                });

            } else {
                Log.e(TAG, "Issue with searching Following object: ", e);
            }
        });
    }

    private String getFormattedDate(Post post) {

        Date now = Calendar.getInstance().getTime();
        Date postDate = post.getCreatedAt();

        String today = formatDate(DATE_FORMAT_1, now);
        String postDay = formatDate(DATE_FORMAT_1, postDate);

        //Check if today's date is the same as post's date
        return today.equals(postDay) ? ("at <b>" + formatDate(DATE_FORMAT_2, postDate) + "</b>")
                                     : ("on <b>" + postDay + "</b>");
    }

    private String formatDate(String format, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }
}