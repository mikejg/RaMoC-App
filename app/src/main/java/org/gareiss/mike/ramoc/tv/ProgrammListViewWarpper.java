package org.gareiss.mike.ramoc.tv;

import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.model.Programme;

import java.text.SimpleDateFormat;

public class ProgrammListViewWarpper {

    TextView title;
    TextView time;
    TextView seriesInfo;
    TextView date;
    TextView description;
    ImageView state;

    public ProgrammListViewWarpper(View base) {
        title = (TextView) base.findViewById(R.id.pr_title);
        description = (TextView) base.findViewById(R.id.pr_desc);
        seriesInfo = (TextView) base.findViewById(R.id.pr_series_info);

        time = (TextView) base.findViewById(R.id.pr_time);
        date = (TextView) base.findViewById(R.id.pr_date);

        state = (ImageView) base.findViewById(R.id.pr_state);
    }

    public void repaint(Programme p)
    {
        //RaMoCApplication app = (RaMoCApplication) getApplication();
        title.setText(p.title);

        /*if (p.recording == null) {
            state.setImageDrawable(null);
        } else if (p.recording.error != null) {
            state.setImageResource(R.drawable.ic_error_small);
        } else if ("completed".equals(p.recording.state)) {
            state.setImageResource(R.drawable.ic_success_small);
        } else if ("invalid".equals(p.recording.state)) {
            state.setImageResource(R.drawable.ic_error_small);
        } else if ("missed".equals(p.recording.state)) {
            state.setImageResource(R.drawable.ic_error_small);
        } else if ("recording".equals(p.recording.state)) {
            state.setImageResource(R.drawable.ic_rec_small);
        } else if ("scheduled".equals(p.recording.state)) {
            state.setImageResource(R.drawable.ic_schedule_small);
        } else {
            state.setImageDrawable(null);
        }*/

        if(p.pendingIntent != null)
            state.setImageResource(R.drawable.ic_schedule_small);

        title.invalidate();

        /*String s = buildSeriesInfoString(p.seriesInfo);
        if(s.length() == 0) {
        	s = contentTypes.get(p.contentType);
        }*/

        //seriesInfo.setText(s);
        //seriesInfo.invalidate();

        if (p.description.length() > 0) {
            description.setText(p.description);
            description.setVisibility(TextView.VISIBLE);
        } else {
            description.setText("");
            description.setVisibility(TextView.GONE);
        }
        description.invalidate();

        if (DateUtils.isToday(p.start.getTime()))
        {
            date.setText(" ");
        } else if(p.start.getTime() < System.currentTimeMillis() + 1000*60*60*24*2 &&
                p.start.getTime() > System.currentTimeMillis() - 1000*60*60*24*2) {
            date.setText(DateUtils.getRelativeTimeSpanString(p.start.getTime(),
                    System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS));
        } else if(p.start.getTime() < System.currentTimeMillis() + 1000*60*60*24*6 &&
                p.start.getTime() > System.currentTimeMillis() - 1000*60*60*24*2) {
            date.setText(new SimpleDateFormat("EEEE").format(p.start.getTime()));
        } else {
            date.setText(DateFormat.getDateFormat(date.getContext()).format(p.start));
        }

        date.invalidate();

        time.setText(
                DateFormat.getTimeFormat(time.getContext()).format(p.start)
                        + " - "
                        + DateFormat.getTimeFormat(time.getContext()).format(p.stop));
        time.invalidate();
    }
}

