package com.example.user.appforreddit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

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
 * Created by user on 03-01-2018.
 */

public class NetworkUtils {
    private static final String ACCESS_TOKEN_URL =
            "https://www.reddit.com/api/v1/access_token";
    private static final String REDIRECT_URI =
            "http://www.example.com/my_redirect";
    private static final String CLIENT_ID = "2XIKITvqkmX-yg";
    private static String ACCESS_SUBREDDITS_URL = "https://oauth.reddit.com/subreddits/mine/subscriber";
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
}
