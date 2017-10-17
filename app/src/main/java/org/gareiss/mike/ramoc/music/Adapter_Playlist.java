package org.gareiss.mike.ramoc.music;

        import android.annotation.SuppressLint;
        import android.content.Context;
        import android.graphics.Color;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.BaseAdapter;
        import android.widget.TextView;

        import org.gareiss.mike.ramoc.R;
        import org.gareiss.mike.ramoc.model.Track;

        import java.util.ArrayList;

public class Adapter_Playlist extends BaseAdapter
{
    static class ViewHolder
    {
        TextView textView_Artist;
        TextView textView_Album;
        TextView textView_Titel;
    }

    private static ArrayList<Track> arrayList_Tracks;
    private LayoutInflater 			l_Inflater;
    private int selectedIndex;

    public Adapter_Playlist(Context context, ArrayList<Track> results)
    {
        arrayList_Tracks = results;
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
        return arrayList_Tracks.size();
    }

    @Override
    public Object getItem(int position)
    {
        return arrayList_Tracks.get(position);
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
            convertView = l_Inflater.inflate(R.layout.music_playlist_item, null);
            holder = new ViewHolder();

            holder.textView_Artist = (TextView) convertView.findViewById(R.id.music_Playlist_Artist);
            holder.textView_Album = (TextView) convertView.findViewById(R.id.music_Playlist_Album);
            holder.textView_Titel = (TextView) convertView.findViewById(R.id.music_Playlist_Titel);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView_Artist.setText(arrayList_Tracks.get(position).artist);
        holder.textView_Album.setText(arrayList_Tracks.get(position).album);
        holder.textView_Titel.setText(arrayList_Tracks.get(position).titel);

        if(selectedIndex != -1 && position == selectedIndex)
        {
            convertView.setBackgroundColor(Color.LTGRAY);
        }
        else if (arrayList_Tracks.get(position).favorite.equals("1"))
        {
            convertView.setBackgroundColor(Color.argb(32, 70, 43, 83));
        }
        else
        {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }
        return convertView;
    }
}
