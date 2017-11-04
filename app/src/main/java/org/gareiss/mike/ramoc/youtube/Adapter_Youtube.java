package org.gareiss.mike.ramoc.youtube;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.model.Youtube;

import java.util.ArrayList;

/**
 * Created by drue on 04.11.17.
 */

public class Adapter_Youtube extends BaseAdapter
{

    static class ViewHolder
    {
        TextView textView_Title;
        TextView textView_Channel_Title;
        ImageView imageView;
    }

    private static ArrayList<Youtube> arrayList_Youtube;
    private LayoutInflater l_Inflater;
    private int selectedIndex;


    public Adapter_Youtube(Context context, ArrayList<Youtube> results)
    {
        arrayList_Youtube = results;
        l_Inflater = LayoutInflater.from(context);
        selectedIndex = -1;
    }


    public void setSelectedIndex(int ind)
    {
        selectedIndex = ind;
        notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return arrayList_Youtube.size();
    }

    @Override
    public Object getItem(int position)
    {
        return arrayList_Youtube.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        if (convertView == null)
        {
            convertView = l_Inflater.inflate(R.layout.item_youtube, null);
            holder = new ViewHolder();

            holder.textView_Title = (TextView) convertView.findViewById(R.id.title);
            holder.textView_Channel_Title = (TextView) convertView.findViewById(R.id.channel_Title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView_Title.setText(arrayList_Youtube.get(position).title);
        holder.textView_Channel_Title.setText(arrayList_Youtube.get(position).channel_Title);
        holder.imageView.setImageBitmap(arrayList_Youtube.get(position).image);

        return convertView;
    }
}
