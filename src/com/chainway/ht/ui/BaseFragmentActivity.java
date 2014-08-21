package com.chainway.ht.ui;

import com.baidu.mobstat.StatService;
import com.chainway.ht.AppContext;
import com.chainway.ht.AppManager;
import com.chainway.utility.StringUtility;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.FragmentActivity;
import android.view.View;

public class BaseFragmentActivity extends FragmentActivity {
	PowerManager powerManager = null;
	WakeLock wakeLock = null;
	protected AppContext appContext;// 全局Context

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 添加Activity到堆栈
		AppManager.getAppManager().addActivity(this);

		appContext = (AppContext) getApplication();

		this.powerManager = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		this.wakeLock = this.powerManager.newWakeLock(
				PowerManager.FULL_WAKE_LOCK, "My Lock");
	}

	@Override
	protected void onPause() {
		StatService.onPause(this);
		this.wakeLock.release();
		super.onPause();

	}

	@Override
	protected void onResume() {
		StatService.onResume(this);
		this.wakeLock.acquire();

		super.onResume();

	}

	/**
	 * 将ScrollView根据内部控件高度定位到底端
	 * 
	 * @param scroll
	 * @param inner
	 */
	public void scrollToBottom(final View scroll, final View inner) {

		Handler mHandler = new Handler();

		mHandler.post(new Runnable() {
			public void run() {
				if (scroll == null || inner == null) {
					return;
				}
				int offset = inner.getMeasuredHeight() - scroll.getHeight();
				if (offset < 0) {
					offset = 0;
				}

				scroll.scrollTo(0, offset);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// 结束Activity&从堆栈中移除
		AppManager.getAppManager().finishActivity(this);
	}

	public AppContext getAppContext() {
		return appContext;
	}

	/**
	 * 验证十六进制输入是否正确
	 * 
	 * @param str
	 * @return
	 */
	public boolean vailHexInput(String str) {

		if (str == null || str.length() == 0) {
			return false;
		}

		// 长度必须是偶数
		if (str.length() % 2 == 0) {
			return StringUtility.isHexNumberRex(str);
		}

		return false;
	}
}
