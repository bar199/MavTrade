package com.example.mavtrade;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Chat")
public class Chat extends ParseObject {

    public static final String KEY_CONVERSATION = "conversation";
    public static final String KEY_FROM_USER = "fromUser";
    public static final String KEY_TO_USER = "toUser";
    public static final String KEY_BODY = "body";

    public Chat()
    {
        super();
    }

    public Inbox getConversation() { return (Inbox) getParseObject(KEY_CONVERSATION); }

    public void setConversation(Inbox conversation) { put(KEY_CONVERSATION, conversation); }

    public ParseUser getFromUser() { return getParseUser(KEY_FROM_USER); }

    public void setFromUser(ParseUser parseUser) {
        put(KEY_FROM_USER, parseUser);
    }

    public ParseUser getToUser() {
        return getParseUser(KEY_TO_USER);
    }

    public void setToUser(ParseUser parseUser) {
        put(KEY_TO_USER, parseUser);
    }

    public String getBody() {
        return getString(KEY_BODY);
    }

    public void setBody(String data) {
        put(KEY_BODY, data);
    }
}
