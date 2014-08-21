package com.chainway.ht.ui;

import java.util.List;

import com.chainway.ht.R;
import com.chainway.ht.network.WifiAdmin;
import com.chainway.ht.ui.fragment.MobileFragment;
import com.chainway.ht.ui.fragment.WiFiFragment;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class NetworkStatusActivity extends BaseFragmentActivity {

	private FragmentTabHost mTabHost;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network_status);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

		mTabHost.addTab(mTabHost.newTabSpec("wifi").setIndicator("WiFi"),
				WiFiFragment.class, null);
		mTabHost.addTab(
				mTabHost.newTabSpec("mobile").setIndicator(
						getString(R.string.network_msg_title_mobile)),
				MobileFragment.class, null);

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
