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
package com.pixmob.droidlink.services;

import static com.pixmob.droidlink.Constants.DEVELOPER_MODE;
import static com.pixmob.droidlink.Constants.GOOGLE_ACCOUNT;
import static com.pixmob.droidlink.Constants.SHARED_PREFERENCES_FILE;
import static com.pixmob.droidlink.Constants.SP_KEY_ACCOUNT;
import static com.pixmob.droidlink.Constants.SP_KEY_DEVICE_ID;
import static com.pixmob.droidlink.Constants.TAG;
import static com.pixmob.droidlink.providers.EventsContract.Event.CREATED;
import static com.pixmob.droidlink.providers.EventsContract.Event.DEVICE_ID;
import static com.pixmob.droidlink.providers.EventsContract.Event.MESSAGE;
import static com.pixmob.droidlink.providers.EventsContract.Event.NAME;
import static com.pixmob.droidlink.providers.EventsContract.Event.NUMBER;
import static com.pixmob.droidlink.providers.EventsContract.Event.TYPE;
import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;

import com.pixmob.actionservice.ActionExecutionFailedException;
import com.pixmob.actionservice.ActionService;
import com.pixmob.droidlink.providers.EventsContract;
import com.pixmob.droidlink.util.PhoneUtils;

/**
 * Find and store the last SMS to the database.
 * @author Pixmob
 */
public class SmsHandlerService extends ActionService {
    private SharedPreferences prefs;
    
    public SmsHandlerService() {
        super("DroidLink/SmsHandler");
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences(SHARED_PREFERENCES_FILE, MODE_PRIVATE);
    }
    
    @Override
    protected void onHandleAction(Intent intent) throws ActionExecutionFailedException,
            InterruptedException {
        final Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        if (pdus == null) {
            Log.w(TAG, "Got no SMS messages, since the intent is missing the extra pdus");
            return;
        }
        
        final String[] contactProjection = { ContactsContract.PhoneLookup.DISPLAY_NAME };
        
        for (final Object pdu : pdus) {
            // Parse the message from the raw value (PDU).
            final SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
            final String fromAddress = PhoneUtils.getPhoneNumber(message.getOriginatingAddress());
            String fromDisplayName = null;
            
            // Read the contact database to get a name for the message author.
            final Uri contactUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(fromAddress));
            // Query the filter URI.
            final Cursor cursor = getContentResolver().query(contactUri, contactProjection, null,
                null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        // We found a contact name for this message author.
                        fromDisplayName = cursor.getString(0);
                    }
                } finally {
                    cursor.close();
                }
            }
            
            Log.i(TAG, "Got SMS: number=" + fromAddress + ", name=" + fromDisplayName + ", time="
                    + message.getTimestampMillis());
            writeSmsEvent(fromAddress, fromDisplayName, message.getMessageBody(),
                message.getTimestampMillis());
        }
        
        if (pdus.length != 0) {
            // Start synchronization.
            final String accountName = prefs.getString(SP_KEY_ACCOUNT, null);
            if (accountName != null) {
                ContentResolver.requestSync(new Account(accountName, GOOGLE_ACCOUNT),
                    EventsContract.AUTHORITY, new Bundle());
            }
        }
    }
    
    private void writeSmsEvent(String number, String name, String message, long date) {
        final String deviceId = prefs.getString(SP_KEY_DEVICE_ID, null);
        if (deviceId == null) {
            Log.wtf(TAG, "No device id set");
            return;
        }
        
        final ContentValues cv = new ContentValues();
        cv.put(DEVICE_ID, deviceId);
        cv.put(CREATED, date);
        cv.put(NUMBER, number);
        cv.put(NAME, name);
        cv.put(MESSAGE, message);
        cv.put(TYPE, EventsContract.RECEIVED_SMS_TYPE);
        
        final Uri uri = getContentResolver().insert(EventsContract.CONTENT_URI, cv);
        if (DEVELOPER_MODE) {
            Log.i(TAG, "New event created for SMS: " + uri);
        }
    }
}
