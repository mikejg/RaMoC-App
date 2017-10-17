package org.gareiss.mike.ramoc.tcp;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.gareiss.mike.ramoc.RaMoCApplication;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class TCPService extends Service implements TCPConnectionListener
{
    private String TAG = "TCPService";

    public static final String ACTION_CONNECT =     "org.gareiss.mike.ramoc.tcp.CONNECT";
    public static final String ACTION_SEND =        "org.gareiss.mike.ramoc.tcp.SEND";
    public static final String ACTION_RECONNECT =   "org.gareiss.mike.ramoc.tcp.RECONNECT";

    private TCPConnection connection = null;
    private ScheduledExecutorService execService;
    PackageInfo packInfo;
    RaMoCApplication app;

    public class LocalBinder extends Binder
    {
        TCPService TCPService()
        {
            return TCPService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate()
    {
        //app = (RaMoCApplication) getApplication();
        execService = Executors.newScheduledThreadPool(5);
        try
        {
            packInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        }
        catch (PackageManager.NameNotFoundException ex)
        {
            Log.i(TAG, "Can't get package info", ex);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, intent.getAction());
        if (ACTION_CONNECT.equals(intent.getAction()))
        {
            app = (RaMoCApplication) getApplication();
            if (connection == null || !connection.isConnected())
            {
                connection = new TCPConnection(app.getRaspberryIP(), this);
                execService.execute(new Runnable()
                {
                    public void run()
                    {
                        connection.connect();
                    }
                });
            }
        }
        if(ACTION_SEND.equals(intent.getAction()))
        {
            String str = intent.getStringExtra("String");
            if(connection.isConnected())
            {
                connection.sendMessage(str);
            }

            else
            {
                connection = new TCPConnection(app.getRaspberryIP(), this);
                execService.execute(new Runnable() {
                    public void run() {
                        connection.connect();
                    }
                });

            }
        }

        if(ACTION_RECONNECT.equals(intent.getAction()))
        {
            app = (RaMoCApplication) getApplication();
            connection.stopClient();
            execService.execute(new Runnable()
            {
                public void run()
                {
                    connection.reconnect(app.getRaspberryIP());
                }
            });
        }

        return START_NOT_STICKY;
    }

    public void onMessage(String str)
    {
        RaMoCApplication app = (RaMoCApplication) getApplication();
        app.newTCPMessage(str);
    }

    public void onError(int errorCode)
    {
        // TODO Auto-generated method stub
        RaMoCApplication app = (RaMoCApplication) getApplication();
        switch(errorCode)
        {
            case TCPConnection.CONNECTION_REFUSED_ERROR:
                app.newTCPError("Konnte keine Verbindung mit " + app.getRaspberryIP() + " aufbauen");
                break;

            case TCPConnection.CONNECTED:
                app.setDataBase();
                app.newTCPError("Verbindung mit " + app.getRaspberryIP() + " wurde aufgebaut");
                break;

            case TCPConnection.DISCONNECTED:
                app.newTCPError("Keine Verbindung zu " + app.getRaspberryIP());
                break;
        }
    }

    public void onError(Exception ex)
    {
        // TODO Auto-generated method stub

    }
}
