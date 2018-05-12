package org.gareiss.mike.ramoc.tvshow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.gareiss.mike.ramoc.DataBase;
import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;
import org.gareiss.mike.ramoc.model.Actor;
import org.gareiss.mike.ramoc.model.Movie;
import org.gareiss.mike.ramoc.model.TVShow;
import org.gareiss.mike.ramoc.movie.Adapter_ActorList;
import org.gareiss.mike.ramoc.movie.Dialog_Subtitle;
import org.gareiss.mike.ramoc.tcp.TCPConstants;
import org.gareiss.mike.ramoc.tcp.TCPListener;
import org.gareiss.mike.ramoc.tcp.TCPService;

import java.util.ArrayList;

import static java.sql.Types.NULL;
import static org.gareiss.mike.ramoc.R.drawable.movie;

public class Activity_TVShow extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TCPListener {

    public static final String STATE_IDLE    = "0";
    public static final String STATE_PLAYING = "1";
    public static final String STATE_PAUSED  = "2";

    private String TAG = "Activity_TVShow";
    private TextView textView_MovieTitle;
    private TextView textView_MoviePlot;
    private TextView textView_FSK;
    private TextView textView_Runtime;
    private ImageView imageView_MovieCover;
    private Movie tvShow;
    private DataBase dataBase;
    private ImageView imageView_Poster;
    private RecyclerView listView_Actor;
    private RaMoCApplication ramocApp;
    //private ArrayList<Actor>    arrayList_Actor;
    private Adapter_ActorList adapter_ActorList;
    private Intent tcpIntent;
    private AsyncCallTVShowPlot tvShowPlot;
    private Menu navMenu;
    public MenuItem item_PlayPause;
    public Boolean bool_isMovie;
    public Boolean bool_isEpisoden;
    private RelativeLayout relativeLayout;
    private ListView listView_Episoden;
    private ScrollView scrollView;
    private ArrayList<TVShow> arrayList_Episode;
    private Adapter_Episoden adapter_Episoden;
    private Button button_Play;
    private String              state;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tvshow);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        button_Play = (Button) findViewById(R.id.play);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navMenu = navigationView.getMenu();

        textView_MovieTitle = (TextView) findViewById(R.id.tvshow_TextView_Title);
        textView_MoviePlot = (TextView) findViewById(R.id.tvshow_TextView_Plot);
        textView_FSK = (TextView) findViewById(R.id.tvshow_TextView_fsk);
        textView_Runtime = (TextView) findViewById(R.id.tvshow_TextView_Runtime);
        imageView_Poster = (ImageView) findViewById(R.id.tvshow_ImageView_Poster);
        imageView_MovieCover = (ImageView) findViewById(R.id.tvshow_ImageView_Cover);
        listView_Actor = (RecyclerView) findViewById(R.id.tvshow_ListView);
        listView_Actor.setVisibility(RecyclerView.INVISIBLE);
        relativeLayout = (RelativeLayout) findViewById(R.id.content_activity_tvshow);
        listView_Episoden = (ListView) findViewById(R.id.tvshow_Episoden);
        listView_Episoden.setVisibility(ListView.INVISIBLE);
        scrollView = (ScrollView) findViewById(R.id.tvshow_ScrollView);
        ramocApp = (RaMoCApplication) getApplicationContext();
        ramocApp.addTCPListener(this);
        tvShow = new Movie();
        dataBase = ramocApp.getDataBase();
        tcpIntent = ramocApp.getTcpIntent();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tvShow.setTitel(extras.getString("TVShowTitle"));
            textView_MovieTitle.setText(tvShow.getTitel());
            tvShow.setCover(extras.getByteArray("Cover"));
            imageView_MovieCover.setImageBitmap(tvShow.getCover());
            tvShow.setId(extras.getString("TVShowId"));
            Log.e(TAG, extras.getString("TVShowId"));

        }

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Inform the user the button has been clicked
                imageView_MovieCover.setVisibility(ImageView.VISIBLE);
                listView_Actor.setVisibility(RecyclerView.INVISIBLE);
            }
        });

        listView_Episoden.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id)
            {
                Object o = listView_Episoden.getItemAtPosition(position);
                tvShow.setFile(((TVShow) o).getFile());
                Log.i(TAG, tvShow.getFile());
                tcpIntent.setAction(TCPService.ACTION_SEND);
                tcpIntent.putExtra("String", TCPConstants.play + tvShow.getFile() + "\n");
                ramocApp.setCurrentPath(tvShow.getFile());
                startService(tcpIntent);
                //startIntent(TCPConstants.playMovie + tvShow.getFile());

                new AsyncCall_GetEpisoden().execute();
            }
        });

        tvShowPlot = new AsyncCallTVShowPlot();
        tvShowPlot.execute();

        bool_isEpisoden = false;
        bool_isMovie = true;
    }

    @Override
    protected void onResume()
    {
        Log.i(TAG, "onResume");
        super.onResume();
        state = ramocApp.getState();
        Log.i(TAG, "State = " + state);

        item_PlayPause = navMenu.findItem(R.id.nav_TVShow_Play);
        if(state.equals(STATE_IDLE ) || state.equals(STATE_PAUSED))
        {
            button_Play.setBackgroundResource(R.drawable.toolbar_play);
            item_PlayPause.setIcon(R.drawable.play);
            item_PlayPause.setTitle("Play");
        }
        if(state.equals(STATE_PLAYING ) )
        {
            button_Play.setBackgroundResource(R.drawable.toolbar_pause);
            item_PlayPause.setIcon(R.drawable.pause);
            item_PlayPause.setTitle("Pause");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity__tvshow, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    public void button_Play_Click(View view)
    {
        tcpIntent.setAction(TCPService.ACTION_SEND);
        tcpIntent.putExtra("String", TCPConstants.play + ramocApp.getCurrentPath()+ "\n");
        startService(tcpIntent);
    }

    public void button_Stop_Click(View view)
    {
        tcpIntent.setAction(TCPService.ACTION_SEND);
        tcpIntent.putExtra("String", TCPConstants.playerStop +  "\n");
        startService(tcpIntent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (id == R.id.nav_TVShow_Actor) {
            imageView_MovieCover.setVisibility(ImageView.INVISIBLE);
            listView_Actor.setVisibility(RecyclerView.VISIBLE);
            drawer.closeDrawer(GravityCompat.START);
        }
        if (id == R.id.nav_TVShow_Episoden) {
            if (bool_isEpisoden) {
                imageView_MovieCover.setVisibility(ImageView.VISIBLE);
                listView_Actor.setVisibility(RecyclerView.INVISIBLE);
                listView_Episoden.setVisibility(ListView.INVISIBLE);
                scrollView.setVisibility(ScrollView.VISIBLE);

                bool_isMovie = true;
                bool_isEpisoden = false;
            } else {
                imageView_MovieCover.setVisibility(ImageView.VISIBLE);
                listView_Actor.setVisibility(RecyclerView.INVISIBLE);
                listView_Episoden.setVisibility(ListView.VISIBLE);
                scrollView.setVisibility(ScrollView.INVISIBLE);

                bool_isMovie = true;
                bool_isEpisoden = true;

                new AsyncCall_GetEpisoden().execute();
            }
            drawer.closeDrawer(GravityCompat.START);
        }
        if (id == R.id.nav_TVShow_Play)
        {
            //if(!test_CurrentPath()) return true;

            tcpIntent.setAction(TCPService.ACTION_SEND);
            tcpIntent.putExtra("String", TCPConstants.play + ramocApp.getCurrentPath()+ "\n");
            startService(tcpIntent);
            drawer.closeDrawer(GravityCompat.START);
        }

        if (id == R.id.nav_TVShow_Stop)
        {
            tcpIntent.setAction(TCPService.ACTION_SEND);
            tcpIntent.putExtra("String", TCPConstants.playerStop +  "\n");
            startService(tcpIntent);
            drawer.closeDrawer(GravityCompat.START);
        }

        if (id == R.id.nav_TVShow_forward)
        {
            tcpIntent.setAction(TCPService.ACTION_SEND);
            tcpIntent.putExtra("String", TCPConstants.forward +  "\n");
            startService(tcpIntent);
        }

        if (id == R.id.nav_TVShow_backward)
        {
            tcpIntent.setAction(TCPService.ACTION_SEND);
            tcpIntent.putExtra("String", TCPConstants.backward +  "\n");
            startService(tcpIntent);
        }

        if(id == R.id.nav_TVShow_Archive)
        {
            tcpIntent.setAction(TCPService.ACTION_SEND);
            tcpIntent.putExtra("String", TCPConstants.archiveMovie + "|" + tvShow.getId() + "\n");
            startService(tcpIntent);

            Log.e(TAG, "Archive: " + tvShow.getId());
            //drawer.closeDrawer(GravityCompat.START);
           finish();
        }

        if(id == R.id.nav_TVShow_Subtitle)
        {

            Intent myIntent = new Intent(getApplicationContext(), Dialog_Subtitle.class);
            startActivity(myIntent);
            tcpIntent.setAction(TCPService.ACTION_SEND);
        }

        return true;
    }

    void setActors() {
        adapter_ActorList = new Adapter_ActorList(tvShow.getActor());
        listView_Actor.setAdapter(adapter_ActorList);
        tvShowPlot.cancel(true);
    }

    void setAdapter() {

		/*ListView.getFirstVisiblePosition() returns the top visible list item. But this item may
		 * be partially scrolled out of view, and if you want to restore the exact scroll position
		 * of the list you need to get this offset. So ListView.getChildAt(0) returns the View for
		 * the top list item, and then View.getTop() - mList.getPaddingTop() returns its relative
		 * offset from the top of the ListView. Then, to restore the ListView's scroll position, we
		 * call ListView.setSelectionFromTop() with the index of the item we want and an offset to
		 * position its top edge from the top of the ListView.
		 */

        int index = listView_Episoden.getFirstVisiblePosition();
        View v = listView_Episoden.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - listView_Episoden.getPaddingTop());

        adapter_Episoden = new Adapter_Episoden(this, arrayList_Episode);

        listView_Episoden.setAdapter(adapter_Episoden);

        listView_Episoden.setSelectionFromTop(index, top);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     * public Action getIndexApiAction() {
     * Thing object = new Thing.Builder()
     * .setName("Activity_TVShow Page") // TODO: Define a title for the content shown.
     * // TODO: Make sure this auto-generated URL is correct.
     * .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
     * .build();
     * return new Action.Builder(Action.TYPE_VIEW)
     * .setObject(object)
     * .setActionStatus(Action.STATUS_TYPE_COMPLETED)
     * .build();
     * }
     *
     * @Override public void onStart() {
     * super.onStart();
     * <p>
     * // ATTENTION: This was auto-generated to implement the App Indexing API.
     * // See https://g.co/AppIndexing/AndroidStudio for more information.
     * client.connect();
     * AppIndex.AppIndexApi.start(client, getIndexApiAction());
     * }
     * @Override public void onStop() {
     * super.onStop();
     * <p>
     * // ATTENTION: This was auto-generated to implement the App Indexing API.
     * // See https://g.co/AppIndexing/AndroidStudio for more information.
     * AppIndex.AppIndexApi.end(client, getIndexApiAction());
     * client.disconnect();
     * }
     */
    private class AsyncCallTVShowPlot extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            dataBase.getMovieInfo(tvShow);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            textView_MoviePlot.setText(tvShow.getBeschreibung());
            textView_FSK.setText("FSK: " + tvShow.getFSK());
            textView_Runtime.setText("Laufzeit: " + tvShow.getLaufzeit() + " min");
            imageView_Poster.setImageBitmap(tvShow.getPoster());
            setActors();
        }

        @Override
        protected void onPreExecute() {
            //Log.i(TAG, "onPreExecute");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // Log.i(TAG, "onProgressUpdate");
        }

    }

    private class AsyncCall_GetEpisoden extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            arrayList_Episode = dataBase.getEpisoden(tvShow.getId());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            setAdapter();
        }
    }

    /*
    public boolean test_CurrentPath()
    {
        boolean bool_return = false;
        if(arrayList_Episode == null) return false;

        if(ramocApp.getCurrentPath() == null) return false;

        for(int i = 0; i < arrayList_Episode.size(); i++)
        {

            if(ramocApp.getCurrentPath().equals(arrayList_Episode.get(i).getFile()))
            {
                bool_return = true;
                break;
            }
        }

        return bool_return;
    }*/

    public void onTCPMessage(String tcpString)
    {
        String[] tmp = tcpString.split("\\|");

        if(tmp[0].startsWith(TCPConstants.playedSet))
        {
            Log.i(TAG, "PlayedSet");
            new AsyncCall_GetEpisoden().execute();
        }

        if(tcpString.startsWith(TCPConstants.newState)) {
            if (tmp.length != 2)
                return;

            if (tmp[1].equals("0")) {
                Log.i(TAG, "State Idle");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button_Play.setBackgroundResource(R.drawable.toolbar_play);
                        item_PlayPause.setIcon(R.drawable.play);
                        item_PlayPause.setTitle("Play");
                    }
                });
            }

            if (tmp[1].equals("1")) {
                Log.i(TAG, "State Play");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button_Play.setBackgroundResource(R.drawable.toolbar_pause);
                        item_PlayPause.setIcon(R.drawable.pause);
                        item_PlayPause.setTitle("Pause");
                    }
                });
            }

            if (tmp[1].equals("2")) {
                Log.i(TAG, "State Paused");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button_Play.setBackgroundResource(R.drawable.toolbar_play);
                        item_PlayPause.setIcon(R.drawable.play);
                        item_PlayPause.setTitle("Play");
                    }
                });
            }
        }
    }
}
