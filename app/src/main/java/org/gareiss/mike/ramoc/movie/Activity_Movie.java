package org.gareiss.mike.ramoc.movie;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.gareiss.mike.ramoc.DataBase;
import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;
import org.gareiss.mike.ramoc.model.Actor;
import org.gareiss.mike.ramoc.model.Movie;
import org.gareiss.mike.ramoc.tcp.TCPListener;
import org.gareiss.mike.ramoc.tcp.TCPService;
import org.gareiss.mike.ramoc.tcp.TCPConstants;

import java.util.ArrayList;

public class Activity_Movie extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TCPListener
{
    public static final String STATE_IDLE    = "0";
    public static final String STATE_PLAYING = "1";
    public static final String STATE_PAUSED  = "2";

    private String 				TAG ="Activity_Movie";
    private TextView 			textView_MovieTitle;
    private TextView 			textView_MoviePlot;
    private TextView 			textView_FSK;
    private TextView            textView_Runtime;
    private ImageView			imageView_MovieCover;
    private Movie               movie;
    private DataBase            dataBase;
    private ImageView           imageView_Poster;
    private RecyclerView        listView_Actor;
    private RaMoCApplication    ramocApp;
    private ArrayList<Actor>    arrayList_Actor;
    private Adapter_ActorList	adapter_ActorList;
    private Intent              tcpIntent;
    private AsyncCallMoviePlot  moviePlot;
    private Menu                navMenu;
    public MenuItem             item_PlayPause;
    public Boolean              bool_isMovie;
    private RelativeLayout      relativeLayout;
    private String              state;
    private Button              button_Play;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
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

        textView_MovieTitle = (TextView) findViewById(R.id.movie_TextView_Title);
        textView_MoviePlot = (TextView) findViewById(R.id.movie_TextView_Plot);
        textView_FSK = (TextView) findViewById(R.id.movie_TextView_fsk);
        textView_Runtime = (TextView) findViewById(R.id.movie_TextView_Runtime);
        imageView_Poster = (ImageView) findViewById(R.id.movie_ImageView_Poster);
        imageView_MovieCover = (ImageView) findViewById(R.id.movie_ImageView_Cover);
        listView_Actor = (RecyclerView) findViewById(R.id.movie_ListView);
        listView_Actor.setVisibility(RecyclerView.INVISIBLE);
        relativeLayout = (RelativeLayout) findViewById(R.id.content_activity__movie);

