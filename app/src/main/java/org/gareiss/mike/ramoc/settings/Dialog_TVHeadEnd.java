package org.gareiss.mike.ramoc.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;
import org.gareiss.mike.ramoc.tcp.TCPConstants;
import org.gareiss.mike.ramoc.tcp.TCPService;

public class Dialog_TVHeadEnd extends AppCompatActivity
{
    private EditText editText_IP;
    private EditText editText_User;
    private EditText editText_Passwort;

    private Intent tcpIntent;
    private RaMoCApplication ramocApp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_tvhead_end);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ramocApp 		= (RaMoCApplication) getApplicationContext();
        tcpIntent = ramocApp.getTcpIntent();

        editText_IP = (EditText) findViewById(R.id.editText_IP);
        editText_User = (EditText) findViewById(R.id.editText_User);
        editText_Passwort = (EditText) findViewById(R.id.editText_Passwort);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        String setting = ramocApp.getTvHeadEnd();

        String[] settings = setting.split("\\|");
        editText_IP.setText(settings[0]);
        editText_User.setText(settings[1]);
        editText_Passwort.setText(settings[2]);

    }

    public void dialog_tvHeadEnd_Button_Click(View view)
    {

        String ip 	= editText_IP.getText().toString();
        String benutzer	= editText_User.getText().toString();
        String passwort	= editText_Passwort.getText().toString();

        String settings = ip + "|" + benutzer + "|" + passwort + "|";
        settings = settings.replace(" ", "");
        startIntent(TCPConstants.setTVHeadEnd + "|" + settings );


        ramocApp.setTVHeadEnd(settings);
        finish();
    }

    private void startIntent(String str)
    {
        tcpIntent.setAction(TCPService.ACTION_SEND);
        tcpIntent.putExtra("String", str +  "\n");
        startService(tcpIntent);
    }
}
