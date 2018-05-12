package org.gareiss.mike.ramoc.music;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.gareiss.mike.ramoc.DataBase;
import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;
import org.gareiss.mike.ramoc.model.Track;
import org.gareiss.mike.ramoc.tcp.TCPConstants;
import org.gareiss.mike.ramoc.tcp.TCPListener;
import org.gareiss.mike.ramoc.tcp.TCPService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Activity_Music extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TCPListener
{
    String TAG = "Music";
    String activityTitle;
    String tmp[];
    String jsonString;

    ExpandableListView              listView_Artists;
    ExpandableListAdapter           adapter_Artists;
    List<String> 				    listDataHeader;
    HashMap<String, List<String>>   listDataChild;

    DataBase            dataBase;
    Track               currentTrack;
    Boolean	            isOnCreate;
    RaMoCApplication    ramocApp;

    ArrayList<Track>    arrayList_Playlist;
    ListView            listView_Playlist;
    Adapter_Playlist	adapter_Playlist;

    TextView textView_Title;
    TextView textView_Artist;
    TextView textView_Album;
    ImageView imageView_Cover;

    Intent tcpIntent;

    AsyncCall_GetCover getCover;
    Button button_Play;
    Button button_Favoriten;
    Menu optionMenu;
    MenuItem sampler;
    Boolean isSampler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ramocApp = (RaMoCApplication) getApplicationContext();
        ramocApp.addTCPListener(this);
        dataBase = ramocApp.getDataBase();
        tcpIntent = ramocApp.getTcpIntent();

        button_Play = (Button) findViewById(R.id.play);
        button_Favoriten = (Button) findViewById(R.id.favoriten);
        currentTrack = null;

        imageView_Cover = (ImageView) findViewById(R.id.imageView_Cover);
        textView_Title = (TextView) findViewById(R.id.textView_Title);
        textView_Album = (TextView) findViewById(R.id.textView_Album);
        textView_Artist = (TextView) findViewById(R.id.textView_Artist);

        listView_Artists = (ExpandableListView) findViewById(R.id.lvExp);
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        adapter_Artists = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        listView_Artists.setAdapter(adapter_Artists);

        listView_Artists.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id)
            {
                startIntent(TCPConstants.insertAlbum + "|" + listDataHeader.get(groupPosition) + "|"
                        + listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition));

                return false;
            }
        });

        listView_Playlist = (ListView) findViewById(R.id.listView_Playlist);
        listView_Playlist.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id)
            {
                //adapter_Playlist.setSelectedIndex(position);
                Object o = listView_Playlist.getItemAtPosition(position);
                currentTrack = (Track) o;
                currentTrack.position = Integer.toString(position);
                startIntent(TCPConstants.playTrack + "|" + Integer.toString(position));
            }
        });

    }

    @Override
    public void onResume()
    {
        super.onResume();
        arrayList_Playlist = new ArrayList<Track>();
        adapter_Playlist = new Adapter_Playlist(this, arrayList_Playlist);
        listView_Playlist.setAdapter(adapter_Playlist);

        startIntent(TCPConstants.sendPlaylist);
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        ramocApp.delTCPListener(this);
    }

    public void button_Play_Click(View view)
    {
        if(currentTrack == null)
            return;

        startIntent(TCPConstants.playTrack + "|" + currentTrack.position);
        //tcpIntent.setAction(TCPService.ACTION_SEND);
        //tcpIntent.putExtra("String", TCPConstants.play + currentTrack.path + "\n");
        //startService(tcpIntent);
    }

    public void button_Stop_Click(View view)
    {
        tcpIntent.setAction(TCPService.ACTION_SEND);
        tcpIntent.putExtra("String", TCPConstants.playerStop +  "\n");
        startService(tcpIntent);
    }

    public void button_Clear_Click(View view)
    {
        tcpIntent.setAction(TCPService.ACTION_SEND);
        tcpIntent.putExtra("String", TCPConstants.deletePlaylist +  "\n");
        startService(tcpIntent);

        arrayList_Playlist.clear();
        adapter_Playlist.notifyDataSetChanged();

        textView_Title.setText("");
        textView_Album.setText("");
        textView_Artist.setText("");
        imageView_Cover.setImageResource(R.drawable.no_cover);

    }

    public void button_Fav_Click(View view)
    {
        startIntent(TCPConstants.setFavorite + "|" + currentTrack.ramocId);
        if(currentTrack.favorite.equals("1"))
        {
            currentTrack.favorite = "0";
            button_Favoriten.setBackgroundResource(R.drawable.toolbar_nofav);
        }
        else
        {
            currentTrack.favorite = "1";
            button_Favoriten.setBackgroundResource(R.drawable.toolbar_fav);
        }
    }

    public void button_Edit_Click(View view)
    {
        Intent myIntent = new Intent(getApplicationContext(), Dialog_Edit_ID3Tag.class);
        myIntent.putExtra("Title",currentTrack.titel);
        myIntent.putExtra("Album", currentTrack.album);
        myIntent.putExtra("Interpret", currentTrack.artist);
        myIntent.putExtra("ramocID", currentTrack.ramocId);
        startActivity(myIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity__music, menu);
        sampler = menu.findItem(R.id.action_sampler);
        sampler.setChecked(false);
        isSampler = false;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sampler)
        {
            isSampler = !sampler.isChecked();
            sampler.setChecked(isSampler);
            if(isSampler)
            {
                startIntent(TCPConstants.setSampler + "|" + currentTrack.albumId);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String str = "";

        if (id == R.id.nav_Favoriten)
        {
            startIntent(TCPConstants.getFavorite);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        else if (id == R.id.nav_Random)
        {
            startIntent(TCPConstants.getRandom);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        else if (id == R.id.nav_Sampler)
        {
            str="Sampler";
        }
        else if (id == R.id.nav_ABC)
        {
            str = "ABC";
        }
        else if (id == R.id.nav_DEF)
        {
            str = "DEF";
        }
        else if (id == R.id.nav_GHI)
        {
            str = "GHI";
        }
        else if (id == R.id.nav_JKL)
        {
            str = "JKL";
        }
        else if (id == R.id.nav_MNO)
        {
            str = "MNO";
        }
        else if (id == R.id.nav_PQR)
        {
            str = "PQR";
        }
        else if (id == R.id.nav_STU)
        {
            str = "STU";
        }
        else if (id == R.id.nav_VWX)
        {
            str = "VWX";
        }
        else if (id == R.id.nav_YZ)
        {
            str = "YZ";
        }


        for(int i = 0; i < adapter_Artists.getGroupCount(); i++)
        {
            listView_Artists.collapseGroup(i);
        }
        listDataChild.clear();
        listDataHeader.clear();
        startIntent(TCPConstants.ArtistAlben + "|" + str);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void addNewTracks()
    {
        JSONObject json_data;
        Track track;
        tmp = jsonString.split("\\|");
        if(tmp.length > 1)
        {
            try
            {
                JSONArray jArray = new JSONArray(tmp[1]);

                for(int i=0;i<jArray.length();i++)
                {
                    json_data = jArray.getJSONObject(i);

                    track = new Track();
                    track.ramocId = json_data.getString("id");
                    track.albumId = json_data.getString("album_id");
                    track.path = json_data.getString("pfad");
                    track.titel = json_data.getString("title");
                    track.album = json_data.getString("album");
                    track.artist = json_data.getString("artist");
                    track.favorite = json_data.getString("favorite");
                    track.sampler = json_data.getString("sampler");
                    Log.i(TAG + " addNewTracks", json_data.getString("title"));

                    arrayList_Playlist.add(track);
                    adapter_Playlist.notifyDataSetChanged();

                }
            }
            catch(JSONException e)
            {
                Log.e(TAG, "Error parsing data "+e.toString());
            }
        }
    }

    @Override
    public void onTCPMessage(final String tcpString)
    {
        Log.i(TAG + " onTCPMessage", tcpString);
        tmp = tcpString.split("\\|");
        if(tcpString.startsWith(TCPConstants.ArtistAlben) && tmp.length > 2)
        {
            listDataHeader.add(tmp[1]);
            List<String> list = new ArrayList<String>();

            for(int i = 2; i < tmp.length; i++)
            {
                list.add(tmp[i]);
            }
            listDataChild.put(tmp[1], list);
            startIntent(TCPConstants.nextArtistAlben);
        }

        if(tcpString.startsWith(TCPConstants.endArtistAlben) )
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    adapter_Artists.notifyDataSetChanged();
                }
            });

        }

        if(tcpString.startsWith(TCPConstants.newTracks) )
        {
            jsonString = tcpString;
            runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    addNewTracks();
                }
            });
        }

        if(tcpString.startsWith(TCPConstants.playTrack))
        {
            Log.i(TAG, "tmp.legth = " + Integer.toString(tmp.length));
            for (int i = 0; i < tmp.length; i++)
                Log.i(TAG, Integer.toString(i) + ": " + tmp[i]);

            runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    Bitmap bitmap = null;
                    byte[] rawImage;
                    adapter_Playlist.setSelectedIndex(Integer.parseInt(tmp[1]));
                    Object o = listView_Playlist.getItemAtPosition(Integer.parseInt(tmp[1]));
                    currentTrack = (Track) o;
                    textView_Title.setText(currentTrack.titel);
                    textView_Album.setText(currentTrack.album);
                    textView_Artist.setText(currentTrack.artist);
                    if(currentTrack.favorite.equals("1"))
                    {
                        button_Favoriten.setBackgroundResource(R.drawable.toolbar_fav);
                    }
                    else
                    {
                        button_Favoriten.setBackgroundResource(R.drawable.toolbar_nofav);
                    }

                    if(currentTrack.sampler.equals("1"))
                    {
                        sampler.setChecked(true);
                        isSampler = true;
                    }
                    else
                    {
                        sampler.setChecked(false);
                        isSampler = false;
                    }
                    String[] strings = {currentTrack.artist + " + " + currentTrack.album,
                                        currentTrack.albumId};


                    getCover = new AsyncCall_GetCover(Activity_Music.this);
                    getCover.execute(strings);
                    button_Play.setBackgroundResource(R.drawable.toolbar_pause);
                    //button_PlayPause.setImageResource(R.drawable.pause);
                }
            });
        }

        if(tcpString.startsWith(TCPConstants.sendPlaylist) ) {
            tmp = tcpString.split("\\|");
            jsonString = tmp[0] + "|" + tmp[1];
            jsonString = tcpString;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = null;
                    byte[] rawImage;
                    arrayList_Playlist.clear();
                    addNewTracks();
                    tmp = tcpString.split("\\|");
                    if (tmp.length > 2) {
                        int nr = Integer.parseInt(tmp[2]);
                        if (nr > -1) {

                            adapter_Playlist.setSelectedIndex(nr);
                            /*
                            Object o = listView_Playlist.getItemAtPosition(nr);
                            currentTrack = (Track) o;
                            */
                            currentTrack = arrayList_Playlist.get(nr);
                            textView_Title.setText(currentTrack.titel);
                            textView_Album.setText(currentTrack.album);
                            textView_Artist.setText(currentTrack.artist);
                            if (currentTrack.favorite.equals("1"))
                            {
                                button_Favoriten.setBackgroundResource(R.drawable.toolbar_fav);
                            }
                            else
                            {
                                button_Favoriten.setBackgroundResource(R.drawable.toolbar_nofav);
                            }

                            String[] strings = {currentTrack.artist + " + " + currentTrack.album,
                                    currentTrack.albumId};


                            getCover = new AsyncCall_GetCover(Activity_Music.this);
                            getCover.execute(strings);

                        }
                    }
                    startIntent(TCPConstants.ArtistAlben + "|" + "ABC");
                }
            });
        }

        if(tcpString.startsWith(TCPConstants.newState))
        {
            String[] tmp = tcpString.split("\\|");
            if(tmp.length != 2)
                return;

            if(tmp[1].equals("0"))
            {
                Log.i(TAG, "State Idle");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button_Play.setBackgroundResource(R.drawable.toolbar_play);
                    }
                });
            }

            if(tmp[1].equals("1"))
            {
                Log.i(TAG, "State Play");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button_Play.setBackgroundResource(R.drawable.toolbar_pause);
                    }
                });
            }

            if(tmp[1].equals("2"))
            {
                Log.i(TAG, "State Paused");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button_Play.setBackgroundResource(R.drawable.toolbar_play);
                    }
                });
            }
        }
    }

    private void startIntent(String str)
    {
        tcpIntent.setAction(TCPService.ACTION_SEND);
        tcpIntent.putExtra("String", str +  "\n");
        startService(tcpIntent);
    }


    private class AsyncCall_GetCover extends AsyncTask<String, Void, Void>
    {
        /** progress dialog to show user that the backup is processing. */
        private ProgressDialog dialog;
        /** application context. */
        @SuppressWarnings("unused")
        private Activity activity;
        private Bitmap bitmap;

        public AsyncCall_GetCover(Activity activity)
        {
            //this.activity = activity;
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected Void doInBackground(String... params)
        {
            bitmap = dataBase.loadAlbumCover(params[0], params[1]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if (dialog.isShowing())
                dialog.dismiss();

            if(bitmap != null)
            {
                imageView_Cover.setImageBitmap(bitmap);
            }

            else
                imageView_Cover.setImageResource(R.drawable.no_cover);

            //Load Genre from DataBase
            //new AsyncCall_GetGenre().execute();
        }

        @Override
        protected void onPreExecute()
        {
            this.dialog.setMessage("Load Cover");
            this.dialog.show();
        }

    }
}
