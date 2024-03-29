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

interface ISibylservice {

    void playlistChange();

    void start();
    /* start playing the current item in the selected playlist */
    
    void stop();
    /* stop playing */
    
    void clear();
    /* clear playlist & stop playing */
    
    void pause();
    /* pause the song. To start playing the paused song at its current position
        use start */
    
    int getState();
    /* returns the state of the core service: CsState is an enum as defined in 
       Music.java of the possible states
       This function replaces isPlaying() */
       
    int getCurrentSongIndex();
    /* returns the index in the playlist of the song currently played */
    
    int getCurrentPosition();
    /* returns the current position in the played song in milliseconds */
    
    boolean setCurrentPosition(in int msec);
    /* sets the current position in the played song in milliseconds
        Returns true if the pos has been successfully changed, and false otherwise.
        (i.e: returns false if the service is in a different state than playing 
        or paused and does nothing in this case)
    */
    
    int getDuration();
    /* returns the duration of the played song in milliseconds */
    
    
    void setLoopMode(int mode);
    /* sets the loop mode which is one of the loop mode defined in Music.java
        NO_REPEAT: each song will be played once
        REPEAT_SONG: the current song will be repeated while loopmode is REPEAT_SONG 
        REPEAT_PLAYLIST: the current playlist will be repeated when finished  
    */
        
    int getLooping();
    /* get looping state : (see Music.java)
        NO_REPEAT : no loop
        REPEAT_SONG : loop song
        REPEAT_PLAYLIST : loop all songs  */
        
    void playSongPlaylist(int pos);
    /* plays the song at position pos in the playlist */
    
    void next();
    /* play the next song of the current playlist*/
    
    void prev();
    /* play the previous song of the current playlist */   
    
    void setPlayMode(int mode);
    /* sets the play mode of the service. The mode is one of the modes defined in Music.java
        NORMAL or RANDOM
    */
    int getPlayMode();
    
}