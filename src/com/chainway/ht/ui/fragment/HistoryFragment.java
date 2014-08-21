package com.chainway.ht.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import com.ab.activity.AbActivity;
import com.chainway.deviceapi.Fingerprint.BufferEnum;
import com.chainway.deviceapi.exception.RFIDNotFoundException;
import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.adapter.FingerprintAdapter;
import com.chainway.ht.bean.Fingerprint;
import com.chainway.ht.dao.FingerprintSDDao;
import com.chainway.ht.ui.FingerprintActivity;
import com.chainway.ht.ui.ISO15693Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class HistoryFragment extends Fragment {

	private static final String TAG = "HistoryFragment";

	private FingerprintActivity mContext;

	private FingerprintSDDao dao;
	// 列表数据
	private List<Fingerprint> fList = new ArrayList<Fingerprint>();

	// 每一页显示的行数
	public int pageSize = 10;
	// 当前页数
	public int pageNum = 1;

	private ListView lvData;
	private FingerprintAdapter adapter;

	private Button btnImport;
	private Button btnClear;
	private TextView tvLocalNum;
	private TextView tvModelNum;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View viewRoot = inflater.inflate(R.layout.fingerprint_history_fragment,
				container, false);
		AbActivity.initIocView(this, viewRoot);
		return viewRoot;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mContext = (FingerprintActivity) getActivity();

		init();
		initData();
	}

	private void init() {
		tvLocalNum = (TextView) mContext.findViewById(R.id.tvLocalNum);
		tvModelNum = (TextView) mContext.findViewById(R.id.tvModelNum);
		lvData = (ListView) mContext.findViewById(R.id.lvData);
		btnImport = (Button) mContext.findViewById(R.id.btnImport);
		btnClear = (Button) mContext.findViewById(R.id.btnClear);

		registerForContextMenu(lvData);

		btnImport.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new ImportTask().execute();

			}
		});

		btnClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				new AlertDialog.Builder(mContext)
						.setTitle(R.string.rfid_msg_confirm_title)
						.setMessage(R.string.fingerprint_msg_sure_clear)
						.setPositiveButton(R.string.rfid_msg_confirm_true,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										new EmptyTask().execute();

									}
								})
						.setNegativeButton(R.string.rfid_msg_confirm_flase,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();

									}
								}).show();

			}
		});

	}

	private void initData() {

		dao = new FingerprintSDDao(mContext);
		// (1)获取数据库
		dao.startReadableDatabase(false);
		// (2)执行查询
		fList = dao.queryList(null, null, null, null, null,
				"create_time desc limit " + String.valueOf(pageSize)
						+ " offset " + 0, null);
		// (3)关闭数据库
		dao.closeDatabase(false);

		adapter = new FingerprintAdapter(mContext, fList);
		lvData.setAdapter(adapter);

		tvLocalNum.setText(fList.size() + "");

		new ModelCountTask().execute();

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = mContext.getMenuInflater();
		inflater.inflate(R.menu.fingerprint, menu);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.action_finger_del) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			delFingerprint(Integer.parseInt(info.id + ""));
			return true;
		}

		return super.onContextItemSelected(item);
	}

	private void delFingerprint(int index) {
		Fingerprint f = fList.get(index);

		if (fList.remove(f)) {
			Log.i(TAG, "onContextItemSelected PageId:" + f.getPageId());
			dao.startWritableDatabase(false);
			dao.delete(" pageId=? ", new String[] { f.getPageId() + "" });
			dao.closeDatabase(false);
			adapter.notifyDataSetChanged();

			tvLocalNum.setText(fList.size() + "");
		}
	}

	class ModelCountTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... params) {

			return mContext.mFingerprint.validTempleteNum();
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			tvModelNum.setText(result + "");
		}

	}

	class EmptyTask extends AsyncTask<Integer, Integer, Integer> {
		ProgressDialog mypDialog;

		@Override
		protected Integer doInBackground(Integer... params) {

			if (mContext.mFingerprint.empty()) {
				return 0;
			}

			return 1;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			mypDialog.cancel();
			if (result == 0) {
				new ModelCountTask().execute();
				mContext.playSound(1);
				UIHelper.ToastMessage(mContext,
						R.string.fingerprint_msg_clear_succ);
			} else {
				UIHelper.ToastMessage(mContext,
						R.string.fingerprint_msg_clear_fail);
				mContext.playSound(2);
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mypDialog = new ProgressDialog(mContext);
			mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mypDialog.setCanceledOnTouchOutside(false);
			mypDialog.show();

		}

	}

	class ImportTask extends AsyncTask<Integer, Integer, String> {
		ProgressDialog mypDialog;

		int iSucc = 0;
		int iFail = 0;

		@Override
		protected String doInBackground(Integer... params) {

			if (fList == null || fList.size() < 1) {
				return null;

			}

			for (Fingerprint item : fList) {

				Log.i(TAG, "PageId:" + item.getPageId() + " Features:"
						+ item.getFeatures().trim());

				if (mContext.mFingerprint.downChar(BufferEnum.B1, item
						.getFeatures().trim())) {
					if (mContext.mFingerprint.storChar(BufferEnum.B1,
							item.getPageId())) {
						iSucc++;
					} else {
						Log.e(TAG, "fail pageId:" + item.getPageId());
						iFail++;
					}
				} else {
					iFail++;

					Log.e(TAG, "fail pageId:" + item.getPageId());
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			mypDialog.cancel();

			if (iSucc > 0) {
				new ModelCountTask().execute();
			}
			if (iFail == 0) {

				mContext.playSound(1);
				UIHelper.ToastMessage(mContext,
						R.string.fingerprint_msg_import_succ);
			} else {
				UIHelper.ToastMessage(mContext, String.format(mContext
						.getString(R.string.fingerprint_msg_import_fail),
						iSucc, iFail));
				mContext.playSound(2);
			}

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mypDialog = new ProgressDialog(mContext);
			mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mypDialog.setCanceledOnTouchOutside(false);
			mypDialog.show();

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

	};
}
