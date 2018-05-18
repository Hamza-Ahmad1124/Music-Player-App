package com.example.android.player;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import static android.R.attr.resource;

/**
 * Created by Sherlock on 12/16/2017.
 */

public class MediaAdapter extends ArrayAdapter<Song>
{
    public MediaAdapter(Context context , ArrayList<Song> songs)
    {
        super(context , 0 , songs);
    }
    
    @NonNull
    @Override
    public View getView (int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View currentView = convertView;
        
        if(currentView == null)
        {
            currentView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
    
        Song currentSong = getItem(position);
        
        currentSong.setPosition(position);
        
        ImageView albumArt_ImageView = (ImageView) currentView.findViewById(R.id.album_art);
        
        if (currentSong.albumArt == null)
        {
            albumArt_ImageView.setImageResource(R.drawable.default_album_art);
        }
        
        else
        {
            albumArt_ImageView.setImageDrawable(currentSong.albumArt);
        }
        
        TextView songTitle_TextView = (TextView) currentView.findViewById(R.id.song_title);
        songTitle_TextView.setText(currentSong.songTitle);
        
        TextView artistName_TextView = (TextView) currentView.findViewById(R.id.artist_name);
        artistName_TextView.setText(currentSong.artistName);
        
        TextView duration_TextView = (TextView) currentView.findViewById(R.id.duration);
        duration_TextView.setText(currentSong.duration);
        
        return currentView;
    }
}