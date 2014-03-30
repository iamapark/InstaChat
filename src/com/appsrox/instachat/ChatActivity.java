package com.appsrox.instachat;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChatActivity extends Activity implements MessagesFragment.OnFragmentInteractionListener {
	 
    private EditText msgEdit;
    private Button sendBtn;
    private String profileId, profileName, profileEmail;
    private GcmUtil gcmUtil;
    
    private BroadcastReceiver registrationStatusReceiver = new  BroadcastReceiver() {

        @SuppressLint("NewApi")
		@Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Common.ACTION_REGISTER.equals(intent.getAction())) {
                switch (intent.getIntExtra(Common.EXTRA_STATUS, 100)) {
                case Common.STATUS_SUCCESS:
                    getActionBar().setSubtitle("online");
                    break;
                     
                case Common.STATUS_FAILED:
                    getActionBar().setSubtitle("offline");                  
                    break;                  
                }
            }
        }
    };
 
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        profileId = getIntent().getStringExtra(Common.PROFILE_ID);
        msgEdit = (EditText) findViewById(R.id.msg_edit);
        sendBtn = (Button) findViewById(R.id.send_btn);
         
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send(msgEdit.getText().toString());
                msgEdit.setText(null);
            }
        });
         
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
         
        Cursor c = getContentResolver().query(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), null, null, null, null);
        if (c.moveToFirst()) {
            profileName = c.getString(c.getColumnIndex(DataProvider.COL_NAME));
            profileEmail = c.getString(c.getColumnIndex(DataProvider.COL_EMAIL));
            actionBar.setTitle(profileName);
            Log.d("profileName", profileName);
        }
        actionBar.setSubtitle("connecting ...");
        
        registerReceiver(registrationStatusReceiver, new IntentFilter(Common.ACTION_REGISTER));
        gcmUtil = new GcmUtil(getApplicationContext());
    }   
     
    @Override
    public String getProfileEmail() {
        return profileEmail;
    }   
 
    @Override
    protected void onPause() {
        //reset new messages count
        ContentValues values = new ContentValues(1);
        values.put(DataProvider.COL_COUNT, 0);
        getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), values, null, null);
        super.onPause();
    }
 
    @Override
    protected void onDestroy() {
        unregisterReceiver(registrationStatusReceiver);
        gcmUtil.cleanup();
        super.onDestroy();
    }
    
    private void send(final String txt) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    ServerUtilities.send(txt, profileEmail);
                     
                    ContentValues values = new ContentValues(2);
                    values.put(DataProvider.COL_MSG, txt);
                    values.put(DataProvider.COL_TO, profileEmail);
                    getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
                     
                } catch (IOException ex) {
                    msg = "Message could not be sent";
                }
                return msg;
            }
     
            @Override
            protected void onPostExecute(String msg) {
                if (!TextUtils.isEmpty(msg)) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);        
    }
}
