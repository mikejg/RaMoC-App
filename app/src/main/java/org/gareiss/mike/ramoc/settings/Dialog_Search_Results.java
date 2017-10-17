package org.gareiss.mike.ramoc.settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.gareiss.mike.ramoc.DataBase;
import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.RaMoCApplication;

import java.util.ArrayList;

public class Dialog_Search_Results extends AppCompatActivity
{
    ListView listView;
    EditText editText;

    ArrayAdapter<String> adapter;
    ArrayList<String> array;
    String TAG = "PropsalMovie";
    String movieName;
    String moviePath;
    AsyncTask_GetList asyncTask;

    DataBase dataBase;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_search_results);

        final RaMoCApplication ramocApp = (RaMoCApplication)  getApplicationContext();

        array = new ArrayList<String>();
        movieName = "";
        editText = (EditText) findViewById(R.id.editText);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, array);
        listView.setAdapter(adapter);
        dataBase = ramocApp.getDataBase();
        asyncTask = new AsyncTask_GetList(Dialog_Search_Results.this);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            moviePath = extras.getString("path");
            String[] s = extras.getString("path").split("/");
            if(s.length > 0)
            {
                movieName = s[s.length-1];
                String[] r = movieName.split("\\.");
                if(r.length > 0)
                {
                    movieName = r[0];
                }

                editText.setText(movieName);
            }
        }

        editText.setOnEditorActionListener(new OnEditorActionListener());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Object o = listView.getItemAtPosition(position);
                String value = (String) o;
                Intent myIntent = new Intent(getApplicationContext(), Dialog_Selection.class);
                myIntent.putExtra("path", moviePath);
               myIntent.putExtra("id", value);
                startActivity(myIntent);
                finish();
            }
        });


        asyncTask.execute(movieName);
    }

    public class OnEditorActionListener implements EditText.OnEditorActionListener
    {
        Boolean test = false;
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
        {
            Log.i(TAG, "Listener");

            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            {
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                //aus irgendeinem Grund wird die Funktion 2 mal aufgerufen :(
                if(!test)
                {
                    Log.i(TAG, editText.getText().toString());
                    new AsyncTask_GetList(Dialog_Search_Results.this).execute(editText.getText().toString());
                    test = true;
                }
                else
                {
                    test = false;
                }
                return true;
            }
            return false;
        }
    }

    private class AsyncTask_GetList extends AsyncTask<String, Void, Void>
    {
        /** progress dialog to show user that the backup is processing. */
        private ProgressDialog dialog;
        private ArrayList<String> arrayList;

        /** application context. */
        @SuppressWarnings("unused")
        private Activity activity;

        public AsyncTask_GetList(Activity activity)
        {
            this.activity = activity;
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected Void doInBackground(String... params)
        {
            //Log.i(TAG, "doInBackground");
            arrayList = dataBase.getProposal(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(TAG, "onPostExecute");
            if (dialog.isShowing())
                dialog.dismiss();

            array.clear();
            for(String str : arrayList)
            {
                array.add(str);
            }
            adapter.notifyDataSetChanged();

            //setAdapter();
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
}
