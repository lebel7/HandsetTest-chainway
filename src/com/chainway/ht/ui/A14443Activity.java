package com.chainway.ht.ui;

import java.util.ArrayList;

import com.chainway.deviceapi.RFIDWithISO14443A;
import com.chainway.deviceapi.exception.ConfigurationException;
import com.chainway.ht.AppConfig;
import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.R.layout;
import com.chainway.ht.R.menu;
import com.chainway.ht.ui.fragment.M1Fragment;
import com.chainway.ht.ui.fragment.Scan14443AFragment;
import com.chainway.ht.ui.fragment.UHFReadFragment;
import com.chainway.ht.ui.fragment.UHFReadTagFragment;
import com.chainway.ht.ui.fragment.UHFSetFragment;
import com.chainway.ht.ui.fragment.UHFWriteFragment;
import com.chainway.ht.ui.fragment.UltralightFragment;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public class A14443Activity extends BaseFragmentActivity {

	public RFIDWithISO14443A mRFID;

	private FragmentTabHost mTabHost;
	private FragmentManager fm;

	boolean bResult = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_a1443);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		fm = getSupportFragmentManager();
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, fm, R.id.realtabcontent);

		mTabHost.addTab(mTabHost.newTabSpec("scan").setIndicator("Scan"),
				Scan14443AFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("M1").setIndicator("M1"),
				M1Fragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("ul").setIndicator("Ultra light"),
				UltralightFragment.class, null);

		try {
			mRFID = RFIDWithISO14443A.getInstance();
			bResult = mRFID.init();

		} catch (ConfigurationException e) {
			UIHelper.ToastMessage(A14443Activity.this,
					R.string.rfid_mgs_error_config);
			return;
		}

		if (!bResult) {
			UIHelper.ToastMessage(A14443Activity.this,
					R.string.rfid_mgs_error_init);
		}

	}

	public void playSound(int id) {
		appContext.playSound(id);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mRFID.free();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == 140) {
			if (event.getRepeatCount() == 0) {

				if (mTabHost != null) {
					switch (mTabHost.getCurrentTab()) {
					case 0: {
						Scan14443AFragment sf = (Scan14443AFragment) fm
								.findFragmentById(R.id.realtabcontent);
						sf.myOnKeyDwon();

					}
						break;
					case 1: {
						M1Fragment sf = (M1Fragment) fm
								.findFragmentById(R.id.realtabcontent);
						sf.myOnKeyDwon();
					}
						break;
					case 2: {
						UltralightFragment sf = (UltralightFragment) fm
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

	public ArrayList<String> builNum(int count) {
		if (count < 1) {
			return null;

		}

		ArrayList<String> arrStr = new ArrayList<String>();

		for (int i = 0; i < count; i++) {
			arrStr.add(String.valueOf(i));

		}
		return arrStr;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i("MY", "onCreateOptionsMenu");

		getMenuInflater().inflate(R.menu.a1443, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		if (item.getItemId() == R.id.action_ver) {
			String rfidVer = "";
			if (mRFID != null) {
				rfidVer = mRFID.getVersion();
			}

			UIHelper.alert(A14443Activity.this, R.string.action_rfid_ver,
					rfidVer, R.drawable.webtext);
		}
		return true;
	}

}
