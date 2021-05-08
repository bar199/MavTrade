package com.example.mavtrade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static final String TAG = "ChatAdapter";
    public static final int MESSAGING_INCOMING = 0;
    public static final int MESSAGING_OUTGOING = 1;
    public static final String DATE_FORMAT_1 = "dd/MM/yy";
    public static final String DATE_FORMAT_2 = "h:mm a";
    public static final String DATE_FORMAT_3 = "dd/MM/yy h:mm a";

    private Context context;
    private List<Chat> messages;
    private Post post;

    public ChatAdapter(Context context, Post post, List<Chat> messages) {
        this.context = context;
        this.post = post;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MESSAGING_INCOMING) {

            View view = LayoutInflater.from(context).inflate(R.layout.message_incoming, parent, false);
            return new IncomingMessageViewHolder(view);

        } else if (viewType == MESSAGING_OUTGOING) {

            View view = LayoutInflater.from(context).inflate(R.layout.message_outgoing, parent, false);
            return new OutgoingMessageViewHolder(view);

        } else {
            throw new IllegalArgumentException("Unknown view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {

        Chat message = messages.get(position);
        holder.bindMessage(message);
    }

    @Override
    public int getItemCount() { return messages.size(); }

    // Clean all elements of the recycler
    public void clear() {
        messages.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Chat> messagesList) {
        messages.addAll(messagesList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (isCurrentUser(position) ? MESSAGING_OUTGOING : MESSAGING_INCOMING);
    }

    private boolean isCurrentUser(int position) {

        Chat message = messages.get(position);
        return (message.getFromUser() != null) && (message.getFromUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId()));
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bindMessage(Chat message);
    }

    public class IncomingMessageViewHolder extends ViewHolder {

        private TextView tvOtherName;
        private TextView tvTime;
        private TextView tvBody;

        public IncomingMessageViewHolder(View itemView) {
            super(itemView);
            tvOtherName = itemView.findViewById(R.id.tvOtherName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvBody = itemView.findViewById(R.id.tvBody);
        }

        @Override
        public void bindMessage(Chat message) {
            tvOtherName.setText(message.getFromUser().getUsername());
            tvTime.setText(getFormattedDate(message));
            tvBody.setText(message.getBody());
        }
    }

    public class OutgoingMessageViewHolder extends ViewHolder {

        private TextView tvTime;
        private TextView tvBody;

        public OutgoingMessageViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvBody = itemView.findViewById(R.id.tvBody);
        }

        @Override
        public void bindMessage(Chat message) {
            tvTime.setText(getFormattedDate(message));
            tvBody.setText(message.getBody());
        }
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
