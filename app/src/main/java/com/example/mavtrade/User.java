package com.example.mavtrade;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseFile;

@ParseClassName("User")
public class User extends ParseObject {
    public static final String KEY_USERNAME = "username";
    public static final String KEY_IMAGE = "image";

    public String getUsername() {
        return getString(KEY_USERNAME);
    }

    public void setUsername(String username) {
        put(KEY_USERNAME, username);
    }
}
