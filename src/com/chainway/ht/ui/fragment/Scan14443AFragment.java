package com.chainway.ht.ui.fragment;

import com.chainway.deviceapi.entity.SimpleRFIDEntity;
import com.chainway.ht.R;
import com.chainway.ht.ui.A14443Activity;
import com.chainway.ht.utils.StringUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class Scan14443AFragment extends Fragment {

	private A14443Activity mContext;

	private Button btn_Start;
	private TextView tv_Result;

	private Button btn_Clear;
	private TextView tv_succ_count;
	private TextView tv_scan_count;
	private CheckBox cbContinuous;
	private EditText et_between;

	private Handler handler;
	private Thread thread;
	private ScrollView svResult;

	int sussCount = 0;
	int failCount = 0;

	private boolean threadStop = true;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mContext = (A14443Activity) getActivity();

		init();

	}

	private void init() {
		tv_Result = (TextView) mContext.findViewById(R.id.tv_result);
		btn_Start = (Button) mContext.findViewById(R.id.btn_Start);
		et_between = (EditText) mContext.findViewById(R.id.et_between);
		svResult = (ScrollView) mContext.findViewById(R.id.svResult);
		cbContinuous = (CheckBox) mContext.findViewById(R.id.cbContinuous);
		tv_scan_count = (TextView) mContext.findViewById(R.id.tv_scan_count);
		tv_succ_count = (TextView) mContext.findViewById(R.id.tv_succ_count);
		btn_Clear = (Button) mContext.findViewById(R.id.btn_Clear);

		btn_Start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				scan();
			}
		});

		btn_Clear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				clear();
			}
		});

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if (msg != null) {
					String strData = "";

					if ((sussCount + failCount) % 1000 == 0) {
						tv_Result.setText("");
					}

					switch (msg.arg1) {
					case 0:
						failCount += 1;
						// tv_Result
						// .append(getString(R.string.rfid_msg_scan_fail));
						// tv_Result.append("\n");

						strData = getString(R.string.rfid_msg_scan_fail) + "\n";
						mContext.playSound(2);
						break;
					case 1:

						sussCount += 1;

						strData = msg.obj.toString() + "\n";
						// tv_Result.append(msg.obj.toString() + "\n");
						mContext.playSound(1);
						break;

					default:
						failCount += 1;
						mContext.playSound(2);
						break;
					}

					tv_Result.append(strData);
					mContext.getAppContext().a14443Queue.add(strData);

					scrollToBottom(svResult, tv_Result);
					stat();
				}
			}

		};

	}

	public void myOnKeyDwon() {
		scan();
	}

	/**
	 * 将ScrollView根据内部控件高度定位到底端
	 * 
	 * @param scroll
	 * @param inner
	 */
	public void scrollToBottom(final View scroll, final View inner) {

		Handler mHandler = new Handler();

		mHandler.post(new Runnable() {
			public void run() {
				if (scroll == null || inner == null) {
					return;
				}
				int offset = inner.getMeasuredHeight() - scroll.getHeight();
				if (offset < 0) {
					offset = 0;
				}

				scroll.scrollTo(0, offset);
			}
		});
	}

	public void scan() {
		if (threadStop) {

			boolean bContinuous = false;

			int iBetween = 0;

			// if (!isEtInit)// 初始条码框没有焦点
			// {
			bContinuous = cbContinuous.isChecked();
			if (bContinuous) {
				btn_Start.setText(getString(R.string.title_stop));
				threadStop = false;

				String strBetween = et_between.getText().toString().trim();
				if (StringUtils.isEmpty(strBetween)) {

				} else {
					iBetween = StringUtils.toInt(strBetween);// 毫秒
				}

				btn_Clear.setEnabled(false);
			}

			thread = new Thread(new ScanRunnable(bContinuous, iBetween));
			thread.start();

		} else {
			btn_Start.setText(getString(R.string.title_scan));
			threadStop = true;

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
		tv_Result.setText("");

		int total = 0;
		sussCount = 0;
		failCount = 0;

		tv_scan_count.setText(String.valueOf(total));
		tv_succ_count.setText(String.valueOf(sussCount));

		btn_Start.setText(getString(R.string.title_scan));
		threadStop = true;

	}

	/**
	 * 扫描线程实现类
	 * 
	 * @author liuruifeng
	 * 
	 */
	class ScanRunnable implements Runnable {

		private boolean isContinuous = false;
		String result = "";
		private long sleepTime = 1000;
		Message msg = null;

		public ScanRunnable(boolean isContinuous) {
			this.isContinuous = isContinuous;
		}

		public ScanRunnable(boolean isContinuous, int sleep) {
			this.isContinuous = isContinuous;
			this.sleepTime = sleep;
		}

		@Override
		public void run() {

			do {
				msg = new Message();
				SimpleRFIDEntity entity = null;
				try {
					entity = mContext.mRFID.request();
				} catch (Exception e1) {
					msg.arg1 = 0;
					msg.obj = "";
				}

				if (entity == null) {
					msg.arg1 = 0;
					msg.obj = "";

				} else {

					msg.arg1 = 1;

					msg.obj = entity.toString();
				}

				handler.sendMessage(msg);

				if (isContinuous) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			} while (isContinuous && !threadStop);

		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.scan1443a_fragment, container, false);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		btn_Start.setText(getString(R.string.title_scan));
		threadStop = true;

		btn_Clear.setEnabled(true);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

}
