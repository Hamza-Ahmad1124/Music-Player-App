package com.example.android.player;

import android.content.Intent;

import android.database.Cursor;
import android.media.MediaPlayer;

import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    public static PlaybackService playbackService;
    public static Intent playbackIntent;
    
    public static MediaPlayer mediaPlayer;
    public ListView songsListView;
    
    private ArrayList<Song> songList;
    private MusicController musicController;
    private int currentPlayingPosition;
    
    public static boolean hide = true;
    
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        songsListView = (ListView) findViewById(R.id.songs_list);
        
        gatherList();
        
        MediaAdapter mediaAdapter = new MediaAdapter(this, songList);
    
        if(playbackIntent == null)
        {
            playbackIntent = new Intent(this, PlaybackService.class);
            startService(playbackIntent);
        }
        
        songsListView.setOnItemClickListener(this);
        songsListView.setAdapter(mediaAdapter);
    }
    
    private void gatherList ()
    {
        songList = new ArrayList<Song>();
    
        String[] songsProjection = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION,};
        
        Cursor musicCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songsProjection, MediaStore.Audio.Media.IS_MUSIC + "=1", null, "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");
        
        if(musicCursor != null && musicCursor.moveToFirst())
        {
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int AlbumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            
            do
            {
                long id = musicCursor.getLong(idColumn);
                String title = musicCursor.getString(titleColumn);
                String artist = musicCursor.getString(artistColumn);
                String duration = musicCursor.getString(durationColumn);
                String albumId = musicCursor.getString(AlbumIdColumn);
                
                songList.add(new Song(id, title, artist, duration, getAlbumArt(albumId)));
            }
            while (musicCursor.moveToNext());
        }
    }
    
    private String getAlbumArt (String albumId)
    {
        String[] albumsProjection = new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART};
        
        Cursor albumCursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumsProjection, MediaStore.Audio.Albums._ID + "=" + albumId, null, null);
        
        albumCursor.moveToFirst();
        
        int albumArtColumn = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
        
        return albumCursor.getString(albumArtColumn);
    }
    
    private void setController ()
    {
        if(musicController == null)
        {
            musicController = new MusicController(this);
        }
        
        musicController.setPrevNextListeners
                (
                        new View.OnClickListener()
                        {
                            public void onClick (View v)
                            {
                                playbackService.playNext();
                            }
                        },
        
                        new View.OnClickListener()
                        {
                            public void onClick (View v)
                            {
                                playbackService.playPrevious();
                            }
                        }
                );
    
        musicController.setMediaPlayer(playbackService);
        musicController.setAnchorView(findViewById(R.id.songs_list));
        musicController.setEnabled(true);
    }
    
    @Override
    public void onItemClick (AdapterView<?> parent, View view, int position, long id)
    {
        playbackService = PlaybackService.playbackService;
    
        setController();
        
        mediaPlayer = playbackService.player;
        playbackService.setSongsList(songList);
        playbackService.setSong(position);
        
        if((mediaPlayer.isPlaying() == false) || currentPlayingPosition != position)
        {
            startService(playbackIntent);
            currentPlayingPosition = position;
        }
    
        if(musicController.isShowing())
        {
            hide = true;
            musicController.hide();
        }
        
        Intent intent = new Intent(this , CurrentPlaying.class);
        startActivity(intent);
    }
    
    @Override
    protected void onResume ()
    {
        super.onResume();
        
        if(mediaPlayer != null)
        {
            hide = false;
            
            try
            {
                musicController.show();
            }
            
            catch(Exception e)
            {
                setController();
                currentPlayingPosition = playbackService.songPosition;
            }
        }
    }
    
    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();
       
        try
        {
            musicController.show();
        }
        
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onPause ()
    {
        super.onPause();
        
        if(musicController != null)
        {
            if(musicController.isShowing())
            {
                hide = true;
                musicController.hide();
            }
        }
    }
    
    @Override
    protected void onDestroy ()
    {
        Intent onDestroyedIntent = new Intent("APP DESTROYED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(onDestroyedIntent);
        
        super.onDestroy();
    }
}