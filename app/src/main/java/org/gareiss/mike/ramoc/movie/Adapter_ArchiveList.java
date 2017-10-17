package org.gareiss.mike.ramoc.movie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.model.Movie;
import org.gareiss.mike.ramoc.music.Adapter_Playlist;

import java.util.ArrayList;

/**
 * Created by drue on 16.07.17.
 */

public class Adapter_ArchiveList extends BaseAdapter
{
    static class ViewHolder
    {
        TextView textView_Title;
        TextView textView_Genre1;
        TextView textView_Genre2;
        TextView textView_FSK;
        TextView textView_Runtime;
        ImageView imageView;
    }


    private static ArrayList<Movie> arrayList_Movie;
    private LayoutInflater l_Inflater;
    private int selectedIndex;

    public Adapter_ArchiveList(Context context, ArrayList<Movie> results)
    {
        arrayList_Movie = results;
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
        return arrayList_Movie.size();
    }

    @Override
    public Object getItem(int position)
    {
        return arrayList_Movie.get(position);
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
            convertView = l_Inflater.inflate(R.layout.item_archive, null);
            holder = new ViewHolder();

            holder.textView_Title = (TextView) convertView.findViewById(R.id.title);
            holder.textView_Genre1 = (TextView) convertView.findViewById(R.id.genre1);
            holder.textView_Genre2 = (TextView) convertView.findViewById(R.id.genre2);
            holder.textView_FSK = (TextView) convertView.findViewById(R.id.fsk);
            holder.textView_Runtime = (TextView) convertView.findViewById(R.id.runtime);
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView_Title.setText(arrayList_Movie.get(position).getTitel());
        String str = "";

        if(arrayList_Movie.get(position).getGenre().size() > 0)
            str = arrayList_Movie.get(position).getGenre().get(0);

        if(arrayList_Movie.get(position).getGenre().size() > 1)
            str = str + " " +
                   arrayList_Movie.get(position).getGenre().get(1);

        if(arrayList_Movie.get(position).getGenre().size() > 2 )
            str = str + " " +
                    arrayList_Movie.get(position).getGenre().get(2);

        if(arrayList_Movie.get(position).getGenre().size() > 3 )
            str = str + " " +
                    arrayList_Movie.get(position).getGenre().get(3);

        holder.textView_Genre1.setText(str);
        holder.textView_FSK.setText("FSK: " + arrayList_Movie.get(position).getFSK());
        holder.textView_Runtime.setText("Laufzeit: " + arrayList_Movie.get(position).getLaufzeit()
                                        + " min");
        holder.imageView.setImageBitmap(arrayList_Movie.get(position).getCover());
        /*
        if(arrayList_Movie.get(position).getGenre().size() > 0)
            holder.textView_Genre1.setText(arrayList_Movie.get(position).getGenre().get(0));
        if(arrayList_Movie.get(position).getGenre().size() > 1)
            holder.textView_Genre2.setText(arrayList_Movie.get(position).getGenre().get(1));
        if(arrayList_Movie.get(position).getGenre().size() > 2)
            holder.textView_Genre3.setText(arrayList_Movie.get(position).getGenre().get(2));
        if(arrayList_Movie.get(position).getGenre().size() > 3)
            holder.textView_Genre4.setText(arrayList_Movie.get(position).getGenre().get(3));
        holder.imageView.setImageBitmap(arrayList_Movie.get(position).getCover());
        */

        return convertView;
    }
}
