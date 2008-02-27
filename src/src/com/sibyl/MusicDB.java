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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

//optimize string concat and static ?
//optimize database -> if needed try triggers

public class MusicDB {

    private SQLiteDatabase mDb;

    private static final String DB_NAME = "music.db";
    private static final int DB_VERSION = 1;

    private static class MusicDBHelper extends SQLiteOpenHelper{
	private static final String GENRE_FILE = "/tmp/tags";

	@Override
	public void onCreate(SQLiteDatabase mDb) {
	    mDb.execSQL("CREATE TABLE song("+
		    "id INTEGER PRIMARY KEY,"+
		    "url VARCHAR UNIQUE,"+
		    "title VARCHAR,"+
		    "last_played DATE DEFAULT 0,"+
		    "count_played NUMBER(5) DEFAULT 0,"+
		    "track NUMBER(2) DEFAULT 0,"+
		    "artist INTEGER,"+
		    "album INTEGER,"+
		    "genre INTEGER"+
		    ")"
	    );
	    mDb.execSQL("CREATE TABLE artist("+
		    "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
		    "artist_name VARCHAR UNIQUE "+
		    ")"
	    );
	    mDb.execSQL("CREATE TABLE album("+
		    "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
		    "album_name VARCHAR UNIQUE "+
		    ")"
	    );
	    mDb.execSQL("CREATE TABLE genre("+
		    "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
		    "genre_name VARCHAR UNIQUE"+
		    ")"
	    );

	    mDb.execSQL("CREATE TABLE current_playlist("+
		    "pos INTEGER PRIMARY KEY,"+
		    "id INTEGER"+
		    ")"
	    );
	    // triggers
	    mDb.execSQL("CREATE TRIGGER t_del_song_artist " +
		    "AFTER DELETE ON song " +
		    "FOR EACH ROW " +
		    "WHEN ( OLD.artist!=1 AND (SELECT COUNT(*) FROM SONG WHERE artist=OLD.artist) == 0) " +
		    "BEGIN " +
		    "DELETE FROM artist WHERE id=OLD.artist; " +
	    "END;");
	    mDb.execSQL("CREATE TRIGGER t_del_song_album " +
		    "AFTER DELETE ON song " +
		    "FOR EACH ROW " +
		    "WHEN ( OLD.album!=1 AND (SELECT COUNT(*) FROM SONG WHERE album=OLD.album) == 0) " +
		    "BEGIN " +
		    "DELETE FROM album WHERE id=OLD.album; " +
	    "END;");
	    mDb.execSQL("CREATE TRIGGER t_del_song_genre " +
		    "AFTER DELETE ON song " +
		    "FOR EACH ROW " +
		    "WHEN ( OLD.genre!=1 AND (SELECT COUNT(*) FROM SONG WHERE genre=OLD.genre) == 0) " +
		    "BEGIN " +
		    "DELETE FROM genre WHERE id=OLD.genre; " +
	    "END;");

	    // default values for undefined tags
	    mDb.execSQL("INSERT INTO artist(artist_name) VALUES('')");
	    mDb.execSQL("INSERT INTO album(album_name) VALUES('')");
	    mDb.execSQL("INSERT INTO genre(genre_name) VALUES('')");
	    // read id3v1 genre from file
	    try{
		BufferedReader f = new BufferedReader(new FileReader(GENRE_FILE));
		String line;
		while( (line=f.readLine()) != null){
		    mDb.execSQL(line);
		}
	    }catch(FileNotFoundException fnfe){

	    }catch(IOException ioe){

	    }

	    // how to ensure that ?
	}

	@Override
	public void onUpgrade(SQLiteDatabase mDb, int oldVersion, int newVersion) {
	    // drop and re-create tables
	    mDb.execSQL("DROP TABLE IF EXISTS current_playlist");
	    mDb.execSQL("DROP TABLE IF EXISTS genre");
	    mDb.execSQL("DROP TABLE IF EXISTS album");
	    mDb.execSQL("DROP TABLE IF EXISTS artist");
	    mDb.execSQL("DROP TABLE IF EXISTS song");
	    mDb.execSQL("DROP TRIGGER IF EXISTS t_del_song_genre");
	    mDb.execSQL("DROP TRIGGER IF EXISTS t_del_song_artist");
	    mDb.execSQL("DROP TRIGGER IF EXISTS t_del_song_album");
	    onCreate(mDb);
	}
    }

    public MusicDB( Context c ) { 
	// exceptions ??
	mDb = (new MusicDBHelper()).openDatabase(c, DB_NAME, null, DB_VERSION);
    }

    public void insert(String[] url) throws FileNotFoundException, IOException, SQLiteException{
	for(int i=0; i<url.length; i++){
	    insert(url[i]);
	}
    }

