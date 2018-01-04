package com.example.user.appforreddit;

/**
 * Created by user on 04-01-2018.
 */

public class articleCustomObject {
    private String resourceURL, articleTitle, articleThumbnail, articleId, subredditURL;

    //constructor
    public articleCustomObject(String url, String title, String imageThumbnail, String id, String pSubredditURL){
        resourceURL = url;
        articleTitle = title;
        articleThumbnail = imageThumbnail;
        articleId = id;
        subredditURL = pSubredditURL;
    }

    public String getResourceURL(){return resourceURL;}
    public String getArticleTitle(){return articleTitle;}
    public String getArticleThumbnail(){return articleThumbnail;}
    public String getArticleId(){return articleId;}
    public String getSubredditURL(){return subredditURL;}
}
