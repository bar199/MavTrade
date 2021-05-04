package com.example.mavtrade.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.example.mavtrade.Post;
import com.example.mavtrade.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DetailsFragment extends Fragment {

    public static final String TAG = "DetailsFragment";
    public static final String DATE_FORMAT_1 = "dd-MMM-yy";
    public static final String DATE_FORMAT_2 = "h:mm a";

    private String objectId;
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

        queryPost(objectId);
    }

    protected void queryPost(String objectId) {
        ParseQuery<Post> query = ParseQuery.getQuery("Post");
        query.include(Post.KEY_USER);

        query.getInBackground(objectId, new GetCallback<Post>() {
            public void done(Post post, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting post with objectId " + objectId, e);
                    return;
                }

                tvDetailsTitle.setText(post.getTitle());

                // Format price
                int dollars = (int) post.getPrice() / 100;
                int cents = (int) post.getPrice() % 100;

                if (cents < 10) {
                    tvPrice.setText("$" + dollars + ".0" + cents);
                } else {
                    tvPrice.setText("$" + dollars + "." + cents);
                }

                tvAuthor.setText(post.getUser().getUsername());
                tvDate.setText(Html.fromHtml(getFormattedDate(post)));

                ParseFile image = post.getImage();
                if (image != null) {
                    try {
                        Glide.with(requireContext()).load(image.getUrl()).into(ivDetailsImage);
                    } catch (Exception ex) {
                        Log.e(TAG, "Issue with loading post image: ", ex);
                    }
                }

                tvDetailsDescription.setText(post.getDescription());
            }
        });
    }

    private String getFormattedDate(Post post) {
        String date;

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