package org.gareiss.mike.ramoc.movie;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class Dialog_Archive extends AppCompatActivity
{

    private String 				TAG ="Dialog_Archive";
    private Movie movie;
    private TextView textView_MovieTitle;
    private TextView 			textView_MoviePlot;
    private TextView 			textView_FSK;
    private TextView            textView_Runtime;
    private TextView            textView_title;
    private ImageView imageView_MovieCover;

    private DataBase dataBase;
    private RaMoCApplication ramocApp;
    private Intent tcpIntent;
    private AsyncCallMoviePlot  moviePlot;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_archive);
        setTitle("");
        textView_MovieTitle = (TextView) findViewById(R.id.archive_title);
        textView_MoviePlot = (TextView) findViewById(R.id.textView);
        textView_FSK = (TextView) findViewById(R.id.fsk);
        textView_Runtime = (TextView) findViewById(R.id.runtime);
        imageView_MovieCover = (ImageView) findViewById(R.id.imageView);

        movie = new Movie();
        ramocApp = (RaMoCApplication) getApplicationContext();
        tcpIntent = ramocApp.getTcpIntent();
        dataBase = ramocApp.getDataBase();

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            movie.setTitel(extras.getString("MovieTitle"));
            textView_MovieTitle.setText(movie.getTitel());
            movie.setCover(extras.getByteArray("Cover"));
            imageView_MovieCover.setImageBitmap(movie.getCover());
            movie.setId(extras.getString("MovieId"));
        }
        moviePlot = new AsyncCallMoviePlot();
        moviePlot.execute();
    }

    public void button_Restore_Click(View view)
    {
        startIntent(TCPConstants.restoreMovie + "|" + movie.getId());
        this.finish();
    }

    private class AsyncCallMoviePlot extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            Log.i(TAG, "doInBackground");
            dataBase.getMovieInfo(movie);
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            Log.i(TAG, "onPostExecute");
            textView_MoviePlot.setText(movie.getBeschreibung());
            textView_FSK.setText("FSK: " + movie.getFSK());
            textView_Runtime.setText("Laufzeit: " + movie.getLaufzeit() + " min");
        }

        @Override
        protected void onPreExecute() {
            //Log.i(TAG, "onPreExecute");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // Log.i(TAG, "onProgressUpdate");
        }

    }

    private void startIntent(String str)
    {
        tcpIntent.setAction(TCPService.ACTION_SEND);
        tcpIntent.putExtra("String", str +  "\n");
        startService(tcpIntent);
    }
}
