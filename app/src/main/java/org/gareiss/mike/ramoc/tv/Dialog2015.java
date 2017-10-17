package org.gareiss.mike.ramoc.tv;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.gareiss.mike.ramoc.R;

import java.util.ArrayList;
import java.util.Iterator;

public class Dialog2015 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog2015);
        ListView listView;
        ArrayList<String> array;
        ArrayAdapter<String> adapter;

        array = new ArrayList<String>();
        ArrayList<CharSequence> arrayList;
        listView = (ListView) findViewById(R.id.listView2015);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, array);
        listView.setAdapter(adapter);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            arrayList = extras.getCharSequenceArrayList("Channels");
            Iterator<CharSequence> iterator = arrayList.iterator();
            while(iterator.hasNext())
            {
                array.add((String) iterator.next());
            }
            adapter.notifyDataSetChanged();
        }

    }

}
