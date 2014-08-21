package com.chainway.ht.view;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class ChartView extends LinearLayout {

	private int lastSignal;

	private Timer timer;
	private TimerTask task;
	private Handler handler;
	private String title = "Signal Strength";
	private TimeSeries series;
	private XYMultipleSeriesDataset mDataset;// 数据设置器
	private GraphicalView chart;
	private XYMultipleSeriesRenderer renderer;// 描绘器
	private int addY;
	private long addX;
	private double xMin;
	private double xMax;
	private double yMin;
	private double yMax;

	boolean timerStarted = false;

	Date[] xv = new Date[60];
	int[] yv = new int[60];

	public ChartView(Context context) {
		super(context);

	}

	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		// 这个类用来放置曲线上的所有点，是一个点的集合，根据这些点画出曲线
		series = new TimeSeries(title);

		// 创建一个数据集的实例，这个数据集将被用来创建图表
		mDataset = new XYMultipleSeriesDataset();

		// 将点集添加到这个数据集中
		mDataset.addSeries(series);

		// 以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
		int color = Color.GREEN;
		PointStyle style = PointStyle.POINT;
		renderer = buildRenderer(color, style, true);

		// 设置好图表的样式
		setChartSettings(renderer, "X", "Y", getxMin(), getxMax(), getyMin(),
				getyMax(), Color.WHITE, Color.WHITE);

		// 生成图表
		chart = ChartFactory.getTimeChartView(context, mDataset, renderer,
				"HH:mm:ss");

		// 将图表添加到布局中去
		this.addView(chart, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		// 这里的Handler实例将配合下面的Timer实例，完成定时更新图表的功能
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// 刷新图表
				updateChart();
				super.handleMessage(msg);
			}
		};

	}

	private class MyTimerTask extends TimerTask {
		@Override
		public void run() {
			Message message = new Message();
			message.what = 1;
			handler.sendMessage(message);
		}
	}

	public void setChartSettings() {
		// 设置好图表的样式
		setChartSettings(renderer, "X", "Y", getxMin(), getxMax(), getyMin(),
				getyMax(), Color.WHITE, Color.WHITE);

	}

	protected XYMultipleSeriesRenderer buildRenderer(int color,
			PointStyle style, boolean fill) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

		// 设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(color);
		r.setPointStyle(style);
		r.setFillPoints(fill);
		r.setLineWidth(3);
		renderer.addSeriesRenderer(r);

		return renderer;
	}

	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
			String xTitle, String yTitle, double xMin, double xMax,
			double yMin, double yMax, int axesColor, int labelsColor) {
		// 有关对图表的渲染可参看api文档
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
		renderer.setShowGrid(true);
		renderer.setGridColor(Color.DKGRAY);
		renderer.setXLabels(3);
		renderer.setYLabels(10);
		renderer.setXTitle("Time");
		renderer.setYTitle("dBm");
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setPointSize((float) 2);
		renderer.setShowLegend(false);
		// renderer.setDisplayChartValues(true);
		renderer.setClickEnabled(false);
		renderer.setPanEnabled(false); // 屏蔽移动
		renderer.setZoomEnabled(false); // 屏蔽缩放
	}

	private void updateChart() {

		// 设置好下一个需要增加的节点
		addX = new Date().getTime();
		// addY = (int) (Math.random() * -140);
		addY = lastSignal;

		// 移除数据集中旧的点集
		mDataset.removeSeries(series);
		// 判断当前点集中到底有多少点，因为屏幕总共只能容纳60个，所以当点数超过60时，长度永远是60
		int length = series.getItemCount();
		if (length > 60) {
			length = 60;

		}

		// 将旧的点集中x和y的数值取出来放入backup中
		for (int i = 0; i < length; i++) {
			xv[i] = new Date((long) series.getX(i)); // (int) series.getX(i) +
														// 1;
			yv[i] = (int) series.getY(i);
		}

		// 点集先清空，为了做成新的点集而准备
		series.clear();

		// 将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
		// 这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
		series.add(new Date(addX), addY);
		for (int k = 0; k < length; k++) {
			series.add(xv[k], yv[k]);
		}

		// 更新X轴刻度数据
		renderer.setXAxisMin(new Date(addX - 42 * 1000).getTime());
		renderer.setXAxisMax(addX);

		// 在数据集中添加新的点集
		mDataset.addSeries(series);

		// 视图更新，没有这一步，曲线不会呈现动态
		// 如果在非UI主线程中，需要调用postInvalidate()，具体参考api
		chart.invalidate();
	}

	/**
	 * 设置信号强度值
	 * 
	 * @param signal
	 */
	public void setSignal(int signal) {
		lastSignal = signal;
	}

	/**
	 * 取消数据定时更新
	 * 
	 */
	public void cancelTimer() {
		timer.cancel();
		timerStarted = false;
	}

	/**
	 * 启动数据定时更新
	 */
	public void startTimer() {

		if (timerStarted) {
			cancelTimer();
		}

		xv = new Date[60];
		yv = new int[60];

		// 点集先清空，为了做成新的点集而准备
		series.clear();

		timer = new Timer();
		task = new MyTimerTask();

		timer.schedule(task, 500, 2000);

		timerStarted = true;
	}

	public double getxMin() {
		return xMin;
	}

	public void setxMin(double xMin) {
		this.xMin = xMin;
	}

	public double getxMax() {
		return xMax;
	}

	public void setxMax(double xMax) {
		this.xMax = xMax;
	}

	public double getyMin() {
		return yMin;
	}

	public void setyMin(double yMin) {
		this.yMin = yMin;
	}

	public double getyMax() {
		return yMax;
	}

	public void setyMax(double yMax) {
		this.yMax = yMax;
	}
}
