package org.gareiss.mike.ramoc.tv;

import android.annotation.SuppressLint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.model.Channel;
import org.gareiss.mike.ramoc.model.Programme;

import java.util.Date;
import java.util.Iterator;

/**
 * Created by drue on 12.03.17.
 */

public class ChannelListViewWrapper
{
    private TextView name;
    private TextView nowTitle;
    private TextView nowTime;
    private TextView nextTitle;
    private TextView nextTime;
    private ImageView icon;
    private ImageView nowProgressImage;
    private ClipDrawable nowProgress;

    @SuppressLint("RtlHardcoded")
    @SuppressWarnings("deprecation")
    public ChannelListViewWrapper(View base) {
        name = (TextView) base.findViewById(R.id.ch_name);
        nowTitle = (TextView) base.findViewById(R.id.ch_now_title);

        nowProgressImage = (ImageView) base.findViewById(R.id.ch_elapsedtime);
        nowProgress = new ClipDrawable(nowProgressImage.getDrawable(), Gravity.LEFT, ClipDrawable.HORIZONTAL);
        nowProgressImage.setBackgroundDrawable(nowProgress);

        nowTime = (TextView) base.findViewById(R.id.ch_now_time);
        nextTitle = (TextView) base.findViewById(R.id.ch_next_title);
        nextTime = (TextView) base.findViewById(R.id.ch_next_time);
        icon = (ImageView) base.findViewById(R.id.ch_icon);
    }

    @SuppressWarnings("deprecation")
    public void repaint(Channel channel) {
        nowTime.setText("");
        nowTitle.setText("");
        nextTime.setText("");
        nextTitle.setText("");
        nowProgress.setLevel(0);

        name.setText(channel.name);
        name.invalidate();

        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(icon.getContext());
        //Boolean showIcons = prefs.getBoolean("showIconPref", false);
        icon.setBackgroundDrawable(new BitmapDrawable(channel.iconBitmap));

        Iterator<Programme> it = channel.epg.iterator();
        if (!channel.isTransmitting && it.hasNext()) {
            nowTitle.setText(R.string.ch_no_transmission);
        } else if (it.hasNext()) {
            Programme p = it.next();
            nowTime.setText(
                    DateFormat.getTimeFormat(nowTime.getContext()).format(p.start)
                            + " - "
                            + DateFormat.getTimeFormat(nowTime.getContext()).format(p.stop));

            double duration = (p.stop.getTime() - p.start.getTime());
            double elapsed = new Date().getTime() - p.start.getTime();
            double percent = elapsed / duration;

            nowProgressImage.setVisibility(ImageView.VISIBLE);
            nowProgress.setLevel((int) Math.floor(percent * 10000));
            nowTitle.setText(p.title);
        } else {
            nowProgressImage.setVisibility(ImageView.GONE);
        }
        nowProgressImage.invalidate();
        nowTime.invalidate();
        nowTitle.invalidate();

        if (it.hasNext()) {
            Programme p = it.next();
            nextTime.setText(
                    DateFormat.getTimeFormat(nextTime.getContext()).format(p.start)
                            + " - "
                            + DateFormat.getTimeFormat(nextTime.getContext()).format(p.stop));

            nextTitle.setText(p.title);
        }
        nextTime.invalidate();
        nextTitle.invalidate();
    }

}
