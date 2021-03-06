package com.example.user.appforreddit;

/**
 * Created by Akshay on 04-01-2018.
 */

public class ArticleCustomObject {
    private String resourceURL, articleTitle, articleThumbnail, articleId, subredditURL;

    //constructor
    public ArticleCustomObject(String url, String title, String imageThumbnail, String id, String pSubredditURL) {
        resourceURL = url;
        articleTitle = title;
        articleThumbnail = imageThumbnail;
        articleId = id;
        subredditURL = pSubredditURL;
    }

    public String getResourceURL() {
        return resourceURL;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public String getArticleThumbnail() {
        return articleThumbnail;
    }

    public String getArticleId() {
        return articleId;
    }

    public String getSubredditURL() {
        return subredditURL;
    }
}
