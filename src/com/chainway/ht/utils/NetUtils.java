package com.chainway.ht.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtils {
	/**
	 * 检测网络是否可用
	 * 
	 * @return
	 */
	public static boolean isNetworkConnected(Activity act) {
		ConnectivityManager cm = (ConnectivityManager) act
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	/**
	 * 获取当前网络类型
	 * 
	 * @return 0：没有网络 1：WIFI网络 2：WAP网络 3：NET网络
	 */
	public static String getNetworkType(Activity act) {
		String netType = "";
		ConnectivityManager connectivityManager = (ConnectivityManager) act
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {

			String extraInfo = networkInfo.getExtraInfo();
			if (extraInfo != null) {
				if (extraInfo.toLowerCase().equals("cmnet")) {
					netType = "CMNET";
				} else {
					netType = "CMWAP";
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = "WIFI";
		}
		return netType;
	}
}
