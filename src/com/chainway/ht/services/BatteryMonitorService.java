package com.chainway.ht.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.chainway.ht.AppConfig;
import com.chainway.ht.AppContext;
import com.chainway.ht.R;
import com.chainway.ht.ui.BatteryMonitorActivity;
import com.chainway.ht.ui.PingActivity;
import com.chainway.ht.utils.FileUtils;
import com.chainway.ht.utils.StringUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class BatteryMonitorService extends Service {

	private static final String TAG = "BatteryMonitorService";

	private AppContext appContext;
	private String fileName;

	private NotificationManager manager;
	private Notification notification;
	private PendingIntent pendingIntent;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private int currentBatteryLevel = 0;

	private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			currentBatteryLevel = intent.getIntExtra("level", 0);

			saveRecords(currentBatteryLevel + "");
		}
	};

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		Log.i(TAG, "onCreate");

		appContext = (AppContext) getApplication();
		fileName = StringUtils.getTimeString() + ".txt";
	}

	@Override
	public void onStart(Intent intent, int startId) {

		Log.i(TAG, "onStart");

		registerReceiver(batteryReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED)); // 注册一个动作改变事件捕获，这里为电量改变时即ACTION_BATTERY_CHANGED

		addNotificaction();

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
		notification.tickerText = getString(R.string.battery_msg_background_monitoring);

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

		Intent intent = new Intent(BatteryMonitorService.this,
				BatteryMonitorActivity.class);
		intent.putExtra("pendingIntent", true);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		pendingIntent = PendingIntent.getActivity(BatteryMonitorService.this,
				0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// 点击状态栏的图标出现的提示信息设置
		notification.setLatestEventInfo(this,
				getString(R.string.battery_msg_background_monitoring),
				getString(R.string.battery_msg_runing), pendingIntent);

		manager.notify(2, notification);

	}

	private void removeNotificaction() {
		if (manager != null) {
			manager.cancel(2);
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (batteryReceiver != null) {
			unregisterReceiver(batteryReceiver);
		}
	}

	/**
	 * 保存数据
	 * 
	 * @param fileName
	 * @param data
	 */
	public void saveRecords(String data) {
		if (StringUtils.isEmpty(fileName) || StringUtils.isEmpty(data)) {
			return;
		}

		if (!FileUtils.checkSaveLocationExists()) {
			return;
		}

		String path = AppConfig.DEFAULT_SAVE_PATH + "/BatteryMonitor/";

		if (!FileUtils.checkFilePathExists(path)) {
			FileUtils.createPath(path);
		}

		File logFile = new File(path + fileName);

		FileWriter fw = null;

		try {

			boolean isNew = false;

			if (!logFile.exists()) {

				logFile.createNewFile();
				isNew = true;
			}

			fw = new FileWriter(logFile, true);
			if (isNew) {
				fw.append("\n******" + appContext.getLocalMacAddress()
						+ "******\n");
			}

			fw.append(data + "    --    "
					+ StringUtils.getTimeFormat(System.currentTimeMillis())
					+ "\n");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
				}
			}
		}

	}

}
