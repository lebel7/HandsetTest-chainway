package com.chainway.ht.network;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

public abstract class WifiAdmin {

	private static final String TAG = "WifiAdmin";

	private WifiManager mWifiManager;
	private WifiInfo mWifiInfo;
	// 扫描出的网络连接列表
	private List<ScanResult> mWifiList;
	private List<WifiConfiguration> mWifiConfiguration;

	private WifiLock mWifiLock;

	private String mPasswd = "";
	private String mSSID = "";

	private Context mContext = null;

	public WifiAdmin(Context context) {

		mContext = context;

		// 取得WifiManager对象
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		getConnInfo();

		Log.v(TAG, "getIpAddress = " + mWifiInfo.getIpAddress());
	}

	/**
	 * 获取当前wifi连接信息
	 */
	public void getConnInfo() {
		// 取得WifiInfo对象
		mWifiInfo = mWifiManager.getConnectionInfo();
	}

	// 打开WIFI
	public void openWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	// 关闭WIFI
	public void closeWifi() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	public abstract Intent myRegisterReceiver(BroadcastReceiver receiver,
			IntentFilter filter);

	public abstract void myUnregisterReceiver(BroadcastReceiver receiver);

	/**
	 * wifi连接已连接回调方法
	 */
	public abstract void onNotifyWifiConnected();

	/**
	 * wifi连接失败回调方法
	 */
	public abstract void onNotifyWifiConnectFailed();

	/**
	 * wifi连接中回调方法
	 */
	public abstract void onNotifyWifiConnecting();

	/**
	 * wifi已关闭回调方法
	 */
	public abstract void onNotifyWifiClosed();

	/**
	 * wifi信号值改变回调方法
	 */
	public abstract void onNotifyWifiRssiChanged(int newRssi);

	/**
	 * wifi扫描列表改变回调方法
	 * 
	 * @param - newRssi
	 */
	public abstract void onNotifyWifiScanResultsChanged();

	// 添加一个网络并连接
	public void addNetwork(WifiConfiguration wcg) {

		// register();

		int wcgID = mWifiManager.addNetwork(wcg);
		boolean b = mWifiManager.enableNetwork(wcgID, true);
	}

	public static final int TYPE_NO_PASSWD = 0x11;
	public static final int TYPE_WEP = 0x12;
	public static final int TYPE_WPA = 0x13;

