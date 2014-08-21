package com.chainway.ht.ui;

import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.utils.StringUtils;
import com.chainway.ht.view.CustomDialog;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends BaseActivity {
	private String count = "1";
	private String wait = "1000";
	private String packetSize = "56";

	private Button btn_back;
	private Button btn_count;
	private Button btn_wait;
	private Button btn_packetSize;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		init();
	}

	private void init() {

		String strPingCount = appContext.getProperty("pingCount");
		if (StringUtils.isNotEmpty(strPingCount)) {
			count = strPingCount;
		}

		String strPingWait = appContext.getProperty("pingWait");
		if (StringUtils.isNotEmpty(strPingWait)) {
			wait = strPingWait;
		}

		String strPacketSize = appContext.getProperty("packetSize");
		if (StringUtils.isNotEmpty(strPacketSize)) {
			packetSize = strPacketSize;
		}

		btn_back = (Button) findViewById(R.id.btnBack);
		btn_count = (Button) findViewById(R.id.btn_count);
		btn_wait = (Button) findViewById(R.id.btn_wait);
		btn_packetSize = (Button) findViewById(R.id.btn_packetsize);

		btn_count.setText(count);
		btn_wait.setText(wait);
		btn_packetSize.setText(packetSize);

		btn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				onBackPressed();
			}
		});

		btn_count.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CustomDialog.Builder builder = new CustomDialog.Builder(
						SettingsActivity.this);
				builder.setTitle(R.string.title_ping_count);
				// builder.setIcon(R.drawable.key);
				LayoutInflater inflater = (LayoutInflater) SettingsActivity.this
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater
						.inflate(R.layout.ping_count_change, null);
				builder.setView(layout);

				final EditText et_ping_count = (EditText) layout
						.findViewById(R.id.et_ping_count);

				et_ping_count.setText(count);
				et_ping_count.selectAll();

				appContext.showInputMethod(SettingsActivity.this);

				builder.setPositiveButton(R.string.ok,
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String strCount = et_ping_count.getText()
										.toString();
								if (StringUtils.isNotEmpty(strCount)) {
									appContext.setProperty("pingCount",
											strCount);

									count = strCount;
									btn_count.setText(strCount);

									dialog.dismiss();
								} else {
									UIHelper.ToastMessage(
											SettingsActivity.this,
											R.string.ping_msg_not_null);

								}

							}
						});

				builder.setNegativeButton(R.string.cancel,
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();

			}
		});

		btn_wait.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CustomDialog.Builder builder = new CustomDialog.Builder(
						SettingsActivity.this);
				builder.setTitle(R.string.title_ping_wait);
				// builder.setIcon(R.drawable.key);
				LayoutInflater inflater = (LayoutInflater) SettingsActivity.this
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater
						.inflate(R.layout.ping_count_change, null);
				builder.setView(layout);

				final EditText et_ping_wait = (EditText) layout
						.findViewById(R.id.et_ping_count);

				et_ping_wait.setText(wait);

				et_ping_wait.selectAll();

				et_ping_wait.setHint(R.string.title_ping_wait);

				appContext.showInputMethod(SettingsActivity.this);

				builder.setPositiveButton(R.string.ok,
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String strWait = et_ping_wait.getText()
										.toString();
								if (StringUtils.isNotEmpty(strWait)) {
									appContext.setProperty("pingWait", strWait);
									wait = strWait;
									btn_wait.setText(strWait);
									dialog.dismiss();
								} else {
									UIHelper.ToastMessage(
											SettingsActivity.this,
											R.string.ping_msg_not_null);
								}

							}
						});

				builder.setNegativeButton(R.string.cancel,
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();

			}
		});

		btn_packetSize.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CustomDialog.Builder builder = new CustomDialog.Builder(
						SettingsActivity.this);
				builder.setTitle(R.string.title_ping_packet_size);
				// builder.setIcon(R.drawable.key);
				LayoutInflater inflater = (LayoutInflater) SettingsActivity.this
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater
						.inflate(R.layout.ping_count_change, null);
				builder.setView(layout);

				final EditText et_packet_size = (EditText) layout
						.findViewById(R.id.et_ping_count);

				et_packet_size.setText(packetSize);
				et_packet_size.selectAll();

				et_packet_size.setHint(R.string.title_ping_packet_size);

				appContext.showInputMethod(SettingsActivity.this);

				builder.setPositiveButton(R.string.ok,
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String strPacketSize = et_packet_size.getText()
										.toString();
								if (StringUtils.isNotEmpty(strPacketSize)) {

									appContext.setProperty("packetSize",
											strPacketSize);
									packetSize = strPacketSize;
									btn_packetSize.setText(strPacketSize);
									dialog.dismiss();
								} else {
									UIHelper.ToastMessage(
											SettingsActivity.this,
											R.string.ping_msg_not_null);
								}
							}
						});

				builder.setNegativeButton(R.string.cancel,
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();

			}
		});

	}

}
