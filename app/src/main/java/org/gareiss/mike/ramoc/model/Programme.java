package org.gareiss.mike.ramoc.model;

import android.app.PendingIntent;

import java.util.Date;

/**
 *
 * @author john-tornblom
 */
public class Programme implements Comparable<Programme> {

    public long id;
    public long nextId;
    public int contentType;
    public Date start;
    public Date stop;
    public String title;
    public String description;
    public String summary;
    public SeriesInfo seriesInfo;
    public int starRating;
    public Channel channel;
    public Recording recording;
    public PendingIntent pendingIntent;

    public int compareTo(Programme that) {
        return this.start.compareTo(that.start);
    }

    public boolean isRecording() {
        return recording != null && "recording".equals(recording.state);
    }

    public boolean isScheduled() {
        return recording != null && "scheduled".equals(recording.state);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Programme) {
            return ((Programme) o).id == id;
        }

        return false;
    }
}

