package org.gareiss.mike.ramoc.model;

import android.graphics.Bitmap;

/**
 *
 * @author john-tornblom
 */
public class ChannelTag {

    public long id;
    public String name;
    public String icon;
    public Bitmap iconBitmap;

    @Override
    public String toString() {
        return name;
    }
}