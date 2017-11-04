package org.gareiss.mike.ramoc.youtube;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;
import org.gareiss.mike.ramoc.YoutubeAPI;
import org.gareiss.mike.ramoc.model.Youtube;
import org.gareiss.mike.ramoc.tcp.TCPConstants;
import org.gareiss.mike.ramoc.tcp.TCPService;

import java.util.ArrayList;

public class Activity_Youtube extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private String TAG = "activity_youtube";
    private EditText editText;
    private YoutubeAPI youtube;
    private AsyncTask_Youtube asyncTask_Youtube;
    private ArrayList<Youtube> arrayList_Youtube;
    private ListView listView;
    private Adapter_Youtube adapter_Youtube;

    private RaMoCApplication ramocApp;
    private Intent tcpIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);
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
        tcpIntent = ramocApp.getTcpIntent();

        editText = (EditText) findViewById(R.id.editText);
        editText.setOnEditorActionListener(new OnEditorActionListener());

        listView = (ListView) findViewById(R.id.listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id)
            {
                //adapter_Playlist.setSelectedIndex(position);
                Object o = listView.getItemAtPosition(position);
                Youtube yt = (Youtube) o;

                startIntent(TCPConstants.playYoutube + "|" + yt.video_Url);
            }
        });
        youtube = new YoutubeAPI();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity__youtube, menu);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class OnEditorActionListener implements EditText.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
        {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                asyncTask_Youtube = new_AsyncTask_Youtube();
                asyncTask_Youtube.execute(editText.getText().toString());
                return true;
            }
            return false;
        }
    }

    private void setAdapter()
    {
        adapter_Youtube = new Adapter_Youtube(this, arrayList_Youtube);
        listView.setAdapter(adapter_Youtube);

    }
    private class AsyncTask_Youtube extends AsyncTask<String, Void, Void>
    {
        /** progress dialog to show user that the backup is processing. */
        private ProgressDialog dialog;
        private ArrayList<String> arrayList;

        /** application context. */
        @SuppressWarnings("unused")
        private Activity activity;

        public AsyncTask_Youtube(Activity activity)
        {
            this.activity = activity;
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected Void doInBackground(String... params)
        {
            Log.i(TAG, "doInBackground");
            arrayList_Youtube = youtube.search_By_Keyword(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if (dialog.isShowing())
                dialog.dismiss();

            setAdapter();
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Load");
            this.dialog.show();
        }
    }

    public AsyncTask_Youtube new_AsyncTask_Youtube()
    {
        return new AsyncTask_Youtube(this);
    }

    private void startIntent(String str)
    {
        tcpIntent.setAction(TCPService.ACTION_SEND);
        tcpIntent.putExtra("String", str +  "\n");
        startService(tcpIntent);
    }
}
