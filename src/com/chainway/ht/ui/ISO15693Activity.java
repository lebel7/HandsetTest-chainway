package com.chainway.ht.ui;

import java.util.ArrayList;

import com.chainway.deviceapi.RFIDWithISO14443A;
import com.chainway.deviceapi.RFIDWithISO15693;
import com.chainway.deviceapi.entity.ISO15693Entity;
import com.chainway.deviceapi.entity.SimpleRFIDEntity;
import com.chainway.deviceapi.exception.ConfigurationException;
import com.chainway.deviceapi.exception.RFIDNotFoundException;
import com.chainway.deviceapi.exception.RFIDReadFailureException;
import com.chainway.ht.AppConfig;
import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.R.layout;
import com.chainway.ht.R.menu;
import com.chainway.ht.ui.fragment.M1Fragment;
import com.chainway.ht.ui.fragment.Scan14443AFragment;
import com.chainway.ht.ui.fragment.UltralightFragment;
import com.chainway.ht.utils.StringUtils;
import com.chainway.utility.StringUtility;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class ISO15693Activity extends BaseActivity {
	RFIDWithISO15693 mRFID;
	boolean bResult = false;

	private Spinner spBlock;
	private Button btnRead;
	private Button btnReadId;
	private Button btnWrite;
	private TextView tvResult;
	private EditText etWriteData;
	private EditText etAFI;
	private EditText etDSFID;
	private Button btnWriteAFI;
	private Button btnWriteDSFID;
	private Button btnLockAFI;
	private Button btnLockDSFID;
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
					ISO15693Entity entity = (ISO15693Entity) msg.obj;

					tvResult.append(getString(R.string.rfid_msg_uid) + " "
							+ entity.getId());
					if (entity.getType().length() > 0) {
						tvResult.append(getString(R.string.rfid_msg_type) + " "
								+ entity.getType());
					}

					tvResult.append(getString(R.string.rfid_msg_data) + " "
							+ entity.getData());

					tvResult.append("\n==============================\n");

					appContext.playSound(1);
				}
					break;
				case 0: {
					readFailCount++;
					tvResult.append(msg.obj.toString());
					tvResult.append("\n==============================\n");
					appContext.playSound(2);
				}
					break;
				}

				statContinuous();
				scrollToBottom(svResult, tvResult);
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

					tvResult.append(getString(R.string.rfid_msg_write_succ));
					tvResult.append("\n==============================\n");

					appContext.playSound(1);
				}
					break;
				case 0: {
					writeFailCount++;
					tvResult.append(msg.obj.toString());
					tvResult.append("\n==============================\n");

					appContext.playSound(2);
				}
					break;
				}

				statContinuous();

				scrollToBottom(svResult, tvResult);

			}

		}
	};

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
			ISO15693Entity entity = null;

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
					entity = mRFID.read(block);

					if (entity == null) {
						msg.arg1 = 0;
						msg.obj = getText(R.string.rfid_mgs_error_not_found);

						readHandler.sendMessage(msg);
						continue;
					} else {
						msg.arg1 = 1;
						msg.obj = entity;

						readHandler.sendMessage(msg);
					}

				} catch (RFIDReadFailureException e) {
					msg.arg1 = 0;
					msg.obj = getText(R.string.rfid_msg_read_fail);

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

				boolean bResult = false;

				try {
					bResult = mRFID.write(block, strData);
				} catch (RFIDNotFoundException e) {
					msg.arg1 = 0;
					msg.obj = getText(R.string.rfid_mgs_error_not_found);

					writeHandler.sendMessage(msg);
					continue;
				}

				if (bResult) {
					// 写入成功
					msg.arg1 = 1;
					msg.obj = getText(R.string.rfid_msg_write_succ);

					writeHandler.sendMessage(msg);
					continue;
				} else {
					msg.arg1 = 0;
					msg.obj = getText(R.string.rfid_msg_write_fail);

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
		rRead = (RadioButton) findViewById(R.id.rRead);
		rWrite = (RadioButton) findViewById(R.id.rWrite);
		rReadWrite = (RadioButton) findViewById(R.id.rReadWrite);
		rgReadWrite = (RadioGroup) findViewById(R.id.rgReadWrite);

		et_between = (EditText) findViewById(R.id.et_between);
		btnStart = (Button) findViewById(R.id.btnStart);
		btnClear = (Button) findViewById(R.id.btnClear);
		btnBack = (Button) findViewById(R.id.btnBack);

		tv_read_succ_count = (TextView) findViewById(R.id.tv_read_succ_count);
		tv_read_fail_count = (TextView) findViewById(R.id.tv_read_fail_count);
		tv_write_succ_count = (TextView) findViewById(R.id.tv_write_succ_count);
		tv_write_fail_count = (TextView) findViewById(R.id.tv_write_fail_count);
		tv_continuous_count = (TextView) findViewById(R.id.tv_continuous_count);

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
				llMultiple.setAnimation(AnimationUtils.loadAnimation(
						ISO15693Activity.this, android.R.anim.slide_out_right));
				llSingle.setVisibility(View.VISIBLE);
				llSingle.setAnimation(AnimationUtils.loadAnimation(
						ISO15693Activity.this, android.R.anim.slide_in_left));

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

						if (strData.length() == 0) {
							// 写入内容不能为空
							tvResult.setText(R.string.rfid_mgs_error_not_write_null);
							return;
						} else if (!vailHexInput(strData)) {
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_iso15693);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		try {
			mRFID = RFIDWithISO15693.getInstance();
		} catch (ConfigurationException e) {
			UIHelper.ToastMessage(ISO15693Activity.this,
					R.string.rfid_mgs_error_config);
			return;
		}

		bResult = mRFID.init();
		if (!bResult) {
			UIHelper.ToastMessage(ISO15693Activity.this,
					R.string.rfid_mgs_error_init);
		}

		init();
		initData();
		initContinuous();
	}

	private void init() {
		svResult = (ScrollView) findViewById(R.id.svResult);

		spBlock = (Spinner) findViewById(R.id.spBlock);
		btnRead = (Button) findViewById(R.id.btnRead);
		btnReadId = (Button) findViewById(R.id.btnReadId);
		btnWrite = (Button) findViewById(R.id.btnWrite);
		tvResult = (TextView) findViewById(R.id.tvResult);
		etWriteData = (EditText) findViewById(R.id.etWriteData);
		etAFI = (EditText) findViewById(R.id.etAFI);
		etDSFID = (EditText) findViewById(R.id.etDSFID);
		btnWriteAFI = (Button) findViewById(R.id.btnWriteAFI);
		btnWriteDSFID = (Button) findViewById(R.id.btnWriteDSFID);
		btnLockAFI = (Button) findViewById(R.id.btnLockAFI);
		btnLockDSFID = (Button) findViewById(R.id.btnLockDSFID);

		llSingle = (LinearLayout) findViewById(R.id.llSingle);
		llMultiple = (LinearLayout) findViewById(R.id.llMultiple);
		btnContinuous = (Button) findViewById(R.id.btnContinuous);

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
				scan();

			}
		});
		btnWriteAFI.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				writeAFI();

			}
		});
		btnWriteDSFID.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				writeDSFID();

			}
		});

		btnLockAFI.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				lockAFI();

			}
		});

		btnLockDSFID.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				lockDSFID();

			}
		});

		btnContinuous.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				llSingle.setVisibility(View.GONE);
				llSingle.setAnimation(AnimationUtils.loadAnimation(
						ISO15693Activity.this, android.R.anim.slide_out_right));
				llMultiple.setVisibility(View.VISIBLE);
				llMultiple.setAnimation(AnimationUtils.loadAnimation(
						ISO15693Activity.this, android.R.anim.slide_in_left));

				resetContinuous();

			}
		});

	}

	public void writeAFI() {
		String afi = etAFI.getText().toString();

		if (afi.length() != 2) {
			tvResult.setText(R.string.rfid_msg_1byte_fail);
			return;
		} else if (!vailHexInput(afi)) {
			// 请输入十六进制数
			tvResult.setText(R.string.rfid_mgs_error_nohex);
			return;
		}

		boolean bResult = false;
		try {
			bResult = mRFID.writeAFI(Integer.parseInt(afi));
		} catch (NumberFormatException e) {
			tvResult.setText(R.string.rfid_msg_1byte_fail);
			return;
		} catch (RFIDNotFoundException e) {
			tvResult.setText(R.string.rfid_mgs_error_not_found);
			return;
		}

		if (bResult) {
			// 写入成功
			tvResult.setText(R.string.rfid_msg_write_succ);

			appContext.playSound(1);

		} else {
			tvResult.setText(R.string.rfid_msg_write_fail);
			appContext.playSound(2);
		}
	}

	public void lockAFI() {
		new AlertDialog.Builder(ISO15693Activity.this)
				.setTitle(R.string.rfid_msg_confirm_title)
				.setMessage(R.string.rfid_msg_confirm_afi)
				.setPositiveButton(R.string.rfid_msg_confirm_true,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								boolean bResult = false;
								try {
									bResult = mRFID.lockAFI();

								} catch (NumberFormatException e) {
									tvResult.setText(R.string.rfid_msg_1byte_fail);
									return;
								} catch (RFIDNotFoundException e) {
									tvResult.setText(R.string.rfid_mgs_error_not_found);
									return;
								}

								if (bResult) {
									// 锁定成功
									tvResult.setText(R.string.rfid_msg_lock_succ);

									appContext.playSound(1);
								} else {
									tvResult.setText(R.string.rfid_msg_lock_fail);
									appContext.playSound(2);
								}

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

	public void writeDSFID() {
		String dsfid = etDSFID.getText().toString();

		if (dsfid.length() != 2) {
			tvResult.setText(R.string.rfid_msg_1byte_fail);
			return;
		} else if (!vailHexInput(dsfid)) {
			// 请输入十六进制数
			tvResult.setText(R.string.rfid_mgs_error_nohex);
			return;
		}

		boolean bResult = false;
		try {
			bResult = mRFID.writeDSFID(Integer.parseInt(dsfid));
		} catch (NumberFormatException e) {
			tvResult.setText(R.string.rfid_msg_1byte_fail);
			return;
		} catch (RFIDNotFoundException e) {
			tvResult.setText(R.string.rfid_mgs_error_not_found);
			return;
		}

		if (bResult) {
			// 写入成功
			tvResult.setText(R.string.rfid_msg_write_succ);

			appContext.playSound(1);

		} else {
			tvResult.setText(R.string.rfid_msg_write_fail);
			appContext.playSound(2);
		}
	}

	public void lockDSFID() {

		new AlertDialog.Builder(ISO15693Activity.this)
				.setTitle(R.string.rfid_msg_confirm_title)
				.setMessage(R.string.rfid_msg_confirm_dsfid)
				.setPositiveButton(R.string.rfid_msg_confirm_true,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								boolean bResult = false;
								try {
									bResult = mRFID.lockDSFID();

								} catch (NumberFormatException e) {
									tvResult.setText(R.string.rfid_msg_1byte_fail);
									return;
								} catch (RFIDNotFoundException e) {
									tvResult.setText(R.string.rfid_mgs_error_not_found);
									return;
								}

								if (bResult) {
									// 锁定成功
									tvResult.setText(R.string.rfid_msg_lock_succ);

									appContext.playSound(1);
								} else {
									tvResult.setText(R.string.rfid_msg_lock_fail);

									appContext.playSound(2);
								}

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

	public void readTag() {
		ISO15693Entity entity = null;

		try {
			entity = mRFID.read(spBlock.getSelectedItemPosition());

			if (entity == null) {
				tvResult.append(getString(R.string.rfid_mgs_error_not_found)
						+ "\n");
				appContext.playSound(2);
				return;
			}

		} catch (RFIDReadFailureException e) {
			tvResult.append(getString(R.string.rfid_msg_read_fail) + "\n");
			appContext.playSound(2);
			return;
		}

		tvResult.setText("");
		tvResult.append(getString(R.string.rfid_msg_uid) + " " + entity.getId());
		if (entity.getType().length() > 0) {
			tvResult.append(getString(R.string.rfid_msg_type) + " "
					+ entity.getType());
		}

		tvResult.append(getString(R.string.rfid_msg_data) + " "
				+ entity.getData());

		appContext.playSound(1);

	}

	private void writeTag() {
		String strData = etWriteData.getText().toString();

		if (strData.length() == 0) {
			// 写入内容不能为空
			tvResult.setText(R.string.rfid_mgs_error_not_write_null);
			return;
		} else if (!vailHexInput(strData)) {
			// 请输入十六进制数
			tvResult.setText(R.string.rfid_mgs_error_nohex);
			return;
		}

		int block = spBlock.getSelectedItemPosition();

		boolean bResult = false;

		try {
			bResult = mRFID.write(block, strData);
		} catch (RFIDNotFoundException e) {
			tvResult.setText(R.string.rfid_mgs_error_not_found);
			return;
		}

		if (bResult) {
			// 写入成功
			tvResult.setText(R.string.rfid_msg_write_succ);

			appContext.playSound(1);
		} else {
			tvResult.setText(R.string.rfid_msg_write_fail);
			appContext.playSound(2);
		}

	}

	private void scan() {
		ISO15693Entity entity = null;

		entity = mRFID.inventory();

		if (entity == null) {
			tvResult.append(getString(R.string.rfid_mgs_error_not_found));
			tvResult.append("\n============\n");
			scrollToBottom(svResult, tvResult);
			appContext.playSound(2);
			return;
		}

		tvResult.append(getString(R.string.rfid_msg_uid) + " " + entity.getId());
		if (entity.getType().length() > 0) {
			tvResult.append(getString(R.string.rfid_msg_type) + " "
					+ entity.getType());
		}

		if (entity.getAFI().length() > 0) {
			tvResult.append("\nAFI:" + entity.getAFI());
		}

		if (entity.getDESFID().length() > 0) {
			tvResult.append("\nDESFID:" + entity.getDESFID());
		}

		tvResult.append("\n============\n");

		appContext.playSound(1);

		scrollToBottom(svResult, tvResult);
	}

	private void initData() {

		// adapterBlock
		arrBlock.clear();
		arrBlock.addAll(builNum(28));

		adapterBlock = new ArrayAdapter<String>(ISO15693Activity.this,
				android.R.layout.simple_spinner_item, arrBlock);
		adapterBlock
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spBlock.setAdapter(adapterBlock);
	}

	public ArrayList<String> builNum(int count) {
		if (count < 1) {
			return null;

		}

		ArrayList<String> arrStr = new ArrayList<String>();

		for (int i = 0; i < count; i++) {
			arrStr.add(String.valueOf(i));

		}
		return arrStr;
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
	protected void onDestroy() {
		super.onDestroy();

		mRFID.free();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;

		default:
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == 140) {
			if (event.getRepeatCount() == 0) {
				scan();
			}

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}
