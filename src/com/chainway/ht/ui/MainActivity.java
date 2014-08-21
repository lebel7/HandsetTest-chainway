package com.chainway.ht.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.chainway.deviceapi.VersionInfo;
import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.adapter.ModulesAdapter;
import com.chainway.ht.bean.Module;
import com.chainway.ht.utils.NetUtils;
import com.chainway.ht.utils.PopupToolType;
import com.chainway.ht.utils.PopupWindowUtil;
import com.chainway.ht.utils.StringUtils;

import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseActivity {

	private ListView lvModule;

	private ModulesAdapter adapter;

	// 模块列表
	private List<Module> lstModule = new ArrayList<Module>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		init();
		initData();

		tv_title.setText(String.format(getString(R.string.app_title),
				appContext.getPackageInfo().versionName));

	}

	private void init() {

		lvModule = (ListView) findViewById(R.id.lvModule);

		tv_title = (TextView) findViewById(R.id.tvTitle);

		// 注册进入配置界面事件
		tv_title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				UIHelper.showAppSetDialog(MainActivity.this);

			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		initData();
	}

	private void initData() {

		lstModule.clear();

		// 获取设备型号
		String model = android.os.Build.MODEL.toLowerCase();
		String[] modules = null;
		String[] moduleIcons = null;
		String[] moduleClasss = null;

		String ids = appContext.getProperty("ModuleIds");

		if (StringUtils.isEmpty(ids)) {
			try {
				modules = getResources().getStringArray(
						getResources().getIdentifier(model + "name", "array",
								getPackageName()));
				moduleIcons = getResources().getStringArray(
						getResources().getIdentifier(model + "icon", "array",
								getPackageName()));
				moduleClasss = getResources().getStringArray(
						getResources().getIdentifier(model + "cls", "array",
								getPackageName()));

			} catch (Exception e) {
				// 如果没有对应型号则加载默认列表
				modules = getResources().getStringArray(
						getResources().getIdentifier("defaultname", "array",
								getPackageName()));
				moduleIcons = getResources().getStringArray(
						getResources().getIdentifier("defaulticon", "array",
								getPackageName()));
				moduleClasss = getResources().getStringArray(
						getResources().getIdentifier("defaultcls", "array",
								getPackageName()));
			}
		} else {

			String[] sids = ids.split(",");

			String[] modulesTemp = getResources().getStringArray(
					getResources().getIdentifier("allname", "array",
							getPackageName()));
			String[] moduleIconsTemp = getResources().getStringArray(
					getResources().getIdentifier("allicon", "array",
							getPackageName()));
			String[] moduleClasssTemp = getResources().getStringArray(
					getResources().getIdentifier("allcls", "array",
							getPackageName()));

			modules = new String[sids.length];
			moduleIcons = new String[sids.length];
			moduleClasss = new String[sids.length];

			int tint = 0;
			for (int i = 0; i < sids.length; i++) {
				tint = Integer.parseInt(sids[i].trim());
				modules[i] = modulesTemp[tint];
				moduleIcons[i] = moduleIconsTemp[tint];
				moduleClasss[i] = moduleClasssTemp[tint];
			}

		}

		Module module = null;
		int icon = -1;
		for (int i = 0; i < modules.length; i++) {
			try {
				icon = getResources().getIdentifier(moduleIcons[i], "drawable",
						getPackageName());
			} catch (Exception e) {
				icon = R.drawable.p;
			}

			module = new Module(modules[i], icon, moduleClasss[i]);

			lstModule.add(module);
		}

		adapter = new ModulesAdapter(MainActivity.this, lstModule,
				R.layout.module_list_item);

		lvModule.setAdapter(adapter);

		lvModule.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				Log.i("MY", "arg2=" + arg2);

				lstModule.get(arg2).toActivity(MainActivity.this);

			}
		});

	}

	/**
	 * 监听返回--是否退出程序
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean flag = true;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 是否退出应用
			// UIHelper.Exit(this);

			UIHelper.Exit_Toast(this, "再按一次退出");

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void checkNet() {
		super.checkNet();

		tv_title.setText(String.format(getString(R.string.app_title), strNet));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i("MY", "onCreateOptionsMenu");

		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		if (item.getItemId() == R.id.action_about) {

			StringBuilder s = new StringBuilder();
			s.append(getString(R.string.title_about_model));
			s.append(appContext.getDeviceNum());
			s.append("\n");
			s.append(getString(R.string.title_about_device_release));
			s.append(android.os.Build.DISPLAY);
			s.append("\n");
			s.append(getString(R.string.title_about_android_release));
			s.append(android.os.Build.VERSION.RELEASE);
			s.append("\n");
			s.append(getString(R.string.title_about_mac));
			s.append(appContext.getLocalMacAddress());
			s.append("\n");
			s.append(getString(R.string.title_about_jar_release));
			s.append(getVersion());
			s.append("\n");

			UIHelper.alert(MainActivity.this, R.string.action_about,
					s.toString(), R.drawable.webtext);
		}
		return true;
	}

	public String getVersion() {
		return VersionInfo.getVersion();
	}

}
