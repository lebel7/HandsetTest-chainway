package com.chainway.ht.ui;

import com.chainway.ht.R;
import com.chainway.ht.R.layout;
import com.chainway.ht.R.menu;
import com.chainway.ht.ui.fragment.KeyLayoutC4000;
import com.chainway.ht.ui.fragment.KeyLayoutFragment;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

public class KeyTestActivity extends BaseFragmentActivity {
	private static final String TAG = "KeyTestActivity";

	private FragmentManager fm;
	private KeyLayoutFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_key_test);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		fragment = null;

		if (appContext.getDeviceNum().toUpperCase().equals("C4000")
				|| appContext.getDeviceNum().toUpperCase().equals("I760")) {
			fragment = new KeyLayoutC4000();
		}

		if (fragment != null) {
			fm = getSupportFragmentManager();
			fm.beginTransaction().add(R.id.fragment_container, fragment)
					.commit();
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		fragment = (KeyLayoutFragment) fm
				.findFragmentById(R.id.fragment_container);
		fragment.onKeyUp(keyCode);

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i(TAG, "keyCode:" + keyCode);

		fragment = (KeyLayoutFragment) fm
				.findFragmentById(R.id.fragment_container);
		fragment.onKeyDown(keyCode);

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;

		default:
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	public void playSound(int id) {
		appContext.playSound(id);
	}
}
