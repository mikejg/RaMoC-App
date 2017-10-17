package org.gareiss.mike.ramoc.tv;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;
import org.gareiss.mike.ramoc.model.Channel;
import org.gareiss.mike.ramoc.model.HttpTicket;
import org.gareiss.mike.ramoc.model.Programme;
import org.gareiss.mike.ramoc.model.Recording;
import org.gareiss.mike.ramoc.tcp.TCPConstants;
import org.gareiss.mike.ramoc.tcp.TCPService;
import org.gareiss.mike.ramoc.tv.htsp.HTSListener;
import org.gareiss.mike.ramoc.tv.htsp.HTSService;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Activity_TV extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, HTSListener
{
    private String				    TAG = "RaMoC TV_Activity";
    private ChannelListAdapter 	    chAdapter;
    private ListView                listView_Channel;
    private ListView			    listView_Programm;
    private String 				    httpURL;
    private volatile String		    oldURL;
    private List<Programme>         prList;
    private ProgrammeListAdapter    prAdapter;
    private Channel                 channel;
    private Programme			    programm;
    private TextView                textView_HeaderProgramm;
    private String				    hostname;
    private String				    username;
    private String				    password;
    private int					    port;
    public	long				    timerChannelId;
    private RaMoCApplication        ramocApp;
    private Intent                  tcpIntent;
    private Intent                  htsIntent;
    private Button                  button_Play;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv);
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

        ramocApp.addListener(this);
        tcpIntent = ramocApp.getTcpIntent();

        listView_Programm = (ListView) findViewById(R.id.tv_ProgrammList);
        textView_HeaderProgramm = new TextView(this);
        textView_HeaderProgramm.setTextSize(30);
        textView_HeaderProgramm.setBackgroundColor(getResources().getColor(R.color.grey));
        listView_Programm.addHeaderView(textView_HeaderProgramm);

        listView_Channel = (ListView) findViewById(R.id.tv_ChannelList);
        listView_Channel.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		/* Adapter für die Programmliste */
        chAdapter = new ChannelListAdapter(this, new ArrayList<Channel>());
        listView_Channel.setAdapter(chAdapter);

        //Eine Sender wird angeclickt
        listView_Channel.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id)
            {
                chAdapter.setSelectedIndex(position);
                Object o = listView_Channel.getItemAtPosition(position);
                channel = (Channel) o;

                setProgrammAdapter(channel);
            }
        });

        //Ein Programm wird angeclickt
        listView_Programm.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id)
            {
                RaMoCApplication app = (RaMoCApplication) getApplication();

                Object o = listView_Programm.getItemAtPosition(position);
                programm = (Programme) o;
                app.setCurrentProgramm(programm);

                Intent myIntent = new Intent(getApplicationContext(), Dialog_ProgrammInfo.class);
                startActivity(myIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String str = ramocApp.getTvHeadEnd();
        String[] array_str = str.split("\\|");
        hostname = array_str[0];
        port = 9982;
        username = array_str[1];
        password = array_str[2];
        connect(false);
        setLoading(ramocApp.isLoading());
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
        getMenuInflater().inflate(R.menu.activity__tv, menu);
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
        if ( channel != null)
        {
            htsIntent.setAction(HTSService.ACTION_GET_TICKET);
            htsIntent.putExtra("channelId", channel.id);
            startService(htsIntent);
        }
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

        if (id == R.id.nav_TV_Play && channel != null)
        {
            htsIntent.setAction(HTSService.ACTION_GET_TICKET);
            htsIntent.putExtra("channelId", channel.id);
            startService(htsIntent);
        }

        if (id == R.id.nav_TV_Stop)
        {
            tcpIntent.setAction(TCPService.ACTION_SEND);
            tcpIntent.putExtra("String", TCPConstants.playerStop +  "\n");
            startService(tcpIntent);
        }
/*
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
*/

        if (id == R.id.nav_Records)
        {
            Intent myIntent = new Intent(getApplicationContext(), Dialog_Records.class);
            startActivity(myIntent);
        }

        if (id == R.id.nav_2015)
        {
            Integer count = chAdapter.getCount();
            Channel channel;
            Iterator<Programme> iterator;
            Date date;
            Integer minutes;
            Integer hours;
            Programme programm;
            String str;
            ArrayList<CharSequence> arrayList = new ArrayList<CharSequence>();

            for(Integer c = 0; c < count; c++)
            {
                channel = chAdapter.getItem(c);

                iterator = channel.epg.iterator();
                while(iterator.hasNext())
                {
                    programm = iterator.next();
                    date = programm.start;
                    minutes = date.getMinutes();
                    hours = date.getHours();
                    if(hours == 20 && minutes > 10 && minutes < 20)
                    {
                        str = channel.name + "\n" + programm.title;
                        arrayList.add(str);

                        Log.i(TAG, channel.name + "\n" + programm.title);
                    }
                }

            }
            Intent myIntent = new Intent(getApplicationContext(), Dialog2015.class);
            myIntent.putCharSequenceArrayListExtra("Channels", arrayList);

            	/*myIntent.putExtra("ProgrammDescription", programm.description);
            	myIntent.putExtra("ProgammStart", formatter.format(programm.start));
            	myIntent.putExtra("LongStart", long_Start);
            	myIntent.putExtra("ChannelId", channel.id);
            	myIntent.putExtra("ChannelName", channel.name);*/

            startActivity(myIntent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onMessage(String action, final Object obj)
    {
//        RaMoCApplication app = (RaMoCApplication) getApplication();
        Log.i(TAG, "onMessage( " + action + ", " + obj.toString() );
        if (action.equals(RaMoCApplication.ACTION_LOADING))
        {
            runOnUiThread(new Runnable()
            {
                public void run()
                {
                    boolean loading = (Boolean) obj;
                    setLoading(loading);
                }
            });
        }
        else if (action.equals(RaMoCApplication.ACTION_CHANNEL_ADD))
        {
            runOnUiThread(new Runnable()
            {
                public void run()
                {
                    //Log.i(TAG, "ADD Channel");
                    chAdapter.add((Channel) obj);
                    chAdapter.notifyDataSetChanged();
                    chAdapter.sort();
                }
            });
        }
        else if (action.equals(RaMoCApplication.ACTION_CHANNEL_DELETE))
        {
            runOnUiThread(new Runnable()
            {
                public void run()
                {
                    chAdapter.remove((Channel) obj);
                    chAdapter.notifyDataSetChanged();
                }
            });
        }
        else if (action.equals(RaMoCApplication.ACTION_CHANNEL_UPDATE))
        {
            runOnUiThread(new Runnable()
            {
                public void run()
                {
                    Channel channel = (Channel) obj;
                    //Log.i(TAG, "ACTION_CHANNEL_UPDATE " + channel.name);
                    chAdapter.updateView(listView_Channel, channel);
                    chAdapter.notifyDataSetChanged();
                }
            });

        }
        else if (action.equals(RaMoCApplication.ACTION_TICKET_ADD))
        {
            HttpTicket t = (HttpTicket) obj;
            httpURL = "http://" + hostname + ":9981" +
                    t.path +
                    "?ticket=" +
                    t.ticket;

            if(httpURL.equals(oldURL))
                return;

            String tmp;
            if(channel != null)
                tmp = TCPConstants.newTVChannel + "|" + httpURL + "|" + channel.name.replace(".", "_").replace(" ", "_");
            else
                tmp = TCPConstants.newTVChannel + "|" + httpURL + "| " ;
            Log.i(TAG, tmp);

            tcpIntent.setAction(TCPService.ACTION_SEND);
            tcpIntent.putExtra("String", tmp + "\n");
            ramocApp.setCurrentPath(httpURL);

            startService(tcpIntent);
            oldURL = httpURL;
        }
        else if (action.equals(RaMoCApplication.ACTION_PROGRAMME_ADD))
        {
            //Programme prg = (Programme) obj;
            //Log.e(TAG, "ACTION_PROGRAMME_ADD");
            //Log.e(TAG, "Channel: " + prg.channel.name);
            runOnUiThread(new Runnable() {

                public void run() {
                    Programme p = (Programme) obj;
                    if (channel != null && p.channel.id == channel.id) {
                        prAdapter.add(p);
                        prAdapter.notifyDataSetChanged();
                        prAdapter.sort();
                    }
                }
            });
        }
        else if (action.equals(RaMoCApplication.ACTION_PROGRAMME_DELETE))
        {
            //Programme prg = (Programme) obj;
            //Log.e(TAG, "ACTION_PROGRAMME_DELETE");
            //Log.e(TAG, "Channel: " + prg.channel.name);
            runOnUiThread(new Runnable() {

                public void run() {

                    Programme p = (Programme) obj;
                    if(prAdapter != null && channel != null && p.channel.id == channel.id)
                    {
                        prAdapter.remove(p);
                        prAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
        else if (action.equals(RaMoCApplication.ACTION_PROGRAMME_UPDATE))
        {
            //Programme prg = (Programme) obj;
            //Log.e(TAG, "ACTION_PROGRAMME_UPDATE");
            //Log.e(TAG, "Channel: " + prg.channel.name);
            runOnUiThread(new Runnable() {

                public void run() {

                    Programme p = (Programme) obj;
                    if(prAdapter != null && channel != null && p.channel.id == channel.id)
                    {
                        prAdapter.updateView(listView_Programm, p);
                    }
                }
            });
        }
        else if (action.equals(RaMoCApplication.ACTION_TAG_UPDATE))
        {
            //NOP
        }
    }

    void connect(boolean force)
    {
        Log.i(TAG, "connect( " + force + " )");
        if (force)
        {
            //chAdapter.clear();
        }
        Log.i(TAG, "start Connection");

        htsIntent = new Intent(Activity_TV.this, HTSService.class);
        htsIntent.setAction(HTSService.ACTION_CONNECT);
        htsIntent.putExtra("hostname", hostname);
        htsIntent.putExtra("port", port);
        htsIntent.putExtra("username", username);
        htsIntent.putExtra("password", password);
        htsIntent.putExtra("force", force);

        startService(htsIntent);

        ramocApp.setHtsIntent(htsIntent);
    }

    private void populateList() {
        Log.i(TAG, "populateList");

        chAdapter.clear();

        for (Channel ch : ramocApp.getChannels())
        {
            chAdapter.add(ch);
            Log.i(TAG, "Channel Icon: " + ch.icon);
        }

        chAdapter.sort();
        chAdapter.notifyDataSetChanged();
    }

    private void setLoading(boolean loading)
    {
        Log.i(TAG, "setLoading( " + loading + " )");
        //tagBtn.setEnabled(!loading);
        if (loading) {
            //pb.setVisibility(ProgressBar.VISIBLE);
            //tagTextView.setText(R.string.inf_load);
            //tagImageView.setVisibility(ImageView.INVISIBLE);
        } else {
            //pb.setVisibility(ProgressBar.GONE);
            //tagImageView.setVisibility(ImageView.VISIBLE);

            //RaMoCApplication app = (RaMoCApplication) getApplication();
            //tagAdapter.clear();
            //for (ChannelTag t : app.getChannelTags()) {
            //  tagAdapter.add(t);
            // }

            populateList();
            //setCurrentTag(currentTag);
        }
    }

    public void setProgrammAdapter(Channel channel)
    {
        Log.i(TAG, "setProgrammAdatper: " + channel.name);
        prList = new ArrayList<Programme>();
        prList.addAll(channel.epg);
        prAdapter = new ProgrammeListAdapter(this, prList);
        prAdapter.sort();
        listView_Programm.setAdapter(prAdapter);
        textView_HeaderProgramm.setText(channel.name);

    }
}
