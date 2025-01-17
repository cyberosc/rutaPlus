package com.acktos.rutaplus;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Broadcast that allows receiving intents to launch new background service
 * to manage new service state push notification.
 */
public class GCMBroadcastReceiver extends WakefulBroadcastReceiver {
	
	@Override
    public void onReceive(Context context, Intent intent) {
		//Log.i("debug broadcast","entry to onReceive");
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),GCMIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        Log.i(getClass().getSimpleName(), intent.getExtras().toString());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
