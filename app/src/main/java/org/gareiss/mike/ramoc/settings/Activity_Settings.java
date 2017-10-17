package org.gareiss.mike.ramoc.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;
import org.gareiss.mike.ramoc.tcp.TCPConstants;
import org.gareiss.mike.ramoc.tcp.TCPService;

public class Activity_Settings extends AppCompatActivity
{

    private RaMoCApplication    ramocApp;
    private Intent              tcpIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ramocApp = (RaMoCApplication) getApplicationContext();
        tcpIntent = ramocApp.getTcpIntent();
    }

    public void button_IP_Click(View view)
    {
        Intent mIntent = new Intent(getApplicationContext(), Dialog_RaMoC_IP.class);
        startActivity(mIntent);
    }

    public void button_Scann_Click(View view)
    {
        tcpIntent.setAction(TCPService.ACTION_SEND);
        tcpIntent.putExtra("String", TCPConstants.scannHD + "\n" );
        startService(tcpIntent);
    }

    public void button_New_Click(View view)
    {
        Intent myIntent = new Intent(getApplicationContext(), Dialog_Unsorted_Files.class);
        startActivity(myIntent);
    }

    public void button_TV_Click(View view)
    {
        Intent myIntent = new Intent(getApplicationContext(), Dialog_TVHeadEnd.class);
        startActivity(myIntent);
    }

    public void button_NAS_Click(View view)
    {
        Intent myIntent = new Intent(getApplicationContext(), Dialog_SMB.class);
        startActivity(myIntent);
    }
}
