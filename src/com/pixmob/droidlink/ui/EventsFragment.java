/*
 * Copyright (C) 2011 Pixmob (http://github.com/pixmob)
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
package com.pixmob.droidlink.ui;

import static android.provider.BaseColumns._ID;
import static com.pixmob.droidlink.Constants.DEVELOPER_MODE;
import static com.pixmob.droidlink.Constants.GOOGLE_ACCOUNT;
import static com.pixmob.droidlink.Constants.SHARED_PREFERENCES_FILE;
import static com.pixmob.droidlink.Constants.SP_KEY_ACCOUNT;
import static com.pixmob.droidlink.Constants.TAG;
import static com.pixmob.droidlink.providers.EventsContract.Event.CREATED;
import static com.pixmob.droidlink.providers.EventsContract.Event.MESSAGE;
import static com.pixmob.droidlink.providers.EventsContract.Event.NAME;
import static com.pixmob.droidlink.providers.EventsContract.Event.NUMBER;
import static com.pixmob.droidlink.providers.EventsContract.Event.TYPE;
import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;

import com.pixmob.droidlink.R;
import com.pixmob.droidlink.providers.EventsContract;

/**
 * Fragment for displaying device events.
 * @author Pixmob
 */
class EventsFragment extends ListFragment implements LoaderCallbacks<Cursor> {
    private static final String[] EVENT_COLUMNS = { _ID, CREATED, NUMBER, NAME, TYPE, MESSAGE };
    private EventCursorAdapter cursorAdapter;
    private SharedPreferences prefs;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        
        cursorAdapter = new EventCursorAdapter(getActivity(), null);
        setListAdapter(cursorAdapter);
        
        setHasOptionsMenu(true);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, R.string.refresh, Menu.NONE, R.string.refresh)
                .setIcon(R.drawable.gd_action_bar_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(Menu.NONE, R.string.settings, Menu.NONE, R.string.settings)
                .setIcon(R.drawable.gd_action_bar_settings)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.string.refresh:
                onRefresh();
                return true;
            case R.string.settings:
                onSettings();
                return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void onRefresh() {
        final String accountName = prefs.getString(SP_KEY_ACCOUNT, null);
        if (accountName != null) {
            final Bundle options = new Bundle();
            options.putInt(EventsContract.SYNC_STRATEGY, EventsContract.LIGHT_SYNC);
            ContentResolver.requestSync(new Account(accountName, GOOGLE_ACCOUNT),
                EventsContract.AUTHORITY, options);
        }
    }
    
    private void onSettings() {
        getActivity().startActivity(new Intent(getActivity(), PreferencesActivity.class));
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        final Long itemId = (Long) v.getTag(EventCursorAdapter.TAG_ID);
        final Uri itemUri = ContentUris.withAppendedId(EventsContract.CONTENT_URI, itemId);
        
        if (DEVELOPER_MODE) {
            Log.i(TAG, "Opening event details for " + itemUri);
        }
        
        // TODO start the event details activity
    }
    
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), EventsContract.CONTENT_URI, EVENT_COLUMNS, null,
                null, null);
    }
    
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
        
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }
    
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }
}