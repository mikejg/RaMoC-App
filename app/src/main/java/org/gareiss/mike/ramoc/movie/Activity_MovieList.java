package org.gareiss.mike.ramoc.movie;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import org.gareiss.mike.ramoc.DataBase;
import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;
import org.gareiss.mike.ramoc.model.Movie;
import org.gareiss.mike.ramoc.tcp.TCPConstants;
import org.gareiss.mike.ramoc.tcp.TCPListener;
import org.gareiss.mike.ramoc.tcp.TCPService;
import org.gareiss.mike.ramoc.tools.RecyclerItemClickListener;

import java.util.ArrayList;

public class Activity_MovieList extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        TCPListener
{
    private String TAG ="MovieList";

    public static final String STATE_IDLE    = "0";
    public static final String STATE_PLAYING = "1";
    public static final String STATE_PAUSED  = "2";

    public  DataBase            dataBase;
    public ArrayList<Movie>    arrayList_Movie;
    private ArrayList<Movie>    arrayList_MovieSelection;
    public RecyclerView        listView_Movie;
    private Adapter_MovieList   adapter_MovieList;
    private Menu                navMenu;
    public MenuItem             item_PlayPause;
    private Intent              tcpIntent;
    private RaMoCApplication    ramocApp;
    public AsyncCall_GetList   getList;
    private String              state;
    private EditText            editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navMenu = navigationView.getMenu();

        ramocApp = (RaMoCApplication)  getApplicationContext();
        dataBase = ramocApp.getDataBase();
        tcpIntent = ramocApp.getTcpIntent();
        ramocApp.addTCPListener(this);

        listView_Movie = (RecyclerView) findViewById(R.id.movieList_ListView);
        editText = (EditText) findViewById(R.id.movieList_EditText);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        listView_Movie.setLayoutManager(mLayoutManager);
        listView_Movie.addOnItemTouchListener(
                new RecyclerItemClickListener(this, listView_Movie,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Object o = adapter_MovieList.getItem(position);
                        Movie obj_Movie = (Movie) o;

                        Intent myIntent = new Intent(getApplicationContext(), Activity_Movie.class);
                        myIntent.putExtra("MovieTitle",obj_Movie.getTitel());
                        myIntent.putExtra("MovieId", obj_Movie.getId());
                        myIntent.putExtra("Cover",obj_Movie.getCoverAsByteArry());
                        startActivity(myIntent);

                        Log.i(TAG, obj_Movie.getTitel());
                        // do whatever
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );

        editText.addTextChangedListener(new TextWatcher()
        {
            @SuppressLint("DefaultLocale")
            public void afterTextChanged(Editable s)
            {
                arrayList_MovieSelection = null;
                adapter_MovieList = null;
                System.gc();

                Movie movie;
                String stringName;
                String stringEditText;
                arrayList_MovieSelection = new ArrayList<Movie>();

                for(int i = 0; i < arrayList_Movie.size(); i++)
                {
                    movie = arrayList_Movie.get(i);
                    stringName = movie.getTitel();
                    stringEditText = s.toString();

                    if(stringName.toLowerCase().contains(stringEditText.toLowerCase()))
                    {
                        arrayList_MovieSelection.add(movie);
                    }
                }
                adapter_MovieList = new Adapter_MovieList(arrayList_MovieSelection);
                listView_Movie.setAdapter(adapter_MovieList);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }
        });
        getList = new AsyncCall_GetList(Activity_MovieList.this);
        getList.execute(false);
    }

    @Override
    protected void onResume()
    {
        Log.i(TAG, "onResume");
        super.onResume();
        state = ramocApp.getState();
        Log.i(TAG, "State = " + state);

        item_PlayPause = navMenu.findItem(R.id.nav_main_Play);
        if(state.equals(STATE_IDLE ) || state.equals(STATE_PAUSED))
        {
            item_PlayPause.setIcon(R.drawable.play);
            item_PlayPause.setTitle("Play");
        }
        if(state.equals(STATE_PLAYING ) )
        {
            Log.i(TAG, "State Playing");
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main_Play)
        {
            Log.i(TAG, "Play");
            tcpIntent.setAction(TCPService.ACTION_SEND);
            tcpIntent.putExtra("String", TCPConstants.play +
                                         ramocApp.getCurrentPath() + "\n");
            startService(tcpIntent);
        }

        if (id == R.id.nav_main_Stop)
        {
            tcpIntent.setAction(TCPService.ACTION_SEND);
            tcpIntent.putExtra("String", TCPConstants.playerStop +  "\n");
            startService(tcpIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void setAdapter()
    {
        Log.i(TAG, "Set Adapter");

        arrayList_MovieSelection = new ArrayList<Movie>();
        for(int i = 0; i < arrayList_Movie.size(); i++)
        {
            arrayList_MovieSelection.add(arrayList_Movie.get(i));
        }

        adapter_MovieList = new Adapter_MovieList(arrayList_MovieSelection);
        listView_Movie.setAdapter(adapter_MovieList);
        getList.cancel(true);
    }


    private class AsyncCall_GetList extends AsyncTask<Boolean, Void, Void>
    {
        /** progress dialog to show user that the backup is processing. */
        private ProgressDialog dialog;
        /** application context. */
        @SuppressWarnings("unused")
        private Activity activity;

        public AsyncCall_GetList(Activity activity)
        {
            //this.activity = activity;
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected Void doInBackground(Boolean... params)
        {
            arrayList_Movie = dataBase.getList(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if (dialog.isShowing())
                dialog.dismiss();
            setAdapter();

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
    @Override
    public void onTCPMessage(String tcpString)
    {
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
                    }
                });
            }
        }
    }
}
