package com.chainway.ht.ui;

import com.chainway.ht.AppContext;
import com.chainway.ht.R;
import com.chainway.ht.R.layout;
import com.chainway.ht.R.menu;
import com.chainway.ht.services.BatteryMonitorService;
import com.chainway.ht.services.PingService;

import android.os.Bundle;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class BatteryMonitorActivity extends BaseActivity {

	private Button btnOpertaor;
	private boolean isRuning = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_battery_monitor);

		btnOpertaor = (Button) findViewById(R.id.btnOpertaor);
		isRuning = AppContext.isServiceRunning(appContext,
				BatteryMonitorService.class.getName());

		if (isRuning) {
			btnOpertaor.setText(R.string.title_stop);

		} else {
			btnOpertaor.setText(R.string.title_start);
		}

		btnOpertaor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (isRuning) {
					NotificationManager manager = (NotificationManager) appContext
							.getSystemService(Context.NOTIFICATION_SERVICE);

					manager.cancel(2);

					Intent intent = new Intent(appContext,
							BatteryMonitorService.class);

					stopService(intent);

					isRuning = false;

					btnOpertaor.setText(R.string.title_start);

				} else {
					Intent intent = new Intent(appContext,
							BatteryMonitorService.class);
					startService(intent);
					isRuning = true;
					btnOpertaor.setText(R.string.title_stop);
				}

			}
		});

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
