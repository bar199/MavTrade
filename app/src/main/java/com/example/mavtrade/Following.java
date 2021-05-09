package com.example.mavtrade;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseFile;
import com.parse.ParseUser;

@ParseClassName("Following")
public class Following extends ParseObject {
    public static final String KEY_POST = "following";
    public static final String KEY_USER = "user";

    public Following()
    {
        super();
    }

    public Post getPost() { return (Post) getParseObject(KEY_POST); }

    public void setPost(Post post) { put(KEY_POST, post); }

    public ParseUser getUser() { return getParseUser(KEY_USER); }

    public void setUser(ParseUser parseUser) {
        put(KEY_USER, parseUser);
    }
}
