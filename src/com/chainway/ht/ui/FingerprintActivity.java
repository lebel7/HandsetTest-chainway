package com.chainway.ht.ui;

import com.chainway.deviceapi.Fingerprint;
import com.chainway.ht.AppConfig;
import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.ui.fragment.AcquisitionFragment;
import com.chainway.ht.ui.fragment.HistoryFragment;
import com.chainway.ht.ui.fragment.IdentificationFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.view.MenuItem;

public class FingerprintActivity extends BaseFragmentActivity {

	public Fingerprint mFingerprint;

	private FragmentTabHost mTabHost;
	private FragmentManager fm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_fingerprint);

		fm = getSupportFragmentManager();
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, fm, R.id.realtabcontent);

		mTabHost.addTab(
				mTabHost.newTabSpec("identification").setIndicator(
						getString(R.string.fingerprint_tab_identification)),
				IdentificationFragment.class, null);
		mTabHost.addTab(
				mTabHost.newTabSpec("acquisition").setIndicator(
						getString(R.string.fingerprint_tab_acquisition)),
				AcquisitionFragment.class, null);
		mTabHost.addTab(
				mTabHost.newTabSpec("history").setIndicator(
						getString(R.string.fingerprint_tab_history)),
				HistoryFragment.class, null);

		try {

			mFingerprint = Fingerprint.getInstance();
			if (!mFingerprint.init()) {

				UIHelper.ToastMessage(FingerprintActivity.this,
						R.string.fingerprint_msg_init_fail);

			}

		} catch (Exception ex) {

			UIHelper.ToastMessage(FingerprintActivity.this, ex.getMessage());
			return;
		}

	}

	public void playSound(int id) {
		appContext.playSound(id);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mFingerprint != null) {
			mFingerprint.free();
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
}
