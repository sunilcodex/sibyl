/* 
 *
 * Copyright (C) 2007-2008 sibyl project
 * http://code.google.com/p/sibyl/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sibyl;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.os.DeadObjectException;

import com.sibyl.ui.IPlayerUI;


public class Sibylservice extends Service
{
    
    public enum CsState {
        STOPPED, PAUSED, PLAYING
    }
    
    private MediaPlayer mp;
    private MusicDB mdb;
    private CsState playerState;
    private int currentSong;
    private IPlayerUI uiHandler;

    /** creation of the service */
    @Override
    protected void onCreate()
    {        
        playerState=CsState.STOPPED;
        currentSong=1;
        mp = new MediaPlayer();
        mp.setOnCompletionListener(endSongListener);
        
        //create or connect to the Database
    	try{
    	    mdb = new MusicDB(this);
    	    Log.v(TAG,"BD OK");
    	}
        catch(Exception ex){
    	    Log.v(TAG, ex.toString()+" Create");
    	}

    }
    
    @Override
    protected void onStart(int startId, Bundle arguments)
    {

    }
    
    @Override
    protected void onDestroy()
    {
        mp.stop();
        mp.release();
    }
    
    protected void play() {
        if( playerState != CsState.PAUSED ) {
            play_next();
        }
        else {
            mp.start();
            playerState=CsState.PLAYING;
        }

    }
    
    protected void play_next() {
        Log.v(TAG,">>> Play_next() called: currentSong="+currentSong);
        String filename=mdb.getSong(currentSong++);
        playerState=CsState.PLAYING;
        //obser.notifyObservers();
        if(filename != null) playSong(filename);
        else playerState=CsState.STOPPED;
    }
    
    protected void play_prev() {
        Log.v(TAG,">>> play_prev() called: currentSong="+currentSong);
        currentSong-=2;
        String filename=mdb.getSong(currentSong++);
        playerState=CsState.PLAYING;
        //obser.notifyObservers();
        if(filename != null) playSong(filename);
        else playerState=CsState.STOPPED;
    }
    
    protected void playSong(String filename) 
    {
        Log.v(TAG,">>> PlaySong("+filename+") called");
        if( playerState != CsState.PAUSED ) {
        //we're not in pause so we start playing a new song
            try{
                mp.reset();
                mp.setDataSource(/*Music.MUSIC_DIR+"/"+*/filename);
                mp.prepare();
            }
            catch ( Exception e) {
                //remplacant du NotificationManager/notifyWithText
                Log.v(TAG, "playSong: exception: "+e.toString());
                //Notifications removed else they throw an exception
                //surely a problem of multithreading
               /* Toast.makeText(Sibylservice.this, "Exception: "+e.toString(), 
                    Toast.LENGTH_SHORT).show();*/
            }
            mp.start();
            
            //remplacant du NotificationManager/notifyWithText
            /*Toast.makeText(Sibylservice.this, "Playing song: "+filename, 
                    Toast.LENGTH_LONG).show();*/
        }
        else {
        //we're in pause so we continue playing the paused song
            mp.start();
            /*Toast.makeText(Sibylservice.this, "reprise", 
                    Toast.LENGTH_LONG).show();*/
            playerState=CsState.PLAYING;
        }

    }
    
    protected void playNumberI(int i)
    {
        currentSong = i;
        play();
        try 
        {
            uiHandler.handleEndSong();
        } catch(DeadObjectException e) {}
    }

    @Override
    public IBinder onBind(Intent i)
    {
        return mBinder;
    }
    
    //interface accessible par les autres classes (cf aidl)
    private final ISibylservice.Stub mBinder = new ISibylservice.Stub() {
        
        public void connectToReceiver(IPlayerUI receiver) {
            uiHandler=receiver;
        }

        public void start() {
            play();
        }
        
        public void stop() {
            mp.stop();
            playerState=CsState.STOPPED;
        }
        
        public void pause() {
            mp.pause();
            playerState=CsState.PAUSED;;
        }
        
        public CsState getState() {
            return playerState;
        }
        
        public boolean isPlaying() {
            return playerState==CsState.PLAYING;
        }
        
        public int getCurrentSongIndex() {
            return currentSong-1;
        }
        
        public int getCurrentPosition() {
            return mp.getCurrentPosition();
        }
        
        public int getDuration() {
            return mp.getDuration();
        }
        
        public void setCurrentPosition(int msec) {
            mp.seekTo(msec);
        }
        
        public void setLooping(int looping) {
            mp.setLooping(looping);
        }
        
        public void next() {
            play_next();
        }
        
        public void prev() {
            play_prev();
        }

        public void playSongPlaylist(int pos) 
        {
            playNumberI(pos);
        }
         
    };
    
    private OnCompletionListener endSongListener = new OnCompletionListener() 
    {
        public void onCompletion(MediaPlayer mp) 
        {
            play_next();
            try {
                uiHandler.handleEndSong();
            } catch(DeadObjectException e) {}
        } 
    };
   

}