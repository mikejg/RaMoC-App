package org.gareiss.mike.ramoc;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.gareiss.mike.ramoc.model.Channel;
import org.gareiss.mike.ramoc.model.ChannelTag;
import org.gareiss.mike.ramoc.model.HttpTicket;
import org.gareiss.mike.ramoc.model.Packet;
import org.gareiss.mike.ramoc.model.Programme;
import org.gareiss.mike.ramoc.model.Recording;
import org.gareiss.mike.ramoc.model.Subscription;
import org.gareiss.mike.ramoc.tcp.TCPConstants;
import org.gareiss.mike.ramoc.tcp.TCPListener;
import org.gareiss.mike.ramoc.tv.htsp.HTSListener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by drue on 23.02.17.
 */

public class RaMoCApplication extends Application
{
    public static final String ACTION_CHANNEL_ADD = "org.gareiss.ramoc.tv.CHANNEL_ADD";
    public static final String ACTION_CHANNEL_DELETE = "org.gareiss.ramoc.tv.CHANNEL_DELETE";
    public static final String ACTION_CHANNEL_UPDATE = "org.gareiss.ramoc.tv.CHANNEL_UPDATE";
    public static final String ACTION_TAG_ADD = "org.gareiss.ramoc.tv.TAG_ADD";
    public static final String ACTION_TAG_DELETE = "org.gareiss.ramoc.tv.TAG_DELETE";
    public static final String ACTION_TAG_UPDATE = "org.gareiss.ramoc.tv.TAG_UPDATE";
    public static final String ACTION_DVR_ADD = "org.gareiss.ramoc.tv.DVR_ADD";
    public static final String ACTION_DVR_DELETE = "org.gareiss.ramoc.tv.DVR_DELETE";
    public static final String ACTION_DVR_UPDATE = "org.gareiss.ramoc.tv.DVR_UPDATE";
    public static final String ACTION_PROGRAMME_ADD = "org.gareiss.ramoc.tv.PROGRAMME_ADD";
    public static final String ACTION_PROGRAMME_DELETE = "org.gareiss.ramoc.tv.PROGRAMME_DELETE";
    public static final String ACTION_PROGRAMME_UPDATE = "org.gareiss.ramoc.tv.PROGRAMME_UPDATE";
    public static final String ACTION_SUBSCRIPTION_ADD = "org.gareiss.ramoc.tv.SUBSCRIPTION_ADD";
    public static final String ACTION_SUBSCRIPTION_DELETE = "org.gareiss.ramoc.tv.SUBSCRIPTION_DELETE";
    public static final String ACTION_SUBSCRIPTION_UPDATE = "org.gareiss.ramoc.tv.SUBSCRIPTION_UPDATE";
    public static final String ACTION_PLAYBACK_PACKET = "org.gareiss.ramoc.tv.PLAYBACK_PACKET";
    public static final String ACTION_LOADING = "org.gareiss.ramoc.tv.LOADING";
    public static final String ACTION_TICKET_ADD = "org.gareiss.ramoc.tv.TICKET";
    public static final String ACTION_TIMER_TICKET_ADD = "org.gareiss.ramoc.tv.TIMER_TICKET";
    public static final String ACTION_ERROR = "org.gareiss.ramoc.tv.ERROR";
    public static final String STATE_IDLE = "0";
    public static final String STATE_PLAYING = "1";
    public static final String STATE_PAUSED = "2";
    private static final String TAG = "RaMoCApplication";

    private final List<HTSListener> listeners = new ArrayList<HTSListener>();
    private final List<ChannelTag> tags = Collections.synchronizedList(new ArrayList<ChannelTag>());
    private final List<Channel> channels = Collections.synchronizedList(new ArrayList<Channel>());
    private final List<Recording> recordings = Collections.synchronizedList(new ArrayList<Recording>());
    private final List<Subscription> subscriptions = Collections.synchronizedList(new ArrayList<Subscription>());

    private volatile boolean loading = false;

    private String string_NAS;
    private String string_RaspberryIP;
    private String string_tvHeadEnd;

    private FileInputStream     fileInputStream = null;
    private InputStreamReader 	inputStreamReader = null;
    private FileOutputStream    fileOutputStream = null;
    private OutputStreamWriter 	outputStreamWriter = null;

