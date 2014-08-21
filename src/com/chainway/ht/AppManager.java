package com.chainway.ht;

import java.util.Stack;

import com.chainway.ht.services.FileService;
import com.chainway.ht.services.PingService;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * 应用程序Activity管理类：用于Activity管理和应用程序退出
 */
public class AppManager {

	private static final String TAG = "AppManager";

	private static Stack<Activity> activityStack;
	private static AppManager instance;

	private AppManager() {
	}

	/**
	 * 单一实例
	 */
	public static AppManager getAppManager() {
		if (instance == null) {
			instance = new AppManager();
		}
		return instance;
	}

	/**
	 * 添加Activity到堆栈
	 */
	public void addActivity(Activity activity) {
		if (activityStack == null) {
			activityStack = new Stack<Activity>();
		}
		activityStack.add(activity);
	}

	/**
	 * 获取当前Activity（堆栈中最后一个压入的）
	 */
	public Activity currentActivity() {
		Activity activity = activityStack.lastElement();
		return activity;
	}

	/**
	 * 结束当前Activity（堆栈中最后一个压入的）
	 */
	public void finishActivity() {
		Activity activity = activityStack.lastElement();
		finishActivity(activity);
	}

	/**
	 * 结束指定的Activity
	 */
	public void finishActivity(Activity activity) {
		if (activity != null) {
			activityStack.remove(activity);
			activity.finish();
			activity = null;
		}
	}

	/**
	 * 结束指定类名的Activity
	 */
	public void finishActivity(Class<?> cls) {
		for (Activity activity : activityStack) {
			if (activity.getClass().equals(cls)) {
				finishActivity(activity);
			}
		}
	}

	/**
	 * 结束所有Activity
	 */
	public void finishAllActivity() {

		for (int i = 0, size = activityStack.size(); i < size; i++) {
			if (null != activityStack.get(i)) {

				if (i == 0) {
					// 关闭服务
					Intent intent = new Intent(activityStack.get(i),
							FileService.class);
					activityStack.get(i).stopService(intent);

					Log.i(TAG, "close FileService");
				}

				activityStack.get(i).finish();
			}
		}
		activityStack.clear();
	}

	public int getStackSize() {
		return activityStack.size();
	}

	/**
	 * 退出应用程序
	 */
	public void AppExit(Context context) {
		try {

			finishAllActivity();

			Log.d(TAG, "AppExit size=" + activityStack.size());

			ActivityManager activityMgr = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);

			int sdk = Integer.valueOf(Build.VERSION.SDK).intValue();
			if (sdk < 8) {
				activityMgr.restartPackage(context.getPackageName());
			} else {
				activityMgr.killBackgroundProcesses(context.getPackageName());
			}
			System.exit(0);

		} catch (Exception e) {
		}
	}
}