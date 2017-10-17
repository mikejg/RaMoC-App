package org.gareiss.mike.ramoc.settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.gareiss.mike.ramoc.DataBase;
import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;
import org.gareiss.mike.ramoc.model.Movie;
import org.gareiss.mike.ramoc.tcp.TCPConstants;
import org.gareiss.mike.ramoc.tcp.TCPService;

public class Dialog_Selection extends AppCompatActivity
{
    private String 			TAG = "Dialog_Selection";
    private TextView        textView;
    private ImageView       imageView;
    private String			moviePath;
    private String			movieId;
    private String			title;
    private DataBase        dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_selection);

        final RaMoCApplication ramocApp = (RaMoCApplication)  getApplicationContext();

        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);
        dataBase = ramocApp.getDataBase();

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            moviePath = extras.getString("path");
            String[] s = extras.getString("id").split("\\|");
            if(s.length > 1)
            {
                title = s[1];
                this.setTitle(s[1]);
                movieId = s[0];
            }
        }

        new AsyncTask_GetMovie(Dialog_Selection.this).execute(movieId);
    }

    public void button_Click(View view)
    {
        startIntent(TCPConstants.insertMovie + "|" + moviePath + "|" + movieId + "|" + title);
        this.finish();
    }

    private class AsyncTask_GetMovie extends AsyncTask<String, Void, Void>
    {
        /** progress dialog to show user that the backup is processing. */
        private ProgressDialog dialog;
        private Movie movie;
        /** application context. */
        @SuppressWarnings("unused")
        private Activity activity;

        public AsyncTask_GetMovie(Activity activity)
        {
            this.activity = activity;
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected Void doInBackground(String... params)
        {
            //Log.i(TAG, "doInBackground");
            movie = dataBase.getSelectedMovie(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(TAG, "onPostExecute");
            if (dialog.isShowing())
                dialog.dismiss();
            textView.setText(movie.getBeschreibung());
            imageView.setImageBitmap(movie.getPoster());
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Load Proposal");
            this.dialog.show();
            //Log.i(TAG, "onPreExecute");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }
    }

    private void startIntent(String str)
    {
        Intent intent = new Intent(Dialog_Selection.this, TCPService.class);
        intent.setAction(TCPService.ACTION_SEND);
        intent.putExtra("String", str + "\n");
        startService(intent);
    }

}
