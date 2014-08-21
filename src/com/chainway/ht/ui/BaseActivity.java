package com.chainway.ht.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.baidu.mobstat.StatService;
import com.chainway.ht.AppContext;
import com.chainway.ht.AppManager;
import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.utils.NetUtils;
import com.chainway.utility.StringUtility;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.Selection;
import android.text.Spannable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 应用程序Activity的基类
 * 
 */
public class BaseActivity extends Activity {

	private static final String TAG = "BaseActivity";

	public Animation shake;

	private NetwrokBroadcastReceiver mNetworkStateReceiver;

	/**
	 * 是否监测网络
	 */

	protected Boolean isRegisterBroadcastReceiver = false;

	protected TextView tv_title;
	protected String strNet = "";

	protected AppContext appContext;// 全局Context

	protected boolean networkExist = false;

	// 是否允许全屏
	private boolean allowFullScreen = true;

	// 是否允许销毁
	private boolean allowDestroy = true;

	private View view;

	PowerManager powerManager = null;
	WakeLock wakeLock = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		shake = AnimationUtils.loadAnimation(this, R.anim.shake);

		allowFullScreen = true;
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
		if (isRegisterBroadcastReceiver) {
			unregBroadcastReceiver();
		}
		this.wakeLock.release();
		super.onPause();

	}

	@Override
	protected void onResume() {
		StatService.onResume(this);
		if (isRegisterBroadcastReceiver) {
			regBroadcastReceiver();
		}

		this.wakeLock.acquire();

		super.onResume();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// 结束Activity&从堆栈中移除
		AppManager.getAppManager().finishActivity(this);
	}

	public boolean isAllowFullScreen() {
		return allowFullScreen;
	}

	/**
	 * 设置是否可以全屏
	 * 
	 * @param allowFullScreen
	 */
	public void setAllowFullScreen(boolean allowFullScreen) {
		this.allowFullScreen = allowFullScreen;
	}

	public void setAllowDestroy(boolean allowDestroy) {
		this.allowDestroy = allowDestroy;
	}

	public void setAllowDestroy(boolean allowDestroy, View view) {
		this.allowDestroy = allowDestroy;
		this.view = view;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && view != null) {
			view.onKeyDown(keyCode, event);
			if (!allowDestroy) {
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public String getFormatDailyTime(String dailyTime) {
		Date date = new Date();
		try {
			date = new SimpleDateFormat("yyyyMMdd").parse(dailyTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new SimpleDateFormat("yyyy年MM月dd日").format(date);

	}

	public void setListViewHeightBasedOnChildren(ListView listView) {
		// 获取ListView对应的Adapter
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		int totalHeight = 0;
		for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
			// listAdapter.getCount()返回数据项的数目
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			// 计算子项View 的宽高
			totalHeight += listItem.getMeasuredHeight();
			// 统计所有子项的总高度
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1))
				+ 20;
		// listView.getDividerHeight()获取子项间分隔符占用的高度
		// params.height最后得到整个ListView完整显示需要的高度
		listView.setLayoutParams(params);
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
				Log.i(TAG, " inner=" + inner.getMeasuredHeight() + " scroll="
						+ scroll.getHeight());

				int offset = inner.getMeasuredHeight() - scroll.getHeight();
				if (offset < 0) {
					offset = 0;
				}

				scroll.scrollTo(0, offset);
			}
		});
	}

	protected void regBroadcastReceiver() {

		Log.d(TAG, "regBroadcastReceiver");

		if (isRegisterBroadcastReceiver) {

			// 网络连接判断
			if (!appContext.isNetworkConnected())
				UIHelper.ToastMessage(this, R.string.network_not_connected);

			Log.d(TAG, "regBroadcastReceiver.isRegisterBroadcastReceiver");

			mNetworkStateReceiver = new NetwrokBroadcastReceiver();

			// 注册网络监听
			IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			registerReceiver(mNetworkStateReceiver, filter);
			isRegisterBroadcastReceiver = true;
		}
	}

	protected void unregBroadcastReceiver() {
		Log.i("MY", isRegisterBroadcastReceiver + "");
		if (mNetworkStateReceiver != null && isRegisterBroadcastReceiver) {
			Log.e("MY", " 取消监听");
			unregisterReceiver(mNetworkStateReceiver); // 取消监听
			isRegisterBroadcastReceiver = false;
		}
	}

	protected class NetwrokBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			checkNet();

			Log.d(TAG, "NetwrokBroadcastReceiver.onReceive");

		}

	}

	protected void checkNet() {

		if (!NetUtils.isNetworkConnected(this)) {

			networkExist = false;

			strNet = "(" + getString(R.string.msg_network_unavailable) + ")";

			UIHelper.ToastMessage(this, R.string.msg_network_none);
		} else {
			networkExist = true;
			strNet = "(" + NetUtils.getNetworkType(this) + ")";
		}

	}

	// 将EditText的光标定位到字符的最后面
	public void setEditTextCursorLocation(EditText editText) {
		CharSequence text = editText.getText();
		if (text instanceof Spannable) {
			Spannable spanText = (Spannable) text;
			Selection.setSelection(spanText, text.length());
		}
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
