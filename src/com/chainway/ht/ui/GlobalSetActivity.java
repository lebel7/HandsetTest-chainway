package com.chainway.ht.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.R.layout;
import com.chainway.ht.adapter.ModulesAdapter;
import com.chainway.ht.bean.Module;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class GlobalSetActivity extends BaseActivity {

	private final static String TAG = "GlobalSetActivity";

	private Button btn_back;
	private Button btn_Save;

	private ListView lvModule;

	private ModulesAdapter adapter;

	// 模块列表
	private List<Module> lstModule = new ArrayList<Module>();

	// 选中的模块Id列表
	private List<String> lstModuleId = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_global_set);

		init();
		initData();
	}

	private void init() {
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_Save = (Button) findViewById(R.id.btn_Save);
		lvModule = (ListView) findViewById(R.id.lvModel);

		btn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				onBackPressed();
			}
		});
		btn_Save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Collections.sort(lstModuleId);
				String ids = lstModuleId.toString();
				ids = ids.replace("[", "").replace("]", "");
				appContext.setProperty("ModuleIds", ids);

				Log.i(TAG, "ids=" + appContext.getProperty("ModuleIds"));

				UIHelper.showMain(GlobalSetActivity.this);

				// Intent intent=new Intent();
				// intent.setClass(GlobalSetActivity.this, MainActivity.class);
				//
				// startActivityForResult(intent, 0);
			}
		});
	}

	private void initData() {

		lstModule.clear();

		String[] modules = null;
		String[] moduleIcons = null;
		String[] moduleClasss = null;

		try {
			modules = getResources().getStringArray(
					getResources().getIdentifier("allname", "array",
							getPackageName()));
			moduleIcons = getResources().getStringArray(
					getResources().getIdentifier("allicon", "array",
							getPackageName()));
			moduleClasss = getResources().getStringArray(
					getResources().getIdentifier("allcls", "array",
							getPackageName()));

		} catch (Exception e) {

		}

		Module module = null;
		int icon = -1;
		for (int i = 0; i < modules.length; i++) {
			try {
				icon = R.drawable.cancel; // getResources().getIdentifier(moduleIcons[i],
											// "drawable",getPackageName());
			} catch (Exception e) {
				icon = R.drawable.cancel;
			}

			module = new Module(modules[i], icon, moduleClasss[i]);

			lstModule.add(module);
		}

		adapter = new ModulesAdapter(GlobalSetActivity.this, lstModule,
				R.layout.module_sel_list_item);

		lvModule.setAdapter(adapter);

		lvModule.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				Log.i("MY", "arg2=" + arg2);

				// lstModule.get(arg2).toActivity(GlobalSetActivity.this);

				ImageView ivIcon = (ImageView) arg1.findViewById(R.id.ivIcon);

				Module m = (Module) lstModule.get(arg2);

				Log.i(TAG, "ivIcon.getTag()=" + ivIcon.getTag());

				if (ivIcon.getTag().equals("false")) {
					m.setIcon(R.drawable.ok);

					ivIcon.setImageResource(R.drawable.ok);
					ivIcon.setTag("true");

					lstModuleId.add(String.valueOf(arg2));
				} else {
					m.setIcon(R.drawable.cancel);
					lstModuleId.remove(String.valueOf(arg2));
					ivIcon.setTag("false");
					ivIcon.setImageResource(R.drawable.cancel);
				}

			}
		});

	}

}
