package org.gareiss.mike.ramoc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.gareiss.mike.ramoc.model.Actor;
import org.gareiss.mike.ramoc.model.Movie;
import org.gareiss.mike.ramoc.model.TVShow;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by drue on 23.02.17.
 */

public class DataBase
{
    private String TAG = "DataBase";

    private String string_IP;
    private String stringResult;

    private Context                     context;
    private ArrayList<NameValuePair>    nameValuePairs;
    private JSONObject                  json_data;
    private InputStream                 inputstream;

    private Movie 						movie;
    private Actor                       actor;
    private TVShow                      tvShow;
    DataBase(String ip, Context c)
    {
        Log.i(TAG, "Constructor" + ip);
        string_IP = ip;
        context = c;

        nameValuePairs = new ArrayList<NameValuePair>();
    }

    public ArrayList<Movie> getData()
    {
        ArrayList<Movie> arrayList_Movie = new ArrayList<Movie>();

        //Jetzt wird der 'String result' in einen JSONArray convertiert so kann er leichter ausgelesen werden!
        try
        {
            JSONArray jArray = new JSONArray(stringResult);

            for(int i=0;i<jArray.length();i++)
            {
                json_data = jArray.getJSONObject(i);

                movie = new Movie();
                movie.setTitel((String) json_data.get("titel"));
                movie.setId(json_data.getString("id"));
                movie.setFSK(json_data.getString("fsk"));
                movie.setLaufzeit(json_data.getString("laufzeit"));
                movie.addGenre(json_data.getString("genre1"));
                movie.addGenre(json_data.getString("genre2"));
                movie.addGenre(json_data.getString("genre3"));
                movie.addGenre(json_data.getString("genre4"));


                movie.setCover(loadCover(json_data.getString("coverhash")));

                arrayList_Movie.add(movie);

            }

        }
        catch(JSONException e)
        {
            Log.e(TAG, "DataBase::getData");
            Log.e(TAG, "Error parsing data "+e.toString());
        }
        return arrayList_Movie;
    }

    public ArrayList<Movie> getList(Boolean bool)
    {
        startConnection("http://" + string_IP + "/getList.php");
        return getData();
    }

    public ArrayList<Movie> getArchive(String order)
    {
        nameValuePairs.add(new BasicNameValuePair("order", order));
        startConnection("http://" + string_IP + "/getArchive.php");
        return getData();
    }

    public ArrayList<Movie> getGenre(String genre)
    {
        nameValuePairs.add(new BasicNameValuePair("genre", genre));
        startConnection("http://" + string_IP + "/getGenre.php");
        return getData();
    }
    public ArrayList<String> getProposal(String title)
    {
        String str;
        ArrayList<String> arrayList = new ArrayList<String>();
        nameValuePairs.add(new BasicNameValuePair("query", title));
        nameValuePairs.add(new BasicNameValuePair("api_key", "9ce3b6ead4014e1e2c976df65a530a11"));
        nameValuePairs.add(new BasicNameValuePair("language", "de"));
        startConnection("https://api.themoviedb.org/3/search/movie");
        //Log.i(TAG, stringResult);

        try
        {
            JSONObject jObject = new JSONObject(stringResult);

            JSONArray jArray = jObject.optJSONArray("results");

            for(int i=0;i<jArray.length();i++)
            {
                json_data = jArray.getJSONObject(i);
                str = json_data.getString("id") + "|" + json_data.getString("title");
                arrayList.add(str);
                Log.i(TAG, str);
            }
        }
        catch(JSONException e)
        {
            Log.e(TAG, "Error parsing data "+e.toString());
        }


        return arrayList;
    }

    public ArrayList<Movie> getTVShowList()
    {
        startConnection("http://" + string_IP + "/getTVShowList.php");
        return getData();
    }

