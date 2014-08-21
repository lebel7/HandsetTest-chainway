package com.chainway.ht.ui;

import com.chainway.deviceapi.RFIDWithLF;
import com.chainway.deviceapi.exception.ConfigurationException;
import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.R.layout;
import com.chainway.ht.R.menu;
import com.chainway.ht.ui.fragment.LFEM4305Fragment;
import com.chainway.ht.ui.fragment.LFHitagSFragment;
import com.chainway.ht.ui.fragment.LFScanFragment;
import com.chainway.ht.ui.fragment.M1Fragment;
import com.chainway.ht.ui.fragment.Scan14443AFragment;
import com.chainway.ht.ui.fragment.UltralightFragment;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public class LFActivity extends BaseFragmentActivity {

	private static final String TAG = "LFActivity";

	private FragmentTabHost mTabHost;
	private FragmentManager fm;

	public RFIDWithLF mLF;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lf);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		fm = getSupportFragmentManager();
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, fm, R.id.realtabcontent);

		mTabHost.addTab(mTabHost.newTabSpec("scan").setIndicator("Scan"),
				LFScanFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("HitagS").setIndicator("HitagS"),
				LFHitagSFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("EM4305").setIndicator("EM4305"),
				LFEM4305Fragment.class, null);

		try {
			mLF = RFIDWithLF.getInstance();

		} catch (ConfigurationException e) {

			UIHelper.ToastMessage(LFActivity.this,
					R.string.rfid_mgs_error_config);
			return;
		}

		boolean bResult = mLF.init();

		if (!bResult) {
			UIHelper.ToastMessage(LFActivity.this, R.string.rfid_mgs_error_init);
			return;
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (mLF != null) {
			mLF.free();
		}
	}

	public void playSound(int id) {
		appContext.playSound(id);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		if (item.getItemId() == R.id.action_ver) {
			String rfidVer = mLF.getHardwareVersion();

			UIHelper.alert(LFActivity.this, R.string.action_rfid_ver, rfidVer,
					R.drawable.webtext);
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.l, menu);
		return true;
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == 140) {
			if (event.getRepeatCount() == 0) {

				if (mTabHost != null) {
					switch (mTabHost.getCurrentTab()) {
					case 0: {
						LFScanFragment sf = (LFScanFragment) fm
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
