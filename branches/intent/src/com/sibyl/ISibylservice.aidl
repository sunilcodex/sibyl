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
    
    boolean isPlaying();
    /* returns true if the service is playing: /!\ deprecated */
    
    int getState();
    /* returns the state of the core service: CsState is an enum as defined in 
       Music.java of the possible states
       This function replaces isPlaying() */
       
    int getCurrentSongIndex();
    /* returns the index in the playlist of the song currently played */
    
    int getCurrentPosition();
    /* returns the current position in the played song in milliseconds */
    
    void setCurrentPosition(in int msec);
    /* sets the current position in the played song in milliseconds */
    
    int getDuration();
    /* returns the duration of the played song in milliseconds */
    
    void setLooping(in boolean looping);
    /* activate or deactivate looping / repetition of the current song 
        If looping is set to 0: the current song is played once 
        If looping is set to 1: the current song is repeated while looping is not 0 */
        
    int getLooping();
    /* get looping state :
        0 : no loop
        1 : loop song
        2 : loop all songs  */
        
    void playSongPlaylist(int pos);
    /* joue la chanson pos de la playlist */
    
    void next();
    /* play the next song of the current playlist*/
    
    void prev();
    /* play the previous song of the current playlist */
    
    void setRepeatAll();
    
}