    private Handler                 handler = new Handler();
    private DataBase                dataBase = null;
    private final List<TCPListener> tcpListeners = new ArrayList<TCPListener>();
    private Intent                  tcpIntent;
    private Intent                  htsIntent;
    private String                  currentPath;
    private String                     state;
    private Programme               currentProgramm;
    @Override
    public void onCreate()
    {
        super.onCreate();
        String string_Output = null;
        char[] char_InputBuffer = new char[255];
        //ramocConnection = new RaMoCConnection(this);

        string_NAS = " | | | | | ";
        string_tvHeadEnd = " | | ";
        state = STATE_IDLE;

        try
        {
            fileInputStream = this.openFileInput("RaMoC-Settings");
            inputStreamReader = new InputStreamReader(fileInputStream);
            inputStreamReader.read(char_InputBuffer);
            string_Output = new String(char_InputBuffer);
            inputStreamReader.close();

            string_Output = string_Output.trim();
            string_RaspberryIP = string_Output.replace("\n", "");

            Log.i(TAG, "RaspberryIP " + string_RaspberryIP + " aus internen Speicher gelesen");

        }
        catch (IOException e)
        {
            string_RaspberryIP = "ramoc";
        }
    }

    public String  getCurrentPath() { return currentPath;}
    public void    setCurrentPath(String currentPath) { this.currentPath = currentPath;}

    public Intent   getTcpIntent() { return tcpIntent;}
    public void     setTcpIntent(Intent intent) { tcpIntent = intent;}

    public Intent getHtsIntent() { return htsIntent;}
    public void   setHtsIntent(Intent intent) { htsIntent = intent;}

    public DataBase getDataBase()
    {
        return dataBase;
    }
    public void     setDataBase()
    {
        if(dataBase == null)
            dataBase = new DataBase(string_RaspberryIP,this);
    }

    public String getRaspberryIP()
    {
        return string_RaspberryIP;
    }
    public void setRaspberryIP(String string_Rp)
    {
        string_RaspberryIP = string_Rp;
        string_RaspberryIP = string_RaspberryIP.trim();
        string_RaspberryIP = string_RaspberryIP.replace("\n", "");
        try
        {
            fileOutputStream = this.openFileOutput("RaMoC-Settings", Context.MODE_PRIVATE);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(string_Rp);
            outputStreamWriter.flush();
            //dataBase = new DataBase(string_RaspberryIP,this);
        }
        catch(IOException e)
        {
        }
    }

    public Programme getCurrentProgramm() { return currentProgramm; }
    public void setCurrentProgramm(Programme pr)
    {
        currentProgramm = pr;
    }

    public void addChannel(Channel channel)
    {
        channels.add(channel);

        if (!loading) {
            broadcastMessage(ACTION_CHANNEL_ADD, channel);
        }
    }

    public void addChannelTag(ChannelTag tag) {
        tags.add(tag);

        if (!loading) {
            broadcastMessage(ACTION_TAG_ADD, tag);
        }
    }

    public void addListener(HTSListener l) {listeners.add(l);}

    public void addProgramme(Programme p)
    {
        if (!loading) {
            broadcastMessage(ACTION_PROGRAMME_ADD, p);
        }
    }

    public void addRecording(Recording rec)
    {
        recordings.add(rec);

        if (!loading) {
            broadcastMessage(ACTION_DVR_ADD, rec);
        }
    }

    public void addSubscription(Subscription s)
    {
        subscriptions.add(s);

        if (!loading) {
            broadcastMessage(ACTION_SUBSCRIPTION_ADD, s);
        }
    }

    public void addTCPListener(TCPListener tl)
    {
        tcpListeners.add(tl);
    }

    public void addTicket(HttpTicket t)
    {
        broadcastMessage(ACTION_TICKET_ADD, t);
    }

    public void addTimerTicket(HttpTicket t)
    {
        broadcastMessage(ACTION_TIMER_TICKET_ADD, t);
    }