    public Movie getSelectedMovie(String id)
    {
        Movie movie = new Movie();
        Log.i(TAG, "Movie ID: " + id);

        nameValuePairs.add(new BasicNameValuePair("api_key", "9ce3b6ead4014e1e2c976df65a530a11"));
        nameValuePairs.add(new BasicNameValuePair("language", "de"));
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet("https://api.themoviedb.org/3/movie/" + id + "?api_key=9ce3b6ead4014e1e2c976df65a530a11&language=de");
        HttpResponse response;
        BufferedReader rd;
        try
        {
            response = client.execute(request);
            rd = new BufferedReader
                    (new InputStreamReader(
                            response.getEntity().getContent()));


            try
            {
                stringResult = rd.readLine();
            }
            catch (IOException e)
            {
                stringResult = "";
            }
        }
        catch (IOException e)
        {

        }

        Log.i(TAG, stringResult);
        try
        {
            JSONObject jObject = new JSONObject(stringResult);
            movie.setBeschreibung(jObject.getString("overview"));
            String url = "http://image.tmdb.org/t/p/w342" + jObject.getString("poster_path");
            Log.i(TAG, url);
            try
            {
                InputStream in = new java.net.URL(url).openStream();
                movie.setPoster(BitmapFactory.decodeStream(in));
            }
            catch(Exception e)
            {

            }
        }
        catch(JSONException e)
        {
            Log.e(TAG, "Error parsing data "+e.toString());
        }
        return movie;
    }

    @SuppressWarnings("static-access")
    public Bitmap loadCover(String coverHash)
    {
        Bitmap bitmap_Cover = null;
        String string_CoverString;
        byte[] rawImage;
        String log_tag = TAG + " getFilmCover";
        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;

        try
        {
            fileInputStream = context.openFileInput("Cover_" + coverHash);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            bitmap_Cover = BitmapFactory.decodeStream(bufferedInputStream);
            return bitmap_Cover;
        }
        catch(IOException e)
        {

        }

        String url = "http://" + string_IP +"/Pictures/Poster/" + coverHash;
        Log.i(TAG, url);
        try
        {
            InputStream in = new java.net.URL(url).openStream();
            bitmap_Cover = BitmapFactory.decodeStream(in);
        }
        catch(Exception e)
        {
            Log.i(TAG, e.toString());
            Log.i(TAG, url);
        }

        if(bitmap_Cover != null)
        {
            try {
                fileOutputStream = context.openFileOutput("Cover_" + coverHash, context.MODE_PRIVATE);
                bitmap_Cover.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            } catch (IOException e) {
                Log.e(log_tag, "Poster konnte nicht geschrieben werden");
            }
        }
        return bitmap_Cover;

        /*
        //Log.i(log_tag, "Datenbank Cover für " + string_FilmTitel);
        //Cover aus der Datenbank lesen und local abspeichern
        nameValuePairs.add(new BasicNameValuePair("filmId", string_FilmId));
        startConnection("http://" + string_IP + "/getMovieCover.php");
        try
        {
            JSONArray jArray = new JSONArray(stringResult);

            for(int i=0;i<jArray.length();i++)
            {
                json_data = jArray.getJSONObject(i);
                string_CoverString = (String) json_data.get("cover");
                rawImage = Base64.decode(string_CoverString, Base64.DEFAULT);
                bitmap_Cover = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);

                if(bitmap_Cover != null)
                {
                    try
                    {
                        fileOutputStream = context.openFileOutput(string_FilmTitel, context.MODE_PRIVATE);
                        bitmap_Cover.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    }
                    catch(IOException e)
                    {
                        //Toast.makeText(context_Item, "Daten konnten nicht geschrieben werden", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        catch(JSONException e)
        {
            Log.e(log_tag, "Error parsing data "+e.toString());
        }

        string_CoverString = null;
        rawImage = null;
        return bitmap_Cover;
        */
    }

