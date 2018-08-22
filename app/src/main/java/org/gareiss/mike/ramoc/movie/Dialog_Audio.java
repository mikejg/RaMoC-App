package org.gareiss.mike.ramoc.movie;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;
import org.gareiss.mike.ramoc.tcp.TCPConstants;
import org.gareiss.mike.ramoc.tcp.TCPListener;
import org.gareiss.mike.ramoc.tcp.TCPService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Dialog_Audio extends AppCompatActivity implements TCPListener
{

    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> array;
    String TAG = "Dialog Audio";

    private Intent tcpIntent;
    private JSONObject json_data;
    private RaMoCApplication ramocApp;
    public String[] tmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_audio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ramocApp 		= (RaMoCApplication) getApplication();
        ramocApp.addTCPListener(this);
        tcpIntent = ramocApp.getTcpIntent();

        array = new ArrayList<String>();

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Object o = listView.getItemAtPosition(position);
                String value = (String) o;
                Log.i(TAG, value);

                tcpIntent.setAction(TCPService.ACTION_SEND);
                tcpIntent.putExtra("String", "030|" + value.charAt(0)+  "\n");
                startService(tcpIntent);
                finish();
            }
        });

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, array);
        listView.setAdapter(adapter);
    }
    @Override
    public void onResume()
    {
        super.onResume();

        tcpIntent.setAction(TCPService.ACTION_SEND);
        tcpIntent.putExtra("String", TCPConstants.getAudio + "\n");
        startService(tcpIntent);
    }


    void setAdapter(String result)
    {
        try
        {
            JSONArray jArray = new JSONArray(result);

            for(int i=0;i<jArray.length();i++)
            {
                json_data = jArray.getJSONObject(i);
                array.add(json_data.getString("path"));
            }
        }
        catch(JSONException je)
        {
            Log.e(TAG, "Error parsing data "+je.toString());
        }

        adapter.notifyDataSetChanged();

    }
    @Override
    public void onTCPMessage(String tcpString)
    {
        Log.i(TAG, tcpString);
        if(!tcpString.startsWith(TCPConstants.getAudio)) {

            return;
        }
        tmp = tcpString.split("\\|");
        if(tmp.length == 2)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    setAdapter(tmp[1]);
                }
            });
        }
    }
}
