package com.example.android.player;

import android.content.Context;
import android.widget.MediaController;

/**
 * Created by Sherlock on 12/17/2017.
 */

public class MusicController extends MediaController
{
    public MusicController (Context context)
    {
        super(context);
    }
    
    @Override
    public void hide ()
    {
        if(MainActivity.hide == false)
        {
            return;
        }
        
        super.hide();
    }
}