package com.chainway.ht.ui.fragment;

import java.util.logging.SimpleFormatter;

import com.chainway.deviceapi.entity.AnimalEntity;
import com.chainway.ht.R;
import com.chainway.ht.ui.A14443Activity;
import com.chainway.ht.ui.LFActivity;
import com.chainway.ht.ui.fragment.Scan14443AFragment.ScanRunnable;
import com.chainway.ht.utils.StringUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public class LFScanFragment extends Fragment {

	private static final String TAG = "LFScanFragment";

	private LFActivity mContext;

	private Button btn_Start;
	private Button btn_Clear;
	private CheckBox cbContinuous;
	private EditText et_between;
	private TextView tv_result;
	private TextView tv_scan_count;
	private TextView tv_succ_count;
	private Spinner spType;
	private ScrollView svResult;

	int sussCount = 0;
	int failCount = 0;

	private Handler mHandler;

	private ScanThread scanThread;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		mContext = (LFActivity) getActivity();

		init();
	}

	@Override
	public void onPause() {
		super.onPause();
		btn_Start.setText(getString(R.string.title_scan));
		if (scanThread != null) {
			scanThread.cancel();
		}

		btn_Clear.setEnabled(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.lf_scan_fragment, container, false);
	}

	private void init() {
		btn_Start = (Button) mContext.findViewById(R.id.btn_Start);
		btn_Clear = (Button) mContext.findViewById(R.id.btn_Clear);
		cbContinuous = (CheckBox) mContext.findViewById(R.id.cbContinuous);
		et_between = (EditText) mContext.findViewById(R.id.et_between);
		tv_result = (TextView) mContext.findViewById(R.id.tv_result);
		tv_scan_count = (TextView) mContext.findViewById(R.id.tv_scan_count);
		tv_succ_count = (TextView) mContext.findViewById(R.id.tv_succ_count);
		spType = (Spinner) mContext.findViewById(R.id.spType);
		svResult = (ScrollView) mContext.findViewById(R.id.svResult);

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);

				if (msg.arg1 == 1) {
					// 成功

					switch (msg.arg2) {
					case 0:
					case 2:
					case 4:
						tv_result.append(spType.getSelectedItem() + ":");
						tv_result.append(msg.obj.toString().replace("-", ""));
						tv_result.append("\r\n");
						break;
					case 1:
					case 3:
						AnimalEntity entity = (AnimalEntity) msg.obj;
						tv_result.append("Country ID:" + entity.getCountryID());
						tv_result.append("\r\n");

						tv_result.append("National ID:"
								+ String.format("%1$012d",
										entity.getNationalID()));
						tv_result.append("\r\n");
						tv_result.append("Reserved:" + entity.getReserved());
						tv_result.append("\r\n");
						break;

					}
					sussCount++;

					mContext.playSound(1);

				} else {
					failCount++;
					tv_result.append(getString(R.string.lf_msg_scan_fail));
					tv_result.append("\r\n");

					mContext.playSound(2);
				}

				mContext.scrollToBottom(svResult, tv_result);

				stat();
			}

		};

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

	}

	private class ScanThread extends Thread {
		boolean mIsAuto;
		int mTime;
		int mTagType;
		boolean threadStop = true;

		public ScanThread(boolean isAuto, int time, int tagType) {
			mIsAuto = isAuto;
			mTime = time;
			mTagType = tagType;
			threadStop = false;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();

			do {
				Message msg = mHandler.obtainMessage();
				Object result = null;

				switch (mTagType) {
				case 0:
					result = mContext.mLF.readDataWithIDCard(0);

					break;
				case 1:
					result = mContext.mLF.readAnimalTags(1);

					break;
				case 2:
					result = mContext.mLF.getUIDWithHitagS();
					break;
				case 3:
					result = mContext.mLF.readAnimalTags(2);
					break;
				case 4:
					result = mContext.mLF.getUIDWith4450Card();
					break;

				default:
					break;
				}

				if (result == null) {

					msg.arg1 = 0;
					msg.arg2 = mTagType;
				} else {
					msg.arg1 = 1;
					msg.arg2 = mTagType;
					msg.obj = result;

				}
				mHandler.sendMessage(msg);

				if (mIsAuto) {
					try {
						sleep(mTime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} while (mIsAuto && !threadStop);

			threadStop = true;
		}

		public boolean isStop() {
			return threadStop;
		}

		public void cancel() {
			threadStop = true;
		}

	}

	public void myOnKeyDwon() {
		scan();
	}

	public void scan() {
		boolean bContinuous = false;

		int iBetween = 0;

		bContinuous = cbContinuous.isChecked();

		if (scanThread == null || !bContinuous || scanThread.isStop()) {
			if (bContinuous) {
				btn_Start.setText(getString(R.string.title_stop));

				String strBetween = et_between.getText().toString().trim();

				iBetween = StringUtils.toInt(strBetween, 0);// 毫秒

				btn_Clear.setEnabled(false);

			}

			scanThread = new ScanThread(bContinuous, iBetween,
					spType.getSelectedItemPosition());
			scanThread.start();

		} else {
			btn_Start.setText(getString(R.string.title_scan));
			scanThread.cancel();

			btn_Clear.setEnabled(true);
		}
	}

	private void stat() {
		int total = sussCount + failCount;

		if (total > 0) {
			tv_scan_count.setText(String.valueOf(total));
			tv_succ_count.setText(String.valueOf(sussCount));
		}
	}

	private void clear() {
		tv_result.setText("");

		int total = 0;
		sussCount = 0;
		failCount = 0;

		tv_scan_count.setText(String.valueOf(total));
		tv_succ_count.setText(String.valueOf(sussCount));

		btn_Start.setText(getString(R.string.title_scan));

	}

}
