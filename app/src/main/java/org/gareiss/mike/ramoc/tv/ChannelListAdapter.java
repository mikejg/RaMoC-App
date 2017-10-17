package org.gareiss.mike.ramoc.tv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.model.Channel;

import java.util.Comparator;
import java.util.List;

/**
 * Created by drue on 12.03.17.
 */

public class ChannelListAdapter extends ArrayAdapter<Channel>
{
    //	private final String TAG="RaMoC ChannelListAdapter";
    private int selectedIndex;

    ChannelListAdapter(Activity context, List<Channel> list) {
        super(context, R.layout.item_channel, list);

        selectedIndex = -1;
    }

    public void setSelectedIndex(int ind)
    {
        selectedIndex = ind;
        notifyDataSetChanged();
    }

    public void sort() {
        sort(new Comparator<Channel>() {

            public int compare(Channel x, Channel y) {
                return x.compareTo(y);
            }
        });
    }

    public void updateView(ListView listView, Channel channel) {
        for (int i = 0; i < listView.getChildCount(); i++) {
            View view = listView.getChildAt(i);
            int pos = listView.getPositionForView(view);
            Channel ch = (Channel) listView.getItemAtPosition(pos);

            if (view.getTag() == null || ch == null) {
                continue;
            }

            if (channel.id != ch.id) {
                continue;
            }

            ChannelListViewWrapper wrapper = (ChannelListViewWrapper) view.getTag();
            wrapper.repaint(channel);
            break;
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ChannelListViewWrapper wrapper;

        Channel ch = getItem(position);
        Activity activity = (Activity) getContext();

        if (row == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            row = inflater.inflate(R.layout.item_channel, null, false);
            row.requestLayout();
            wrapper = new ChannelListViewWrapper(row);
            row.setTag(wrapper);

        } else {
            wrapper = (ChannelListViewWrapper) row.getTag();
        }

        if(selectedIndex != -1 && position == selectedIndex)
        {
            row.setBackgroundColor(Color.LTGRAY);
        }
        else
        {
            row.setBackgroundColor(Color.TRANSPARENT);
        }

        wrapper.repaint(ch);

        return row;
    }
}