    public void broadcastError(final String error)
    {
        //Log.i(TAG, "broadcastError: " + error);
        //Don't show error if no views are open
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
        }
        handler.post(new Runnable() {

            public void run() {

                try {
                    Toast toast = Toast.makeText(RaMoCApplication.this, error, Toast.LENGTH_LONG);
                    toast.show();
                } catch (Throwable ex) {
                }
            }
        });
        broadcastMessage(ACTION_ERROR, error);
    }

    private void broadcastMessage(String action, Object obj)
    {
        //Log.i(TAG, "broadcastMessage " + action + " " + obj.toString());
        synchronized (listeners) {
            for (HTSListener l : listeners) {
                //Log.i(TAG, l.toString());
                l.onMessage(action, obj);
            }
        }
    }

    public void broadcastPacket(Packet p)
    {
        broadcastMessage(ACTION_PLAYBACK_PACKET, p);
    }

    public void clearAll()
    {
        Log.i(TAG, "clearAll");
        tags.clear();
        recordings.clear();

        for (Channel ch : channels) {
            ch.epg.clear();
            ch.recordings.clear();
        }
        channels.clear();

        for (Subscription s : subscriptions) {
            s.streams.clear();
        }
        subscriptions.clear();

        ChannelTag tag = new ChannelTag();
        tag.id = 0;
        tag.name = getString(R.string.pr_all_channels);
        tags.add(tag);
    }

    public void delTCPListener(TCPListener tl)
    {
        tcpListeners.remove(tl);
    }

    public Channel getChannel(long id) {
        for (Channel ch : getChannels()) {
            if (ch.id == id) {
                return ch;
            }
        }
        return null;
    }

    public List<Channel> getChannels() {return channels;}

    public ChannelTag getChannelTag(long id)
    {
        for (ChannelTag tag : getChannelTags())
        {
            if (tag.id == id) {
                return tag;
            }
        }
        return null;
    }

    public List<ChannelTag> getChannelTags() {return tags;}
    public String getNAS() { return string_NAS; }

    public Recording getRecording(long id)
    {
        for (Recording rec : getRecordings()) {
            if (rec.id == id) {
                return rec;
            }
        }
        return null;
    }

    public List<Recording> getRecordings() {return recordings;}

    public List<Recording> getRecordingsByType(int type) {
        List<Recording> recs = new ArrayList<Recording>();

        switch (type) {
            case Constants.RECORDING_TYPE_COMPLETED:
                synchronized (recordings) {
                    for (Recording rec : recordings) {
                        // Include all recordings that are marked as completed, also
                        // include recordings marked as auto recorded
                        if (rec.error == null && rec.state.equals("completed")) {
                            recs.add(rec);
                        }
                    }
                }
                break;

            case Constants.RECORDING_TYPE_SCHEDULED:
                synchronized (recordings) {
                    for (Recording rec : recordings) {
                        // Include all scheduled recordings in the list, also
                        // include recordings marked as auto recorded
                        if (rec.error == null
                                && (rec.state.equals("scheduled") || rec.state.equals("recording"))) {
                            recs.add(rec);
                        }
                    }
                }
                break;

            case Constants.RECORDING_TYPE_FAILED:
                synchronized (recordings) {
                    for (Recording rec : recordings) {
                        // Include all failed recordings in the list
                        if ((rec.error != null || (rec.state.equals("missed") || rec.state.equals("invalid")))) {
                            recs.add(rec);
                        }
                    }
                }
                break;
        }
        return recs;
    }
    public List<Subscription> getSubscriptions() {return subscriptions;}

    public String getState() { return state; }

    public Subscription getSubscription(long id)
    {
        for (Subscription s : getSubscriptions()) {
            if (s.id == id) {
                return s;
            }
        }
        return null;
    }

    public String getTvHeadEnd() { return string_tvHeadEnd; }
    public void setTVHeadEnd(String str) { string_tvHeadEnd = str;}

    public boolean isLoading() {return loading;}

    public void newTCPError(final String error)
    {
        //Don't show error if no views are open
        synchronized (tcpListeners)
        {
            if (tcpListeners.isEmpty())
            {
                return;
            }
        }
        handler.post(new Runnable()
        {
            public void run()
            {
                try
                {
                    Toast toast = Toast.makeText(RaMoCApplication.this, error, Toast.LENGTH_LONG);
                    toast.show();
                }
                catch (Throwable ex)
                {
                }
            }
        });
    }

    public void newTCPMessage(String str)
    {
        Log.i(TAG, "newTCPMessage " + str);

        str = str.replace("\n", "");
        String[] tmp = str.split("\\|");

        if(str.startsWith("001"))
        {
            setSettings(str);
        }
        else
        {
           if(str.startsWith(TCPConstants.newState))
           {
               Log.i(TAG, tmp[1]);
               if(tmp[1].equals(STATE_PLAYING))
               {
                   state = STATE_PLAYING;
                   Log.i(TAG, "state = STATE_PLAYING");
               }
               if(tmp[1].equals(STATE_PAUSED))
               {
                   state = STATE_PAUSED;
                   Log.i(TAG, "state = STATE_PAUSED");
               }
               if(tmp[1].equals(STATE_IDLE))
               {
                   state = STATE_IDLE;
                   Log.i(TAG, "state = STATE_IDLE");
               }
           }

            if(str.startsWith(TCPConstants.playTrack))
            {
                state = STATE_PLAYING;
                Log.i(TAG, "state = STATE_PLAYING");
            }

            for (TCPListener l : tcpListeners)
            {
                l.onTCPMessage(str);
            }
        }
    }

    public void removeChannel(long id)
    {
        for (Channel ch : getChannels()) {
            if (ch.id == id) {
                removeChannel(ch);
                return;
            }
        }
    }

    public void removeChannel(Channel channel)
    {
        channels.remove(channel);

        if (!loading) {
            broadcastMessage(ACTION_CHANNEL_DELETE, channel);
        }
    }
    public void removeChannelTag(long id)
    {
        for (ChannelTag tag : getChannelTags()) {
            if (tag.id == id)
            {
                removeChannelTag(tag);
                return;
            }
        }
    }

    public void removeChannelTag(ChannelTag tag)
    {
        tags.remove(tag);

        if (!loading) {
            broadcastMessage(ACTION_TAG_DELETE, tag);
        }
    }

    public void removeProgramme(Programme p)
    {
        if (!loading) {
            broadcastMessage(ACTION_PROGRAMME_DELETE, p);
        }
    }

    public void removeRecording(Recording rec)
    {
        recordings.remove(rec);

        if (!loading) {
            broadcastMessage(ACTION_DVR_DELETE, rec);
        }
    }

    public void removeSubscription(Subscription s)
    {
        s.streams.clear();
        subscriptions.remove(s);

        if (!loading) {
            broadcastMessage(ACTION_SUBSCRIPTION_DELETE, s);
        }
    }

    public void removeSubscription(long id)
    {
        for (Subscription s : getSubscriptions()) {
            if (s.id == id) {
                removeSubscription(s);
                return;
            }
        }
    }

    public void setLoading(boolean b) {
        //Log.i(TAG, "setLoading");
        if (loading != b) {
            broadcastMessage(ACTION_LOADING, b);
        }
        loading = b;
    }

    public void setSettings(String string_Settings)
    {
        Log.i(TAG, "setSettings " + string_Settings);
        String[] tmp = string_Settings.split("\\|");
        Log.i(TAG, "setSettings tmp.length " + Integer.toString(tmp.length));
        if(tmp.length > 10)
        {
            string_NAS = 	tmp[1] + "|" +
                    tmp[2] + "|" +
                    tmp[3] + "|" +
                    tmp[4] + "|" +
                    tmp[5] + "|" +
                    tmp[6] + "|" ;

            string_tvHeadEnd = 	tmp[7] + "|" +
                    tmp[8] + "|" +
                    tmp[9] + "|";
            state = tmp[10];
        }
        Log.i(TAG, "string_NAS: " + string_NAS);
        Log.i(TAG, "string_tvHeadEnd: " + string_tvHeadEnd);
    }

    public void updateChannel(Channel ch)
    {
        if (!loading) {
            broadcastMessage(ACTION_CHANNEL_UPDATE, ch);
        }
    }

    public void updateChannelTag(ChannelTag tag)
    {
        if (!loading) {
            broadcastMessage(ACTION_TAG_UPDATE, tag);
        }
    }
    public void updateProgramme(Programme p)
    {
        if (!loading) {
            broadcastMessage(ACTION_PROGRAMME_UPDATE, p);
        }
    }

    public void updateRecording(Recording rec)
    {
        if (!loading) {
            broadcastMessage(ACTION_DVR_UPDATE, rec);
        }
    }

    public void updateSubscription(Subscription s)
    {
        if (!loading) {
            broadcastMessage(ACTION_SUBSCRIPTION_UPDATE, s);
        }
    }
}
