package com.chainway.ht.ui.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.adapter.WiFiAdapter;
import com.chainway.ht.network.WifiAdmin;
import com.chainway.ht.ui.NetworkStatusActivity;
import com.chainway.ht.utils.StringUtils;
import com.chainway.ht.view.ChartView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableRow;
import android.widget.TextView;

public class WiFiFragment extends Fragment {

	private ListView lvWiFi;
	private TextView tvCurrSSID;
	private TextView tvCurrApMore;
	private Button btnOp;
	private ImageView ivChart;
	private TextView tvWifiSize;

	private TextView tvBssid;
	private TextView tvSecurity;
	private TextView tvIP;
	private TableRow tbIP;
	private TableRow tbPwd;
	private ChartView llChart;

	private NetworkStatusActivity mContext;

	private WiFiAdapter adapter = null;
	// 扫描结果列表
	private List<ScanResult> list = new ArrayList<ScanResult>();
	private WifiAdmin mWifiAdmin;

	private boolean threadStop = true;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = (NetworkStatusActivity) getActivity();

		lvWiFi = (ListView) mContext.findViewById(R.id.lvWiFi);
		tvCurrSSID = (TextView) mContext.findViewById(R.id.tvCurrSSID);
		tvCurrApMore = (TextView) mContext.findViewById(R.id.tvCurrApMore);
		btnOp = (Button) mContext.findViewById(R.id.btnOp);
		btnOp.setTag("goon");
		tvWifiSize = (TextView) mContext.findViewById(R.id.tvWifiSize);
		ivChart = (ImageView) mContext.findViewById(R.id.ivChart);
		llChart = (ChartView) mContext.findViewById(R.id.llChart);

		Date currDate = new Date();
		llChart.setxMax(currDate.getTime());
		llChart.setxMin(currDate.getTime() - 42 * 1000);
		llChart.setyMax(-20);
		llChart.setyMin(-100);
		llChart.setChartSettings();

