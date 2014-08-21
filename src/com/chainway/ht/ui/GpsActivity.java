package com.chainway.ht.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.R.layout;
import com.chainway.ht.R.menu;

import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class GpsActivity extends BaseActivity {
	private static final String TAG = "GpsActivity";

	private LocationManager locationManager;

	private TextView tvLongitude;
	private TextView tvLatitude;
	private TextView tvSatelliteCount;
	private TextView tvAltitude;
	private TextView tvGpsStatus;
	private TextView tvTime;
	private XYMultipleSeriesDataset mDataset;// 数据设置器
	private GraphicalView graphicalView;
	private XYMultipleSeriesRenderer renderer;// 描绘器
	private XYSeriesRenderer yRenderer;
	private XYSeriesRenderer usedRenderer;
	private XYSeries series;
	private XYSeries usedSeries;// 已用卫星

	private LinearLayout llChart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gps);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		tvLongitude = (TextView) findViewById(R.id.tvLongitude);
		tvLatitude = (TextView) findViewById(R.id.tvLatitude);
		tvSatelliteCount = (TextView) findViewById(R.id.tvSatelliteCount);
		tvAltitude = (TextView) findViewById(R.id.tvAltitude);
		tvGpsStatus = (TextView) findViewById(R.id.tvGpsStatus);
		tvTime = (TextView) findViewById(R.id.tvTime);
		llChart = (LinearLayout) findViewById(R.id.llChart);

		initChart();

	}

	private void initChart() {
		// 这个类用来放置曲线上的所有点，是一个点的集合，根据这些点画出曲线
		series = new XYSeries("");
		usedSeries = new XYSeries("");

		// 创建一个数据集的实例，这个数据集将被用来创建图表
		mDataset = new XYMultipleSeriesDataset();

		renderer = getBarRenderer();

		mDataset.addSeries(series);
		mDataset.addSeries(usedSeries);

		// 以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
		int color = Color.GREEN;
		PointStyle style = PointStyle.POINT;

		Log.v("MY", "dataset.getSeriesCount()=" + mDataset.getSeriesCount()
				+ " renderer" + renderer.getSeriesRendererCount());

		graphicalView = ChartFactory.getBarChartView(GpsActivity.this,
				mDataset, renderer, Type.DEFAULT);

		llChart.addView(graphicalView);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// 判断GPS是否正常启动
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

			new AlertDialog.Builder(GpsActivity.this)
					.setTitle(R.string.gps_title_tip)
					.setMessage(R.string.gps_msg_gps_not_open)
					.setPositiveButton(R.string.gps_btn_yes,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									// 返回开启GPS导航设置界面、
									Intent intent = new Intent(
											Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									startActivityForResult(intent, 0);

								}
							})
					.setNegativeButton(R.string.gps_btn_no,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									onBackPressed();
								}
							}).show();

			// UIHelper.ToastMessage(GpsActivity.this,
			// R.string.gps_msg_open_gps);
			//
			// // 返回开启GPS导航设置界面
			// Intent intent = new
			// Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			// startActivityForResult(intent, 0);
			return;
		}

		registerListener();
	}

	@Override
	protected void onDestroy() {
		unregisterListener();
		super.onDestroy();
	}

	/**
	 * 注册监听
	 */
	private void registerListener() {
		// 为获取地理位置信息时设置查询条件
		String bestProvider = locationManager.getBestProvider(getCriteria(),
				true);
		// 获取位置信息
		// 如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER
		Location location = locationManager.getLastKnownLocation(bestProvider);
		updateView(location);
		// 监听状态
		locationManager.addGpsStatusListener(listener);
		// 绑定监听，有4个参数
		// 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
		// 参数2，位置信息更新周期，单位毫秒
		// 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
		// 参数4，监听
		// 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新

		// 1秒更新一次，或最小位移变化超过1米更新一次；
		// 注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 1, locationListener);
	}

	/**
	 * 移除监听
	 */
	private void unregisterListener() {
		if (locationManager != null) {
			locationManager.removeGpsStatusListener(listener);
			locationManager.removeUpdates(locationListener);
		}
	}

	// 位置监听
	private LocationListener locationListener = new LocationListener() {

		/**
		 * 位置信息变化时触发
		 */
		public void onLocationChanged(Location location) {
			updateView(location);
			Log.i(TAG, "时间：" + new Date(location.getTime()));
			Log.i(TAG, "经度：" + location.getLongitude());
			Log.i(TAG, "纬度：" + location.getLatitude());
			Log.i(TAG, "海拔：" + location.getAltitude());
		}

		/**
		 * GPS状态变化时触发
		 */
		public void onStatusChanged(String provider, int status, Bundle extras) {
			switch (status) {
			// GPS状态为可见时
			case LocationProvider.AVAILABLE:
				Log.i(TAG, "当前GPS状态为可见状态");
				break;
			// GPS状态为服务区外时
			case LocationProvider.OUT_OF_SERVICE:
				Log.i(TAG, "当前GPS状态为服务区外状态");
				break;
			// GPS状态为暂停服务时
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				Log.i(TAG, "当前GPS状态为暂停服务状态");
				break;
			}
		}

		/**
		 * GPS开启时触发
		 */
		public void onProviderEnabled(String provider) {
			Location location = locationManager.getLastKnownLocation(provider);
			updateView(location);
		}

		/**
		 * GPS禁用时触发
		 */
		public void onProviderDisabled(String provider) {
			updateView(null);
		}

	};

	// 状态监听
	GpsStatus.Listener listener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {

			// 获取当前状态
			GpsStatus gpsStatus = locationManager.getGpsStatus(null);

			switch (event) {
			// 第一次定位
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				Log.i(TAG, "第一次定位");

				Log.i(TAG, "耗时：" + gpsStatus.getTimeToFirstFix());
				tvGpsStatus.setText(R.string.gps_msg_Locate_succ);
				tvTime.setText(gpsStatus.getTimeToFirstFix() / 1000 + " s");

				break;
			// 卫星状态改变
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				Log.i(TAG, "卫星状态改变");

				// 获取卫星颗数的默认最大值
				int maxSatellites = gpsStatus.getMaxSatellites();
				// 创建一个迭代器保存所有卫星
				Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
						.iterator();
				int count = 0;// 可视卫星数

				int usedCount = 0;// 已连接卫星数

				ArrayList<float[]> maps = new ArrayList<float[]>();

				float[] yv = new float[3];

				while (iters.hasNext() && count <= maxSatellites) {
					GpsSatellite s = iters.next();
					count++;

					yv = new float[3];
					yv[0] = s.getPrn();
					yv[1] = s.getSnr();
					if (s.usedInFix()) {
						usedCount++;
						yv[2] = 1;// 1为正在使用
					} else {
						yv[2] = 0;// 0为未使用
					}

					maps.add(yv);

				}

				tvSatelliteCount.setText(usedCount + "/" + count);

				updateChart(maps);// 绘制图表

				System.out.println("搜索到：" + count + "颗卫星");
				break;
			// 定位启动
			case GpsStatus.GPS_EVENT_STARTED:
				Log.i(TAG, "定位启动");

				tvGpsStatus.setText(R.string.gps_msg_Locateing);
				break;
			// 定位结束
			case GpsStatus.GPS_EVENT_STOPPED:
				Log.i(TAG, "定位结束");

				tvGpsStatus.setText(R.string.gps_msg_Locate_stop);

				break;
			}
		};
	};

	/**
	 * 实时更新文本内容
	 * 
	 * @param location
	 */
	private void updateView(Location location) {
		if (location != null) {

			tvLongitude.setText(location.getLongitude() + "");
			tvLatitude.setText(location.getLatitude() + "");
			tvAltitude.setText(location.getAltitude() + "");

			Log.i(TAG, "经度：" + location.getLongitude());
			Log.i(TAG, "纬度：" + location.getLatitude());
			Log.i(TAG, "海拔：" + location.getAltitude());

		} else {
			// 清空

			tvLongitude.setText("");
			tvLatitude.setText("");
			tvAltitude.setText("");
			tvTime.setText("");
			tvSatelliteCount.setText("0");

		}
	}

	/**
	 * 返回查询条件
	 * 
	 * @return
	 */
	private Criteria getCriteria() {
		Criteria criteria = new Criteria();
		// 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		// 设置是否要求速度
		criteria.setSpeedRequired(false);
		// 设置是否允许运营商收费
		criteria.setCostAllowed(false);
		// 设置是否需要方位信息
		criteria.setBearingRequired(false);
		// 设置是否需要海拔信息
		criteria.setAltitudeRequired(true);
		// 设置对电源的需求
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		return criteria;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;

		default:
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	// 柱状图渲染器
	public XYMultipleSeriesRenderer getBarRenderer() {

		renderer = new XYMultipleSeriesRenderer();

		yRenderer = new XYSeriesRenderer();
		yRenderer.setDisplayChartValues(true);
		yRenderer.setChartValuesTextAlign(Align.CENTER);
		yRenderer.setColor(Color.LTGRAY); // 設定Series顏色
		yRenderer.setChartValuesTextSize(16);
		renderer.addSeriesRenderer(yRenderer);

		usedRenderer = new XYSeriesRenderer();
		usedRenderer.setDisplayChartValues(true);
		usedRenderer.setChartValuesTextSize(16);
		usedRenderer.setChartValuesTextAlign(Align.CENTER);
		usedRenderer.setColor(Color.YELLOW); // 設定Series顏色
		renderer.addSeriesRenderer(usedRenderer);

		// renderer.setMarginsColor(Color.WHITE); // 設定圖外圍背景顏色
		renderer.setTextTypeface(null, Typeface.BOLD); // 設定文字style

		renderer.setShowGrid(true); // 設定網格
		renderer.setGridColor(Color.GRAY); // 設定網格顏色

		renderer.setChartTitle(getString(R.string.gps_msg_title_satellite_signal)); // 設定標頭文字
		renderer.setLabelsColor(Color.WHITE); // 設定標頭文字顏色
		renderer.setChartTitleTextSize(20); // 設定標頭文字大小
		renderer.setLabelsTextSize(15);
		renderer.setAxesColor(Color.WHITE); // 設定雙軸顏色
		renderer.setBarSpacing(0.5); // 設定bar間的距離

		renderer.setXLabelsColor(Color.WHITE); // 設定X軸文字顏色
		renderer.setYLabelsColor(0, Color.GREEN); // 設定Y軸文字顏色
		renderer.setXLabelsAlign(Align.CENTER); // 設定X軸文字置中
		renderer.setYLabelsAlign(Align.CENTER); // 設定Y軸文字置中

		renderer.setXLabels(0); // 設定X軸不顯示數字, 改以程式設定文字
		renderer.setYAxisMin(0); // 設定Y軸文最小值
		renderer.setXAxisMax(6.5);
		renderer.setXAxisMin(0.5);
		renderer.setYLabels(0);
		renderer.setYAxisMin(0);
		renderer.setYAxisMax(80);
		renderer.setShowLegend(false);
		renderer.setClickEnabled(false);
		renderer.setPanEnabled(false); // 屏蔽移动
		renderer.setZoomEnabled(false); // 屏蔽缩放
		renderer.setBarWidth(40);

		return renderer;
	}

	private void updateChart(ArrayList<float[]> maps) {

		Log.v("MY", "updateChart");

		// 移除数据集中旧的点集
		mDataset.removeSeries(series);
		mDataset.removeSeries(usedSeries);

		series.clear();
		usedSeries.clear();

		float maxY = 20;// Y轴最大值
		float maxX = 6.5f;// X轴最大值

		for (int i = 0; i < maps.size(); i++) {

			renderer.addXTextLabel(i + 1, (int) maps.get(i)[0] + "#");

			if (maps.get(i)[2] == 1) {
				usedSeries.add(i + 0.6, maps.get(i)[1]);
			} else {
				series.add(i + 1.4, maps.get(i)[1]);
			}

			if (maps.get(i)[1] > maxY) {
				maxY = maps.get(i)[1];

			}

			Log.v("MY", maps.get(i)[0] + "#" + maps.get(i)[1]);

		}

		if (maps.size() > maxX) {
			maxX = (float) (maps.size() + 0.5);
		}

		renderer.setRange(new double[] { 0.5, maxX, 0, maxY + 5 });

		mDataset.addSeries(series);

		mDataset.addSeries(usedSeries);

		// 视图更新，没有这一步，曲线不会呈现动态
		// 如果在非UI主线程中，需要调用postInvalidate()，具体参考api
		graphicalView.invalidate();
	}

}
