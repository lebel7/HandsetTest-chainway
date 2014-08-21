package com.chainway.ht.ui;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import com.chainway.deviceapi.Barcode1D;
import com.chainway.deviceapi.exception.ConfigurationException;
import com.chainway.ht.AppConfig;
import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.R.layout;
import com.chainway.ht.R.menu;
import com.chainway.ht.utils.StringUtils;

import android.R.bool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class YiDActivity extends BaseActivity {

	private Button btn_Start;
	private Button btn_Clear;
	// private Reader mReader;
	private Button btn_back;
	private TextView tv_Result;
	private TextView tv_scan_count;
	private TextView tv_succ_count;
	private TextView tv_fail_count;
	private TextView tv_error_count;
	private TextView tv_succ_rate;
	private TextView tv_fail_rate;
	private TextView tv_error_rate;
	private CheckBox cbContinuous;
	private CheckBox cbCompare;
	private EditText et_between;

	private Handler handler;
	private Thread thread;
	private ScrollView svResult;
	private EditText et_init_barcode;
	private String init_barcode;
	int sussCount = 0;
	int failCount = 0;
	int errorCount = 0;

	int readerStatus = 0;

	private boolean threadStop = true;

	private boolean isBarcodeOpened = false;

	private Barcode1D mInstance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_yi_d);
		init();

	}

	private void init() {
		btn_Start = (Button) findViewById(R.id.btn_Start);
		btn_Clear = (Button) findViewById(R.id.btn_Clear);
		tv_Result = (TextView) findViewById(R.id.tv_result);
		cbContinuous = (CheckBox) findViewById(R.id.cbContinuous);
		cbCompare = (CheckBox) findViewById(R.id.cbCompare);
		et_between = (EditText) findViewById(R.id.et_between);
		btn_back = (Button) findViewById(R.id.btnBack);
		svResult = (ScrollView) findViewById(R.id.svResult);
		et_init_barcode = (EditText) findViewById(R.id.et_init_barcode);

		tv_scan_count = (TextView) findViewById(R.id.tv_scan_count);
		tv_succ_count = (TextView) findViewById(R.id.tv_succ_count);
		tv_fail_count = (TextView) findViewById(R.id.tv_fail_count);
		tv_error_count = (TextView) findViewById(R.id.tv_error_count);
		tv_succ_rate = (TextView) findViewById(R.id.tv_succ_rate);
		tv_fail_rate = (TextView) findViewById(R.id.tv_fail_rate);
		tv_error_rate = (TextView) findViewById(R.id.tv_error_rate);

		btn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				onBackPressed();
			}
		});

		btn_Clear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				clear();

			}
		});

		try {
			mInstance = Barcode1D.getInstance();
			isBarcodeOpened = mInstance.open();

		} catch (ConfigurationException e) {
			UIHelper.ToastMessage(YiDActivity.this,
					R.string.rfid_mgs_error_config);
			return;
		}

		btn_Start.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				scan();

			}
		});

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if (msg != null) {

					String strData = "";

					if ((sussCount + errorCount + failCount) % 1000 == 0) {
						tv_Result.setText("");
					}

					switch (msg.arg1) {
					case 0:
						failCount += 1;
						strData = getString(R.string.yid_msg_scan_fail) + "\n";
						appContext.playSound(2);

						break;
					case 1:

						if (cbCompare.isChecked()) {

							init_barcode = et_init_barcode.getText().toString();

							if (StringUtils.isEmpty(init_barcode))// 设置初始条码
							{
								et_init_barcode.setText(msg.obj.toString());

								init_barcode = et_init_barcode.getText()
										.toString();
							}

							if (init_barcode.equals(msg.obj)) {
								sussCount += 1;
							} else {
								errorCount += 1;
                                // tv_Result
                                // .append(getString(R.string.yid_msg_scan_error));
                                // tv_Result.append("：");

                                strData = getString(R.string.yid_msg_scan_error)
                                        + "：";

                                // appContext.saveRecords("bc1d_err.txt",
                                // strData+msg.obj.toString() + "\n");
							}
						} else {
							sussCount += 1;
							init_barcode = "";
							et_init_barcode.setText("");
						}

						strData += msg.obj.toString() + "\n";
						// tv_Result.append(msg.obj.toString() + "\n");

						appContext.playSound(1);

						break;

					default:
						failCount += 1;
						appContext.playSound(2);
						break;
					}

					tv_Result.append(strData);
					appContext.d1Queue.add(strData);

					scrollToBottom(svResult, tv_Result);
					stat();
				}
			}

		};

	}

	private void scan() {
		if (threadStop) {

			Log.i("MY", "readerStatus " + readerStatus);

			boolean bContinuous = false;

			int iBetween = 0;

			bContinuous = cbContinuous.isChecked();
			if (bContinuous) {
				btn_Start.setText(getString(R.string.title_stop));
				threadStop = false;

				String strBetween = et_between.getText().toString();
				if (StringUtils.isEmpty(strBetween)) {

				} else {
					iBetween = StringUtils.toInt(strBetween);// 毫秒
				}

				btn_Clear.setEnabled(false);
			}
			init_barcode = et_init_barcode.getText().toString();

			thread = new Thread(new GetBarcode(bContinuous, iBetween));
			thread.start();

		} else {
			btn_Start.setText(getString(R.string.title_scan));
			threadStop = true;

			btn_Clear.setEnabled(true);
		}
	}

	private void stat() {
		int total = sussCount + failCount + errorCount;

		if (total > 0) {
			tv_scan_count.setText(String.valueOf(total));
			tv_succ_count.setText(String.valueOf(sussCount));
			tv_fail_count.setText(String.valueOf(failCount));
			tv_error_count.setText(String.valueOf(errorCount));

			tv_error_rate.setText(String.valueOf(errorCount * 1000 / total)
					+ "‰");
			tv_succ_rate
					.setText(String.valueOf(sussCount * 1000 / total) + "‰");
			tv_fail_rate
					.setText(String.valueOf(failCount * 1000 / total) + "‰");
		}
	}

	private void clear() {
		tv_Result.setText("");

		int total = 0;
		sussCount = 0;
		failCount = 0;
		errorCount = 0;

		et_init_barcode.setText("");
		tv_scan_count.setText(String.valueOf(total));
		tv_succ_count.setText(String.valueOf(sussCount));
		tv_fail_count.setText(String.valueOf(failCount));
		tv_error_count.setText(String.valueOf(errorCount));

		tv_error_rate.setText(String.valueOf(0));
		tv_succ_rate.setText(String.valueOf(0));
		tv_fail_rate.setText(String.valueOf(0));

		btn_Start.setText(getString(R.string.title_scan));
		threadStop = true;

	}

	class GetBarcode implements Runnable {

		private boolean isContinuous = false;
		String barCode = "";
		private long sleepTime = 1000;
		Message msg = null;

		public GetBarcode(boolean isContinuous) {
			this.isContinuous = isContinuous;
		}

		public GetBarcode(boolean isContinuous, int sleep) {
			this.isContinuous = isContinuous;
			this.sleepTime = sleep;
		}

		@Override
		public void run() {

			do {
				barCode = mInstance.scan();

				Log.i("MY", "barCode " + barCode.trim());

				msg = new Message();

				if (StringUtils.isEmpty(barCode)) {
					msg.arg1 = 0;
					msg.obj = "";

				} else {

					msg.arg1 = 1;

					msg.obj = barCode;
				}

				handler.sendMessage(msg);

				if (isContinuous) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			} while (isContinuous && !threadStop);

		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == 140) {
			if (event.getRepeatCount() == 0) {

				scan();
				Log.i("MY", "keyCode " + keyCode);
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		btn_Start.setText(getString(R.string.title_scan));
		threadStop = true;

		btn_Clear.setEnabled(true);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isBarcodeOpened) {
			mInstance.close();
		}
	}

}
