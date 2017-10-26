package org.gareiss.mike.ramoc.music;

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

public class Dialog_Edit_ID3Tag extends AppCompatActivity
{

    private EditText editText_Title;
    private EditText editText_Album;
    private EditText editText_Interpret;

    private String title;
    private String album;
    private String interpret;
    private String ramocID;

    private RaMoCApplication ramocApp;
    private Intent tcpIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_id3_tag);

        editText_Title = (EditText) findViewById(R.id.editText_Title);
        editText_Album = (EditText) findViewById(R.id.editText_Album);
        editText_Interpret = (EditText) findViewById(R.id.editText_Interpret);

        editText_Title.setOnEditorActionListener(new OnEditorActionListener());
        editText_Album.setOnEditorActionListener(new OnEditorActionListener());
        editText_Interpret.setOnEditorActionListener(new OnEditorActionListener());

        ramocApp 		= (RaMoCApplication) getApplicationContext();
        tcpIntent = ramocApp.getTcpIntent();

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            editText_Title.setText(extras.getString("Title"));
            editText_Album.setText(extras.getString("Album"));
            editText_Interpret.setText(extras.getString("Interpret"));
            ramocID = extras.getString("ramocID");
        }
    }

    public void button_Click(View view)
    {
        String title 	= editText_Title.getText().toString();
        String album		= editText_Album.getText().toString();
        String interpret	= editText_Interpret.getText().toString();

        String settings = ramocID + "|" + title + "|" + album +"|" + interpret;
        startIntent(TCPConstants.setID3Tag + "|" + settings );
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
