package com.chainway.ht.ui.fragment;

import java.util.List;

import com.chainway.deviceapi.Fingerprint.BufferEnum;
import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.bean.Fingerprint;
import com.chainway.ht.dao.FingerprintSDDao;
import com.chainway.ht.ui.A14443Activity;
import com.chainway.ht.ui.FingerprintActivity;
import com.chainway.ht.utils.StringUtils;
import com.chainway.utility.StringUtility;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

public class AcquisitionFragment extends Fragment {

	private static final String TAG = "AcquisitionFragment";

	private FingerprintActivity mContext;

	private EditText etPageId;
	private EditText etName;
	private Button btnSave;
	private CheckBox cbShowImg;
	private ImageView ivFinger;

	private FingerprintSDDao dao;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fingerprint_acquisition_fragment,
				container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mContext = (FingerprintActivity) getActivity();

		init();
	}

	private void init() {
		etPageId = (EditText) mContext.findViewById(R.id.etPageId);
		etName = (EditText) mContext.findViewById(R.id.etName);
		btnSave = (Button) mContext.findViewById(R.id.btnSave);
		cbShowImg = (CheckBox) mContext.findViewById(R.id.cbShowImg);
		ivFinger = (ImageView) mContext.findViewById(R.id.ivFinger);
		btnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String pageId = etPageId.getText().toString().trim();
				String name = etName.getText().toString().trim();

				if (TextUtils.isEmpty(pageId)) {
					UIHelper.ToastMessage(mContext,
							R.string.fingerprint_msg_page_id_not_null);
					return;
				}

				if (TextUtils.isEmpty(name)) {
					UIHelper.ToastMessage(mContext,
							R.string.fingerprint_msg_name_not_null);
					return;
				}

				if (!TextUtils.isDigitsOnly(pageId)) {
					UIHelper.ToastMessage(mContext,
							R.string.fingerprint_msg_page_id_need_digits);
					return;
				}

				int iPageId = Integer.parseInt(pageId);

				if (iPageId < 0 || iPageId > 254) {
					UIHelper.ToastMessage(mContext,
							R.string.fingerprint_msg_page_id_need_0_to_254);
					return;
				}

				new AcqTask(iPageId, name, cbShowImg.isChecked()).execute(
						iPageId, cbShowImg.isChecked() ? 1 : 0);

			}
		});

		dao = new FingerprintSDDao(mContext);
	}

	class AcqTask extends AsyncTask<Integer, Integer, String> {
		ProgressDialog mypDialog;

		int pid;
		String uname;
		boolean isShowImg;
		String data;

		public AcqTask(int pageId, String name, boolean showImg) {
			pid = pageId;
			uname = name;
			isShowImg = showImg;
		}

		@Override
		protected String doInBackground(Integer... params) {

			boolean exeSucc = false;

			// 采集指纹
			if (!mContext.mFingerprint.getImage()) {
				return null;
			}

			// 生成特征值到B1
			if (mContext.mFingerprint.genChar(BufferEnum.B1)) {
				exeSucc = true;
			}

			// 再次采集指纹
			if (!mContext.mFingerprint.getImage()) {
				return null;
			}

			// 生成特征值到B2
			if (mContext.mFingerprint.genChar(BufferEnum.B2)) {
				exeSucc = true;
			}

			// 合并两个缓冲区到B1
			if (mContext.mFingerprint.regModel()) {
				exeSucc = true;
			}

			if (exeSucc) {
				if (mContext.mFingerprint.storChar(BufferEnum.B1, pid)) {

					data = mContext.mFingerprint.upChar(BufferEnum.B1);
					Log.i(TAG, "data:" + data);

					if (isShowImg) {
						// 显示指纹图片
						if (mContext.mFingerprint.getImage()) {
							if (mContext.mFingerprint.upImage(1,
									mContext.getFilesDir() + "/finger.bmp") != -1) {
								return "img-ok";

							} else {
								return "img-fail";
							}
						} else {
							return "img-fail";
						}
					}
					return "ok";
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			mypDialog.cancel();

			if (TextUtils.isEmpty(result)) {
				UIHelper.ToastMessage(mContext,
						R.string.fingerprint_msg_acq_fail);
				mContext.playSound(2);
				return;
			} else if (result.equals("img-fail")) {
				UIHelper.ToastMessage(mContext,
						R.string.fingerprint_title_get_img_fail);
				mContext.playSound(2);
				return;

			} else if (result.equals("img-ok")) {
				Bitmap bitmap = BitmapFactory.decodeFile(mContext.getFilesDir()
						+ "/finger.bmp");
				ivFinger.setImageBitmap(bitmap);
			}

			dao.startReadableDatabase(false);

			Fingerprint f = dao.query(pid);
			if (f == null) {
				f = new Fingerprint();
				f.setPageId(pid);
				f.setName(uname);
				f.setFeatures(data);
				f.setCreateTime(StringUtils.getTimeFormat(System
						.currentTimeMillis()));
				dao.insert(f);
			} else {
				f.setPageId(pid);
				f.setName(uname);
				f.setFeatures(data);
				f.setCreateTime(StringUtils.getTimeFormat(System
						.currentTimeMillis()));
				dao.update(f);
			}
			dao.closeDatabase(false);

			UIHelper.ToastMessage(mContext, R.string.fingerprint_msg_acq_succ);
			mContext.playSound(1);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mypDialog = new ProgressDialog(mContext);
			mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mypDialog.setCanceledOnTouchOutside(false);
			mypDialog.show();

			ivFinger.setImageBitmap(null);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

	};
}
