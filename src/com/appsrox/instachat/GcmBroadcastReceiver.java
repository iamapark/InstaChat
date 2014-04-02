package com.appsrox.instachat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
	
	@Override
    public void onReceive(Context context, Intent intent) {
		Log.i("keke", "recieve");
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        
    	//Intent msgService = new Intent(context, GcmIntentService.class);
    	//startWakefulService(context, msgService);
        
        setResultCode(Activity.RESULT_OK);
        
    }

	
	/*private static final String TAG = "GcmBroadcastReceiver";
	private Context ctx;    
	 
    @Override
    public void onReceive(Context context, Intent intent) {
    	
    	Log.d("kaka", intent.toString());
    	
    	
    	
        ctx = context;
         
        PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        WakeLock mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();
         
        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
             
            String messageType = gcm.getMessageType(intent);
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error", false);
                 
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server", false);
                 
            } else {
                String msg = intent.getStringExtra(DataProvider.COL_MSG);
                String email = intent.getStringExtra(DataProvider.COL_FROM);
                 
                ContentValues values = new ContentValues(2);
                values.put(DataProvider.COL_MSG, msg);
                values.put(DataProvider.COL_FROM, email);
                //context.getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
                 
                if (Common.isNotify()) {
                		ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
                    // Start the service, keeping the device awake while it is launching.
                    startWakefulService(context, (intent.setComponent(comp)));
                }
            }
            setResultCode(Activity.RESULT_OK);
             
        } finally {
            mWakeLock.release();
        }
        
    }
        @SuppressLint("NewApi")
		private void sendNotification(String text, boolean launchApp) {
            NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
             
            Notification.Builder mBuilder = new Notification.Builder(ctx)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(ctx.getString(R.string.app_name))
                .setContentText(text);
         
            if (!TextUtils.isEmpty(Common.getRingtone())) {
                mBuilder.setSound(Uri.parse(Common.getRingtone()));
            }
             
            if (launchApp) {
                Intent intent = new Intent(ctx, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pi);
            }
             
            mNotificationManager.notify(1, mBuilder.getNotification());
        }*/
}
