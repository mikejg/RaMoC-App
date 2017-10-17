package org.gareiss.mike.ramoc.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by drue on 23.02.17.
 */

public class Movie
{
    private String string_Titel;
    private String string_Id;
    private String string_Jahr;
    private String string_Beschreibung;
    private String string_BildUrl;
    private String string_Regie;
    //private String string64_Cover;
    private String string_File;
    private String string_FSK;
    private String string_Laufzeit;
    private Bitmap bitmap_Cover;
    private Bitmap bitmap_Poster;
    private String string_Trailer;
    private String string_BackdropHash;
    private ArrayList<String> array_Genre;
    private ArrayList<Actor> array_Actor;

    public Movie()
    {
        array_Genre = new ArrayList<String>();
        array_Actor = new ArrayList<Actor>();
    }

    public void setFSK(String t) { string_FSK = t; }
    public String getFSK() { return string_FSK; }

    public void setBackdropHash(String t) { string_BackdropHash = t; }
    public String getBackdropHash() { return string_BackdropHash; }

    public void setTrailer(String t) { string_Trailer = t; }
    public String getTrailer() { return string_Trailer; }

    public void setLaufzeit(String t) { string_Laufzeit = t; }
    public String getLaufzeit() { return string_Laufzeit; }

    public void setTitel(String t) { string_Titel = t; }
    public String getTitel() { return string_Titel; }

    public void setRegie(String r) { string_Regie = r; }
    public String getRegie() { return string_Regie; }

    public void setId(String i) { string_Id = i; }
    public String getId() { return string_Id; }

    public void setJahr(String j) { string_Jahr = j; }
    public String getJahr() { return string_Jahr; }

    //public void setCoverString64 ( String s) { string64_Cover = s; }
    //String getCoverString64() { return string64_Cover; }

    public void setBeschreibung(String b) { string_Beschreibung = b; }
    public String getBeschreibung() { return string_Beschreibung; }

    public void setBildUrl(String bu) { string_BildUrl = bu; }
    public String getBildUrl() { return string_BildUrl; }

    public void addGenre(String g) { array_Genre.add(g); }
    public ArrayList<String> getGenre() { return array_Genre; }

    public void addActor(Actor a) { array_Actor.add(a); }
    public ArrayList<Actor> getActor() { return array_Actor; }

    public void setFile(String f) { string_File = f; }
    public String getFile() { return string_File; }

    public void setCover(Bitmap bm) { bitmap_Cover = bm; }
    public void setCover(byte[] byteArray)
    {
        bitmap_Cover = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
    public Bitmap getCover() { return bitmap_Cover; }
    public byte[] getCoverAsByteArry()
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap_Cover.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
    public void setPoster(Bitmap bm) { bitmap_Poster = bm; }
    public void setPoster(byte[] byteArray)
    {
        bitmap_Poster = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
    public Bitmap getPoster() { return bitmap_Poster; }
    public byte[] getPosterAsByteArry()
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap_Poster.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
}
