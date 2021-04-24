package com.example.mavtrade;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_USER = "user";
    public static final String KEY_TITLE = "title";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_PRICE = "price";
    public static final String KEY_CREATED_AT = "createdAt";

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser parseUser) {
        put(KEY_USER, parseUser);
    }

    public String getTitle() {
        return getString(KEY_TITLE);
    }

    public void setTitle(String title) {
        put(KEY_TITLE, title);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile parseFile) {
        put(KEY_IMAGE, parseFile);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public Number getPrice() {
        return getNumber(KEY_PRICE);
    }

    public void setPrice(Number price) {
        put(KEY_PRICE, price);
    }

    public Date getCreatedAt() {
        return getDate(KEY_CREATED_AT);
    }
}
