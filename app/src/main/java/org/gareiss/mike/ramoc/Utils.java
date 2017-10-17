package org.gareiss.mike.ramoc;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.gareiss.mike.ramoc.model.Channel;
import org.gareiss.mike.ramoc.model.Recording;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by drue on 13.05.17.
 */
public class Utils
{
    // Constants required for the date calculation
    private static final int twoDays = 1000 * 3600 * 24 * 2;
    private static final int sixDays = 1000 * 3600 * 24 * 6;

    /**
     * Shows the channel icon and optionally the channel name. The icon will
     * only be shown when the user has activated the setting and an icon is
     * actually available. If no icon is available the channel name will be
     * shown as a placeholder.
     *
     * @param icon
     * @param iconText
     * @param channel
     * @param ch
     */
    public static void setChannelIcon(ImageView icon, TextView iconText, final Channel ch) {
        if (icon != null) {
            // Get the setting if the channel icon shall be shown or not
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(icon.getContext());
            final boolean showIcons = prefs.getBoolean("showIconPref", true);

            if (ch != null) {
                // Show the channels icon if available. If not hide the view.
                if (icon != null) {
                    icon.setImageBitmap((ch.iconBitmap != null) ? ch.iconBitmap : null);
                    icon.setVisibility((showIcons && ch.iconBitmap != null) ? ImageView.VISIBLE : ImageView.GONE);
                }
                // If the channel icon is not available show the channel name as a placeholder.
                if (iconText != null) {
                    iconText.setText(ch.name);
                    iconText.setVisibility((showIcons && ch.iconBitmap == null) ? ImageView.VISIBLE : ImageView.GONE);
                }
            } else {
                // Show a blank icon if no channel icon exists and they shall be shown.
                icon.setImageBitmap(null);
                icon.setVisibility(showIcons ? ImageView.VISIBLE : ImageView.GONE);
            }
        }
    }

    /**
     * Shows the given date. The date for the first days will be shown as words.
     * After one week the date value will be used.
     *
     * @param date
     * @param start
     */
    public static void setDate(TextView date, final Date start) {
        if (date == null || start == null) {
            return;
        }
        String dateText = "";
        if (DateUtils.isToday(start.getTime())) {
            // Show the string today
            dateText = date.getContext().getString(R.string.today);
        } else if (start.getTime() < System.currentTimeMillis() + twoDays
                && start.getTime() > System.currentTimeMillis() - twoDays) {
            // Show a string like "42 minutes ago"
            dateText = DateUtils.getRelativeTimeSpanString(start.getTime(), System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS).toString();
        } else if (start.getTime() < System.currentTimeMillis() + sixDays
                && start.getTime() > System.currentTimeMillis() - twoDays) {
            // Show the day of the week, like Monday or Tuesday
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.US);
            dateText = sdf.format(start.getTime());
        } else {
            // Show the regular date format like 31.07.2013
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
            dateText = sdf.format(start.getTime());
        }

        // Translate the day strings
        if (dateText.equals("today")) {
            date.setText(R.string.today);
        } else if (dateText.equals("tomorrow")) {
            date.setText(R.string.tomorrow);
        } else if (dateText.equals("in 2 days")) {
            date.setText(R.string.in_2_days);
        } else if (dateText.equals("Monday")) {
            date.setText(R.string.monday);
        } else if (dateText.equals("Tuesday")) {
            date.setText(R.string.tuesday);
        } else if (dateText.equals("Wednesday")) {
            date.setText(R.string.wednesday);
        } else if (dateText.equals("Thursday")) {
            date.setText(R.string.thursday);
        } else if (dateText.equals("Friday")) {
            date.setText(R.string.friday);
        } else if (dateText.equals("Saturday")) {
            date.setText(R.string.saturday);
        } else if (dateText.equals("Sunday")) {
            date.setText(R.string.sunday);
        } else if (dateText.equals("yesterday")) {
            date.setText(R.string.yesterday);
        } else if (dateText.equals("2 days ago")) {
            date.setText(R.string.two_days_ago);
        } else {
            date.setText(dateText);
        }
    }
    /**
     * Shows the given time for the given view.
     *
     * @param time
     * @param start
     * @param stop
     */
    public static void setTime(TextView time, final Date start, final Date stop) {
        if (time == null || start == null || stop == null) {
            return;
        }
        time.setVisibility(View.VISIBLE);
        final String startTime = DateFormat.getTimeFormat(time.getContext()).format(start);
        final String endTime = DateFormat.getTimeFormat(time.getContext()).format(stop);
        time.setText(startTime + " - " + endTime);
    }

    /**
     * Shows the given duration for the given view. If the duration is zero the
     * view will be hidden.
     *
     * @param duration
     * @param start
     * @param stop
     */
    public static void setDuration(TextView duration, final Date start, final Date stop) {
        if (duration == null || start == null || stop == null) {
            return;
        }
        duration.setVisibility(View.VISIBLE);
        // Get the start and end times so we can show them
        // and calculate the duration. Then show the duration in minutes
        final double durationTime = ((stop.getTime() - start.getTime()) / 1000 / 60);
        final String s = duration.getContext().getString(R.string.minutes, (int) durationTime);
        duration.setText(duration.getContext().getString(R.string.minutes, (int) durationTime));
        duration.setVisibility((s.length() > 0) ? View.VISIBLE : View.GONE);
    }

    /**
     * Shows the given description text for the given view. If the text is empty
     * then the view will be hidden.
     *
     * @param descriptionLabel
     * @param description
     * @param desc
     */
    public static void setDescription(TextView descriptionLabel, TextView description, final String desc) {
        if (description == null) {
            return;
        }
        description.setText(desc);
        description.setVisibility((desc != null && desc.length() > 0) ? View.VISIBLE : View.GONE);
        if (descriptionLabel != null) {
            descriptionLabel.setVisibility((desc != null && desc.length() > 0) ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Shows the reason why a recording has failed. If the text is empty then
     * the view will be hidden.
     *
     * @param failed_reason
     * @param rec
     */
    public static void setFailedReason(final TextView failed_reason, final Recording rec) {
        if (failed_reason == null) {
            return;
        }

        // Make the text field visible as a default
        failed_reason.setVisibility(View.VISIBLE);

        // Show the reason why it failed
        if (rec.error != null && rec.error.equals("File missing")) {
            failed_reason.setText(failed_reason.getResources().getString(R.string.recording_file_missing));
        } else if (rec.error != null && rec.error.equals("Aborted by user")) {
            failed_reason.setText(failed_reason.getResources().getString(R.string.recording_canceled));
        } else if (rec.state != null && rec.state.equals("missed")) {
            failed_reason.setText(failed_reason.getResources().getString(R.string.recording_time_missed));
        } else if (rec.state != null && rec.state.equals("invalid")) {
            failed_reason.setText(failed_reason.getResources().getString(R.string.recording_file_invalid));
        } else {
            failed_reason.setVisibility(View.GONE);
        }
    }

}

