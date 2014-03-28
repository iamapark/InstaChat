package com.appsrox.instachat;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	
	 private SimpleCursorAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		adapter = new SimpleCursorAdapter(this, R.layout.main_list_item, null, new String[]{DataProvider.COL_NAME, DataProvider.COL_COUNT},  new int[]{R.id.text1, R.id.text2}, 0);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				switch(view.getId()) {
				case R.id.text2:
					int count = cursor.getInt(columnIndex);
					if (count>0) {
						((TextView)view).setText(String.format("%d new message%s", count, count == 1 ? "" : "s"));
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
												DataProvider.CONTENT_URI_PROFILE,
												new String[]{DataProvider.COL_ID, DataProvider.COL_NAME, DataProvider.COL_COUNT},
												null,
												null,
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
