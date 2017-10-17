package org.gareiss.mike.ramoc.tv;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.gareiss.mike.ramoc.Constants;
import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;
import org.gareiss.mike.ramoc.model.HttpTicket;
import org.gareiss.mike.ramoc.model.Recording;
import org.gareiss.mike.ramoc.tcp.TCPConnection;
import org.gareiss.mike.ramoc.tv.htsp.HTSListener;
import org.gareiss.mike.ramoc.tv.htsp.HTSService;

import java.util.ArrayList;

public class Dialog_Records extends AppCompatActivity implements HTSListener
{
    private String				TAG = "RaMoC Records";
    private ListView listView;
    private TextView textView_Summary;
    private TextView textView_Title;
    private int adapterLayout = R.layout.widget_recording_list;
    protected RecordingListAdapter adapter;
    private RaMoCApplication app ;
    private Recording recording;

    private TCPConnection tcpConnection;
    private String 				httpURL;
    private volatile String		oldURL;
    private String				hostname;
    private String				username;
    private String				password;
    private int					port;
    private Intent              htsIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_records);

        listView = (ListView) findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        adapter = new RecordingListAdapter(this, new ArrayList<Recording>(), adapterLayout);
        listView.setAdapter(adapter);

        app = (RaMoCApplication) getApplication();
        app.addListener(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id)
            {
                Object o = listView.getItemAtPosition(position);
                recording = (Recording) o;

                htsIntent = app.getHtsIntent();
                htsIntent.setAction(HTSService.ACTION_GET_TICKET);
                htsIntent.putExtra("dvrId", recording.id);

                startService(htsIntent);

                finish();
            }
        });

        populateList();
    }
    private void populateList() {
        Log.i(TAG, "populateList");
        // Clear the list and add the recordings
        adapter.clear();
        for (Recording rec : app.getRecordingsByType(Constants.RECORDING_TYPE_COMPLETED))
        {
            adapter.add(rec);
        }

        // Show the newest completed recordings first
        adapter.sort(Constants.RECORDING_SORT_ASCENDING);
        adapter.notifyDataSetChanged();

    }
    public void onMessage(String action, final Object obj)
    {
        Log.d(TAG, action);
        if (action.equals(RaMoCApplication.ACTION_LOADING))
        {
            runOnUiThread(new Runnable() {
                public void run() {
                    boolean loading = (Boolean) obj;
                    if (loading) {
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                    } else {
                        populateList();
                    }
                }
            });
        }
        else if (action.equals(Constants.ACTION_DVR_ADD)
                || action.equals(Constants.ACTION_DVR_DELETE)
                || action.equals(Constants.ACTION_DVR_UPDATE))
        {
            runOnUiThread(new Runnable() {
                public void run() {
                    populateList();
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

            /*String tmp = "013|" + httpURL + "| " ;
            Log.i(TAG, tmp);
            new TCPTask().execute(tmp);
            oldURL = httpURL;*/
        }
    }
}
