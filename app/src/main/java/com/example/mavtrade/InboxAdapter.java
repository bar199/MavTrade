package com.example.mavtrade;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    public static final String TAG = "InboxAdapter";
    public static final String DATE_FORMAT_1 = "dd/MM/yy";
    public static final String DATE_FORMAT_2 = "h:mm a";
    public static final String DATE_FORMAT_3 = "dd/MM/yy h:mm a";
    private Context context;
    private List<Inbox> conversations;

    public InboxAdapter(Context context, List<Inbox> conversations) {
        this.context = context;
        this.conversations = conversations;
    }

    @NonNull
    @Override
    public InboxAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.inbox_chat, parent, false);
        return new InboxAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxAdapter.ViewHolder holder, int position) {
        Inbox conversation = conversations.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        conversations.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Inbox> conversationList) {
        conversations.addAll(conversationList);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivInboxPost;
        private ImageView ivInboxUser;
        private TextView tvInboxPost;
        private TextView tvInboxUser;
        private TextView tvInboxTime;
        private TextView tvInboxMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivInboxPost = itemView.findViewById(R.id.ivInboxPost);
            ivInboxUser = itemView.findViewById(R.id.ivInboxUser);
            tvInboxPost = itemView.findViewById(R.id.tvInboxPost);
            tvInboxTime = itemView.findViewById(R.id.tvInboxTime);
            tvInboxUser = itemView.findViewById(R.id.tvInboxUser);
            tvInboxMessage = itemView.findViewById(R.id.tvInboxMessage);
        }

        public void bind(Inbox conversation) {
            ParseFile postImage = conversation.getPost().getImage();
            if (postImage != null) {
                Glide.with(context).load(postImage.getUrl())
                                    .circleCrop()
                                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                    .into(ivInboxPost);
            }

            // Get other user's profile image
            queryProfileImage(conversation);

            tvInboxPost.setText(conversation.getPost().getTitle());

            //Get latest message and the user who sent it
            queryLatestMessage(conversation);
        }

        public void queryProfileImage(Inbox conversation) {
            ParseUser otherUser;

            if (conversation.getSeller().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                otherUser = conversation.getQuerier();
            } else {
                otherUser = conversation.getSeller();
            }

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
                        Glide.with(context).load(profileImage.getUrl())
                                            .circleCrop()
                                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                            .into(ivInboxUser);
                    }
                }
            });
        }

        public void queryLatestMessage(Inbox conversation) {
            ParseQuery<Chat> queryLatestMessage = ParseQuery.getQuery(Chat.class);
            queryLatestMessage.include(Chat.KEY_FROM_USER);

            queryLatestMessage.whereEqualTo(Chat.KEY_CONVERSATION, conversation);
            queryLatestMessage.addDescendingOrder(Chat.KEY_UPDATED_AT);

            queryLatestMessage.getFirstInBackground(new GetCallback<Chat>() {
                @Override
                public void done(Chat message, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Issue with finding latest message", e);
                    }

                    tvInboxUser.setText(message.getFromUser().getUsername());
                    tvInboxTime.setText(getFormattedDate(message));
                    tvInboxMessage.setText(message.getBody());

                    Log.i(TAG, "Post: " + tvInboxPost.getText()
                            + ", Sender: " + tvInboxUser.getText()
                            + ", Latest message: " + tvInboxMessage.getText());
                }
            });
        }

        private String getFormattedDate(Chat message) {

            Date now = Calendar.getInstance().getTime();
            Date messageDate = message.getCreatedAt();

            String today = formatDate(DATE_FORMAT_1, now);
            String messageDay = formatDate(DATE_FORMAT_1, messageDate);

            //Check if today's date is the same as post's date
            return today.equals(messageDay) ? (formatDate(DATE_FORMAT_2, messageDate))
                    : (formatDate(DATE_FORMAT_3, messageDate));
        }

        private String formatDate(String format, Date date) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat.format(date);
        }
    }

}
