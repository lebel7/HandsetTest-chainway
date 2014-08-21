package com.chainway.ht.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.R.layout;
import com.chainway.ht.R.menu;
import com.chainway.ht.bean.URLs;
import com.chainway.ht.filebrowser.FileManagerActivity;
import com.chainway.ht.network.HttpMultipartPost;
import com.chainway.ht.utils.FileUtils;
import com.chainway.ht.utils.StringUtils;

import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.Selection;
import android.text.Spannable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UploadActivity extends BaseActivity {
	private GetPathBroadcastReceiver mGetpathReceiver = null;
	private Button btnBrowser;
	private Button btn_back;
	private Button btn_Upload;
	private EditText etPath;
	private HttpMultipartPost post;

	private TextView tvResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		isRegisterBroadcastReceiver = true;
		init();
	}

	private void init() {
		btn_back = (Button) findViewById(R.id.btnBack);
		btnBrowser = (Button) findViewById(R.id.btnBrowser);
		btn_Upload = (Button) findViewById(R.id.btnUpload);
		etPath = (EditText) findViewById(R.id.et_file);

		tv_title = (TextView) findViewById(R.id.tvTitle);
		tvResult = (TextView) findViewById(R.id.tvResult);

		btnBrowser.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				UIHelper.showFileManager(UploadActivity.this);

			}
		});

		btn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				onBackPressed();
			}
		});

		btn_Upload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				upFile();
			}
		});

		mGetpathReceiver = new GetPathBroadcastReceiver();
		IntentFilter filterPosition = new IntentFilter();
		filterPosition.addAction(FileManagerActivity.FILE_PATH_ACTION);
		registerReceiver(mGetpathReceiver, filterPosition);

	}

	private class GetPathBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(FileManagerActivity.FILE_PATH_ACTION)) {
				String strFilePath = intent.getStringExtra("filepath");
				etPath.setText((CharSequence) strFilePath);
				setEditTextCursorLocation(etPath);
			}
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

	@Override
	protected void checkNet() {
		super.checkNet();

		tv_title.setText(String
				.format(getString(R.string.upload_title), strNet));
	}

	private void upFile() {

		if (!networkExist) {
			UIHelper.ToastMessage(UploadActivity.this,
					R.string.up_msg_net_not_conn);
			return;
		}

		String filePath = etPath.getText().toString();

		if (StringUtils.isEmpty(filePath)) {
			UIHelper.ToastMessage(UploadActivity.this, R.string.up_msg_sel_file);
			return;
		}

		File file = new File(filePath);
		if (file.exists()) {

			Log.i("MY", "CheckActivity.upCheckMenu 1 ");

			post = new MyPost(UploadActivity.this, filePath, URLs.URL_UP_FILE
					+ "?FileName=" + StringUtils.getTimeString() + ".rar");
			post.execute();

			Log.i("MY", "CheckActivity.upCheckMenu 2 ");

			// lvData.clear();
			// productAdapter.notifyDataSetChanged();

		} else {
			UIHelper.ToastMessage(UploadActivity.this,
					R.string.up_msg_file_not_exist);

		}

	}

	private class MyPost extends HttpMultipartPost {
		long startTime;
		long endTime;

		public MyPost(Context context, String filePath, String postUrl) {
			super(context, filePath, postUrl);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			tvResult.setText("");
			startTime = System.currentTimeMillis();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			endTime = System.currentTimeMillis();
			long between = ((endTime - startTime) / 1000);

			long speed = 0;

			if (between == 0) {
				speed = totalSize;
			} else {
				speed = totalSize / between;
			}

			tvResult.setText(getString(R.string.up_msg_file_size)
					+ FileUtils.formatFileSize(totalSize) + "\n"
					+ getString(R.string.up_msg_start_time)
					+ StringUtils.getTimeFormat(startTime) + "\n"
					+ getString(R.string.up_msg_stop_time)
					+ StringUtils.getTimeFormat(endTime) + "\n"
					+ getString(R.string.up_msg_total_time) + between + "s\n"
					+ getString(R.string.up_msg_avg_speed)
					+ FileUtils.formatFileSize(speed) + "/s");
		}

	}

}
