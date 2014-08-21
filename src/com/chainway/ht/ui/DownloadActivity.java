package com.chainway.ht.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.chainway.ht.AppConfig;
import com.chainway.ht.AppException;
import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.R.layout;
import com.chainway.ht.R.menu;
import com.chainway.ht.adapter.FileListAdapter;
import com.chainway.ht.bean.SFile;
import com.chainway.ht.bean.SFileList;
import com.chainway.ht.filebrowser.FileManagerActivity;
import com.chainway.ht.network.ServerAgent;
import com.chainway.ht.utils.FileUtils;
import com.chainway.ht.utils.StringUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadActivity extends BaseActivity {

	private static final String TAG = "DownloadActivity";

	private FileListAdapter adapter;
	private List<SFile> listItems = new ArrayList<SFile>();

	private TextView tvLoding;
	private TextView tvResult;
	private ListView lv;

	private Button btn_back;

	private String downPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);

		isRegisterBroadcastReceiver = true;

		init();
	}

	private void init() {
		tvLoding = (TextView) findViewById(R.id.tvLoding);
		lv = (ListView) findViewById(R.id.lvFiles);
		tv_title = (TextView) findViewById(R.id.tvTitle);
		btn_back = (Button) findViewById(R.id.btnBack);
		tvResult = (TextView) findViewById(R.id.tvResult);

		btn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				onBackPressed();
			}
		});

		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				TextView tv = (TextView) arg1.findViewById(R.id.tvTitle);
				downPath = tv.getTag().toString();
				Log.i("MY", "path " + downPath);

				new AlertDialog.Builder(DownloadActivity.this)
						.setTitle(R.string.download_msg_down_file)
						.setMessage(R.string.download_msg_down_confirm)
						.setPositiveButton(R.string.download_msg_confirm,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										if (StringUtils.isEmpty(downPath)) {
											UIHelper.ToastMessage(
													DownloadActivity.this,
													R.string.download_msg_file_path_not_exist);
											return;
										}

										if (!networkExist) {
											UIHelper.ToastMessage(
													DownloadActivity.this,
													R.string.up_msg_net_not_conn);
											return;
										}

										new DownloadTask().execute();

									}
								})
						.setNegativeButton(R.string.download_msg_cancel,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// Toast.makeText(DownloadActivity.this,
										// "取消选择",
										// Toast.LENGTH_SHORT).show();

									}
								}).show();

			}
		});

		new GetFileListTask().execute();

	}

	private void fillAdapter() {
		adapter = new FileListAdapter(DownloadActivity.this, listItems,
				R.layout.file_list_item);

		lv.setAdapter(adapter);

	}

	@Override
	protected void checkNet() {
		super.checkNet();

		tv_title.setText(String.format(getString(R.string.download_title),
				strNet));

		Log.d(TAG, "checkNet");
	}

	// 获取文件列表
	private class GetFileListTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {

			SFileList fileList = null;

			try {
				fileList = ServerAgent.getFileList(appContext);
			} catch (AppException e) {
				e.printStackTrace();
			}

			if (fileList != null) {
				listItems = fileList.getFilelist();

				Log.i("MY", "listItems.size " + listItems.size());

			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			fillAdapter();

			if (listItems == null || listItems.size() == 0) {
				tvLoding.setText(getString(R.string.msg_no_data));
				lv.setVisibility(View.GONE);
				tvLoding.setVisibility(View.VISIBLE);
			} else {
				tvLoding.setVisibility(View.GONE);
				lv.setVisibility(View.VISIBLE);

				setListViewHeightBasedOnChildren(lv);
			}

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			tvLoding.setVisibility(View.VISIBLE);
			tvLoding.setText(getString(R.string.msg_loding));

			lv.setVisibility(View.GONE);

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

	}

	// 下载任务
	private class DownloadTask extends AsyncTask<String, Integer, String> {

		private ProgressDialog pd;
		protected long totalSize;
		protected long downSize;
		String savePath;
		String tmpFilePath;

		long startTime;
		long endTime;

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			Log.i("MY", "onPostExecute() ");

			endTime = System.currentTimeMillis();
			long between = ((endTime - startTime) / 1000);
			long speed = 0;

			if (between > 0) {
				speed = totalSize / between;
			}

			UIHelper.ToastMessage(DownloadActivity.this,
					R.string.download_msg_down_succ);

			pd.dismiss();

			// tvResult.setText("文件大小：" + FileUtils.formatFileSize(totalSize)
			// + "\n开始时间：" + StringUtils.getTimeFormat(startTime)
			// + "\n结束时间：" + StringUtils.getTimeFormat(endTime)
			// + "\n下载耗时：" + between + "秒\n平均速度："
			// + FileUtils.formatFileSize(speed) + "/s");

			new AlertDialog.Builder(DownloadActivity.this)
					.setTitle(R.string.download_msg_down_report)
					.setMessage(
							getString(R.string.up_msg_file_size)
									+ FileUtils.formatFileSize(totalSize)
									+ "\n"
									+ getString(R.string.download_msg_down_file_size)
									+ FileUtils.formatFileSize(downSize)
									+ "\n"
									+ getString(R.string.up_msg_start_time)
									+ StringUtils.getTimeFormat(startTime)
									+ "\n"
									+ getString(R.string.up_msg_stop_time)
									+ StringUtils.getTimeFormat(endTime)
									+ "\n"
									+ getString(R.string.download_msg_total_time)
									+ between + "s\n"
									+ getString(R.string.up_msg_avg_speed)
									+ FileUtils.formatFileSize(speed) + "/s")
					.setNegativeButton(R.string.download_msg_close,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// Toast.makeText(DownloadActivity.this,
									// "取消选择",
									// Toast.LENGTH_SHORT).show();

								}
							}).show();

			Log.i("MY", "Result " + tvResult.getText().toString());
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			String tmpApk = "down.tmp";
			// 判断是否挂载了SD卡
			String storageState = Environment.getExternalStorageState();
			if (storageState.equals(Environment.MEDIA_MOUNTED)) {
				savePath = AppConfig.DEFAULT_SAVE_PATH + "Downloadtest/";
				File file = new File(savePath);
				if (!file.exists()) {
					file.mkdirs();
				}
				tmpFilePath = savePath + tmpApk;
			}

			// 没有挂载SD卡，无法下载文件
			if (tmpFilePath == null || tmpFilePath == "") {
				UIHelper.ToastMessage(DownloadActivity.this,
						R.string.download_msg_sdcard_not_exist);
				return;
			}

			pd = new ProgressDialog(DownloadActivity.this);
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setMessage(getString(R.string.download_msg_downing));
			pd.setCancelable(false);
			pd.show();

			startTime = System.currentTimeMillis();
			tvResult.setText("");

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			pd.setProgress((int) (values[0]));

			Log.i("MY", "onProgressUpdate() " + values[0]);

		}

		@Override
		protected String doInBackground(String... params) {

			Log.i("MY", "down() START");

			try {

				Log.i("MY", "down() 1");

				File tmpFile = new File(tmpFilePath);

				// 是否已下载更新文件
				if (tmpFile.exists()) {
					tmpFile.delete();
				}

				Log.i("MY", "down() 2");

				// 输出临时下载文件
				FileOutputStream fos = new FileOutputStream(tmpFile);

				URL url = new URL(downPath);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.connect();
				totalSize = conn.getContentLength();

				Log.i("MY", "conn.getContentLength " + totalSize);

				InputStream is = conn.getInputStream();

				// // 显示文件大小格式：2个小数点显示
				// DecimalFormat df = new DecimalFormat("0.00");
				// // 进度条下面显示的总文件大小
				// String fileSize = df.format((float) length / 1024 / 1024)
				// + "MB";

				int count = 0;
				int numread = 0;
				byte buf[] = new byte[1024];

				while ((numread = is.read(buf)) > 0) {
					count += numread;
					publishProgress((int) (count * 100 / totalSize));
					Log.i("MY", "Progress " + count + " " + totalSize + " "
							+ (count * 100 / totalSize));
					fos.write(buf, 0, numread);
				}
				downSize = count;

				fos.close();
				is.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Log.i("MY", "down() END");

			return null;
		}

	}

}
