package com.chainway.ht.ui;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.chainway.ht.R;
import com.chainway.ht.R.layout;
import com.chainway.ht.R.menu;
import com.chainway.ht.UIHelper;
import com.chainway.ht.adapter.WiFiAdapter;
import com.chainway.ht.network.WifiAdmin;
import com.chainway.ht.ui.fragment.MobileFragment;
import com.chainway.ht.ui.fragment.WiFiFragment;
import com.chainway.utility.StringUtility;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

public class TestActivity extends Activity {

	Button button1;
	EditText editText1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		button1 = (Button) findViewById(R.id.button1);
		editText1 = (EditText) findViewById(R.id.editText1);

		button1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Log.i("TestActivity",
						StringUtility.isHexNumberRex(editText1.getText()
								.toString()) + "");

				UIHelper.ToastMessage(
						TestActivity.this,
						StringUtility.isHexNumberRex(editText1.getText()
								.toString()) + "");
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
