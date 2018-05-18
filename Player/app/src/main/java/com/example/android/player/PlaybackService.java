package com.example.android.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.MediaController;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Sherlock on 12/16/2017.
 */

public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MediaController.MediaPlayerControl
{
    public static MediaPlayer player;
    private ArrayList<Song> songs;
    public static int songPosition;
    private static final int NOTIFY_ID = 1;
    public static PlaybackService playbackService;
    private RemoteViews remoteViews;
    
    @Nullable
    @Override
    public IBinder onBind (Intent intent)
    {
        return null;
    }
    
    @Override
    public void onCreate ()
    {
        super.onCreate();
    
        playbackService = PlaybackService.this;
    
        player = new MediaPlayer();
    
        songPosition = 0;
    
        initMediaPlayer();
    }
    
    private void initMediaPlayer()
    {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }
    
    public void setSong(int songIndex)
    {
        songPosition = songIndex;
    }
    
    public Song getCurrentPlayingSong()
    {
        return songs.get(songPosition);
    }
    
    public void setSongsList(ArrayList<Song> songsList)
    {
        songs = songsList;
    }
    
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        LocalBroadcastManager.getInstance(this).registerReceiver(onDestroyReceiver, new IntentFilter("APP DESTROYED"));
    
        playbackService = PlaybackService.this;
        
        if(songs != null)
        {
            playSong();
        }
        
        Toast.makeText(getApplicationContext() , "Service Started" , Toast.LENGTH_SHORT).show();
        
        return START_STICKY;
    }
    
    private void playSong()
    {
        player.reset();
        
        Song playSong = songs.get(songPosition);
        
        long currentSong = playSong.songID;
        
        Uri currentTrackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong);
    
        try
        {
            player.setDataSource(getApplicationContext() , currentTrackUri);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    
        player.prepareAsync();
    }
    
    public void playNext ()
    {
        if (songPosition == (songs.size() - 1))
        {
            songPosition = 0;
        }
    
        else
        {
            songPosition ++;
        }
    
        playSong();
    }
    
    public void playPrevious ()
    {
        if (songPosition == 0)
        {
            songPosition = (songs.size() - 1);
        }
        
        else
        {
            songPosition --;
        }
        
        playSong();
    }
    
    @Override
    public void onDestroy ()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onDestroyReceiver);
        stopForeground(true);
        player.stop();
        Toast.makeText(getApplicationContext() , "Service Stopped" , Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
    
    @Override
    public void onPrepared (MediaPlayer mp)
    {
        player = mp;
        player.start();
        
        Intent onChangedIntent = new Intent("SONG_CHANGED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(onChangedIntent);
        
        createCustomNotification();
    }
    
    @Override
    public void onCompletion (MediaPlayer mp)
    {
        playNext();
    }
    
    @Override
    public boolean onError (MediaPlayer mp, int what, int extra)
    {
        mp.reset();
        return false;
    }
    
    private void createCustomNotification()
    {
        Song currentPlayingSong = songs.get(songPosition);
    
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
    
        if(currentPlayingSong.albumArtPath == null)
        {
            remoteViews.setImageViewResource(R.id.notification_image , R.drawable.default_album_art);
        }

        else
        {
            Uri albumArtUri = Uri.parse(currentPlayingSong.albumArtPath);
            remoteViews.setImageViewUri(R.id.notification_image , albumArtUri);
        }
        
        remoteViews.setTextViewText(R.id.notification_song_name , currentPlayingSong.songTitle);
        
        remoteViews.setImageViewResource(R.id.notification_play_pause , R.drawable.ic_pause_black_48dp);
        
        Intent skip_previous_intent = new Intent("Skip_Previous_Clicked");
        Intent play_pause_intent = new Intent("Play_Pause_Clicked");
        Intent skip_next_intent = new Intent("Skip_Next_Clicked");

        skip_previous_intent.putExtra("working", "skip_previous");
        play_pause_intent.putExtra("working", "play_pause");
        skip_next_intent.putExtra("working", "skip_next");
        
        PendingIntent skip_previous_pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 68 , skip_previous_intent, 0);
        PendingIntent play_pause_pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 69 , play_pause_intent, 0);
        PendingIntent skip_next_pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 70 , skip_next_intent, 0);

        remoteViews.setOnClickPendingIntent(R.id.notification_skip_previous , skip_previous_pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.notification_play_pause , play_pause_pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.notification_skip_next , skip_next_pendingIntent);

        Intent notificationIntent = new Intent(this, CurrentPlaying.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
    
        builder.setContentIntent(pendingIntent)
                .setContentTitle("Sample Text")
                .setSmallIcon(R.drawable.ic_play_circle_filled_white_48dp)
                .setContent(remoteViews);
    
        Notification notification = builder.build();
        
        startForeground(NOTIFY_ID, notification);
    }
    
    public void updatePlayPauseNotificationButton()
    {
        if(player.isPlaying())
        {
            remoteViews.setImageViewResource(R.id.notification_play_pause, R.drawable.ic_pause_black_48dp);
        }
        
        else
        {
            remoteViews.setImageViewResource(R.id.notification_play_pause, R.drawable.ic_play_arrow_black_48dp);
        }
    
        Intent notificationIntent = new Intent(this, CurrentPlaying.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    
        Notification.Builder builder = new Notification.Builder(this);
    
        builder.setContentIntent(pendingIntent)
                .setContentTitle("Sample Text")
                .setSmallIcon(R.drawable.ic_play_circle_filled_white_48dp)
                .setContent(remoteViews);
    
        Notification notification = builder.build();
    
        startForeground(NOTIFY_ID, notification);
    }
    
    public void start ()
    {
        player.start();
    }
    
    public void pause ()
    {
        player.pause();
    }
    
    public int getDuration ()
    {
        return player.getDuration();
    }
    
    public int getCurrentPosition ()
    {
        return player.getCurrentPosition();
    }
    
    public void seekTo (int i)
    {
        player.seekTo(i);
    }
    
    public boolean isPlaying ()
    {
        return player.isPlaying();
    }
    
    public int getBufferPercentage ()
    {
        return 0;
    }
    
    public boolean canPause ()
    {
        return true;
    }
    
    public boolean canSeekBackward ()
    {
        return true;
    }
    
    public boolean canSeekForward ()
    {
        return true;
    }
    
    public int getAudioSessionId ()
    {
        return 0;
    }
    
    private BroadcastReceiver onDestroyReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context c, Intent i)
        {
            destroyService();
        }
    };
    
    private void destroyService()
    {
        if (!(player.isPlaying()))
        {
            stopSelf();
        }
    }
}
