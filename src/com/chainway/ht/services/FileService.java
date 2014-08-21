package com.chainway.ht.services;

import java.sql.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.chainway.ht.AppContext;
import com.chainway.ht.utils.StringUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class FileService extends Service {

	private static final String TAG = "FileService";

	private AppContext appContext;
	private Timer timer = null;
	private TimerTask myTask = null;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		appContext = (AppContext) getApplication();

		startTimer();
	}

	private void startTimer() {
		if (timer == null) {
			timer = new Timer();
		}

		if (myTask == null) {
			myTask = new TimerTask() {

				@Override
				public void run() {
					String strData1D = "";
					String strData14443A = "";
					String strData15693 = "";
					String strDataUHF = "";

					while (appContext.d1Queue.size() > 0) {
						strData1D += appContext.d1Queue.remove();
					}

					while (appContext.a14443Queue.size() > 0) {
						strData14443A += appContext.a14443Queue.remove();
					}

					while (appContext.r15693Queue.size() > 0) {
						strData15693 += appContext.r15693Queue.remove();
					}

					while (appContext.uhfQueue.size() > 0) {
						strDataUHF += appContext.uhfQueue.remove();
					}

					appContext.saveRecords("bc1d.txt", strData1D);
					appContext.saveRecords("rfid14443.txt", strData14443A);
					appContext.saveRecords("rfid15693.txt", strData15693);
					appContext.saveRecords("uhf.txt", strDataUHF);

					// Log.i(TAG, "FileService pop Queue ");

				}
			};
		}

		if (timer != null && myTask != null) {
			timer.schedule(myTask, 1000 * 5, 1000 * 30);
		}

	}

	private void stopTimer() {

		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		if (myTask != null) {
			myTask.cancel();
			myTask = null;
		}

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "onDestroy()");

		stopTimer();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

}
