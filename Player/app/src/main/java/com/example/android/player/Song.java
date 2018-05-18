package com.example.android.player;

import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.Log;

import java.io.Serializable;

/**
 * Created by Sherlock on 12/16/2017.
 */

public class Song
{
    public long songID ;
    public String songTitle;
    public String artistName;
    public String duration;
    public Drawable albumArt;
    public String albumArtPath;
    
    private int position = 0;
    
    public Song(long mSongID, String mSongTitle, String mArtistName, String mDuration, String mAlbumArtPath)
    {
        songID = mSongID;
        songTitle = mSongTitle;
        artistName = mArtistName;
        
        if(mAlbumArtPath != null)
        {
            albumArtPath = mAlbumArtPath;
            albumArt = Drawable.createFromPath(mAlbumArtPath);
        }
        
        duration = setDuration(mDuration);
    }
    
    private String setDuration(String mDuration)
    {
        long duration = Long.parseLong(mDuration);
        
        int milliseconds = (int) duration % 1000;
        
        duration = (int)(duration / 1000);
        
        int seconds = (int)duration % 60;
        
        String seconds_String = seconds + "";
        
        if(seconds_String.length() == 1)
        {
            seconds_String = "0" + seconds_String;
        }
        
        int minutes = (int)duration / 60;
    
        String minutes_String = minutes + "";
    
        if(minutes_String.length() == 1)
        {
            minutes_String = "0" + minutes_String;
        }
        
        return (minutes_String + ":" + seconds_String);
    }
    
    public void setPosition(int mPosition)
    {
        position = mPosition;
    }
    
    public int getPosition()
    {
        return position;
    }
}