    public Bitmap loadAlbumCover(String string_AlbumName,String string_AlbumID )
    {
        Bitmap bitmap_Cover = null;
        String string_CoverString = "";
        byte[] rawImage;
        String log_tag = TAG + " getAlbumCover";
        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;

        try
        {
            fileInputStream = context.openFileInput(string_AlbumName);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            bitmap_Cover = BitmapFactory.decodeStream(bufferedInputStream);
            return bitmap_Cover;
        }
        catch(IOException e)
        {

        }

        Log.i(log_tag, "Datenbank Cover für " + string_AlbumName );
        Log.i(log_tag, "Datenbank Cover für " + string_AlbumID );
        //Cover aus der Datenbank lesen und local abspeichern
        nameValuePairs.add(new BasicNameValuePair("albumID", string_AlbumID));
        startConnection("http://" + string_IP + "/music/getCover.php");

        try
        {
            JSONArray jArray = new JSONArray(stringResult);
            Log.i(TAG, "jArray");

            for(int i=0;i<jArray.length();i++)
            {
                Log.i(TAG, "in for");
                json_data = jArray.getJSONObject(i);
                Log.i(TAG, "json_data");
                string_CoverString = (String) json_data.get("image");
                Log.i(TAG, "string_CoverString: " + string_CoverString);

                    rawImage = Base64.decode(string_CoverString, Base64.DEFAULT);
                    bitmap_Cover = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);

                    if (bitmap_Cover != null) {
                        try {
                            fileOutputStream = context.openFileOutput(string_AlbumName, context.MODE_PRIVATE);
                            bitmap_Cover.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                        } catch (IOException e)
                        {
                            Log.e(TAG, e.toString());
                            //Toast.makeText(context_Item, "Daten konnten nicht geschrieben werden", Toast.LENGTH_SHORT).show();
                        }
                    }
            }
        }
        catch(JSONException e)
        {
            //Log.e(log_tag, "Error parsing data "+e.toString());
            Log.e(log_tag, "Error parsing data "+ string_CoverString);
        }
        catch(ClassCastException e)
        {
            Log.e(TAG, e.toString());
        }

