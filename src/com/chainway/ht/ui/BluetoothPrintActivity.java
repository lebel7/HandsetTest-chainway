package com.chainway.ht.ui;

import java.io.UnsupportedEncodingException;

import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.R.layout;
import com.chainway.ht.R.menu;
import com.chainway.ht.filebrowser.FileManagerActivity;
import com.chainway.ht.utils.BluetoothService;
import com.chainway.ht.utils.FileUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothPrintActivity extends BaseActivity {

	private static final String TAG = "BluetoothPrintActivity";

	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	private static final int REQUEST_CONNECT_DEVICE = 11;
	private static final int REQUEST_ENABLE_BT = 12;

	private static final int REQUEST_BARCODE = 1;
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothService mService = null;
	private String mConnectedDeviceName = null;
	private StringBuffer mOutStringBuffer;

	private Button btn_back;
	private Button btn_print;
	private EditText et_content;
	private TextView tv_printInfo;
	private LinearLayout ll_status;

	private Button btnBrowser;
	private GetPathBroadcastReceiver mGetpathReceiver = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_print);

		init();

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter == null) {
			UIHelper.ToastMessage(this, R.string.bluetooth_msg_not_supp);
			onBackPressed();
			return;
		}

		et_content.append("\nMAC:" + mBluetoothAdapter.getAddress());

	}

	private void init() {
		tv_printInfo = (TextView) findViewById(R.id.tv_printInfo);
		et_content = (EditText) findViewById(R.id.et_content);
		btn_print = (Button) findViewById(R.id.btn_print);
		btn_back = (Button) findViewById(R.id.btnBack);
		ll_status = (LinearLayout) findViewById(R.id.ll_status);
		btnBrowser = (Button) findViewById(R.id.btnBrowser);

		btn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				onBackPressed();
			}
		});

		tv_printInfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent serverIntent = new Intent(BluetoothPrintActivity.this,
						DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

				// Intent serverIntent = new
				// Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
				// startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			}
		});

		btnBrowser.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				UIHelper.showFileManager(BluetoothPrintActivity.this);

			}
		});

		mGetpathReceiver = new GetPathBroadcastReceiver();
		IntentFilter filterPosition = new IntentFilter();
		filterPosition.addAction(FileManagerActivity.FILE_PATH_ACTION);
		registerReceiver(mGetpathReceiver, filterPosition);

		setEditTextCursorLocation(et_content);

	}

	private class GetPathBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(FileManagerActivity.FILE_PATH_ACTION)) {
				String strFilePath = intent.getStringExtra("filepath");

				Log.i(TAG, "FilePath=" + strFilePath);
				et_content.setText(FileUtils.readFile(strFilePath));

			}
		}

	}

	@Override
	public void onStart() {
		super.onStart();

		Log.i(TAG, "onStart()");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			// 设置蓝牙可见性，最多300秒
			enableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
					300);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the session
		}
		// else if (mBluetoothAdapter.getBondedDevices().size() < 1) {
		// // 没有配对的蓝牙
		//
		// new AlertDialog.Builder(BluetoothPrintActivity.this)
		// .setTitle(R.string.bluetooth_title_tip)
		// .setMessage(R.string.bluetooth_msg_not_adapter)
		// .setPositiveButton(R.string.bluetooth_btn_yes,
		// new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog,
		// int which) {
		//
		// // 开启蓝牙设置界面、
		// Intent intent = new Intent(
		// Settings.ACTION_BLUETOOTH_SETTINGS);
		// startActivityForResult(intent, 0);
		//
		// }
		// })
		// .setNegativeButton(R.string.bluetooth_btn_no,
		// new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog,
		// int which) {
		// dialog.dismiss();
		// onBackPressed();
		// }
		// }).show();
		// return;
		// }
		else {
			if (mService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();

		if (mService != null) {
			if (mService.getState() == BluetoothService.STATE_NONE) {
				mService.start();
			}
		}

	}

	private void setupChat() {

		btn_print.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (mService.getState() != BluetoothService.STATE_CONNECTED) {
					// Toast.makeText(this, R.string.not_connected,
					// Toast.LENGTH_SHORT)
					// .show();

					Intent serverIntent = new Intent(
							BluetoothPrintActivity.this,
							DeviceListActivity.class);
					startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
					return;
				} else {
					sendMessage(et_content.getText().toString());
				}

			}
		});

		mService = new BluetoothService(this, mHandler);

		mOutStringBuffer = new StringBuffer("");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth services
		if (mService != null)
			mService.stop();

		if (mGetpathReceiver != null) {
			unregisterReceiver(mGetpathReceiver);
		}
	}

	/**
	 * Set font gray scale
	 */
	private void fontGrayscaleSet(int ucFontGrayscale) {
		if (mService.getState() != BluetoothService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (ucFontGrayscale < 1)
			ucFontGrayscale = 4;
		if (ucFontGrayscale > 8)
			ucFontGrayscale = 8;
		byte[] send = new byte[3];// ESC m n
		send[0] = 0x1B;
		send[1] = 0x6D;
		send[2] = (byte) ucFontGrayscale;
		mService.write(send);
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 * 
	 */
	private void sendMessage(String message) {

		fontGrayscaleSet(4);

		if (message.length() > 0) {

			message += "\n\n\n";
			byte[] send;
			try {

				send = message.getBytes("GB2312");

			} catch (UnsupportedEncodingException e) {
				send = message.getBytes();
			}

			mService.write(send);

		}

	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					tv_printInfo.setText(R.string.title_connected_to);
					tv_printInfo.append(mConnectedDeviceName);
					break;
				case BluetoothService.STATE_CONNECTING:
					tv_printInfo.setText(R.string.title_connecting);
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					tv_printInfo.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				// byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				// String writeMessage = new String(writeBuf);
				break;
			case MESSAGE_READ:
				// byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				// String readMessage = new String(readBuf, 0, msg.arg1);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:

				String str = msg.getData().getString(TOAST);
				if ("Unable to connect device".equals(str)) {
					str = getString(R.string.bluetooth_msg_connection_failed);
				} else if ("Device connection was lost".equals(str)) {
					tv_printInfo.setText(R.string.title_not_connected);
					break;
				}
				Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {

		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mService.connect(device);
				appContext.setProperty("bluetooth.addr", address);// 保存蓝牙地址
				//
				// Log.i(TAG, "bluetooth address:"+address);
				//
				// for(String str:data.getExtras().keySet())
				// {
				// Log.i(TAG, "keySet:"+str);
				// }

			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
			}
		}

	}

}
