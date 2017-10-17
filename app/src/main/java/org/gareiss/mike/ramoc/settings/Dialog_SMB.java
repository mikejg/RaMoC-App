package org.gareiss.mike.ramoc.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;
import org.gareiss.mike.ramoc.tcp.TCPConstants;
import org.gareiss.mike.ramoc.tcp.TCPService;

public class Dialog_SMB extends AppCompatActivity
{
    private EditText editText_NAS_IP;
    private EditText editText_NAS_MAC;
    private EditText editText_NAS_Freigabe;
    private EditText editText_NAS_Benutzer;
    private EditText editText_NAS_Passwort;

    private RaMoCApplication ramocApp;
    private Intent tcpIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_nas);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        ramocApp 		= (RaMoCApplication) getApplicationContext();
        tcpIntent = ramocApp.getTcpIntent();
        editText_NAS_IP = (EditText) findViewById(R.id.dialog_NAS_IP_L);
        editText_NAS_MAC = (EditText) findViewById(R.id.dialog_NAS_MAC_L);
        editText_NAS_Freigabe = (EditText) findViewById(R.id.dialog_NAS_Freigabe_L);
        editText_NAS_Benutzer = (EditText) findViewById(R.id.dialog_NAS_Benutzer_L);
        editText_NAS_Passwort = (EditText) findViewById(R.id.dialog_NAS_Passwort_L);

        editText_NAS_IP.setOnEditorActionListener(new OnEditorActionListener());
        editText_NAS_MAC.setOnEditorActionListener(new OnEditorActionListener());
        editText_NAS_Freigabe.setOnEditorActionListener(new OnEditorActionListener());
        editText_NAS_Benutzer.setOnEditorActionListener(new OnEditorActionListener());
        editText_NAS_Passwort.setOnEditorActionListener(new OnEditorActionListener());
    }

    @Override
    public void onResume()
    {
        super.onResume();

        String setting = ramocApp.getNAS();

        String[] settings = setting.split("\\|");
        editText_NAS_IP.setText(settings[0]);
        editText_NAS_Freigabe.setText(settings[1]);
        editText_NAS_Benutzer.setText(settings[2]);
        editText_NAS_MAC.setText(settings[5]);
    }
    public void button_RaMoC_NAS_Click(View view)
    {
        String nasIP 	= editText_NAS_IP.getText().toString();
        String mac		= editText_NAS_MAC.getText().toString();
        String freigabe	= editText_NAS_Freigabe.getText().toString();
        String benutzer	= editText_NAS_Benutzer.getText().toString();
        String passwort	= editText_NAS_Passwort.getText().toString();

        String settings = nasIP + "|" + freigabe +"|" + benutzer + "|" + passwort + "|" + mac + "|";
        settings = settings.replace(" ", "");
        startIntent(TCPConstants.setNAS + "|" + settings );
        finish();
    }

    public class OnEditorActionListener implements EditText.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        }
    }

    private void startIntent(String str)
    {
        tcpIntent.setAction(TCPService.ACTION_SEND);
        tcpIntent.putExtra("String", str +  "\n");
        startService(tcpIntent);
    }
}
