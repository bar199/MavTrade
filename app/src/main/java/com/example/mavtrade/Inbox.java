package com.example.mavtrade;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseFile;
import com.parse.ParseUser;

import static com.example.mavtrade.Post.KEY_USER;

@ParseClassName("Inbox")
public class Inbox extends ParseObject {
    public static final String KEY_POST = "post";
    public static final String KEY_SELLER = "seller";
    public static final String KEY_QUERIER = "querier";

    public Inbox()
    {
        super();
    }

    public Post getPost() { return (Post) getParseObject(KEY_POST); }

    public void setPost(Post post) { put(KEY_POST, post); }

    public ParseUser getSeller() { return getParseUser(KEY_SELLER); }

    public void setSeller(ParseUser parseUser) {
        put(KEY_SELLER, parseUser);
    }

    public ParseUser getQuerier() {
        return getParseUser(KEY_QUERIER);
    }

    public void setQuerier(ParseUser parseUser) {
        put(KEY_QUERIER, parseUser);
    }
}
