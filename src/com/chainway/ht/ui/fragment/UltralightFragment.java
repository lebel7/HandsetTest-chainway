package com.chainway.ht.ui.fragment;

import java.util.ArrayList;

import com.chainway.deviceapi.RFIDWithISO14443A;
import com.chainway.deviceapi.entity.SimpleRFIDEntity;
import com.chainway.deviceapi.exception.RFIDNotFoundException;
import com.chainway.deviceapi.exception.RFIDReadFailureException;
import com.chainway.deviceapi.exception.RFIDVerificationException;
import com.chainway.ht.R;
import com.chainway.ht.ui.A14443Activity;
import com.chainway.ht.ui.fragment.M1Fragment.ReadRunnable;
import com.chainway.ht.ui.fragment.M1Fragment.SectorSpinnerSelectedListener;
import com.chainway.ht.ui.fragment.M1Fragment.TagTypeSpinnerSelectedListener;
import com.chainway.ht.ui.fragment.M1Fragment.WriteRunnable;
import com.chainway.ht.utils.StringUtils;
import com.chainway.utility.StringUtility;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class UltralightFragment extends Fragment {

	private static final String TAG = "UltralightFragment";

	private A14443Activity mContext;

	private Spinner spBlock;
	private Button btnRead;
	private Button btnReadId;
	private Button btnWrite;
	private TextView tvResult;
	private EditText etWriteData;

	private ArrayAdapter adapterBlock;
	private ArrayList<String> arrBlock = new ArrayList<String>();

	private Button btnContinuous;
	private LinearLayout llSingle;
	private ScrollView svResult;

	// 连续操作变量START
	private LinearLayout llMultiple;

	private int readSuccCount = 0;
	private int readFailCount = 0;
	private int writeSuccCount = 0;
	private int writeFailCount = 0;

	private RadioButton rRead;
	private RadioButton rWrite;
	private RadioButton rReadWrite;
	private RadioGroup rgReadWrite;

	private EditText et_between;
	private Button btnStart;
	private Button btnClear;
	private Button btnBack;

	private TextView tv_read_succ_count;
	private TextView tv_read_fail_count;
	private TextView tv_write_succ_count;
	private TextView tv_write_fail_count;
	private TextView tv_continuous_count;
	private boolean threadStop = true;
	private Thread readThread;
	private Thread writeThread;
	private boolean isContinuous = true;

	// 连续操作变量END

	/**
	 * 读Handler
	 */
	private Handler readHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			if (msg != null && !threadStop) {
				switch (msg.arg1) {
				case 1: {
					readSuccCount++;
					SimpleRFIDEntity entity = (SimpleRFIDEntity) msg.obj;

					tvResult.append(mContext.getString(R.string.rfid_msg_uid)
							+ " " + entity.getId());
					tvResult.append(mContext.getString(R.string.rfid_msg_data)
							+ " " + entity.getData());

					tvResult.append(mContext
							.getString(R.string.rfid_msg_read_succ) + "!");
					tvResult.append("\n==============================\n");
					mContext.playSound(1);
				}
					break;
				case 0: {
					readFailCount++;
					tvResult.append(msg.obj.toString());
					tvResult.append("\n==============================\n");
					mContext.playSound(2);
				}
					break;
				}

				statContinuous();
				mContext.scrollToBottom(svResult, tvResult);
			}

		}
	};

	/**
	 * 写Handler
	 */
	private Handler writeHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			if (msg != null && !threadStop) {
				switch (msg.arg1) {
				case 1: {
					writeSuccCount++;

					tvResult.append(mContext
							.getString(R.string.rfid_msg_write_succ));
					tvResult.append("\n==============================\n");

					mContext.playSound(1);
				}
					break;
				case 0: {
					writeFailCount++;
					tvResult.append(msg.obj.toString());
					tvResult.append("\n==============================\n");
					mContext.playSound(2);
				}
					break;
				}

				statContinuous();

				mContext.scrollToBottom(svResult, tvResult);

			}

		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mContext = (A14443Activity) getActivity();

		init();
		initData();
		initContinuous();
	}

	private void init() {
		svResult = (ScrollView) mContext.findViewById(R.id.svResult);

		spBlock = (Spinner) mContext.findViewById(R.id.spBlock);
		btnRead = (Button) mContext.findViewById(R.id.btnRead);
		btnReadId = (Button) mContext.findViewById(R.id.btnReadId);
		btnWrite = (Button) mContext.findViewById(R.id.btnWrite);
		tvResult = (TextView) mContext.findViewById(R.id.tvResult);
		etWriteData = (EditText) mContext.findViewById(R.id.etWriteData);
		btnContinuous = (Button) mContext.findViewById(R.id.btnContinuous);
		llSingle = (LinearLayout) mContext.findViewById(R.id.llSingle);
		llMultiple = (LinearLayout) mContext.findViewById(R.id.llMultiple);

		btnRead.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				readTag();

			}
		});

		btnWrite.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				writeTag();

			}
		});

		btnReadId.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				readTagId();

			}
		});

		btnContinuous.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				llSingle.setVisibility(View.GONE);
				llSingle.setAnimation(AnimationUtils.loadAnimation(mContext,
						android.R.anim.slide_out_right));
				llMultiple.setVisibility(View.VISIBLE);
				llMultiple.setAnimation(AnimationUtils.loadAnimation(mContext,
						android.R.anim.slide_in_left));

				resetContinuous();

			}
		});

	}

	private void initData() {

		// adapterBlock
		arrBlock.clear();
		arrBlock.addAll(mContext.builNum(16));

		adapterBlock = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, arrBlock);
		adapterBlock
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spBlock.setAdapter(adapterBlock);

		spBlock.setSelection(4);
	}

	/**
	 * 读线程
	 * 
	 * @author liuruifeng
	 * 
	 */
	class ReadRunnable implements Runnable {
		private boolean isContinuous = false;
		private long sleepTime = 1000;
		private int block;
		Message msg = null;

		public ReadRunnable(boolean isContinuous, int sleep, int block) {
			this.isContinuous = isContinuous;
			this.sleepTime = sleep;
			this.block = block;
		}

		@Override
		public void run() {
			SimpleRFIDEntity entity = null;

			do {

				msg = new Message();

				if (isContinuous) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				try {
					entity = mContext.mRFID.read(block);

					if (entity == null) {
						msg.arg1 = 0;
						msg.obj = mContext
								.getText(R.string.rfid_mgs_error_not_found);

						readHandler.sendMessage(msg);
						continue;
					} else {
						msg.arg1 = 1;
						msg.obj = entity;
						readHandler.sendMessage(msg);
					}

				} catch (RFIDReadFailureException e) {

					msg.arg1 = 0;
					msg.obj = mContext.getText(R.string.rfid_msg_read_fail);

					readHandler.sendMessage(msg);
					continue;
				}
			} while (isContinuous && !threadStop);
		}

	}

	/**
	 * 写线程
	 * 
	 * @author liuruifeng
	 * 
	 */
	class WriteRunnable implements Runnable {

		private boolean isContinuous = false;
		private long sleepTime = 1000;
		private int block;
		String strData;
		Message msg = null;

		public WriteRunnable(boolean isContinuous, int sleep, int block,
				String data) {
			this.isContinuous = isContinuous;
			this.sleepTime = sleep;
			this.block = block;
			this.strData = data;
		}

		@Override
		public void run() {

			SimpleRFIDEntity entity = null;

			do {
				msg = new Message();
				if (isContinuous) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				try {
					if (mContext.mRFID.write(block, strData)) {
						msg.arg1 = 1;
						msg.obj = mContext
								.getText(R.string.rfid_msg_write_succ);

						writeHandler.sendMessage(msg);
						continue;
					} else {
						msg.arg1 = 0;
						msg.obj = mContext
								.getText(R.string.rfid_msg_write_fail);

						writeHandler.sendMessage(msg);
						continue;
					}
				} catch (RFIDNotFoundException e) {
					msg.arg1 = 0;
					msg.obj = mContext
							.getText(R.string.rfid_mgs_error_not_found);

					writeHandler.sendMessage(msg);
					continue;
				}
			} while (isContinuous && !threadStop);

		}

	}

	/**
	 * 初始化连续读写控件变量
	 */
	private void initContinuous() {
		rRead = (RadioButton) mContext.findViewById(R.id.rRead);
		rWrite = (RadioButton) mContext.findViewById(R.id.rWrite);
		rReadWrite = (RadioButton) mContext.findViewById(R.id.rReadWrite);
		rgReadWrite = (RadioGroup) mContext.findViewById(R.id.rgReadWrite);

		et_between = (EditText) mContext.findViewById(R.id.et_between);
		btnStart = (Button) mContext.findViewById(R.id.btnStart);
		btnClear = (Button) mContext.findViewById(R.id.btnClear);
		btnBack = (Button) mContext.findViewById(R.id.btnBack);

		tv_read_succ_count = (TextView) mContext
				.findViewById(R.id.tv_read_succ_count);
		tv_read_fail_count = (TextView) mContext
				.findViewById(R.id.tv_read_fail_count);
		tv_write_succ_count = (TextView) mContext
				.findViewById(R.id.tv_write_succ_count);
		tv_write_fail_count = (TextView) mContext
				.findViewById(R.id.tv_write_fail_count);
		tv_continuous_count = (TextView) mContext
				.findViewById(R.id.tv_continuous_count);

		readSuccCount = 0;
		readFailCount = 0;
		writeSuccCount = 0;
		writeFailCount = 0;

		btnClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				resetContinuous();

			}
		});

		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				llMultiple.setVisibility(View.GONE);
				llMultiple.setAnimation(AnimationUtils.loadAnimation(mContext,
						android.R.anim.slide_out_right));
				llSingle.setVisibility(View.VISIBLE);
				llSingle.setAnimation(AnimationUtils.loadAnimation(mContext,
						android.R.anim.slide_in_left));

				resetContinuous();
			}
		});

		btnStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				btnClear.setEnabled(!threadStop);

				if (threadStop) {

					threadStop = false;

					btnStart.setText(R.string.title_stop);

					int sleep = 0;// 间隔时间

					String strBetween = et_between.getText().toString().trim();
					if (StringUtils.isEmpty(strBetween)) {

					} else {
						sleep = StringUtils.toInt(strBetween);// 毫秒
					}

					int block = Integer.parseInt(spBlock.getSelectedItem()
							.toString());

					// 判断读写类型。。
					if (rRead.isChecked()) {
						readThread = new Thread(new ReadRunnable(isContinuous,
								sleep, block));

						readThread.start();

					} else {

						String strData = etWriteData.getText().toString();

						Log.d(TAG,
								"isHexNumber="
										+ StringUtility.isHexNumberRex(strData));

						if (strData.length() == 0) {
							// 写入内容不能为空
							tvResult.setText(R.string.rfid_mgs_error_not_write_null);
							return;
						} else if (!mContext.vailHexInput(strData)) {
							// 请输入十六进制数
							tvResult.setText(R.string.rfid_mgs_error_nohex);
							return;
						}

						if (rWrite.isChecked()) {

							writeThread = new Thread(new WriteRunnable(
									isContinuous, sleep, block, strData));

							writeThread.start();

						} else if (rReadWrite.isChecked()) {
							readThread = new Thread(new ReadRunnable(
									isContinuous, sleep, block));

							writeThread = new Thread(new WriteRunnable(
									isContinuous, sleep, block, strData));

							readThread.start();
							writeThread.start();

						}
					}

				} else {
					threadStop = true;
					btnStart.setText(R.string.title_start);
				}

			}
		});

		// 连续操作方式改变时
		rgReadWrite.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				threadStop = true;
				btnStart.setText(R.string.title_start);
				resetContinuous();
			}
		});

	}

	/**
	 * 重置连续读写内容
	 */
	private void resetContinuous() {
		readSuccCount = 0;
		readFailCount = 0;
		writeSuccCount = 0;
		writeFailCount = 0;

		threadStop = true;
		tvResult.setText("");

		tv_continuous_count.setText("0");
		tv_read_fail_count.setText("0");
		tv_read_succ_count.setText("0");
		tv_write_fail_count.setText("0");
		tv_write_succ_count.setText("0");
		btnStart.setText(getString(R.string.title_start));
		btnClear.setEnabled(true);

	}

	/**
	 * 统计相关数据
	 */
	private void statContinuous() {
		int total = readSuccCount + readFailCount + writeSuccCount
				+ writeFailCount;

		if (total % 1000 == 0) {
			tvResult.setText("");
		}

		tv_continuous_count.setText(String.valueOf(total));
		tv_read_succ_count.setText(String.valueOf(readSuccCount));
		tv_read_fail_count.setText(String.valueOf(readFailCount));
		tv_write_succ_count.setText(String.valueOf(writeSuccCount));
		tv_write_fail_count.setText(String.valueOf(writeFailCount));
	}

	/**
	 * 写卡
	 */
	private void writeTag() {
		String strData = etWriteData.getText().toString();
		Log.d(TAG, "isHexNumber=" + StringUtility.isHexNumberRex(strData));
		if (strData.length() == 0) {
			// 写入内容不能为空
			tvResult.setText(R.string.rfid_mgs_error_not_write_null);
			return;
		} else if (!mContext.vailHexInput(strData)) {
			// 请输入十六进制数
			tvResult.setText(R.string.rfid_mgs_error_nohex);
			return;
		}
		int block = Integer.parseInt(spBlock.getSelectedItem().toString());

		try {
			if (mContext.mRFID.write(block, strData)) {
				tvResult.setText(R.string.rfid_msg_write_succ);
				mContext.playSound(1);
				return;
			} else {
				tvResult.setText(R.string.rfid_msg_write_fail);
				mContext.playSound(2);
				return;
			}
		} catch (RFIDNotFoundException e) {
			tvResult.setText(R.string.rfid_mgs_error_not_found);
			return;
		}

	}

	/**
	 * 读卡
	 */
	private void readTag() {
		int block = Integer.parseInt(spBlock.getSelectedItem().toString());
		SimpleRFIDEntity entity = null;
		try {
			entity = mContext.mRFID.read(block);

			if (entity == null) {
				tvResult.setText(R.string.rfid_mgs_error_not_found);
				mContext.playSound(2);
				return;
			}
		} catch (RFIDReadFailureException e) {
			tvResult.setText(R.string.rfid_msg_read_fail);
			mContext.playSound(2);
			return;
		}

		tvResult.setText("");
		tvResult.append(mContext.getString(R.string.rfid_msg_uid) + " "
				+ entity.getId());
		tvResult.append(mContext.getString(R.string.rfid_msg_data) + " "
				+ entity.getData());
		mContext.playSound(1);
	}

	/**
	 * 读卡Id
	 */
	private void readTagId() {
		SimpleRFIDEntity entity = null;
		entity = mContext.mRFID.request();

		if (entity == null) {
			tvResult.setText(R.string.rfid_mgs_error_not_found);
			mContext.playSound(2);
			return;
		}

		tvResult.setText("");
		tvResult.append(mContext.getString(R.string.rfid_msg_uid) + " "
				+ entity.getId());

		mContext.playSound(1);
	}

	public void myOnKeyDwon() {
		readTagId();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		btnStart.setText(getString(R.string.title_start));
		threadStop = true;

		btnClear.setEnabled(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.ultralight_fragment, container, false);
	}
}
