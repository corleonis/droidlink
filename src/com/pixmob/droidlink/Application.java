package com.pixmob.droidlink;

import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;

import com.pixmob.droidlink.services.DeviceIdGeneratorService;

/**
 * Application entry point.
 * @author Pixmob
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        if (Constants.DEVELOPER_MODE) {
            // Enable StrictMode in development mode.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                final StrictMode.ThreadPolicy threadPolicy;
                final StrictMode.VmPolicy vmPolicy;
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    // Honeycomb+
                    threadPolicy = new StrictMode.ThreadPolicy.Builder().detectDiskWrites()
                            .detectNetwork().detectCustomSlowCalls().penaltyLog()
                            .penaltyFlashScreen().build();
                    vmPolicy = new StrictMode.VmPolicy.Builder().detectActivityLeaks()
                            .detectLeakedClosableObjects().detectLeakedSqlLiteObjects()
                            .penaltyLog().build();
                } else {
                    // Gingerbread only
                    threadPolicy = new StrictMode.ThreadPolicy.Builder().detectDiskWrites()
                            .detectNetwork().penaltyLog().build();
                    vmPolicy = new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
                            .penaltyLog().build();
                }
                
                StrictMode.setThreadPolicy(threadPolicy);
                StrictMode.setVmPolicy(vmPolicy);
            }
        }
        
        // Make sure a device id is generated for this device.
        startService(new Intent(this, DeviceIdGeneratorService.class));
    }
}
