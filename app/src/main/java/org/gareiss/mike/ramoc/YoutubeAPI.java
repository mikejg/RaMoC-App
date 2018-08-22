package org.gareiss.mike.ramoc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.gareiss.mike.ramoc.model.Youtube;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;
import static org.apache.http.protocol.HTTP.USER_AGENT;

/**
 * Created by drue on 03.11.17.
 */

public class YoutubeAPI
{
    private String TAG="YoutubeAPI";
    private String stringResult;
    private String apiKey = "AIzaSyBe3KeO4jXAOT4VL_Z6u31v2ORdH3RnG0M";
    private ArrayList<NameValuePair>    nameValuePairs;
    private JSONObject                  json_data;
    private InputStream inputstream;
    HttpURLConnection connection;
    private Youtube youtube;
    private URI uri;

    void YoutubeAPI()
    {
    }

    public void loadStatistic()

    {
        youtube.viewCount = "0";
        youtube.likeCount = "0";
        youtube.dislikeCount = "0";

        String response;
        String url_Completion = "videos?part=statistics&id="
                + youtube.video_ID
                + "&key="
                + apiKey;

        response = startConnection(url_Completion);

        JSONObject responseJSON= null;
        try {
            responseJSON = new JSONObject(response);

            JSONArray items = responseJSON.getJSONArray("items");


            for(int i=0;i<items.length();i++)
            {

                youtube.viewCount = items.getJSONObject(i)
                        .getJSONObject("statistics")
                        .getString("viewCount");

                youtube.likeCount = items.getJSONObject(i)
                        .getJSONObject("statistics")
                        .getString("likeCount");

                youtube.dislikeCount = items.getJSONObject(i)
                        .getJSONObject("statistics")
                        .getString("dislikeCount");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Youtube> search_By_Keyword(String keyWord)
    {
        String response;
        keyWord = keyWord.replace(" ", "%20");
        //uri = URI.create(keyWord);
        //keyWord = uri.toString();
        keyWord = keyWord.replace("|", "%7C");
        keyWord = keyWord.replace("'", "%27");

        String url_Completion = "search?part=snippet&maxResults=10&q="
                              + keyWord
                              + "&type=video&key="
                              + apiKey;

        ArrayList<Youtube> arrayList_Youtube = new ArrayList<>();
        response = startConnection(url_Completion);

        JSONObject responseJSON= null;
        try {
            responseJSON = new JSONObject(response);

            JSONArray items = responseJSON.getJSONArray("items");


            for(int i=0;i<items.length();i++)
            {
                youtube = new Youtube();
                youtube.title = items.getJSONObject(i)
                                     .getJSONObject("snippet")
                                     .getString("title");

                youtube.channel_Title = items.getJSONObject(i)
                                             .getJSONObject("snippet")
                                             .getString("channelTitle");

                youtube.description = items.getJSONObject(i)
                                           .getJSONObject("snippet")
                                           .getString("description");

                youtube.video_Url = "https://www.youtube.com/watch?v="+
                                     items.getJSONObject(i)
                                          .getJSONObject("id")
                                          .getString("videoId");

                youtube.video_ID = items.getJSONObject(i)
                        .getJSONObject("id")
                        .getString("videoId");

                youtube.image_Url = items.getJSONObject(i)
                                                 .getJSONObject("snippet")
                                                 .getJSONObject("thumbnails")
                                                 .getJSONObject("medium")
                                                 .getString("url");
                youtube.image = loadImage(youtube.image_Url);
                loadStatistic();

                youtube.statistic = youtube.channel_Title + " | " +
                                    youtube.viewCount + " Views | " +
                                    youtube.likeCount + " Likes | " +
                                    youtube.dislikeCount + " Dislikes";

                Log.d(TAG, "Title:         " + youtube.title);
                Log.d(TAG, "Channel Title: " + youtube.channel_Title);
                Log.d(TAG, "Description:   " + youtube.description);
                Log.d(TAG, "Image Url:     " + youtube.image_Url);
                Log.d(TAG, "Video Url:     " + youtube.video_Url);
                Log.d(TAG, "Video Id:      " + youtube.video_ID);
                Log.d(TAG, "ViewCount:     " + youtube.viewCount);

                arrayList_Youtube.add(youtube);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return arrayList_Youtube;
    }

    public ArrayList<Youtube> search_Related(String keyWord)
    {
        Log.e(TAG, "search_Related: " + keyWord);
        String response;
        keyWord = keyWord.replace(" ", "%20");
        String url_Completion = "search?part=snippet&relatedToVideoId="
                + keyWord
                + "&type=video&key="
                + apiKey;

        ArrayList<Youtube> arrayList_Youtube = new ArrayList<>();
        response = startConnection(url_Completion);

        JSONObject responseJSON= null;
        try {
            responseJSON = new JSONObject(response);

            JSONArray items = responseJSON.getJSONArray("items");


            for(int i=0;i<items.length();i++)
            {
                youtube = new Youtube();
                youtube.title = items.getJSONObject(i)
                        .getJSONObject("snippet")
                        .getString("title");

                youtube.channel_Title = items.getJSONObject(i)
                        .getJSONObject("snippet")
                        .getString("channelTitle");

                youtube.description = items.getJSONObject(i)
                        .getJSONObject("snippet")
                        .getString("description");

                youtube.video_Url = "https://www.youtube.com/watch?v="+
                        items.getJSONObject(i)
                                .getJSONObject("id")
                                .getString("videoId");

                youtube.video_ID = items.getJSONObject(i)
                                .getJSONObject("id")
                                .getString("videoId");
                youtube.image_Url = items.getJSONObject(i)
                        .getJSONObject("snippet")
                        .getJSONObject("thumbnails")
                        .getJSONObject("medium")
                        .getString("url");
                youtube.image = loadImage(youtube.image_Url);


                Log.d(TAG, youtube.title);
                Log.d(TAG, youtube.channel_Title);
                Log.d(TAG, youtube.description);
                Log.d(TAG, youtube.image_Url);
                Log.d(TAG, youtube.video_Url);

                arrayList_Youtube.add(youtube);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return arrayList_Youtube;
    }
    public ArrayList<String> search_Proposal(String keyWord)
    {
        String response;
        String proposal;
        keyWord = keyWord.replace(" ", "%20");
        keyWord = keyWord.replace("'", "%27");
        keyWord = keyWord.replace("|", "%7C");
        //String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=Goethes%20Erben&type=video&key=AIzaSyBe3KeO4jXAOT4VL_Z6u31v2ORdH3RnG0M";
        String url_Completion = "search?part=snippet&maxResults=5&q="
                + keyWord
                + "&type=video&key="
                + apiKey;

        ArrayList<String> arrayList_Proposal = new ArrayList<>();
        response = startConnection(url_Completion);

        JSONObject responseJSON= null;
        try {
            responseJSON = new JSONObject(response);

            JSONArray items = responseJSON.getJSONArray("items");


            for(int i=0;i<items.length();i++)
            {
                proposal = new String();
                 proposal= items.getJSONObject(i)
                        .getJSONObject("snippet")
                        .getString("title");


                Log.d(TAG, proposal);

                arrayList_Proposal.add(proposal);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return arrayList_Proposal;
    }

    public String startConnection(String completion)
    {
        String content = "";

        String url = "https://www.googleapis.com/youtube/v3/"
                   + completion;

        //uri = URI.create(url);
        //String validUrl = uri.toASCIIString();

        Log.e(TAG, "startConnection " + url);

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

        // add request header
        request.addHeader("User-Agent", USER_AGENT);

        try {
            HttpResponse response = client.execute(request);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null)
            {
                content += line;
            }
            //Log.d(TAG, content);
        }

        catch(IOException e)
        {

        }

        return content;
    }

    public Bitmap loadImage(String url)
    {
        Bitmap bitmap_Cover = null;

        //Log.i(TAG, url);
        try {
            InputStream in = new java.net.URL(url).openStream();
            bitmap_Cover = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.i(TAG, e.toString());
            Log.i(TAG, url);
        }
        return bitmap_Cover;
    }
}