        //string_CoverString = null;
        //rawImage = null;
        return bitmap_Cover;
    }

    public void startConnection(String phpUrl)
    {
        Log.i(TAG, "startConnection " + phpUrl);
        //In diesem Abschnitt wird die Verbindung zu der Webseite und somit auch zu der Php Datei hergestellt!
        //Wenn der Url richtig eingegeben wurde und existiert dürfte eigentlich kein Fehler in der Logcat stehen.
        try
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(phpUrl);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            inputstream = entity.getContent();
        }
        catch(Exception e)
        {
            Log.e(TAG, "Fehler bei der http Verbindung "+e.toString());
        }

        //Im nächsten Abschnitt werden die Tabellendaten, die von der Php Datei ausgelesen wurden
        //in dem 'String result' gespeichert!
        try
        {
            //BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream,"UTF-8"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            inputstream.close();
            stringResult=sb.toString();
        }
        catch(Exception e)
        {
            Log.e(TAG, "Error converting result "+e.toString());
            Log.e(TAG, stringResult);
        }

        nameValuePairs.clear();

        Log.i(TAG, "endConnection " + phpUrl);
    }

    public void getMovieInfo(Movie movie)
    {
        //Log.i(TAG, "DataBase::getMovie");

        String log_tag = TAG + " getMovieInfo";
        nameValuePairs.add(new BasicNameValuePair("filmId", movie.getId()));

        startConnection("http://" + string_IP + "/getMovieInfo.php");
        //Log.i(TAG, "End startConnection");

        try
        {
            JSONArray jArray = new JSONArray(stringResult);

            for(int i=0;i<jArray.length();i++)
            {
                json_data = jArray.getJSONObject(i);
                movie.setBeschreibung(json_data.getString("beschreibung"));

                movie.setFSK(json_data.getString("fsk"));
                movie.setLaufzeit(json_data.getString("laufzeit"));
                movie.setTrailer(json_data.getString("trailer"));
                movie.setBackdropHash(json_data.getString("backdrophash"));
                //Log.i(TAG, "Trailer: " + json_data.getString("trailer"));
            }
        }
        catch(JSONException e)
        {
            Log.e(log_tag, "Error parsing data "+e.toString());
        }

        nameValuePairs.add(new BasicNameValuePair("filmId", movie.getId()));
        startConnection("http://" + string_IP + "/getFileList.php");

        try
        {
            JSONArray jArray = new JSONArray(stringResult);
            json_data = jArray.getJSONObject(0);
            movie.setFile(json_data.getString("dateiname"));
        }

        catch(JSONException e)
        {
            Log.e(TAG, "Error parsing data "+e.toString());
        }

        movie.setPoster(loadPoster(movie.getBackdropHash()));
        getActorList(movie);
        return;
    }

    @SuppressWarnings("static-access")
    public Bitmap loadPoster(String backdropHash)
    {
        Bitmap bitmap_Poster = null;
        String string_PosterString;
        byte[] rawImage;
        String log_tag = TAG + " getFilmPoster";
        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;

        try
        {
            fileInputStream = context.openFileInput("Backdrop_" + backdropHash);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            bitmap_Poster = BitmapFactory.decodeStream(bufferedInputStream);
            return bitmap_Poster;
        }
        catch(IOException e)
        {

        }

        String url = "http://" + string_IP +"/Pictures/Backdrop/" + backdropHash;
        Log.i(TAG, url);
        try
        {
            InputStream in = new java.net.URL(url).openStream();
            bitmap_Poster = BitmapFactory.decodeStream(in);
        }
        catch(Exception e)
        {
            Log.i(TAG, e.toString());
            Log.i(TAG, url);
        }

        if(bitmap_Poster != null)
        {
            try {
                fileOutputStream = context.openFileOutput("Backdrop_" + backdropHash, context.MODE_PRIVATE);
                bitmap_Poster.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            } catch (IOException e) {
                Log.e(log_tag, "Poster konnte nicht geschrieben werden");
            }
        }
        return bitmap_Poster;
    }

    public void getActorList(Movie movie)
    {
        nameValuePairs.add(new BasicNameValuePair("filmId", movie.getId()));
        startConnection("http://" + string_IP + "/getActor.php");

        try
        {
            JSONArray jArray = new JSONArray(stringResult);

            for(int i=0;i<jArray.length();i++)
            {
                json_data = jArray.getJSONObject(i);

                actor = new Actor();
                actor.setId((String) json_data.get("id"));
                actor.setName((String) json_data.get("name"));
                actor.setCharacter((String) json_data.get("rolle"));

                actor.setPortrait(loadPortrait(json_data.getString("profilehash")));
                movie.addActor(actor);
            }

        }
        catch(JSONException e)
        {
            Log.e(TAG, "Error parsing data "+e.toString());
        }
        return;
    }

    @SuppressWarnings("static-access")
    public Bitmap loadPortrait(String profileHash)
    {
        Log.i(TAG, "ProfileHash: " + profileHash);
        Bitmap bitmap_Portrait = null;
        Bitmap bitmap_tmp = null;
        String log_tag = TAG + " getActorPortrait";
        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;

        if(profileHash.equals(""))
        {
            bitmap_tmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.portrait);
            bitmap_Portrait = Bitmap.createScaledBitmap(bitmap_tmp, 226, 352, false);
            return bitmap_Portrait;
        }

        try
        {
            fileInputStream = context.openFileInput("Profile_" + profileHash);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            bitmap_Portrait = BitmapFactory.decodeStream(bufferedInputStream);
            return bitmap_Portrait;
        }
        catch(IOException e)
        {

        }

        String url = "http://" + string_IP +"/Pictures/Actor/" + profileHash;
        Log.i(TAG, url);
        try
        {
            InputStream in = new java.net.URL(url).openStream();
            bitmap_tmp = BitmapFactory.decodeStream(in);
            bitmap_Portrait = Bitmap.createScaledBitmap(bitmap_tmp, 226, 352, false);
        }
        catch(Exception e)
        {
            Log.i(TAG, e.toString());
            Log.i(TAG, url);
        }

        if(bitmap_Portrait != null)
        {
            try {
                fileOutputStream = context.openFileOutput("Profile_" + profileHash, context.MODE_PRIVATE);
                bitmap_Portrait.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            } catch (IOException e) {
                Log.e(log_tag, "Poster konnte nicht geschrieben werden");
            }
        }
        return bitmap_Portrait;
    }

    public ArrayList<TVShow> getEpisoden(String string_Id)
    {
        Log.i(TAG, "getEpisoden: " + string_Id);

        nameValuePairs.add(new BasicNameValuePair("filmId", string_Id));
        startConnection("http://" + string_IP + "/getEpisoden.php");

        ArrayList<TVShow> array_TVShow = new ArrayList<TVShow>();
        try
        {
            JSONArray jArray = new JSONArray(stringResult);
            for(int i=0;i<jArray.length();i++)
            {
                json_data = jArray.getJSONObject(i);
                tvShow = new TVShow();

                String[] array_String = json_data.getString("dateiname").split("/");
                tvShow.setFile(json_data.getString("dateiname"));
                tvShow.setPlayed((json_data.getInt("played") != 0));
                tvShow.setEpisode(array_String[array_String.length -1]);
                array_TVShow.add(tvShow);
            }
        }

        catch(JSONException e)
        {
            Log.e(TAG, "Error parsing data "+e.toString());
        }

        return array_TVShow;
    }

}
