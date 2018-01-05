package com.example.user.appforreddit;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.example.user.appforreddit.Database.subredditsContract;

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

import static com.example.user.appforreddit.databaseUtils.context;

/**
 * Created by user on 03-01-2018.
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
        final SharedPreferences pref = context.getSharedPreferences("AppPref", Context.MODE_PRIVATE);
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
                    //       Toast.makeText(MainActivity.this, accessToken, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Refresh Token = " + refreshToken);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("token", accessToken);
                    edit.putString("refreshToken", refreshToken);
                    edit.commit();
                    // Log.d(TAG, "Flag is set true");
                    obtainedToken = true;
                    //makeSubredditCall(context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return obtainedToken;
    }

    public static boolean getSyncAccessToken(String code, final Context context){
        obtainedToken =false;
        final SharedPreferences pref = context.getSharedPreferences("AppPref", Context.MODE_PRIVATE);
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
           response =  client.newCall(request).execute();
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
            edit.commit();
            if(accessToken != null || accessToken.length() >0) {
                obtainedToken = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obtainedToken;
}

    public static String makeSubredditCall(Context context){
        OkHttpClient client = new OkHttpClient();
        SharedPreferences pref = context.getSharedPreferences("AppPref",Context.MODE_PRIVATE);
        String token =pref.getString("token", "");
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

    public static ArrayList<subredditCustomObject> extractSubredditsFromJSON(String json){
        ArrayList subredditList = new ArrayList<subredditCustomObject>();
        try {
            JSONObject subredditsJSON = new JSONObject(json);
            JSONObject data = subredditsJSON.getJSONObject("data");
            JSONArray subredditsArray = data.getJSONArray("children");
            if(subredditsArray.length() != 0 && subredditsArray != null){
                for (int i = 0; i < subredditsArray.length(); i++){
                    JSONObject subredditArrayEntry = subredditsArray.getJSONObject(i);
                    JSONObject subredditData = subredditArrayEntry.getJSONObject("data");
                    String subredditID = subredditData.getString("id");
                    String prefixedDisplayName = subredditData.getString("display_name_prefixed");
                    String subredditUrl = subredditData.getString("url");
                    String description = subredditData.getString("public_description");
                    subredditCustomObject object =
                            new subredditCustomObject(subredditID, prefixedDisplayName, subredditUrl, description, STRING_SHOW);
                    subredditList.add(object);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return subredditList;
    }

    public static ArrayList<articleCustomObject> getArticlesFromCursor(Cursor c) {
        ArrayList<articleCustomObject> articlesList = new ArrayList<articleCustomObject>();
        for(int i=0; i<c.getCount(); i++){
            c.moveToPosition(i);
            String subredditUrl = c.getString(c.getColumnIndex(subredditsContract.subredditEntry.COLUMN_SUBREDDIT_URL));
            String json = getArticleForSubreddit(subredditUrl, null);
            articleCustomObject a = extractArticleFromJson(json, subredditUrl);
            articlesList.add(a);
        }
        return articlesList;
    }

    public static String getArticleForSubreddit(String subredditURL, @Nullable String previousArticleId){
        String RequestURL = GET_ARTICLES_BASE_URL + subredditURL + "new?limit=1";
        if(previousArticleId != null){
            RequestURL = RequestURL + "&after=t3_" + previousArticleId;
        }
        Log.d(TAG, RequestURL);
        OkHttpClient client = new OkHttpClient();
        SharedPreferences pref = context.getSharedPreferences("AppPref",Context.MODE_PRIVATE);
        String token =pref.getString("token", "");
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

    public static articleCustomObject extractArticleFromJson(String json, String subredditURL){
        articleCustomObject articleObject = null;
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
                    new articleCustomObject(resourceURL, articleTitle, thumbnail, articleId, subredditURL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return articleObject;
    }
}
