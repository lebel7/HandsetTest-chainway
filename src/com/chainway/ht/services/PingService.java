package com.chainway.ht.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.chainway.ht.AppConfig;
import com.chainway.ht.AppContext;
import com.chainway.ht.R;
import com.chainway.ht.ui.PingActivity;
import com.chainway.ht.utils.StringUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class PingService extends Service {

	private static final String TAG = "PingService";

	private AppContext appContext;

	private String count = "1";
	private String host = "127.0.0.1";
	private String wait = "1000";
	private String packetSize = "56";
	private PingThread pingThread;

	private NotificationManager manager;
	private Notification notification;
	private PendingIntent pendingIntent;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		appContext = (AppContext) getApplication();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		if (pingThread != null) {
			pingThread.setStop();
		}
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		initData();

		appContext.pingQueue.clear();

		appContext.pingStop = true;
		pingThread = new PingThread();
		pingThread.start();
		appContext.pingStop = false;

		addNotificaction();

		Log.i(TAG, "onStartCommand");

		return super.onStartCommand(intent, flags, startId);
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

		String strHost = appContext.getProperty("host");

		Log.i(TAG, "strHost " + strHost);
		if (StringUtils.isNotEmpty(strHost)) {
			host = strHost;

		}
	}

	/**
	 * 添加一个notification
	 */
	private void addNotificaction() {
		manager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// 创建一个Notification
		notification = new Notification();
		// notification.flags |= Notification.FLAG_NO_CLEAR
		// | Notification.FLAG_AUTO_CANCEL;

		notification.flags = Notification.FLAG_ONGOING_EVENT;

		// 设置显示在手机最上边的状态栏的图标
		notification.icon = R.drawable.ic_launcher;
		// 当当前的notification被放到状态栏上的时候，提示内容
		notification.tickerText = getString(R.string.ping_msg_background_ping);

		/***
		 * notification.contentIntent:一个PendingIntent对象，当用户点击了状态栏上的图标时，
		 * 该Intent会被触发 notification.contentView:我们可以不在状态栏放图标而是放一个view
		 * notification.deleteIntent 当当前notification被移除时执行的intent
		 * notification.vibrate 当手机震动时，震动周期设置
		 */
		// 添加声音提示
		// notification.defaults=Notification.DEFAULT_SOUND;
		// audioStreamType的值必须AudioManager中的值，代表着响铃的模式
		// notification.audioStreamType=
		// android.media.AudioManager.ADJUST_LOWER;

		// 点击状态栏的图标出现的提示信息设置
		notification.setLatestEventInfo(this,
				getString(R.string.ping_msg_background_ping),
				getString(R.string.ping_msg_runing), pendingIntent);

		manager.notify(1, notification);

	}

	private void removeNotificaction() {
		if (manager != null) {
			manager.cancel(1);
		}
	}

	private class PingThread extends Thread {

		@Override
		public void run() {

			Log.i(TAG, "PingThread run");

			try {
				// ping -c 3 -w 100 中 ，-c 是指ping的次数 3是指ping 3次 ，-w 100
				// 以毫秒为单位指定超时间隔，是指超时时间为100毫秒

				// int iPacketSize=StringUtils.toInt(packetSize,
				// 0)-8;//减去ICMP大小8位
				//
				// iPacketSize=iPacketSize>0?iPacketSize:0;
				// packetSize=String.valueOf(iPacketSize);

				// Process p = Runtime.getRuntime().exec(
				// "ping -c " + count + "  -i 1 -w " + wait + " -s "
				// + packetSize + " " + host);

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

				InputStream input = process.getInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						input));
				String line = "";

				appContext.pingQueue.add(getString(R.string.ping_msg_pinging)
						+ host + " ...\n");

				while (!appContext.pingStop && (line = in.readLine()) != null) {

					appContext.pingQueue.add(line + "\n");

					Log.i(TAG, line);

				}

			} catch (IOException e) {

				appContext.pingQueue.add(getString(R.string.ping_msg_fail)
						+ "\n");

				Log.e(TAG, "IOException " + e.getMessage());
			} finally {
				appContext.pingStop = true;

				Log.i(TAG, "PingThread run finally");

				Intent intent = new Intent(PingService.this, PingActivity.class);
				intent.putExtra("pendingIntent", true);
				intent.putExtra("host", host);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				pendingIntent = PendingIntent.getActivity(PingService.this, 0,
						intent, PendingIntent.FLAG_UPDATE_CURRENT);

				notification.setLatestEventInfo(PingService.this,
						getString(R.string.ping_msg_background_ping),
						getString(R.string.ping_msg_finish), pendingIntent);
				manager.notify(1, notification);

				stopSelf();
			}

		}

		public void setStop() {
			appContext.pingStop = true;
		}

	}

}
