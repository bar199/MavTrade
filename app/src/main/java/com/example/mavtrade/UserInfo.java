package com.example.mavtrade;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseFile;
import com.parse.ParseUser;

@ParseClassName("UserInfo")
public class UserInfo extends ParseObject {
    public static final String KEY_PROFILE_IMAGE = "profileImage";
    public static final String KEY_USER = "user";

    public ParseFile getProfileImage() { return getParseFile(KEY_PROFILE_IMAGE); }

    public void setProfileImage(ParseFile parseFile) {
        put(KEY_PROFILE_IMAGE, parseFile);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser parseUser) {
        put(KEY_USER, parseUser);
    }
}
