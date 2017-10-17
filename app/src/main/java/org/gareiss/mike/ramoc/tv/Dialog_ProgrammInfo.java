package org.gareiss.mike.ramoc.tv;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;
import org.gareiss.mike.ramoc.model.Programme;
import org.gareiss.mike.ramoc.tv.htsp.HTSService;

import java.text.Format;
import java.text.SimpleDateFormat;

public class Dialog_ProgrammInfo extends AppCompatActivity
{
    public static final String ACTION_DVR_ADD = "org.gareiss.ramoc.tv.htsp.DVR_ADD";

    private TextView	textView_ProgrammDescription;
    private TextView    textView_ProgrammStart;
    private String 		TAG = "Programm_Activity";
    private Programme   programm;
    private Intent      htsIntent;
//    private StartProgramm	startProgramm;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_programm_info);

        textView_ProgrammDescription = (TextView) findViewById(R.id.pr_description);
        textView_ProgrammStart = (TextView) findViewById(R.id.pr_start);

        RaMoCApplication app = (RaMoCApplication) getApplication();
        Format formatter = new SimpleDateFormat("HH:mm:ss ");
        programm = app.getCurrentProgramm();

        this.setTitle(app.getCurrentProgramm().title);
        textView_ProgrammDescription.setText(app.getCurrentProgramm().description);
        textView_ProgrammStart.setText(formatter.format(app.getCurrentProgramm().start));

    }

    public void button_Record_Clicked(View view)
    {
        htsIntent = new Intent(Dialog_ProgrammInfo.this, HTSService.class);
        htsIntent.setAction(HTSService.ACTION_DVR_ADD);
        htsIntent.putExtra("channelId", programm.channel.id);
        htsIntent.putExtra("eventId", programm.id);
        startService(htsIntent);
    }
}
