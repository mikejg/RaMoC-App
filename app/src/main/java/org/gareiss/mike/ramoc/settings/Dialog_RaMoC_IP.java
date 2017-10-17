package org.gareiss.mike.ramoc.settings;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;
import org.gareiss.mike.ramoc.tcp.TCPService;

public class Dialog_RaMoC_IP extends AppCompatActivity
{
    private String TAG = "Dialog_RaMoC_IP";

    private EditText            editText_RaspberryIP;
    private RaMoCApplication    ramocApp;
    private Intent              tcpIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ramoc_ip);

        ramocApp = (RaMoCApplication) getApplicationContext();
        tcpIntent = ramocApp.getTcpIntent();

        editText_RaspberryIP = (EditText) findViewById(R.id.ramocIP_EditText);
        editText_RaspberryIP.setText(ramocApp.getRaspberryIP());

        editText_RaspberryIP.setOnEditorActionListener(new EditText.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
                {
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    String text = editText_RaspberryIP.getText().toString().trim();
                    ramocApp.setRaspberryIP(text);

                    tcpIntent.setAction(TCPService.ACTION_RECONNECT);
                    startService(tcpIntent);

                    /*
                    Intent intent = new Intent(Dialog_RaMoCIP_L.this, TCPService.class);
                    intent.setAction(TCPService.ACTION_CONNECT);
                    startService(intent);
                    */
                    return true;
                }
                return false;
            }
        });
    }
}
