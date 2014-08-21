package com.chainway.ht.ui;

import com.chainway.deviceapi.UHFWithRLM;
import com.chainway.deviceapi.UHFWithRLM.UHFCrcFlagEnum;
import com.chainway.ht.AppConfig;
import com.chainway.ht.R;
import com.chainway.ht.R.layout;
import com.chainway.ht.R.menu;
import com.chainway.ht.UIHelper;
import com.chainway.ht.ui.fragment.M1Fragment;
import com.chainway.ht.ui.fragment.Scan14443AFragment;
import com.chainway.ht.ui.fragment.UHFReadFragment;
import com.chainway.ht.ui.fragment.UHFReadTagFragment;
import com.chainway.ht.ui.fragment.UHFSetFragment;
import com.chainway.ht.ui.fragment.UHFWriteFragment;
import com.chainway.ht.ui.fragment.UltralightFragment;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public class UHFMainActivity extends BaseFragmentActivity {

	// public Reader mReader;
	public UHFWithRLM mReader;

	private FragmentTabHost mTabHost;
	private FragmentManager fm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_uhfmain);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		fm = getSupportFragmentManager();
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, fm, R.id.realtabcontent);

		mTabHost.addTab(
				mTabHost.newTabSpec("scan").setIndicator(
						getString(R.string.uhf_msg_tab_scan)),
				UHFReadTagFragment.class, null);
		mTabHost.addTab(
				mTabHost.newTabSpec("rdata").setIndicator(
						getString(R.string.uhf_msg_tab_read)),
				UHFReadFragment.class, null);
		mTabHost.addTab(
				mTabHost.newTabSpec("wdata").setIndicator(
						getString(R.string.uhf_msg_tab_write)),
				UHFWriteFragment.class, null);
		mTabHost.addTab(
				mTabHost.newTabSpec("set").setIndicator(
						getString(R.string.uhf_msg_tab_set)),
				UHFSetFragment.class, null);

		try {
			mReader = UHFWithRLM.getInstance();
		} catch (Exception ex) {
			UIHelper.ToastMessage(UHFMainActivity.this, ex.getMessage());
			return;
		}
		mReader.open(UHFCrcFlagEnum.NONUSE);

	}

	@Override
	public void onStop() {
		Log.i("MY", "UHFMainActivity.onStop");
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i("MY", "UHFMainActivity.onDestroy");
		if (mReader != null) {
			mReader.close();
		}
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == 140) {

			if (event.getRepeatCount() == 0) {

				if (mTabHost != null) {
					switch (mTabHost.getCurrentTab()) {
					case 0: {
						UHFReadTagFragment sf = (UHFReadTagFragment) fm
								.findFragmentById(R.id.realtabcontent);
						sf.myOnKeyDwon();

					}
						break;
					case 1: {
						UHFReadFragment sf = (UHFReadFragment) fm
								.findFragmentById(R.id.realtabcontent);
						sf.myOnKeyDwon();
					}
						break;

					default:
						break;
					}

				}
			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}
