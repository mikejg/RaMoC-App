package org.gareiss.mike.ramoc.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by drue on 23.02.17.
 */

public class Actor
{
    String string_Name;
    String string_Character;
    String string_Id;
    Bitmap bitmap_Portrait;

    public void setId(String i) { string_Id = i; }
    public String getId() { return string_Id; }

    public void setName(String n) { string_Name = n; }
    public String getName() { return string_Name; }

    public void setCharacter(String c) { string_Character = c; }
    public String getCharacter() { return string_Character; }

    public Bitmap getPortrait() { return bitmap_Portrait; }
    public byte[] getPortraitAsByteArry()
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap_Portrait.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
    public void setPortrait(Bitmap bm) { bitmap_Portrait = bm; }
    public void setPortrait(byte[] byteArray)
    {
        bitmap_Portrait = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}