	public void addNetwork(String ssid, String passwd, int type) {
		if (ssid == null || passwd == null || ssid.equals("")) {
			Log.e(TAG, "addNetwork() ## nullpointer error!");
			return;
		}

		if (type != TYPE_NO_PASSWD && type != TYPE_WEP && type != TYPE_WPA) {
			Log.e(TAG, "addNetwork() ## unknown type = " + type);
		}

		// stopTimer();

		addNetwork(createWifiInfo(ssid, passwd, type));
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			updateWifi(intent);

		}
	};

	private void updateWifi(Intent intent) {
		final String action = intent.getAction();

		Log.i("MY2", "updateWifi " + action);

		if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
			final boolean enabled = intent.getIntExtra(
					WifiManager.EXTRA_WIFI_STATE,
					WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED;

			// getConnInfo();
			if (!enabled) {
				// WiFi关闭
				onNotifyWifiClosed();
			}

		} else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			getConnInfo();

			if (isWifiContected(mContext) == WIFI_CONNECTED) {
				stopTimer();
				onNotifyWifiConnected();
			} else if (isWifiContected(mContext) == WIFI_CONNECT_FAILED) {

				wifiConnectFailed();

			} else if (isWifiContected(mContext) == WIFI_CONNECTING) {
				onNotifyWifiConnecting();
			}

		} else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {

			// 信号值改变时触发
			final int newRssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI,
					-200);

			onNotifyWifiRssiChanged(newRssi);

		} else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
			onNotifyWifiScanResultsChanged();
		}
	}

	/**
	 * wifi连接失败处理
	 */
	private void wifiConnectFailed() {
		if (isWifiEnabled()) {

			onNotifyWifiConnecting();

			startTimer();
		} else {
			// WiFi关闭
			onNotifyWifiClosed();
		}
	}

	private final int STATE_REGISTRING = 0x01;
	private final int STATE_REGISTERED = 0x02;
	private final int STATE_UNREGISTERING = 0x03;
	private final int STATE_UNREGISTERED = 0x04;

	private int mHaveRegister = STATE_UNREGISTERED;

	public synchronized void register() {
		Log.v(TAG, "register() ##mHaveRegister = " + mHaveRegister);

		if (mHaveRegister == STATE_REGISTRING
				|| mHaveRegister == STATE_REGISTERED) {
			return;
		}

		mHaveRegister = STATE_REGISTRING;
		IntentFilter intf = new IntentFilter();
		intf.addAction(WifiManager.RSSI_CHANGED_ACTION);
		intf.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intf.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intf.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		myRegisterReceiver(mBroadcastReceiver, intf);
		mHaveRegister = STATE_REGISTERED;

	}

	/**
	 * 取消wifi状态监听
	 */
	public synchronized void unRegister() {
		Log.v(TAG, "unRegister() ##mHaveRegister = " + mHaveRegister);

		if (mHaveRegister == STATE_UNREGISTERED
				|| mHaveRegister == STATE_UNREGISTERING) {
			return;
		}

		mHaveRegister = STATE_UNREGISTERING;
		myUnregisterReceiver(mBroadcastReceiver);
		mHaveRegister = STATE_UNREGISTERED;
	}

	private Timer mTimer = null;
	private MyTimerTask mTimerTask;

	/**
	 * 延迟30S显示失败
	 */
	private void startTimer() {
		stopTimer();

		mTimer = new Timer(true);
		mTimerTask = new MyTimerTask();

		mTimer.schedule(mTimerTask, 2000);
	}

	class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			onNotifyWifiConnectFailed();
		}
	};

	private void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
			mTimerTask = null;
		}
	}

	@Override
	protected void finalize() {
		try {
			super.finalize();
			unRegister();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public WifiConfiguration createWifiInfo(String SSID, String password,
			int type) {

		Log.v(TAG, "SSID = " + SSID + "## Password = " + password
				+ "## Type = " + type);

		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";

		WifiConfiguration tempConfig = this.IsExsits(SSID);
		if (tempConfig != null) {
			mWifiManager.removeNetwork(tempConfig.networkId);
		}

		// 分为三种情况：1没有密码2用wep加密3用wpa加密
		if (type == TYPE_NO_PASSWD) {// WIFICIPHER_NOPASS
			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;

		} else if (type == TYPE_WEP) { // WIFICIPHER_WEP
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + password + "\"";
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		} else if (type == TYPE_WPA) { // WIFICIPHER_WPA
			config.preSharedKey = "\"" + password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}

		return config;
	}

	public static final int WIFI_CONNECTED = 0x01;
	public static final int WIFI_CONNECT_FAILED = 0x02;
	public static final int WIFI_CONNECTING = 0x03;

	/**
	 * 判断wifi是否连接成功,不是network
	 * 
	 * @param context
	 * @return
	 */
	public int isWifiContected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		Log.v(TAG,
				"isConnectedOrConnecting = "
						+ wifiNetworkInfo.isConnectedOrConnecting());
		Log.d(TAG,
				"wifiNetworkInfo.getDetailedState() = "
						+ wifiNetworkInfo.getDetailedState());
		if (wifiNetworkInfo.getDetailedState() == DetailedState.OBTAINING_IPADDR
				|| wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTING) {
			return WIFI_CONNECTING;
		} else if (wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTED) {
			return WIFI_CONNECTED;
		} else {
			Log.d(TAG,
					"getDetailedState() == "
							+ wifiNetworkInfo.getDetailedState());
			return WIFI_CONNECT_FAILED;
		}
	}

	// wifi是否可用
	public boolean isWifiEnabled() {
		return mWifiManager.isWifiEnabled();
	}

	/**
	 * 检查wifi连接配置信息是否存在
	 * 
	 * @param SSID
	 * @return
	 */
	public WifiConfiguration IsExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = mWifiManager
				.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"") /*
																 * &&
																 * existingConfig
																 * .
																 * preSharedKey.
																 * equals("\"" +
																 * password +
																 * "\"")
																 */) {
				return existingConfig;
			}
		}
		return null;
	}

	// 断开指定ID的网络
	public void disconnectWifi(int netId) {
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}

	// 检查当前WIFI状态
	public int checkState() {
		return mWifiManager.getWifiState();
	}

	// 锁定WifiLock
	public void acquireWifiLock() {
		mWifiLock.acquire();
	}

	// 解锁WifiLock
	public void releaseWifiLock() {
		// 判断时候锁定
		if (mWifiLock.isHeld()) {
			mWifiLock.acquire();
		}
	}

	// 创建一个WifiLock
	public void creatWifiLock() {
		mWifiLock = mWifiManager.createWifiLock("Test");
	}

	// 得到配置好的网络
	public List<WifiConfiguration> getConfiguration() {
		return mWifiConfiguration;
	}

	// 指定配置好的网络进行连接
	public void connectConfiguration(int index) {
		// 索引大于配置好的网络索引返回
		if (index > mWifiConfiguration.size()) {
			return;
		}
		// 连接配置好的指定ID的网络
		mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
				true);
	}

	public void startScan() {
		mWifiManager.startScan();
		mWifiList = mWifiManager.getScanResults();
		mWifiConfiguration = mWifiManager.getConfiguredNetworks();

		// onNotifyWifiScanResultsChanged();
	}

	// 得到网络列表
	public List<ScanResult> getWifiList() {
		return mWifiList;
	}

	// 查看扫描结果
	public StringBuilder lookUpScan() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < mWifiList.size(); i++) {
			stringBuilder
					.append("Index_" + new Integer(i + 1).toString() + ":");
			// 将ScanResult信息转换成一个字符串包
			// 其中把包括：BSSID、SSID、capabilities、frequency、level
			stringBuilder.append((mWifiList.get(i)).toString());
			stringBuilder.append("/n");
		}
		return stringBuilder;
	}

	// 得到MAC地址
	public String getMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	// 得到接入点的BSSID
	public String getBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}

	// 得到IP地址
	public int getIPAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	// 得到连接的ID
	public int getNetworkId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	// 得到WifiInfo的所有信息包
	public String getWifiInfo() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}

	public int getRssi() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getRssi();
	}

	public String getSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
	}

	public String getLinkSpeed() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getLinkSpeed()
				+ " Mbps";
	}

	public String getIpAddress() {
		return (mWifiInfo == null) ? "0.0.0.0" : intToIp(mWifiInfo
				.getIpAddress());
	}

	public String intToIp(int i) {

		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + ((i >> 24) & 0xFF);
	}

	/**
	 * 根据frequency获取信道标识符
	 * 
	 * @param frequency
	 * @return
	 */
	public static String convertChannel(int frequency) {
		switch (frequency) {
		case 2412:
			return "1";
		case 2417:
			return "2";
		case 2422:
			return "3";
		case 2427:
			return "4";
		case 2432:
			return "5";
		case 2437:
			return "6";
		case 2442:
			return "7";
		case 2447:
			return "8";
		case 2452:
			return "9";
		case 2457:
			return "10";
		case 2462:
			return "11";
		case 2467:
			return "12";
		case 2472:
			return "13";
		case 2483:
			return "1";

		default:
			return "";
		}
	}

	/**
	 * 转换加密方式
	 * 
	 * @param capabilities
	 * @return
	 */
	public static String getSecurity(String capabilities) {
		if (capabilities.contains("WEP")) {

		} else if (capabilities.contains("PSK")) {

		} else if (capabilities.contains("EAP")) {

		} else {
			return "开放";
		}
		return capabilities;
	}

	/**
	 * 获取扫描到的wifi个数
	 * 
	 * @return
	 */
	public int getWifiListSize() {
		return (mWifiList == null) ? -1 : mWifiList.size();
	}

	/**
	 * 自定义的信号等级计算函数
	 * 
	 * @param rssi
	 * @param numLevels
	 * @return
	 */
	public static int calculateSignalLevel(int rssi, int numLevels) {

		int MIN_RSSI = -100;
		int MAX_RSSI = -55;

		if (rssi <= MIN_RSSI) {
			return 0;
		} else if (rssi >= MAX_RSSI) {
			return numLevels - 1;
		} else {
			float inputRange = (MAX_RSSI - MIN_RSSI);
			float outputRange = (numLevels - 1);
			return (int) ((float) (rssi - MIN_RSSI) * outputRange / inputRange);
		}
	}

}
