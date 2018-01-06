package com.example.user.appforreddit;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.example.user.appforreddit.Database.SubredditsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Akshay on 03-01-2018.
 */

public class NetworkUtils {
    private static final String ACCESS_TOKEN_URL =
            "https://www.reddit.com/api/v1/access_token";
    private static final String REDIRECT_URI =
            "http://www.example.com/my_redirect";
    private static final String CLIENT_ID = "2XIKITvqkmX-yg";
    private static String ACCESS_SUBREDDITS_URL = "https://oauth.reddit.com/subreddits/mine/subscriber";
    private static String GET_ARTICLES_BASE_URL = "https://oauth.reddit.com";
    private static String TAG = "RedditNetworkUtils";
    private static boolean obtainedToken = false;
    private static final String STRING_SHOW = "show";

    public static boolean getAccessToken(String code, final Context context) {
        obtainedToken = false;
        final SharedPreferences pref = context.getSharedPreferences("AppPref", 0);
        OkHttpClient client = new OkHttpClient();
        String authString = CLIENT_ID + ":";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(),
                Base64.NO_WRAP);

        Request request = new Request.Builder()
                .addHeader("User-Agent", "Sample App")
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(ACCESS_TOKEN_URL)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=authorization_code&code=" + code +
                                "&redirect_uri=" + REDIRECT_URI))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    String accessToken = data.optString("access_token");
                    String refreshToken = data.optString("refresh_token");

                    Log.d(TAG, "Access Token = " + accessToken);
                    Log.d(TAG, "Refresh Token = " + refreshToken);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("token", accessToken);
                    edit.putString("refreshToken", refreshToken);
                    edit.commit();
                    obtainedToken = true;
                    //makeSubredditCall(context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return obtainedToken;
    }

    public static boolean getSyncAccessToken(String code, final Context context) {
        obtainedToken = false;
        final SharedPreferences pref = context.getSharedPreferences("AppPref", 0);
        OkHttpClient client = new OkHttpClient();
        String authString = CLIENT_ID + ":";
        Response response = null;
        String json = "";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(),
                Base64.NO_WRAP);

        Request request = new Request.Builder()
                .addHeader("User-Agent", "Sample App")
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(ACCESS_TOKEN_URL)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=authorization_code&code=" + code +
                                "&redirect_uri=" + REDIRECT_URI))
                .build();

        try {
            response = client.newCall(request).execute();
            json = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject data = null;
        try {
            data = new JSONObject(json);
            String accessToken = data.optString("access_token");
            String refreshToken = data.optString("refresh_token");

            Log.d(TAG, "Access Token = " + accessToken);
            Log.d(TAG, "Refresh Token = " + refreshToken);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("token", accessToken);
            edit.putString("refreshToken", refreshToken);
            edit.putBoolean("isLoggedIn", true);
            edit.commit();
            if (accessToken != null || accessToken.length() > 0) {
                obtainedToken = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obtainedToken;
    }

    public static boolean refreshAccessToken(final Context context) {
        obtainedToken = false;
        final SharedPreferences pref = context.getSharedPreferences("AppPref", 0);
        OkHttpClient client = new OkHttpClient();
        String authString = CLIENT_ID + ":";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(),
                Base64.NO_WRAP);
        String refreshToken = pref.getString("refreshToken", "");
        Request request = new Request.Builder()
                .addHeader("User-Agent", "Sample App")
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(ACCESS_TOKEN_URL)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=refresh_token&refresh_token=" + refreshToken))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    String accessToken = data.optString("access_token");
                    Log.d(TAG, "Refreshed Access Token = " + accessToken);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("token", accessToken);
                    edit.commit();
                    obtainedToken = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return obtainedToken;
    }

    public static boolean refreshSyncAccessToken(final Context context) {
        obtainedToken = false;
        final SharedPreferences pref = context.getSharedPreferences("AppPref", 0);
        OkHttpClient client = new OkHttpClient();
        String authString = CLIENT_ID + ":";
        Response response = null;
        String json = "";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(),
                Base64.NO_WRAP);
        String refreshToken = pref.getString("refreshToken", "");
        Request request = new Request.Builder()
                .addHeader("User-Agent", "Sample App")
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(ACCESS_TOKEN_URL)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=refresh_token&refresh_token=" + refreshToken))
                .build();

        try {
            response = client.newCall(request).execute();
            json = response.body().string();

            JSONObject data = null;
            try {
                data = new JSONObject(json);
                String accessToken = data.optString("access_token");
                Log.d(TAG, "Refreshed Access Token = " + accessToken);
                SharedPreferences.Editor edit = pref.edit();
                edit.putString("token", accessToken);
                edit.commit();
                obtainedToken = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obtainedToken;
    }

    public static String makeSubredditCall(Context context) {
        OkHttpClient client = new OkHttpClient();
        SharedPreferences pref = context.getSharedPreferences("AppPref", 0);
        String token = pref.getString("token", "");
        String responseJSON = null;
        Request request = new Request.Builder()
                .addHeader("User-Agent", "Sample App")
                .addHeader("Authorization", "bearer " + token)
                .url(ACCESS_SUBREDDITS_URL)
                .build();

        try {
            Response response = client.newCall(request).execute();
            responseJSON = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseJSON;
    }

    public static ArrayList<SubredditCustomObject> extractSubredditsFromJSON(String json) {
        ArrayList subredditList = new ArrayList<SubredditCustomObject>();
        try {
            JSONObject subredditsJSON = new JSONObject(json);
            JSONObject data = subredditsJSON.getJSONObject("data");
            JSONArray subredditsArray = data.getJSONArray("children");
            if (subredditsArray.length() != 0 && subredditsArray != null) {
                for (int i = 0; i < subredditsArray.length(); i++) {
                    JSONObject subredditArrayEntry = subredditsArray.getJSONObject(i);
                    JSONObject subredditData = subredditArrayEntry.getJSONObject("data");
                    String subredditID = subredditData.getString("id");
                    String prefixedDisplayName = subredditData.getString("display_name_prefixed");
                    String subredditUrl = subredditData.getString("url");
                    String description = subredditData.getString("public_description");
                    SubredditCustomObject object =
                            new SubredditCustomObject(subredditID, prefixedDisplayName, subredditUrl, description, STRING_SHOW);
                    subredditList.add(object);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return subredditList;
    }

    public static ArrayList<ArticleCustomObject> getArticlesFromCursor(Cursor c, Context cnt) {
        ArrayList<ArticleCustomObject> articlesList = new ArrayList<ArticleCustomObject>();
        for (int i = 0; i < c.getCount(); i++) {
             c.moveToPosition(i);

                String subredditUrl = c.getString(c.getColumnIndex(SubredditsContract.subredditEntry.COLUMN_SUBREDDIT_URL));
                String json = getArticleForSubreddit(subredditUrl, null, cnt);
                if (json != null) {
                    if (!json.matches("")) {
                        ArticleCustomObject a = extractArticleFromJson(json, subredditUrl);
                        articlesList.add(a);
                    }

            }
        }
        return articlesList;
    }

    public static String getArticleForSubreddit(String subredditURL, @Nullable String previousArticleId, Context cntxt) {
        String RequestURL = GET_ARTICLES_BASE_URL + subredditURL + "new?limit=1";
        if (previousArticleId != null) {
            RequestURL = RequestURL + "&after=t3_" + previousArticleId;
        }
        Log.d(TAG, RequestURL);
        OkHttpClient client = new OkHttpClient();
        SharedPreferences pref = cntxt.getSharedPreferences("AppPref", 0);
        String token = pref.getString("token", "");
        String responseJSON = null;
        Request request = new Request.Builder()
                .addHeader("User-Agent", "Sample App")
                .addHeader("Authorization", "bearer " + token)
                .url(RequestURL)
                .build();
        try {
            Response response = client.newCall(request).execute();
            responseJSON = response.body().string();
            //   Log.d(TAG, responseJSON);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseJSON;
    }

    public static ArticleCustomObject extractArticleFromJson(String json, String subredditURL) {
        ArticleCustomObject articleObject = null;
        if(json != null) {
            if (!json.matches("")) {
                try {
                    JSONObject articleJSON = new JSONObject(json);
                    JSONObject data = articleJSON.getJSONObject("data");
                    JSONObject article = data.getJSONArray("children").getJSONObject(0).getJSONObject("data");
                    String resourceURL = article.getString("url");
                    String articleTitle = article.getString("title");
                    // Log.d(TAG, articleTitle);
                    String thumbnail = article.getString("thumbnail");
                    String articleId = article.getString("id");
                    articleObject =
                            new ArticleCustomObject(resourceURL, articleTitle, thumbnail, articleId, subredditURL);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return articleObject;
    }
}
