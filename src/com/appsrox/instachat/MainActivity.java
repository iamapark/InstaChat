package com.appsrox.instachat;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MainActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {
	
	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private SimpleCursorAdapter adapter;
	private Context context;
	private GoogleCloudMessaging gcm;
	private AtomicInteger msgId = new AtomicInteger();
	private String regid;
	
	private String TAG = "KAKA";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		context = getApplicationContext();

        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            Log.i(TAG, regid);

            if (regid.isEmpty()) {
                registerInBackground();
        		}
            new Thread(registerId).start();
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
		
		
		
		adapter = new SimpleCursorAdapter(this, R.layout.main_list_item, null,
				new String[] { DataProvider.COL_NAME, DataProvider.COL_COUNT },
				new int[] { R.id.text1, R.id.text2 }, 0);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				switch (view.getId()) {
				case R.id.text2:
					int count = cursor.getInt(columnIndex);
					if (count > 0) {
						((TextView) view).setText(String.format(
								"%d new message%s", count, count == 1 ? ""
										: "s"));
					}
					return true;
				}
				return false;
			}
		});
		setListAdapter(adapter);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		getLoaderManager().initLoader(0, null, this);
	}
	
	 private Runnable registerId = new Runnable(){
	    	@Override
	    	public void run(){
	    		ServerUtilities.register(regid, regid);
	    	}
	    };
	
	/**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    
    @SuppressLint("NewApi")
	private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(Constants.SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    	Log.d(TAG, msg);

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    //sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }
    
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(DemoActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
			AddContactDialog dialog = new AddContactDialog();
			dialog.show(getFragmentManager(), "AddContactDialog");
			return true;

		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(Common.PROFILE_ID, String.valueOf(id));
		startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader loader = new CursorLoader(this,
				DataProvider.CONTENT_URI_PROFILE, new String[] {
						DataProvider.COL_ID, DataProvider.COL_NAME,
						DataProvider.COL_COUNT }, null, null,
				DataProvider.COL_ID + " DESC");
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

}
