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

package com.sibyl.ui;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDiskIOException;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.sibyl.ISibylservice;
import com.sibyl.Music;
import com.sibyl.MusicDB;
import com.sibyl.R;
import com.sibyl.Sibylservice;

public class PlayListUI extends ListActivity
{
    private static final int ADD_ID = Menu.FIRST;
    private static final int NEW_ID = Menu.FIRST +1;
    private static final int BACK_ID = Menu.FIRST +2;

    private static final String TAG = "PLAYLIST";

    ISibylservice mService = null;

    //private TextView playList;

    private MusicDB mdb;    //the database

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) 
    {
	super.onCreate(icicle);
	launchService();
	setContentView(R.layout.playlist);
	try
	{
	    mdb = new MusicDB(this);
	    fillData();
	}
	catch(SQLiteDiskIOException ex)
	{
	    Log.v(TAG, ex.toString());
	}   
    }

    public boolean onCreateOptionsMenu(Menu menu) 
    {
	super.onCreateOptionsMenu(menu);
	menu.add(0, ADD_ID, R.string.menu_add);
	menu.add(0, NEW_ID, R.string.menu_new);
	menu.add(0, BACK_ID, R.string.menu_back);
	return true;
    }

    @Override
    protected void onDestroy() 
    {
	super.onDestroy();
	unbindService(mConnection);        
    }

    /*when a menu is selected*/
    public boolean onMenuItemSelected(int featureId, Item item) 
    {
	super.onMenuItemSelected(featureId, item);
	Intent i =null;
	switch(item.getId()) 
	{
	case ADD_ID:
	    i = new Intent(this, AddUI.class);
	    startSubActivity(i, 0);
	    break;
	case NEW_ID:
	    try 
	    {
		mService.stop();
		mdb.clearPlaylist();
		fillData();
	    } catch (DeadObjectException e) {
		Log.v(TAG, e.toString());
		// warn user
	    }
	    i = new Intent(this, AddUI.class);
	    startSubActivity(i, 0);
	    break;
	case BACK_ID:
	    finish();
	    break;
	}
	return true;
    }

    private void fillData()
    {
	//Log.v(TAG,"Curseur creation");
	Cursor c = mdb.getPlayListInfo();
	startManagingCursor(c);
	/* TODO l'ui doit pas connaitre les champs de la base, donc a changer*/
	ListAdapter adapter = new SimpleCursorAdapter(
		this, R.layout.playlist_row, c, new String[] {Music.SONG.ID,Music.ARTIST.NAME},  
		new int[] {R.id.text1, R.id.text2});
	setListAdapter(adapter);
    }

    /* TODO verifier que l'on se connecte bien au meme service que PlayerUI 
     * et qu'il n'y a pas 2 servi lancés, normalement c'est bon */

    public void launchService() 
    {
	bindService(new Intent(PlayListUI.this,
		Sibylservice.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection()
    {
	public void onServiceConnected(ComponentName className, IBinder service)
	{
	    // This is called when the connection with the service has been
	    // established, giving us the service object we can use to
	    // interact with the service.  We are communicating with our
	    // service through an IDL interface, so get a client-side
	    // representation of that from the raw service object.
	    mService = ISibylservice.Stub.asInterface((IBinder)service);
	    try 
	    {
		/* Positionner le selecteur de la liste sur la chanson en cours
		 * une fois que le service est connecté
		 * */
		setSelection(mService.getCurrentSongIndex()-1);
	    } 
	    catch (DeadObjectException e) 
	    {
		Log.v(TAG, e.toString());
		// warn user ? or not it not really important and it shouldn't happen
	    }
	}

	public void onServiceDisconnected(ComponentName className)
	{
	    // This is called when the connection with the service has been
	    // unexpectedly disconnected -- that is, its process crashed.
	    mService = null;      
	}
    };

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) 
    {
	position ++;
	try {
	    mService.playSongPlaylist(position);
	} catch (DeadObjectException e) {
	    Log.v(TAG, e.toString());
	}
    }



}