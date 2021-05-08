package com.example.mavtrade;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        // Register your parse models
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(Chat.class);
        ParseObject.registerSubclass(Inbox.class);
        ParseObject.registerSubclass(UserInfo.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("p4XW35eqRzZfkslkuE5xR9eJZkHx6gJvT4Xz1JU3")
                .clientKey("anqec9XnlfgOgFmaBN9IjP7L8grJBqiIYqqavYvU")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}