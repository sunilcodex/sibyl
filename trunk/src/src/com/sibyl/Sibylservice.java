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

import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDiskIOException;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.util.Log;

import com.sibyl.ui.IPlayerUI;


public class Sibylservice extends Service
{
    
    public enum CsState {
        STOPPED, PAUSED, PLAYING
    } 
    
    private MediaPlayer mp;
    private MusicDB mdb;
    private CsState playerState;
    private boolean repAll;
    private boolean looping;
    private int currentSong;
    private IPlayerUI uiHandler;
    private NotificationManager nm;

    /** creation of the service */
    @Override
    protected void onCreate()
    {        
        playerState=CsState.STOPPED;
        currentSong=1;
        mp = new MediaPlayer();
        mp.setOnCompletionListener(endSongListener);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        repAll = false;
        looping = false;
        //create or connect to the Database
        try{
            mdb = new MusicDB(this);
        }catch (SQLiteDiskIOException e){
            Log.v("SibylService", e.getMessage());
            // what sould we do ? updateNotification ?
        }
        
        updateNotification(R.drawable.pause,"Sibyl, mobile your music !");
    }
    
    @Override
    protected void onStart(int startId, Bundle arguments)
    {

    }
    
    public void updateNotification(int idIcon, String text)
    {
        Intent appIntent = new Intent(this, com.sibyl.ui.PlayerUI.class);
        
        nm.notify(
                R.layout.notification,                  // we use a string id because it is a unique
                                                   // number.  we use it later to cancel the
                                                   // notification
                new Notification(
                    this,                               // our context
                    idIcon,                             // the icon for the status bar
                    text,                               // the text to display in the ticker
                    System.currentTimeMillis(),         // the timestamp for the notification
                    "Sibyl",                            // the title for the notification
                    text,                               // the details to display in the notification
                    appIntent,                               // the contentIntent (see above)
                    R.drawable.icon,                    // the app icon
                    getText(R.string.app_name),         // the name of the app
                    appIntent)); // the appIntent (see above)

    }
    
    @Override
    protected void onDestroy()
    {
        MediaPlayer mp = this.mp;
        mp.stop();
        mp.release();
        nm.cancel(R.layout.notification);
    }
    
    protected void play() 
    {
        if( playerState != CsState.PAUSED ) 
        {
            launch();
        }
        else 
        {
            resume();
        }

    }
    
    protected void launch()
    {
        String filename = mdb.getSong(currentSong);
        playerState = CsState.PLAYING;
        if(filename != null)
        {
            playSong(filename);
            String [] songInfo = mdb.getSongInfoFromCP(currentSong);
            updateNotification(R.drawable.play, songInfo[0]+"-"+songInfo[1]);
        }
        else
        {
            if(currentSong == 1 || !repAll)
            {
                stop();
                currentSong = 1;
                try 
                {
                    uiHandler.handleEndPlaylist();
                } catch (DeadObjectException e) 
                {
                    e.printStackTrace();
                }   
            }
            else
            {
                currentSong = 1;
                launch();
                // ui will have some troubles, restart app ?
            }
        }
    }
    
    protected void stop()
    {
        mp.stop();
        playerState=CsState.STOPPED;
    }
    
    protected void resume()
    {
        mp.start();
        playerState=CsState.PLAYING;
        String [] songInfo = mdb.getSongInfoFromCP(currentSong);
        // should test songInfo != null if playlist is empty
        updateNotification(R.drawable.play, songInfo[0]+"-"+songInfo[1]);
    }
    
    protected void play_next() 
    {
        Log.v(TAG,">>> Play_next() called: currentSong="+currentSong);
        currentSong++;
        launch();
    }
    
    protected void play_prev() 
    {
        Log.v(TAG,">>> Play_prec() called: currentSong="+currentSong);
        if (playerState != CsState.STOPPED)
        {
            currentSong--; 
            launch();  
        }
    }
    
    protected void playSong(String filename) 
    {
        MediaPlayer mp = this.mp;
        //Log.v(TAG,">>> PlaySong("+filename+") called");
        if( playerState != CsState.PAUSED ) 
        {
            try{
                mp.reset();
                mp.setDataSource(filename);
                mp.prepare();
                uiHandler.handleEndSong();
                mp.start();
            }
            catch ( IOException ioe) {
                Log.v(TAG, ioe.toString());
            }catch (DeadObjectException doe){
        	Log.v(TAG, doe.toString());
            }
        }
        mp.start();
        playerState=CsState.PLAYING;
    }
    
    protected void playNumberI(int i)
    {
        stop();
        currentSong = i;
        play();
        try 
        {
            uiHandler.handleEndSong();
        } catch(DeadObjectException e) {
            Log.v(TAG, e.toString());
            // what are the consequences ?
        }
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
            playerState=CsState.PAUSED;
            updateNotification(R.drawable.pause,"pause");
        }
        
        public CsState getState() {
            return playerState;
        }
        
        public boolean isPlaying() {
            return playerState==CsState.PLAYING;
        }
        
        public int getCurrentSongIndex() {
            return currentSong;
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
        
        public void setLooping(boolean loop) 
        {
            Log.v(TAG,"set looping :"+loop);
            repAll = false;
            looping = loop;
            mp.setLooping(loop ? 1 : 0);
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

        public void setRepeatAll()
        {
            Log.v(TAG,"set repeatALL");
            repAll = true;
            looping = false;
        }
         
    };
    
    private OnCompletionListener endSongListener = new OnCompletionListener() 
    {
        public void onCompletion(MediaPlayer mp) 
        {
            mdb.countUp(currentSong);
            if(!looping)
            {
                play_next();
            }
        } 
    };
   

}
