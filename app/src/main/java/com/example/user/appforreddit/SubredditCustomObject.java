package com.example.user.appforreddit;

/**
 * Created by Akshay on 03-01-2018.
 */

public class SubredditCustomObject {
    private String subredditId, prefixedDisplayName, subredditUrl, subDescription, displaySubreddit;

    //constructor
    public SubredditCustomObject(String Id, String prefixedName, String url, String description, String displayPreferance) {
        subredditId = Id;
        prefixedDisplayName = prefixedName;
        subredditUrl = url;
        subDescription = description;
        displaySubreddit = displayPreferance;
    }

    public String getSubredditId() {
        return subredditId;
    }

    public String getPrefixedDisplayName() {
        return prefixedDisplayName;
    }

    public String getSubredditUrl() {
        return subredditUrl;
    }

    public String getSubDescription() {
        return subDescription;
    }

    public String getDisplaySubreddit() {
        return displaySubreddit;
    }

    public void setDisplaySubreddit(String display) {
        displaySubreddit = display;
    }
}
