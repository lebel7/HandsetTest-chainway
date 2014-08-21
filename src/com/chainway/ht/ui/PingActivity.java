package com.chainway.ht.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.chainway.ht.AppManager;
import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.R.layout;
import com.chainway.ht.R.menu;
import com.chainway.ht.services.PingService;
import com.chainway.ht.utils.NetUtils;
import com.chainway.ht.utils.StringUtils;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class PingActivity extends BaseActivity {

	private static final String TAG = "PingActivity";

	private String count = "1";
	private String host = "127.0.0.1";
	private String wait = "1000";
	private String packetSize = "56";

	private EditText et_ip_or_domain;
	private TextView tv_result;

	private Button btn_Start;

	private ImageButton btn_set;

	private Handler handler;

	private PingThread pingThread;

	private Boolean isPing = false;

	private ScrollView scrollView1;

	private Button btn_back;
	private CheckBox cbBackgroud;
	private Timer timer;
	private TimerTask timerTask;
	// 是否由通知栏启动
	private boolean isPendingIntent = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ping);
		isRegisterBroadcastReceiver = true;

		init();
		tv_title.setText(String.format(getString(R.string.ping_title), ""));

		Log.i(TAG, "onCreate");

		Intent intent = getIntent();

		if (intent != null) {
			if (intent.getBooleanExtra("pendingIntent", false)) {

				et_ip_or_domain.setText(intent.getStringExtra("host"));

				isPendingIntent = true;

				Log.i(TAG, "onCreate pendingIntent queueSize="
						+ appContext.pingQueue.size());

				// Ping服务是否停止
				if (appContext.pingStop) {
					// btn_Start.setText(R.string.title_start);
					if (appContext.pingQueue.size() > 1) {

						tv_result.setText("");
						popQueue();
					}

					NotificationManager manager = (NotificationManager) this
							.getSystemService(Context.NOTIFICATION_SERVICE);

					manager.cancel(1);
				} else {
					// btn_Start.setText(R.string.title_stop);

				}

			}
		}

	}

	private Boolean vail(String str) {
		if (StringUtils.isIP(str)) {
			Log.i("MY", "is ip");
			return true;
		} else if (StringUtils.isDomain(str)) {
			Log.i("MY", "is domain");
			return true;
		}
		Log.i("MY", "is vail");
		return false;
	}

	@Override
	protected void checkNet() {
		super.checkNet();

		tv_title.setText(String.format(getString(R.string.ping_title), strNet));
	}

	private void init() {

		btn_back = (Button) findViewById(R.id.btnBack);
		btn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				onBackPressed();
			}
		});

		cbBackgroud = (CheckBox) findViewById(R.id.cbBackgroud);

		scrollView1 = (ScrollView) findViewById(R.id.scrollView1);

		et_ip_or_domain = (EditText) findViewById(R.id.et_ip_or_domain);
		tv_result = (TextView) findViewById(R.id.tv_result);
		tv_title = (TextView) findViewById(R.id.tvTitle);

		btn_set = (ImageButton) findViewById(R.id.imgBtnRight);
		btn_set.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PingActivity.this,
						SettingsActivity.class);
				startActivity(intent);
			}
		});

		btn_Start = (Button) findViewById(R.id.btn_Start);
		btn_Start.setTag("start");

		btn_Start.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!NetUtils.isNetworkConnected(PingActivity.this)) {
					tv_result.setText(getString(R.string.msg_network_none));
					return;
				}

				Intent intent = new Intent(appContext, PingService.class);

				if (btn_Start.getTag().toString().equals("start")) {

					host = et_ip_or_domain.getText().toString();
					tv_result.setText("");
					if (!vail(host)) {
						UIHelper.ToastMessage(PingActivity.this,
								PingActivity.this
										.getString(R.string.msg_domain_ip_bad));
						et_ip_or_domain.startAnimation(shake);
						return;
					}

					appContext.setProperty("host", host);

					cbBackgroud.setEnabled(false);

					if (cbBackgroud.isChecked()) {
						appContext.pingQueue.clear();
						startService(intent);// 开始服务

						btn_Start.setText(R.string.title_stop);

						// popQueue();

						btn_Start.setTag("stop");

						onBackPressed();

					} else {
						pingThread = new PingThread();
						pingThread.start();
					}

				} else {

					cbBackgroud.setEnabled(true);

					if (cbBackgroud.isChecked()) {
						appContext.pingStop = true;
						stopService(intent);// 停止服务

						btn_Start.setTag("start");
						btn_Start.setText(R.string.title_start);

						stopPopQueue();

						appContext.pingQueue.clear();
					} else {
						if (pingThread != null) {
							pingThread.setStop();
						}
					}

				}

			}
		});

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				if (msg != null) {
					switch (msg.arg1) {
					case 0:
						isPing = false;
						tv_result.setText(getString(R.string.ping_msg_pinging)
								+ " " + host + " ...\n");

						btn_Start.setTag("stop");
						btn_Start.setText(R.string.title_stop);

						System.out.println("T-Handler START\n");

						break;
					case 1:
						isPing = true;
						tv_result.append(msg.obj.toString());
						System.out.println("T-Handler " + msg.obj.toString()
								+ "\n");
						break;
					case 2:
						btn_Start.setTag("start");
						btn_Start.setText(R.string.title_start);

						cbBackgroud.setEnabled(true);

						if (!isPing)// 判断是否Ping通
						{
							tv_result.setText(R.string.ping_msg_fail);
						}
						System.out.println("T-Handler END\n");
						break;

					default:
						break;
					}

				}

				scrollToBottom(scrollView1, tv_result);
			}

		};

	}

	private void initData() {
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

		if (!isPendingIntent) {

			String strHost = appContext.getProperty("host");

			Log.i("MY", "strHost " + strHost);
			if (StringUtils.isNotEmpty(strHost)) {
				host = strHost;

				et_ip_or_domain.setText(host);
			}
		}

	}

	@Override
	protected void onPause() {
		// unregBroadcastReceiver();
		super.onPause();

		stopPopQueue();

		if (pingThread != null) {
			pingThread.setStop();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// if (keyCode == KeyEvent.KEYCODE_BACK) {
		// Log.i(TAG, "onKeyDown KEYCODE_BACK stackSize="
		// + AppManager.getAppManager().getStackSize());
		//
		// if (isPendingIntent) {
		//
		// if (AppManager.getAppManager().getStackSize() < 2) {
		// // UIHelper.showMain(PingActivity.this);
		// Intent LaunchIntent =
		// getPackageManager().getLaunchIntentForPackage(appContext.getPackageInfo().packageName);
		// startActivity(LaunchIntent);
		// }
		// }
		// }
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		// regBroadcastReceiver();
		initData();
		Log.i(TAG, "onResume");
		super.onResume();
	}

	private void popQueue() {

		// if (timer == null) {
		// timer = new Timer();
		// }
		//
		// if (timerTask == null) {
		// timerTask = new TimerTask() {
		//
		// @Override
		// public void run() {
		while (appContext.pingQueue.size() > 0) {

			final String line = appContext.pingQueue.remove();

			Log.i(TAG, line);
			// runOnUiThread(new Runnable() {
			// public void run() {

			tv_result.append(line);

			// }
			// });

		}

		Handler handler = new Handler();

		handler.post(new Runnable() {

			@Override
			public void run() {
				scrollToBottom(scrollView1, tv_result);
			}
		});

		// }
		// };
		// }
		//
		// timer.schedule(timerTask, 0, 1000);
	}

	private void stopPopQueue() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	private class PingThread extends Thread {

		private boolean isStop = false;
		Message msg = new Message();

		@Override
		public void run() {

			String resault = "";
			try {
				// ping -c 3 -w 100 中 ，-c 是指ping的次数 3是指ping 3次 ，-w 100
				// 以毫秒为单位指定超时间隔，是指超时时间为100毫秒

				System.out.println("PingThread Process 1\n");

				// int iPacketSize=StringUtils.toInt(packetSize,
				// 0)-8;//减去ICMP大小8位
				//
				// iPacketSize=iPacketSize>0?iPacketSize:0;
				// packetSize=String.valueOf(iPacketSize);

				List<String> commands = new ArrayList<String>();
				commands.add("ping");
				commands.add("-c " + count);
				commands.add("-i 1");
				commands.add("-w " + wait);
				commands.add("-s " + packetSize);
				commands.add(host);

				ProcessBuilder pbuilder = new ProcessBuilder(commands);
				pbuilder.redirectErrorStream(true);
				Process process = pbuilder.start();

				// Process p = Runtime.getRuntime().exec(
				// "ping -c " + count + "  -i 1 -w " + wait + " -s "
				// + packetSize + " " + host);
				// // int status = p.waitFor();
				//
				// System.out.println("PingThread Process 2\n");

				InputStream input = process.getInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						input));
				StringBuffer buffer = new StringBuffer();
				String line = "";

				msg.arg1 = 0;// 开始ping
				handler.sendMessage(msg);

				while (!isStop && (line = in.readLine()) != null) {
					buffer.append(line);
					buffer.append("\n");
					msg = new Message();
					msg.arg1 = 1;
					msg.obj = line + "\n";

					System.out.println("T-Return ============" + line + "\n");

					handler.sendMessage(msg);

					// sleep(500);

				}

				msg = new Message();
				msg.arg1 = 2;// 结束ping
				handler.sendMessage(msg);

			} catch (IOException e) {

				msg = new Message();
				msg.arg1 = 2;// 结束ping
				handler.sendMessage(msg);

				Log.e("MY", "IOException " + e.getMessage());
			}
		}

		public void setStop() {
			this.isStop = true;
		}

	}

}
