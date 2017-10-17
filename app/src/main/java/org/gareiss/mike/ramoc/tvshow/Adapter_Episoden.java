package org.gareiss.mike.ramoc.tvshow;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.model.TVShow;

import java.util.ArrayList;

/**
 * Created by drue on 09.03.17.
 */

public class Adapter_Episoden extends BaseAdapter
{
    private String TAG="Adapter_TVShowList";

    static class ViewHolder
    {
        TextView textView_File;
    }

    private static ArrayList<TVShow> 	arrayList_TVShow;
    private LayoutInflater 				l_Inflater;

    public Adapter_Episoden(Context context, ArrayList<TVShow> results)
    {
        arrayList_TVShow = results;
        l_Inflater = LayoutInflater.from(context);
    }

    public int getCount()
    {
        return arrayList_TVShow.size();
    }

    public Object getItem(int position)
    {
        return arrayList_TVShow.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        Log.i(TAG, "");
        ViewHolder holder;
        if (convertView == null)
        {
            convertView = l_Inflater.inflate(R.layout.item_episoden, null);
            holder = new ViewHolder();
            holder.textView_File = (TextView) convertView.findViewById(R.id.item_Episoden_TextView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        if (arrayList_TVShow.get(position).getPlayed()) {
            holder.textView_File.setTextColor(Color.parseColor("#808080"));
            Log.i("Adapter Episoden", arrayList_TVShow.get(position).getFile());
        }
        else
            holder.textView_File.setTextColor(Color.parseColor("#000000"));

        holder.textView_File.setText(arrayList_TVShow.get(position).getEpisode());
        return convertView;
    }
}
