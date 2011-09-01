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
package com.pixmob.droidlink.receivers;

import static com.pixmob.droidlink.Constants.TAG;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.pixmob.droidlink.services.DeviceInitService;

/**
 * When the network becomes available, the device configuration initialization
 * is started.
 * @author Pixmob
 */
public class NetworkConnectivityReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (isNetworkAvailable(intent)) {
            Log.i(TAG, "Network is available");
            context.startService(new Intent(context, DeviceInitService.class));
        } else {
            Log.i(TAG, "Network is NOT available");
        }
    }
    
    private boolean isNetworkAvailable(Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            if (!intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                final NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(ConnectivityManager.EXTRA_EXTRA_INFO);
                if (networkInfo != null) {
                    return networkInfo.isAvailable() && networkInfo.isConnected();
                }
            }
        }
        return false;
    }
}
