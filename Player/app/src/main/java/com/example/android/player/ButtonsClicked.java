package com.example.android.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

/**
 * Created by Sherlock on 12/20/2017.
 */

public class ButtonsClicked extends BroadcastReceiver
{
    private MediaPlayer mediaPlayer = PlaybackService.player;
    private PlaybackService playbackService = PlaybackService.playbackService;
    
    @Override
    public void onReceive (Context context, Intent intent)
    {
        String working = intent.getStringExtra("working");
        
        if(working.equals("skip_previous"))
        {
            if(mediaPlayer.isPlaying())
            {
                playbackService.playPrevious();
            }
    
            else
            {
                playbackService.playPrevious();
                playbackService.updatePlayPauseNotificationButton();
            }
        }
        
        if(working.equals("play_pause"))
        {
            if(mediaPlayer.isPlaying())
            {
                mediaPlayer.pause();
                playbackService.updatePlayPauseNotificationButton();
            }
            
            else
            {
                mediaPlayer.start();
                playbackService.updatePlayPauseNotificationButton();
            }
        }
    
        if(working.equals("skip_next"))
        {
            if(mediaPlayer.isPlaying())
            {
                playbackService.playNext();
            }
    
            else
            {
                playbackService.playNext();
                playbackService.updatePlayPauseNotificationButton();
            }
        }
    }
}