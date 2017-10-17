package org.gareiss.mike.ramoc.model;

/**
 * Created by drue on 09.03.17.
 */

public class TVShow
{
    private String string_File;
    private String string_Episode;
    private boolean bool_Played;
    private int int_ID;

    public void setEpisode(String e) { string_Episode = e; }
    public String getEpisode() { return string_Episode; }

    public void setFile(String f) { string_File = f; }
    public String getFile() { return string_File; }

    public void setID(int i) { int_ID = i; }
    public int getID() { return int_ID; }

    public void setPlayed(boolean b) { bool_Played = b; }
    public boolean getPlayed() { return bool_Played; }
}
