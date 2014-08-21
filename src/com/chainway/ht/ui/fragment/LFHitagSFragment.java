package com.chainway.ht.ui.fragment;

import com.chainway.deviceapi.entity.AnimalEntity;
import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.ui.LFActivity;
import com.chainway.ht.utils.StringUtils;
import com.chainway.utility.StringUtility;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public class LFHitagSFragment extends Fragment {
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
		return inflater.inflate(R.layout.lf_hitags_fragment, container, false);
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

			return mContext.mLF.readDataWithHitagS(StringUtils
					.toInt(arg0[0], 0));

		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (TextUtils.isEmpty(result)) {
				tv_result.append(getString(R.string.lf_msg_scan_fail));
				tv_result.append("\r\n");

				mContext.playSound(2);
			} else {

				tv_result.append("HiTag:" + result);
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
			if (mContext.mLF.writeDataWithHitagS(iPage, sData)) {
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
