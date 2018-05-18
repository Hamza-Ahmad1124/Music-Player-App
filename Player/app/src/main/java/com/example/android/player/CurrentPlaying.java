package com.example.android.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;



public class CurrentPlaying extends AppCompatActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener
{
    private Song currentSong;
    private MediaPlayer mediaPlayer = MainActivity.mediaPlayer;
    private TextView currentTime;
    private SeekBar seekBar;
    private ImageView play_pause;
    private Handler progressHandler;
    private TextView currentSongName;
    private TextView currentArtistName;
    private TextView songDuration;
    private int seekBarMaxValue = 1000;
    private ImageView current_albumArt;
    
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_playing);
    
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        currentSong = MainActivity.playbackService.getCurrentPlayingSong();
        
        current_albumArt = (ImageView) findViewById(R.id.current_album_art);
        
        if(currentSong.albumArt == null)
        {
         //   current_albumArt.setImageResource(R.drawable.default_album_art);
        }
        
        else
        {
            current_albumArt.setImageDrawable(currentSong.albumArt);
        }
        
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(seekBarMaxValue);
        seekBar.setOnSeekBarChangeListener(this);
    
        currentSongName = (TextView) findViewById(R.id.song_title_current);
        currentSongName.setText(currentSong.songTitle);
        currentSongName.setSelected(true);
        
        currentArtistName = (TextView) findViewById(R.id.artist_name_current);
        currentArtistName.setText(currentSong.artistName);
        currentArtistName.setSelected(true);
    
        songDuration = (TextView) findViewById(R.id.song_duration);
        songDuration.setText(currentSong.duration);
    
        currentTime = (TextView) findViewById(R.id.current_time);
        
        ImageView skipPrevious = (ImageView) findViewById(R.id.skip_previous);
        ImageView fastRewind = (ImageView) findViewById(R.id.fast_rewind);
        play_pause = (ImageView) findViewById(R.id.play_pause);
        ImageView fastForward = (ImageView) findViewById(R.id.fast_forward);
        ImageView skipNext = (ImageView) findViewById(R.id.skip_next);
        
        skipPrevious.setOnClickListener(this);
        fastRewind.setOnClickListener(this);
        play_pause.setOnClickListener(this);
        fastForward.setOnClickListener(this);
        skipNext.setOnClickListener(this);
    
        progressHandler = new Handler();
        progressHandler.postDelayed(progressRunnable, seekBarMaxValue);
    }
    
    Runnable progressRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            setSeekProgress();
            refreshCurrentTime();
            progressHandler.postDelayed(this, seekBarMaxValue);
        }
    };
    
    @Override
    protected void onDestroy ()
    {
        Intent onDestroyedIntent = new Intent("APP DESTROYED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(onDestroyedIntent);
    
        super.onDestroy();
    }
    
    @Override
    public void onClick (View view)
    {
        int id = view.getId();
        
        if(id == R.id.skip_previous)
        {
            MainActivity.playbackService.playPrevious();
        }
    
        if(id == R.id.fast_rewind)
        {
            int pos = mediaPlayer.getCurrentPosition();
            pos -= 5000; // milliseconds
            mediaPlayer.seekTo(pos);
            setSeekProgress();
        }
    
        if(id == R.id.play_pause)
        {
            if (mediaPlayer.isPlaying())
            {
                play_pause.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                mediaPlayer.pause();
            }
            
            else
            {
                play_pause.setImageResource(R.drawable.ic_pause_white_48dp);
                mediaPlayer.start();
            }
        }
    
        if(id == R.id.fast_forward)
        {
            int pos = mediaPlayer.getCurrentPosition();
            pos += 5000; // milliseconds
            mediaPlayer.seekTo(pos);
            setSeekProgress();
        }
    
        if(id == R.id.skip_next)
        {
            MainActivity.playbackService.playNext();
        }
    }
    
    @Override
    public void onProgressChanged (SeekBar seekBar, int progressValue, boolean fromUser)
    {
        
    }
    
    @Override
    public void onStartTrackingTouch (SeekBar seekBar)
    {
        
    }
    
    @Override
    public void onStopTrackingTouch (SeekBar seekBar)
    {
        long duration = mediaPlayer.getDuration();
        long newposition = (duration * seekBar.getProgress()) / seekBarMaxValue;
        mediaPlayer.seekTo( (int) newposition);
        refreshCurrentTime();
        setSeekProgress();
    }
    
    private void refreshCurrentTime()
    {
        long currentDuration = mediaPlayer.getCurrentPosition();
    
        int milliseconds = (int) currentDuration % seekBarMaxValue;
    
        currentDuration = (int)(currentDuration / seekBarMaxValue);
    
        int seconds = (int)currentDuration % 60;
    
        String seconds_String = seconds + "";
    
        if(seconds_String.length() == 1)
        {
            seconds_String = "0" + seconds_String;
        }
    
        int minutes = (int)currentDuration / 60;
    
        String minutes_String = minutes + "";
    
        if(minutes_String.length() == 1)
        {
            minutes_String = "0" + minutes_String;
        }
    
        currentTime.setText((minutes_String + ":" + seconds_String));
    }
    
    private void setSeekProgress()
    {
        long position = ((seekBarMaxValue * mediaPlayer.getCurrentPosition())/mediaPlayer.getDuration());
        
        seekBar.setProgress(((int) position));
    }
    
    private void refreshAll()
    {
        currentSong = MainActivity.playbackService.getCurrentPlayingSong();
    
        current_albumArt.setImageDrawable(currentSong.albumArt);
        
        currentSongName.setText(currentSong.songTitle);
        currentSongName.setSelected(true);
        
        currentArtistName.setText(currentSong.artistName);
        currentArtistName.setSelected(true);
        
        songDuration.setText(currentSong.duration);
        
        seekBar.setProgress(0);
    
        play_pause.setImageResource(R.drawable.ic_pause_white_48dp);
    }
    
    protected void onResume ()
    {
        super.onResume();
        
        LocalBroadcastManager.getInstance(this).registerReceiver(onChangeReceiver, new IntentFilter("SONG_CHANGED"));
        
        refreshAll();
    }
    
    @Override
    protected void onPause ()
    {
        super.onPause();
        
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onChangeReceiver);
    }
    
    private BroadcastReceiver onChangeReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context c, Intent i)
        {
            refreshAll();
        }
    };
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
        
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                
                if (NavUtils.shouldUpRecreateTask(this, upIntent)|| isTaskRoot())
                {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                            // Navigate up to the closest parent
                            .startActivities();
    
                    MainActivity.playbackIntent = new Intent(this, PlaybackService.class);
                }
                
                else
                {
                    onBackPressed();
                }
                
                return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}