        ramocApp = (RaMoCApplication) getApplicationContext();
        ramocApp.addTCPListener(this);
        movie = new Movie();
        dataBase = ramocApp.getDataBase();
        tcpIntent = ramocApp.getTcpIntent();
        bool_isMovie = true;

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            movie.setTitel(extras.getString("MovieTitle"));
            textView_MovieTitle.setText(movie.getTitel());
            movie.setCover(extras.getByteArray("Cover"));
            imageView_MovieCover.setImageBitmap(movie.getCover());
            movie.setId(extras.getString("MovieId"));
        }

        Log.i(TAG, "Start AsyncCall");
   //     new AsyncCallMoviePlot().execute();
        moviePlot = new AsyncCallMoviePlot();
        moviePlot.execute();


        relativeLayout.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //Inform the user the button has been clicked
                imageView_MovieCover.setVisibility(ImageView.VISIBLE);
                listView_Actor.setVisibility(RecyclerView.INVISIBLE);
            }
        });
    }

    @Override
    protected void onResume()
    {
        Log.i(TAG, "onResume");
        super.onResume();
        state = ramocApp.getState();
        Log.i(TAG, "State = " + state);

        item_PlayPause = navMenu.findItem(R.id.nav_Movie_Play);
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
        ramocApp.delTCPListener(this);
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity__movie, menu);
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
        tcpIntent.putExtra("String", TCPConstants.play + movie.getFile() + "\n");
        ramocApp.setCurrentPath(movie.getFile());
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
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (id == R.id.nav_Movie_Play)
        {
            tcpIntent.setAction(TCPService.ACTION_SEND);
            tcpIntent.putExtra("String", TCPConstants.play + movie.getFile() + "\n");
            ramocApp.setCurrentPath(movie.getFile());
            startService(tcpIntent);
            drawer.closeDrawer(GravityCompat.START);
        }

        if (id == R.id.nav_Movie_Stop)
        {
            tcpIntent.setAction(TCPService.ACTION_SEND);
            tcpIntent.putExtra("String", TCPConstants.playerStop +  "\n");
            startService(tcpIntent);
            drawer.closeDrawer(GravityCompat.START);
        }

        if (id == R.id.nav_Movie_forward)
        {
            tcpIntent.setAction(TCPService.ACTION_SEND);
            tcpIntent.putExtra("String", TCPConstants.forward +  "\n");
            startService(tcpIntent);
        }

        if (id == R.id.nav_Movie_backward)
        {
            tcpIntent.setAction(TCPService.ACTION_SEND);
            tcpIntent.putExtra("String", TCPConstants.backward +  "\n");
            startService(tcpIntent);
        }

     /*   if (id == R.id.nav_Movie_10Min_backward)
        {
            tcpIntent.setAction(TCPService.ACTION_SEND);
            tcpIntent.putExtra("String", TCPConstants.minutesBackward +  "\n");
            startService(tcpIntent);
        }

        if (id == R.id.nav_Movie_10Min_forward)
        {

            startIntent(TCPConstants.minutesForward);
        }
     */

        if (id == R.id.nav_Movie_Actor)
        {
            imageView_MovieCover.setVisibility(ImageView.INVISIBLE);
            listView_Actor.setVisibility(RecyclerView.VISIBLE);
            drawer.closeDrawer(GravityCompat.START);
        }

        if(id == R.id.nav_Movie_Archive)
        {
            startIntent(TCPConstants.archiveMovie + "|" + movie.getId());
            finish();
        }

        if(id == R.id.nav_Movie_delete)
        {
            startIntent(TCPConstants.deleteMovie + "|" + movie.getId());
            drawer.closeDrawer(GravityCompat.START);
            this.finish();
        }

        if(id == R.id.nav_Movie_Subtitle)
        {

            Intent myIntent = new Intent(getApplicationContext(), Dialog_Subtitle.class);
            startActivity(myIntent);
        }

        return true;
    }

    private class AsyncCallMoviePlot extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            Log.i(TAG, "doInBackground");
            dataBase.getMovieInfo(movie);
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            Log.i(TAG, "onPostExecute");
            textView_MoviePlot.setText(movie.getBeschreibung());
            textView_FSK.setText("FSK: " + movie.getFSK());
            textView_Runtime.setText("Laufzeit: " + movie.getLaufzeit() + " min");
            imageView_Poster.setImageBitmap(movie.getPoster());
            setActors();
            /*
            if(movie.getTrailer().isEmpty())
            {
                imageButton_Trailer.setVisibility(ImageButton.INVISIBLE);
            }
            else
            {
                imageButton_Trailer.setVisibility(ImageButton.VISIBLE);
            }
            */

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

    void setActors()
    {
        adapter_ActorList = new Adapter_ActorList(movie.getActor());
        listView_Actor.setAdapter(adapter_ActorList);
        moviePlot.cancel(true);
    }

    @Override
    public void onTCPMessage(String tcpString)
    {
        Log.e(TAG, "onTCPMessage: " + tcpString);

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
                        item_PlayPause.setIcon(R.drawable.play);
                        item_PlayPause.setTitle("Play");
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
                        item_PlayPause.setIcon(R.drawable.pause);
                        item_PlayPause.setTitle("Pause");
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
                        item_PlayPause.setIcon(R.drawable.play);
                        item_PlayPause.setTitle("Play");
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
}
