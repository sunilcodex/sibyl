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

public class Music {

    public static final String[] SUPPORTED_FILE_FORMAT = { ".mp3", ".ogg" };

    public static final class Action {
        // lets try to put strings with different sizes to optimize equals test
        // or thats just totally stupid
        public static final String PLAY  =    "play";
        public static final String PAUSE =    "pause";
        public static final String NEXT  =    "next--";
        public static final String PREVIOUS = "previous";
        public static final String NO_SONG  = "no_song---";
    }

    /* printed name of the application */
    public static final String APPNAME = "Sibyl";

    public static final String PREFS = "sibyl_prefs";

    /* states of the service */
    public static final class State {
        public static final int ERROR = -1;
        public static final int PLAYING = 0;
        public static final int PAUSED = 1;
        public static final int STOPPED = 2;
        public static final int END_PLAYLIST_REACHED = 0x10;
    }

    /* Modes of the service */
    public static final class Mode {
        public static final int NORMAL = 0; // songs are played in the order of the playlist
        public static final int RANDOM = 1; // songs are played randomly
        public static int getText(int mode){
            switch(mode){
                case RANDOM : return R.string.random;
                default : return R.string.normal;
            }
        }
    }

    public static final class LoopMode {
        public static final int NO_REPEAT = 0; // each song will be played once
        public static final int REPEAT_SONG = 1; // the current song will be repeated while loopmode is REPEAT_SONG 
        public static final int REPEAT_PLAYLIST = 2; // the current playlist will be repeated when finished  
    }

    /* other stuff to be commented */
    public static enum Table { 
        SONG, ARTIST, GENRE, ALBUM, CURRENT_PLAYLIST, DIR; 
    }
    public static final String MUSIC_DIR = "/sdcard/";
    public static final String COVER_DIR = MUSIC_DIR+"covers/";

    public static final String[] SONGS = { SONG.ID, SONG.URL, SONG.TITLE,
        SONG.LAST_PLAYED, SONG.COUNT_PLAYED, SONG.TRACK, SONG.ARTIST,
        SONG.ALBUM, SONG.GENRE };
    public static final String[] ARTISTS = { ARTIST.ID, ARTIST.NAME };
    public static final String[] ALBUMS = { ALBUM.ID, ALBUM.NAME, ALBUM.COVER};
    public static final String[] GENRES = { GENRE.ID, GENRE.NAME };
    public static final String[] CURRENT_PLAYLISTS = { CURRENT_PLAYLIST.POS,
        CURRENT_PLAYLIST.ID };
    public static final String[] DIR = {DIRECTORY.ID, DIRECTORY.DIR};

    public static final class SONG {
        public static final String ID = "_id";
        public static final String URL = "url";
        public static final String TITLE = "title";
        public static final String LAST_PLAYED = "last_played";
        public static final String COUNT_PLAYED = "count_played";
        public static final String TRACK = "track";
        public static final String ARTIST = "artist";
        public static final String ALBUM = "album";
        public static final String GENRE = "genre";
    }

    public static final class ARTIST {
        public static final String ID = "_id";
        public static final String NAME = "artist_name";
    }

    public static final class ALBUM {
        public static final String ID = "_id";
        public static final String NAME = "album_name";
        public static final String COVER = "cover_url";
    }

    public static final class GENRE {
        public static final String ID = "_id";
        public static final String NAME = "genre_name";
    }

    public static final class CURRENT_PLAYLIST {
        public static final String POS = "pos";
        public static final String ID = "id";
    }

    public static final class DIRECTORY 
    {
        public static final String ID = "_id";
        public static final String DIR = "dir";
    }

    // describe playlist possiblities
    public static enum SmartPlaylist
    {
        RANDOM, LESS_PLAYED, MOST_PLAYED;

        public static final int SIZE = 25;
        // SQL query associated with a playlist
        public String getQuery(){
            switch(this){
                case RANDOM : 
                    return "INSERT INTO current_playlist(id) SELECT _id FROM song ORDER BY random() LIMIT "+SIZE;
                case LESS_PLAYED : 
                    return "INSERT INTO current_playlist(id) SELECT _id FROM song ORDER BY count_played ASC LIMIT "+SIZE;
                case MOST_PLAYED :
                    return "INSERT INTO current_playlist(id) SELECT _id FROM song ORDER BY count_played DESC LIMIT "+SIZE;
                default : 
                    return "";
            }
        }

        // get string id for the enum values
        public int getStringId(){
            switch(this){
                case RANDOM : 
                    return R.string.playlist_random;
                case LESS_PLAYED : 
                    return R.string.playlist_less_played;
                case MOST_PLAYED :
                    return R.string.playlist_most_played;
                default :
                    return android.R.string.unknownName;
            }
        }
    }

}