    public void insert(String url) throws FileNotFoundException, IOException, SQLiteException{
	// read tags
	ContentValues cv = new ID3TagReader(url).getValues();

	int artist = 0 ,album = 0, genre = 0; // = 0 -> last value, !=0 -> null or select
	//, album = false, genre = false;
	mDb.execSQL("BEGIN TRANSACTION");
	try{
	    if(cv.containsKey(Music.ARTIST.NAME) && cv.getAsString(Music.ARTIST.NAME) != ""){
		Cursor c = mDb.rawQuery("SELECT id FROM artist WHERE artist_name='"+cv.getAsString(Music.ARTIST.NAME)+"'" ,null);
		if(c.next()){
		    artist = c.getInt(0);
		}else{
		    mDb.execSQL("INSERT INTO artist(artist_name) VALUES('"+cv.getAsString(Music.ARTIST.NAME)+"')");
		}
		c.close();
	    }else{
		artist = 1;
	    }

	    if(cv.containsKey(Music.ALBUM.NAME) && cv.getAsString(Music.ALBUM.NAME) != ""){
		Cursor c = mDb.rawQuery("SELECT id FROM album WHERE album_name='"+cv.getAsString(Music.ALBUM.NAME)+"'" ,null);
		if(c.next()){
		    album = c.getInt(0);
		}else{
		    mDb.execSQL("INSERT INTO album(album_name) VALUES('"+cv.getAsString(Music.ALBUM.NAME)+"')");
		}
		c.close();
	    }else{
		album = 1;
	    }

	    if(cv.containsKey(Music.GENRE.NAME) && cv.getAsString(Music.GENRE.NAME) != ""){
		Cursor c = mDb.rawQuery("SELECT id FROM genre WHERE genre_name='"+cv.getAsString(Music.GENRE.NAME)+"'" ,null);
		if(c.next()){
		    genre = c.getInt(0);
		}else{
		    mDb.execSQL("INSERT INTO genre(genre_name) VALUES('"+cv.getAsString(Music.GENRE.NAME)+"')");
		}
		c.close();
	    }else{
		genre = 1;
	    }

	    // insert order in table song
	    // char ' is protected in url, for the tags this is done during reading them
	    mDb.execSQL("INSERT INTO song(url,title,track, artist,album,genre) VALUES('"+
		    url.replace("'", "''")+"','"+
		    (cv.containsKey(Music.SONG.TITLE) ? cv.getAsString(Music.SONG.TITLE) : "")+"','"+
		    (cv.containsKey(Music.SONG.TRACK) ? cv.getAsString(Music.SONG.TRACK) : "")+"',"+
		    (artist != 0 ? artist : "(SELECT max(id) FROM artist)")+","+
		    (album != 0 ? album : "(SELECT max(id) FROM album)")+","+
		    (genre != 0 ? genre : "(SELECT max(id) FROM genre)")+")");
	    // + 1 a cause du commit
	    mDb.execSQL("COMMIT TRANSACTION");

	}catch(SQLiteException e){
	    mDb.execSQL("ROLLBACK");
	    throw e;
	}
    }

    public void deleteSong(String url){
	mDb.execSQL("DELETE FROM song WHERE url='"+url+"'");
    }

    public void deleteSong(int id){
	mDb.execSQL("DELETE FROM song WHERE id='"+id+"'");
    }

    public Cursor rawQuery(String sql, String[] selectionArgs){
	return mDb.rawQuery(sql, selectionArgs);
    }

    public Cursor query(String table, String[] columns, 
	    String selection, String[] selectionArgs, 
	    String groupBy, String having, String orderBy){
	return mDb.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    public Cursor query(boolean distinct, String table, String[] columns, 
	    String selection, String[] selectionArgs, 
	    String groupBy, String having, String orderBy){
	return mDb.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    public String nextSong(int pos){
	Cursor c = mDb.rawQuery("SELECT url FROM song, current_playlist WHERE pos="+pos+"+1 AND song.id=current_playlist.id", null);
	if(!c.first()){
	    return null;
	}
	return c.getString(0);
    }

    public String previousSong(int pos){
	Cursor c = mDb.rawQuery("SELECT url FROM song, current_playlist WHERE pos="+pos+"-1 AND song.id=current_playlist.id", null);
	if(!c.first()){
	    return null;
	}
	return c.getString(0);
    }

    public void execSQL(String query){
	mDb.execSQL(query);
    }

    public void insertPlaylist( int[] ids ){
	for( int id : ids ){
	    mDb.execSQL("INSERT INTO current_playlist(id) VALUES("+id+")");
	}
    }

    public void insertPlaylist(String column, String value){
	if(column == Music.SONG.ARTIST){
	    mDb.execSQL("INSERT INTO current_playlist(id) " +
		    "SELECT song.id FROM song, artist " +
		    "WHERE artist.artist_name = '"+value+"' " +
	    "AND song.artist = artist.id");
	}else if(column == Music.SONG.ALBUM){
	    mDb.execSQL("INSERT INTO current_playlist(id) " +
		    "SELECT song.id FROM song, album " +
		    "WHERE album.album_name = '"+value+"' " +
	    "AND song.album = album.id");
	}else if(column == Music.SONG.GENRE){
	    mDb.execSQL("INSERT INTO current_playlist(id) " +
		    "SELECT song.id FROM song, genre " +
		    "WHERE genre.genre_name = '"+value+"' " +
	    "AND song.genre = genre.id");
	}
    }

}