package org.gareiss.mike.ramoc;

import android.content.Intent;
import android.os.Bundle;
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

import org.gareiss.mike.ramoc.movie.Activity_Archive;
import org.gareiss.mike.ramoc.movie.Activity_MovieList;
import org.gareiss.mike.ramoc.music.Activity_Music;
import org.gareiss.mike.ramoc.settings.Activity_Settings;
import org.gareiss.mike.ramoc.tcp.TCPConstants;
import org.gareiss.mike.ramoc.tcp.TCPListener;
import org.gareiss.mike.ramoc.tcp.TCPService;
import org.gareiss.mike.ramoc.tv.Activity_TV;
import org.gareiss.mike.ramoc.tvshow.Activity_TVShowList;
import org.gareiss.mike.ramoc.youtube.Activity_Youtube;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        TCPListener
{
    public static final String STATE_IDLE    = "0";
    public static final String STATE_PLAYING = "1";
    public static final String STATE_PAUSED  = "2";

    private String				TAG = "MainActivity";
    private Intent              mIntent;
    private String				string_RaspberryIP;
    private RaMoCApplication 	ramocApp;
    private String              state;
    public MenuItem             item_PlayPause;
    private Menu                navMenu;
    private Intent              tcpIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        ramocApp = (RaMoCApplication) getApplication();
        ramocApp.addTCPListener(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.i(TAG, "onResume");
        string_RaspberryIP = ramocApp.getRaspberryIP();

        if(string_RaspberryIP.equals("0.0.0.0"))
        {
            Log.i(TAG, "RaspberryIP 0.0.0.0");
            mIntent = new Intent(getApplicationContext(), Activity_Settings.class);
            startActivity(mIntent);
        }

        Intent intent = new Intent(MainActivity.this, TCPService.class);
        intent.setAction(TCPService.ACTION_CONNECT);
        startService(intent);
        ramocApp.setTcpIntent(intent);
        tcpIntent = intent;

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
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
        if (id == R.id.action_settings)
        {
            mIntent = new Intent(getApplicationContext(), Activity_Settings.class);
            startActivity(mIntent);

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

    public void button_Movie_Click(View view)
    {
        mIntent = new Intent(getApplicationContext(), Activity_MovieList.class);
        startActivity(mIntent);
    }

    public void button_TVShow_Click(View view)
    {
        mIntent = new Intent(getApplicationContext(), Activity_TVShowList.class);
        startActivity(mIntent);
    }

    public void button_Archive_Click(View view)
    {
        mIntent = new Intent(getApplicationContext(), Activity_Archive.class);
        startActivity(mIntent);
    }

    public void button_TV_Click(View view)
    {
        mIntent = new Intent(getApplicationContext(), Activity_TV.class);
        startActivity(mIntent);
    }

    public void button_Music_Click(View view)
    {
        mIntent = new Intent(getApplicationContext(), Activity_Music.class);
        startActivity(mIntent);
    }

    public void button_Youtube_Click(View view)
    {
        mIntent = new Intent(getApplicationContext(), Activity_Youtube.class);
        startActivity(mIntent);
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