		mWifiAdmin = new WifiAdmin(mContext) {

			@Override
			public Intent myRegisterReceiver(BroadcastReceiver receiver,
					IntentFilter filter) {
				mContext.registerReceiver(receiver, filter);
				Log.v("MY2", "######### myRegisterReceiver #########");
				return null;
			}

			@Override
			public void myUnregisterReceiver(BroadcastReceiver receiver) {
				mContext.unregisterReceiver(receiver);

				Log.v("MY2", "######### myUnregisterReceiver #########");
			}

			@Override
			public void onNotifyWifiConnected() {
				Log.v("MY2", "######### have connected success! #########");

				btnOp.setEnabled(true);
				resumeRefreshWiFiThread();

				rssiChanged(mWifiAdmin.getRssi());

			}

			@Override
			public void onNotifyWifiConnectFailed() {
				Log.v("MY2", "######### have connected failed! #########");
				mContext.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						tvCurrSSID.setText(R.string.network_msg_wifi_conn_fail);
						tvCurrApMore.setText("");

						llChart.setSignal(-110);// 设置图标信号值

					}
				});

			}

			@Override
			public void onNotifyWifiConnecting() {

				Log.v("MY2", "######### onNotifyWifiConnecting #########");

				tvCurrSSID.setText(R.string.network_msg_wifi_conning);
				tvCurrApMore.setText("");

			}

			@Override
			public void onNotifyWifiClosed() {

				Log.v("MY2", "######### onNotifyWifiClosed #########");

				threadStop = true;
				Log.i("MY", "!isWifiEnabled");

				tvCurrSSID.setText(R.string.network_msg_wifi_not_enable);
				tvCurrApMore.setText("");

				btnOp.setEnabled(false);

				llChart.setSignal(-110);// 设置图表信号值

				updateResults();// 再调用一次，清空wifi列表
			}

			@Override
			public void onNotifyWifiRssiChanged(int newRssi) {
				Log.v("MY2", "######### onNotifyWifiRssiChanged #########");

				rssiChanged(newRssi);

			}

			@Override
			public void onNotifyWifiScanResultsChanged() {

				Log.v("MY2",
						"######### onNotifyWifiScanResultsChanged #########");

				updateResults();

			}

		};

		adapter = new WiFiAdapter(mContext, list, R.layout.wifi_list_item);

		lvWiFi.setAdapter(adapter);

		Log.i("MY", "onActivityCreated");

		btnOp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (btnOp.getTag().equals("goon")) {
					// 暂停
					btnOp.setTag("pause");
					btnOp.setText(mContext.getString(R.string.ap_title_goon));

					threadStop = true;
				} else {
					btnOp.setTag("goon");
					btnOp.setText(mContext.getString(R.string.ap_title_pause));

					threadStop = false;
					new Thread(new RefreshWiFi()).start();
				}

			}
		});

		lvWiFi.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				createDiglog(list.get(arg2));
			}
		});

		ivChart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (llChart.getVisibility() == View.GONE) {

					TranslateAnimation mShowAction = new TranslateAnimation(
							Animation.RELATIVE_TO_SELF, 0.0f,
							Animation.RELATIVE_TO_SELF, 0.0f,
							Animation.RELATIVE_TO_SELF, -1.0f,
							Animation.RELATIVE_TO_SELF, 0.0f);
					mShowAction.setDuration(500);
					llChart.startAnimation(mShowAction);

					llChart.setVisibility(View.VISIBLE);
				} else {
					TranslateAnimation mHiddenAction = new TranslateAnimation(
							Animation.RELATIVE_TO_SELF, 0.0f,
							Animation.RELATIVE_TO_SELF, 0.0f,
							Animation.RELATIVE_TO_SELF, 0.0f,
							Animation.RELATIVE_TO_SELF, -1.0f);
					mHiddenAction.setDuration(200);
					llChart.startAnimation(mHiddenAction);
					llChart.setVisibility(View.GONE);
				}
			}
		});
	}

	/**
	 * 创建wifi连接对话框
	 * 
	 * @param result
	 */
	private void createDiglog(final ScanResult result) {
		if (result == null) {
			return;

		}

		final String ssid = result.SSID;

		boolean isCurr = false;// 是否当前连接的wifi
		boolean isSave = false;// wifi连接配置信息是否存在
		final WifiConfiguration wifiCon = mWifiAdmin.IsExsits(ssid);

		if (wifiCon != null) {
			// wifi连接配置信息存在
			isSave = true;

		}
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.dialog_wifi, null);

		tvBssid = (TextView) view.findViewById(R.id.tvBssid);
		tvSecurity = (TextView) view.findViewById(R.id.tvSecurity);
		tvIP = (TextView) view.findViewById(R.id.tvIP);
		tbIP = (TableRow) view.findViewById(R.id.tbIP);
		tbPwd = (TableRow) view.findViewById(R.id.tbPwd);

		tvBssid.setText(result.BSSID);
		tvSecurity.setText(result.capabilities);

		if (ssid.equals(mWifiAdmin.getSSID())) {// 已连接的wifi
			tvIP.setText(mWifiAdmin.getIpAddress());

			tbIP.setVisibility(View.VISIBLE);
			tbPwd.setVisibility(View.GONE);

			isCurr = true;
		} else if (isSave) {
			tbIP.setVisibility(View.GONE);
			tbPwd.setVisibility(View.GONE);

			isCurr = false;
		} else {
			tbIP.setVisibility(View.GONE);
			tbPwd.setVisibility(View.VISIBLE);

			isCurr = false;
		}

		AlertDialog.Builder myDialog = new AlertDialog.Builder(mContext);
		myDialog.setView(view);
		myDialog.setTitle(result.SSID);

		if (isCurr) {

		} else {

			if (isSave) {
				myDialog.setPositiveButton(R.string.network_msg_wifi_conn,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mWifiAdmin.addNetwork(wifiCon);

								Log.v("MY", wifiCon.SSID + "@"
										+ wifiCon.networkId + "@"
										+ wifiCon.wepKeys.length);

							}
						});
			} else {
				myDialog.setPositiveButton(R.string.network_msg_wifi_conn,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								EditText et = (EditText) view
										.findViewById(R.id.etPwd);

								mWifiAdmin.addNetwork(ssid, et.getText()
										.toString(), WifiAdmin.TYPE_WPA);

							}
						});
			}
		}

		myDialog.setNegativeButton(R.string.download_msg_cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				});

		myDialog.show();

	}

	/**
	 * 修改操作按钮状态
	 */
	private void changeBtnOp() {
		if (threadStop) {
			btnOp.setTag("goon");
			btnOp.setText(mContext.getString(R.string.ap_title_goon));
		} else {
			btnOp.setTag("pause");
			btnOp.setText(mContext.getString(R.string.ap_title_pause));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.wifi_fragment, container, false);
	}

	@Override
	public void onPause() {
		super.onPause();

		Log.i("MY", "onPause");

		mWifiAdmin.unRegister();
		threadStop = true;

		llChart.cancelTimer();
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.i("MY", "onResume");

		mWifiAdmin.register();

		resumeRefreshWiFiThread();

		llChart.startTimer();

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/*
	 * 恢复wifi监视线程状态
	 */
	public void resumeRefreshWiFiThread() {

		Log.i("MY", "resumeRefreshWiFiThread threadStop=" + threadStop);

		if (btnOp.getTag().equals("pause")) {
			threadStop = true;
		} else {
			threadStop = false;

			new Thread(new RefreshWiFi()).start();
		}

	}

	public void updateResults() {

		if (mWifiAdmin.isWifiEnabled()) {

			tvWifiSize.setText(mWifiAdmin.getWifiListSize() + "");
			if (mWifiAdmin.getWifiListSize() == -1) {
				return;
			}

			Log.i("MY", "isWifiEnabled");
			if (list != null) {
				list.clear();

				Log.i("MY", "list != null");

				list.addAll(mWifiAdmin.getWifiList());
			} else {

				Log.i("MY", "list == null");

				list = mWifiAdmin.getWifiList();
			}

			Log.i("MY", "updateResults list=" + list.size());

			adapter.notifyDataSetChanged();

		} else {
			// 清空wifi列表
			if (list != null) {
				list.clear();
				tvWifiSize.setText("0");
				adapter.notifyDataSetChanged();
			}
		}
	}

	/**
	 * 信号强度改变动作方法
	 */
	private void rssiChanged(int newRssi) {
		if (mWifiAdmin.isWifiContected(mContext) == WifiAdmin.WIFI_CONNECTED) {

			mWifiAdmin.getConnInfo();
			tvCurrSSID.setText(mWifiAdmin.getSSID() + " ["
					+ mWifiAdmin.getBSSID() + "]");
			tvCurrApMore.setText(String.format("IP: %s  %s %s  %s%s",
					mWifiAdmin.getIpAddress(),
					getString(R.string.network_msg_wifi_speed),
					mWifiAdmin.getLinkSpeed(),
					getString(R.string.network_msg_wifi_level), newRssi
							+ " dBm"));

			Log.v("MY", "newRssi=" + newRssi);

			llChart.setSignal(newRssi);// 设置图标信号值

			Log.d("MY2", " #" + mWifiAdmin.getRssi());

		} else {
			llChart.setSignal(-110);// 设置图标信号值
		}
	}

	/**
	 * WiFi列表定时刷新
	 * 
	 * @author liuruifeng
	 * 
	 */
	class RefreshWiFi implements Runnable {

		private long sleepTime = 5000;

		@Override
		public void run() {

			do {
				mWifiAdmin.startScan();

				Log.i("MY2", "RefreshWiFi");

				if (mWifiAdmin.getWifiListSize() == -1) {
					sleepTime = 2000;
				}

				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			} while (!threadStop);

		}

	}

}
