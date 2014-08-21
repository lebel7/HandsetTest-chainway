package com.chainway.ht.ui.fragment;

import java.lang.reflect.Array;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.ui.NetworkStatusActivity;
import com.chainway.ht.utils.StringUtils;
import com.chainway.ht.view.ChartView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MobileFragment extends Fragment {
	private final String TAG = "MobileFragment";

	private NetworkStatusActivity mContext;

	private TelephonyManager mTelephonyManager;
	private PhoneStateMonitor mPhoneStateMonitor;

	private TextView tvOperatorName;
	private TextView tvStatus;
	private TextView tvNetworkType;
	private TextView tvSignal;
	private ChartView llChart;
	private int lastSignal = 0; // 信号强度dBm

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mContext = (NetworkStatusActivity) getActivity();

		tvOperatorName = (TextView) mContext.findViewById(R.id.tvOperatorName);
		tvStatus = (TextView) mContext.findViewById(R.id.tvStatus);
		tvNetworkType = (TextView) mContext.findViewById(R.id.tvNetworkType);
		tvSignal = (TextView) mContext.findViewById(R.id.tvSignal);
		llChart = (ChartView) mContext.findViewById(R.id.llChart);

		mTelephonyManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);

		// 没有Sim卡信息
		if (StringUtils.isEmpty(mTelephonyManager.getSimCountryIso())) {
			UIHelper.ToastMessage(mContext, R.string.network_msg_sim_not_exist);

			return;
		}

		// 返回值MCC + MNC
		// String operator = mTelephonyManager.getNetworkOperator();

		// if (TextUtils.isEmpty(operator)) {
		// UIHelper.ToastMessage(mContext,
		// R.string.network_msg_operator_is_null);
		// return;
		// }

		mPhoneStateMonitor = new PhoneStateMonitor();

		// // # MCC，Mobile Country Code，移动国家代码（中国的为460）；
		// int mcc = Integer.parseInt(operator.substring(0, 3));
		// // # MNC，Mobile Network Code，移动网络号码（中国移动为0，中国联通为1，中国电信为2）；
		// int mnc = Integer.parseInt(operator.substring(3));

		tvOperatorName.setText(mTelephonyManager.getNetworkOperatorName());
		tvNetworkType
				.setText(getNetworkType(mTelephonyManager.getNetworkType()));

		Date currDate = new Date();
		llChart.setxMax(currDate.getTime());
		llChart.setxMin(currDate.getTime() - 42 * 1000);
		llChart.setyMax(-50);
		llChart.setyMin(-140);
		llChart.setChartSettings();

	}

	@Override
	public void onPause() {
		super.onPause();

		mTelephonyManager.listen(mPhoneStateMonitor,
				PhoneStateListener.LISTEN_NONE);
		llChart.cancelTimer();
	}

	@Override
	public void onResume() {
		super.onResume();

		mTelephonyManager.listen(mPhoneStateMonitor,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
						| PhoneStateListener.LISTEN_SERVICE_STATE);
		llChart.startTimer();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 获取当前网络类型 0："unknown" 1："GPRS" 2："EDGE" 3："UMTS"
	 * 4："CDMA: Either IS95A or IS95B" 5："EVDO revision 0" 6："EVDO revision A"
	 * 7："1xRTT" 8："HSDPA" 9："HSUPA" 10："HSPA" 11："iDen" 12： "EVDO revision B"
	 * 13："LTE" 14："eHRPD" 15："HSPA+"
	 */
	private String getNetworkType(int networkType) {
		// TelephonyManager tm = (TelephonyManager)
		// getSystemService(Context.TELEPHONY_SERVICE);
		// int networkType = tm.getNetworkType();

		switch (networkType) {
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			Log.e("DDD", "access_subtype: " + "unknown");

			return "unknown";

		case TelephonyManager.NETWORK_TYPE_GPRS:
			Log.e("DDD", "access_subtype: " + "GPRS");

			return "GPRS";

		case TelephonyManager.NETWORK_TYPE_EDGE:
			Log.e("DDD", "access_subtype: " + "EDGE");
			return "EDGE";

		case TelephonyManager.NETWORK_TYPE_UMTS:
			Log.e("DDD", "access_subtype: " + "UMTS");
			return "UMTS";
		case TelephonyManager.NETWORK_TYPE_CDMA:
			Log.e("DDD", "access_subtype: " + "CDMA: Either IS95A or IS95B");

			return "CDMA: Either IS95A or IS95B";
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			Log.e("DDD", "access_subtype: " + "EVDO revision 0");
			return "EVDO revision 0";
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			Log.e("DDD", "access_subtype: " + "EVDO revision A");
			return "EVDO revision A";
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			Log.e("DDD", "access_subtype: " + "1xRTT");
			return "1xRTT";
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			Log.e("DDD", "access_subtype: " + "HSDPA");
			return "HSDPA";
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			Log.e("DDD", "access_subtype: " + "HSUPA");
			return "HSUPA";
		case TelephonyManager.NETWORK_TYPE_HSPA:
			Log.e("DDD", "access_subtype: " + "HSPA");
			return "HSPA";
		case TelephonyManager.NETWORK_TYPE_IDEN:
			Log.e("DDD", "access_subtype: " + "iDen");
			return "iDen";
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			Log.e("DDD", "access_subtype: " + "EVDO revision B");
			return "EVDO revision B";
		case TelephonyManager.NETWORK_TYPE_LTE:
			Log.e("DDD", "access_subtype: " + "LTE");
			return "LTE";
		case TelephonyManager.NETWORK_TYPE_EHRPD:
			Log.e("DDD", "access_subtype: " + "eHRPD");
			return "eHRPD";

		case TelephonyManager.NETWORK_TYPE_HSPAP:
			Log.e("DDD", "access_subtype: " + "HSPA+");
			return "HSPA+";

		default:
			Log.e("DDD", "access_subtype: " + "error");
			return "error";
		}

	}

	public class PhoneStateMonitor extends PhoneStateListener {

		int phoneType = 1;

		@Override
		public void onCellLocationChanged(CellLocation location) {
			super.onCellLocationChanged(location);

			if (location instanceof GsmCellLocation) {// gsm网络

				// 中国移动和中国联通获取LAC、CID的方式

				// # LAC，Location Area Code，位置区域码；
				int lac = ((GsmCellLocation) location).getLac();
				// # CID，Cell Identity，基站编号；
				int cellId = ((GsmCellLocation) location).getCid();
				//
				phoneType = 1;

				// GsmCell gsmCell = new GsmCell();
				// gsmCell.lac = ((GsmCellLocation) location).getLac();
				// gsmCell.cid = ((GsmCellLocation) location).getCid();
				// /** 获取mcc，mnc */
				// String mccMnc = mTelephonyManager.getNetworkOperator();
				// if (mccMnc != null && mccMnc.length() >= 5) {
				// gsmCell.mcc = mccMnc.substring(0, 3);
				// gsmCell.mnc = mccMnc.substring(3, 5);
				// }
				// gsmCell.signal = lastSignal;
				// gsmCell.time = System.currentTimeMillis();

			} else {// 其他CDMA等网络
				phoneType = 2;
				// 中国电信获取LAC、CID的方式
				// # LAC，Location Area Code，位置区域码；
				int lac = ((CdmaCellLocation) location).getNetworkId();
				// # CID，Cell Identity，基站编号；
				int cellId = ((CdmaCellLocation) location).getBaseStationId();

				// try {
				// Class cdmaClass = Class
				// .forName("android.telephony.cdma.CdmaCellLocation");

				// CdmaCellLocation cdma = (CdmaCellLocation) location;
				// CdmaCell cdmaCell = new CdmaCell();
				// cdmaCell.stationId = cdma.getBaseStationId() >= 0 ? cdma
				// .getBaseStationId() : cdmaCell.stationId;
				// cdmaCell.networkId = cdma.getNetworkId() >= 0 ? cdma
				// .getNetworkId() : cdmaCell.networkId;
				// cdmaCell.systemId = cdma.getSystemId() >= 0 ? cdma
				// .getSystemId() : cdmaCell.systemId;
				// /** 获取mcc，mnc */
				// String mccMnc = mTelephonyManager.getNetworkOperator();
				// if (mccMnc != null && mccMnc.length() >= 5) {
				// cdmaCell.mcc = mccMnc.substring(0, 3);
				// cdmaCell.mnc = mccMnc.substring(3, 5);
				// }
				// cdmaCell.signal = lastSignal;
				// cdmaCell.time = System.currentTimeMillis();
				// int lat = cdma.getBaseStationLatitude();
				// int lon = cdma.getBaseStationLongitude();
				//
				// } catch (ClassNotFoundException classnotfoundexception) {
				// }
			}

		}

		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			/*
			 * signalStrength.isGsm() 是否GSM信号 2G or 3G
			 * signalStrength.getCdmaDbm(); 联通3G 信号强度
			 * signalStrength.getCdmaEcio(); 联通3G 载干比
			 * signalStrength.getEvdoDbm(); 电信3G 信号强度
			 * signalStrength.getEvdoEcio(); 电信3G 载干比
			 * signalStrength.getEvdoSnr(); 电信3G 信噪比
			 * signalStrength.getGsmSignalStrength(); 2G 信号强度
			 * signalStrength.getGsmBitErrorRate(); 2G 误码率
			 * 
			 * 载干比 ，它是指空中模拟电波中的信号与噪声的比值
			 */
			// tv.setText("IsGsm : " + signalStrength.isGsm() + "\nCDMA Dbm : "
			// + signalStrength.getCdmaDbm() + "Dbm" + "\nCDMA Ecio : "
			// + signalStrength.getCdmaEcio() + "dB*10" + "\nEvdo Dbm : "
			// + signalStrength.getEvdoDbm() + "Dbm" + "\nEvdo Ecio : "
			// + signalStrength.getEvdoEcio() + "dB*10"
			// + "\nGsm SignalStrength : "
			// + signalStrength.getGsmSignalStrength()
			// + "\nGsm BitErrorRate : "
			// + signalStrength.getGsmBitErrorRate());

			// mIcon3G.setImageLevel(Math.abs(signalStrength
			// .getGsmSignalStrength()));

			if (phoneType == 1) {
				// Gsm手机
				int asu = signalStrength.getGsmSignalStrength();
				lastSignal = -113 + 2 * asu; // 信号强度dBm

				Log.i("MY", "asu=" + asu);

			} else {
				// cdma手机
				lastSignal = signalStrength.getCdmaDbm();
			}

			tvSignal.setText(lastSignal + " dBm");

			llChart.setSignal(lastSignal);

		}

		public void onServiceStateChanged(ServiceState serviceState) {
			super.onServiceStateChanged(serviceState);

			/*
			 * ServiceState.STATE_EMERGENCY_ONLY 仅限紧急呼叫
			 * ServiceState.STATE_IN_SERVICE 信号正常
			 * ServiceState.STATE_OUT_OF_SERVICE 不在服务区
			 * ServiceState.STATE_POWER_OFF 断电
			 */
			switch (serviceState.getState()) {
			case ServiceState.STATE_EMERGENCY_ONLY:
				Log.d(TAG, "3G STATUS : STATE_EMERGENCY_ONLY");

				tvStatus.setText(R.string.network_msg_emergency_only);

				break;
			case ServiceState.STATE_IN_SERVICE:
				Log.d(TAG, "3G STATUS : STATE_IN_SERVICE");

				tvStatus.setText(R.string.network_msg_in_service);

				break;
			case ServiceState.STATE_OUT_OF_SERVICE:
				Log.d(TAG, "3G STATUS : STATE_OUT_OF_SERVICE");

				tvStatus.setText(R.string.network_msg_out_of_service);

				break;
			case ServiceState.STATE_POWER_OFF:
				Log.d(TAG, "3G STATUS : STATE_POWER_OFF");

				tvStatus.setText(R.string.network_msg_power_off);

				break;
			default:

				tvStatus.setText("");

				break;
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.mobile_fragment, container, false);
	}

}
