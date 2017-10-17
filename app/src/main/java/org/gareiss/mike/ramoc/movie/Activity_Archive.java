package org.gareiss.mike.ramoc.movie;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import org.gareiss.mike.ramoc.DataBase;
import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;
import org.gareiss.mike.ramoc.model.Movie;
import org.gareiss.mike.ramoc.tcp.TCPConstants;

import java.util.ArrayList;
import java.util.List;

public class Activity_Archive extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private String TAG ="Archive";
    public DataBase         dataBase;
    public ArrayList<Movie> arrayList_Movie;
    public ArrayList<Movie> arrayList_MovieSelection;
    private RaMoCApplication ramocApp;

    public ListView listView;
    public Adapter_ArchiveList adapter_archiveList;
    public AsyncCall_GetArchive getArchive;
    public AsyncCall_GetGenre getGenre;
    private EditText editText;
    private Activity_Archive thispointer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView =
                       (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ramocApp = (RaMoCApplication)  getApplicationContext();
        dataBase = ramocApp.getDataBase();
        listView = (ListView) findViewById(R.id.listView);
        editText = (EditText) findViewById(R.id.editText);
        thispointer = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id)
            {
                //adapter_Playlist.setSelectedIndex(position);
                Object o = listView.getItemAtPosition(position);
                Movie obj_Movie = (Movie) o;
                Intent myIntent = new Intent(getApplicationContext(),
                                             Dialog_Archive.class);

                myIntent.putExtra("MovieTitle",obj_Movie.getTitel());
                myIntent.putExtra("MovieId", obj_Movie.getId());
                myIntent.putExtra("Cover",obj_Movie.getCoverAsByteArry());
                startActivity(myIntent);
            }
        });

        editText.addTextChangedListener(new TextWatcher()
        {
            @SuppressLint("DefaultLocale")
            public void afterTextChanged(Editable s)
            {
                arrayList_MovieSelection = null;
                adapter_archiveList = null;
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
                adapter_archiveList = new Adapter_ArchiveList(thispointer, arrayList_MovieSelection);
                listView.setAdapter(adapter_archiveList);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }
        });
        getArchive = new AsyncCall_GetArchive(Activity_Archive.this);
        getArchive.execute(" ");
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
        getMenuInflater().inflate(R.menu.activity__archive, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.fsk0)
        {
            // Handle the camera action
            getArchive = new AsyncCall_GetArchive(Activity_Archive.this);
            getArchive.execute("0");
        }
        else if (id == R.id.fsk6)
        {
            getArchive = new AsyncCall_GetArchive(Activity_Archive.this);
            getArchive.execute("6");

        }
        else if (id == R.id.fsk12)
        {
            getArchive = new AsyncCall_GetArchive(Activity_Archive.this);
            getArchive.execute("12");

        }
        else if (id == R.id.fsk16)
        {
            getArchive = new AsyncCall_GetArchive(Activity_Archive.this);
            getArchive.execute("16");

        }
        else if (id == R.id.fsk18)
        {
            getArchive = new AsyncCall_GetArchive(Activity_Archive.this);
            getArchive.execute("18");
        }
        else
        {
            Log.i(TAG, "Genre: " + item.getTitle().toString());
            getGenre = new AsyncCall_GetGenre(Activity_Archive.this);
            getGenre.execute(item.getTitle().toString());
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void setAdapter()
    {
        Log.i(TAG, "Set Adapter");

        adapter_archiveList = new Adapter_ArchiveList(this, arrayList_Movie);
        listView.setAdapter(adapter_archiveList);
        getArchive.cancel(true);
    }

    private class AsyncCall_GetArchive extends AsyncTask<String, Void, Void>
    {
        /** progress dialog to show user that the backup is processing. */
        private ProgressDialog dialog;
        /** application context. */
        @SuppressWarnings("unused")
        private Activity activity;

        public AsyncCall_GetArchive(Activity activity)
        {
            //this.activity = activity;
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected Void doInBackground(String... params)
        {
            arrayList_Movie = dataBase.getArchive(params[0]);
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
    private class AsyncCall_GetGenre extends AsyncTask<String, Void, Void>
    {
        /** progress dialog to show user that the backup is processing. */
        private ProgressDialog dialog;
        /** application context. */
        @SuppressWarnings("unused")
        private Activity activity;

        public AsyncCall_GetGenre(Activity activity)
        {
            //this.activity = activity;
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected Void doInBackground(String... params)
        {
            arrayList_Movie = dataBase.getGenre(params[0]);
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
}
