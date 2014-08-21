package com.chainway.ht.ui.fragment;

import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.ui.LFActivity;
import com.chainway.ht.utils.StringUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class LFEM4305Fragment extends Fragment {
	private LFActivity mContext;

	private Button btn_Start;
	private Button btn_Clear;
	private Button btn_Write;
	private EditText et_pageId;
	private EditText et_data;
	private TextView tv_result;
	private ScrollView svResult;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		mContext = (LFActivity) getActivity();
		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.lf_em4305_fragment, container, false);
	}

	private void init() {
		btn_Start = (Button) mContext.findViewById(R.id.btn_Start);
		btn_Clear = (Button) mContext.findViewById(R.id.btn_Clear);
		btn_Write = (Button) mContext.findViewById(R.id.btn_Write);
		et_pageId = (EditText) mContext.findViewById(R.id.et_pageId);
		et_data = (EditText) mContext.findViewById(R.id.et_data);
		tv_result = (TextView) mContext.findViewById(R.id.tv_result);
		svResult = (ScrollView) mContext.findViewById(R.id.svResult);

		btn_Start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				scan();

			}
		});

		btn_Clear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				clear();

			}
		});

		btn_Write.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				write();

			}
		});

	}

	private class ScanTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... arg0) {

			return mContext.mLF.readDataWith4305Card(StringUtils.toInt(arg0[0],
					0));

		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (TextUtils.isEmpty(result)) {
				tv_result.append(getString(R.string.lf_msg_scan_fail));
				tv_result.append("\r\n");

				mContext.playSound(2);
			} else {

				tv_result.append("EM4305:" + result);
				tv_result.append("\r\n");

				mContext.playSound(1);
			}

			mContext.scrollToBottom(svResult, tv_result);

		}

	};

	private class WriteTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... arg0) {
			int iPage = StringUtils.toInt(arg0[0], 0);

			String sData = arg0[1];
			if (mContext.mLF.writeDataWith4305Card(iPage, sData)) {
				return "succ";
			} else {
				return "fail";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (result.equals("succ")) {
				tv_result.append(getString(R.string.lf_msg_write_succ));
				tv_result.append("\r\n");

				mContext.playSound(1);

			} else {
				tv_result.append(getString(R.string.lf_msg_write_fail));
				tv_result.append("\r\n");
				mContext.playSound(2);
			}

			mContext.scrollToBottom(svResult, tv_result);

		}

	};

	public void myOnKeyDwon() {
		scan();
	}

	public void write() {
		String str = et_data.getText().toString();
		int iPage = StringUtils.toInt(et_pageId.getText().toString(), 0);

		if (((iPage != 0 && iPage != 3) && iPage < 5) || iPage > 13)// 0、3、5~13
		{
			UIHelper.ToastMessage(mContext,
					R.string.lf_msg_data_page_id_err_em4305);
			return;
		}

		if (TextUtils.isEmpty(str)) {

			UIHelper.ToastMessage(mContext, R.string.lf_msg_data_not_null);
			return;
		}

		if (str.length() != 8 || !StringUtils.isHexNumber(str)) {
			UIHelper.ToastMessage(mContext, R.string.lf_msg_data_not_hex4);
			return;
		}

		new WriteTask().execute(et_pageId.getText().toString(), str);
	}

	public void scan() {
		new ScanTask().execute(et_pageId.getText().toString());

	}

	private void clear() {
		tv_result.setText("");

	}
}
