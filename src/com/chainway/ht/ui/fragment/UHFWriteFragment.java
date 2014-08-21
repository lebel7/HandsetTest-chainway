package com.chainway.ht.ui.fragment;

import com.chainway.deviceapi.UHFWithRLM.BankEnum;
import com.chainway.deviceapi.UHFWithRLM.UHFCrcFlagEnum;
import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.ui.UHFMainActivity;
import com.chainway.ht.utils.StringUtils;
import com.chainway.utility.StringUtility;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class UHFWriteFragment extends Fragment {

	private static final String TAG = "UHFWriteFragment";

	private UHFMainActivity mContext;

	CheckBox CkWithUii_Write;
	EditText EtTagUii_Write;
	Spinner SpinnerBank_Write;
	EditText EtPtr_Write;
	EditText EtLen_Write;
	EditText EtData_Write;
	EditText EtAccessPwd_Write;
	Spinner SpinnerOption_Write;
	Button BtUii_Write;
	Button BtWrite;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.uhf_write_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mContext = (UHFMainActivity) getActivity();

		CkWithUii_Write = (CheckBox) mContext
				.findViewById(R.id.CkWithUii_Write);
		EtTagUii_Write = (EditText) mContext.findViewById(R.id.EtTagUii_Write);
		SpinnerBank_Write = (Spinner) mContext
				.findViewById(R.id.SpinnerBank_Write);
		EtPtr_Write = (EditText) mContext.findViewById(R.id.EtPtr_Write);
		EtLen_Write = (EditText) mContext.findViewById(R.id.EtLen_Write);
		EtData_Write = (EditText) mContext.findViewById(R.id.EtData_Write);
		EtAccessPwd_Write = (EditText) mContext
				.findViewById(R.id.EtAccessPwd_Write);
		SpinnerOption_Write = (Spinner) mContext
				.findViewById(R.id.SpinnerOption_Write);
		BtUii_Write = (Button) mContext.findViewById(R.id.BtUii_Write);
		BtWrite = (Button) mContext.findViewById(R.id.BtWrite);

		EtLen_Write.setEnabled(false);
		BtUii_Write.setEnabled(false);
		EtTagUii_Write.setKeyListener(null);

		CkWithUii_Write.setOnClickListener(new CkWithUii_WriteClickListener());
		BtUii_Write.setOnClickListener(new BtUii_WriteClickListener());
		SpinnerOption_Write
				.setOnItemSelectedListener(new SpinnerOption_WriteSelectedListener());
		BtWrite.setOnClickListener(new BtWriteOnClickListener());

	}

	public class BtUii_WriteClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			String uiiStr = mContext.mReader.inventorySingleTag();

			if (uiiStr != null) {
				EtTagUii_Write.setText(uiiStr);
			} else {
				EtTagUii_Write.setText("");

				UIHelper.ToastMessage(mContext, R.string.uhf_msg_read_tag_fail);
			}
		}
	}

	public class BtWriteOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			String strPtr = EtPtr_Write.getText().toString().trim();

			if (StringUtils.isEmpty(strPtr)) {
				UIHelper.ToastMessage(mContext, R.string.uhf_msg_addr_not_null);

				return;
			} else if (!StringUtility.isDecimal(strPtr)) {
				UIHelper.ToastMessage(mContext,
						R.string.uhf_msg_addr_must_decimal);
				return;
			}

			String strPWD = EtAccessPwd_Write.getText().toString().trim();// 访问密码

			if (StringUtils.isNotEmpty(strPWD)) {
				if (strPWD.length() != 8) {
					UIHelper.ToastMessage(mContext,
							R.string.uhf_msg_addr_must_len8);
					return;
				}

				else if (!mContext.vailHexInput(strPWD)) {
					UIHelper.ToastMessage(mContext,
							R.string.rfid_mgs_error_nohex);

					return;
				}
			} else {
				strPWD = "00000000";
			}

			String strData = EtData_Write.getText().toString().trim();// 要写入的内容

			if (StringUtils.isEmpty(strData)) {
				UIHelper.ToastMessage(mContext,
						R.string.uhf_msg_write_must_not_null);

				return;
			} else if (!mContext.vailHexInput(strData)) {

				UIHelper.ToastMessage(mContext, R.string.rfid_mgs_error_nohex);
				return;
			}

			// 单字单次
			if (SpinnerOption_Write.getSelectedItemPosition() == 0) {

				if (strData.length() != 4) {

					UIHelper.ToastMessage(mContext,
							R.string.uhf_msg_write_must_len4);

					return;
				}

				if (CkWithUii_Write.isChecked())// 指定标签
				{

					String strUII = EtTagUii_Write.getText().toString().trim();
					if (StringUtils.isEmpty(strUII)) {
						UIHelper.ToastMessage(mContext,
								R.string.uhf_msg_tag_must_not_null);
						return;
					}

					if (mContext.mReader.writeDataByEPC(strPWD, BankEnum
							.valueOf(SpinnerBank_Write.getSelectedItem()
									.toString()), Integer.parseInt(strPtr),
							strData, strUII)) {
						UIHelper.ToastMessage(mContext,
								R.string.uhf_msg_write_succ);

						mContext.playSound(1);

					} else {

						UIHelper.ToastMessage(mContext,
								R.string.uhf_msg_write_fail);

						mContext.playSound(2);

					}

				} else {

					String uiiStr = mContext.mReader.writeDataToSingleTag(
							strPWD, BankEnum.valueOf(SpinnerBank_Write
									.getSelectedItem().toString()), Integer
									.parseInt(strPtr), 1, strData);

					if (StringUtils.isNotEmpty(uiiStr)) {
						UIHelper.ToastMessage(mContext,
								getString(R.string.uhf_msg_write_succ)
										+ "\nUII: " + uiiStr);
						mContext.playSound(1);
					} else {
						UIHelper.ToastMessage(mContext,
								R.string.uhf_msg_write_fail);
						mContext.playSound(2);
					}
				}
			} else if (SpinnerOption_Write.getSelectedItemPosition() == 1) {

				// 多字单次

				String cntStr = EtLen_Write.getText().toString().trim();
				if (StringUtils.isEmpty(cntStr)) {
					UIHelper.ToastMessage(mContext,
							R.string.uhf_msg_len_not_null);

					return;
				} else if (!StringUtility.isDecimal(cntStr)) {
					UIHelper.ToastMessage(mContext,
							R.string.uhf_msg_len_must_decimal);

					return;
				}

				if ((strData.length()) % 4 != 0) {
					UIHelper.ToastMessage(mContext,
							R.string.uhf_msg_write_must_len4x);

					return;
				} else if (!mContext.vailHexInput(strData)) {
					UIHelper.ToastMessage(mContext,
							R.string.rfid_mgs_error_nohex);
					return;
				}

				if (CkWithUii_Write.isChecked())// 指定标签
				{

					String strUII = EtTagUii_Write.getText().toString().trim();
					if (StringUtils.isEmpty(strUII)) {
						UIHelper.ToastMessage(mContext,
								R.string.uhf_msg_tag_must_not_null);
						return;
					}

					if (mContext.mReader.blockWriteDataByEPC(strPWD, BankEnum
							.valueOf(SpinnerBank_Write.getSelectedItem()
									.toString()), Integer.parseInt(strPtr),
							Integer.valueOf(cntStr), strData, strUII)) {
						UIHelper.ToastMessage(mContext,
								R.string.uhf_msg_write_succ);
						mContext.playSound(1);
					} else {
						UIHelper.ToastMessage(mContext,
								R.string.uhf_msg_write_fail);

						mContext.playSound(2);
					}

				} else {

					String strReUII = mContext.mReader
							.blockWriteDataToSingleTag(strPWD, BankEnum
									.valueOf(SpinnerBank_Write
											.getSelectedItem().toString()),
									Integer.parseInt(strPtr), Integer
											.valueOf(cntStr), strData);// 返回的UII
					if (StringUtils.isNotEmpty(strReUII)) {

						UIHelper.ToastMessage(mContext,
								getString(R.string.uhf_msg_write_succ)
										+ "\nUII: " + strReUII);
						mContext.playSound(1);
					} else {
						UIHelper.ToastMessage(mContext,
								R.string.uhf_msg_write_fail);
						mContext.playSound(2);
					}
				}
			}
		}
	}

	public class CkWithUii_WriteClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			EtTagUii_Write.setText("");

			if (CkWithUii_Write.isChecked()) {
				BtUii_Write.setEnabled(true);
				BtUii_Write.setVisibility(View.VISIBLE);
			} else {
				BtUii_Write.setEnabled(false);
				BtUii_Write.setVisibility(View.GONE);
			}
		}
	}

	public class SpinnerOption_WriteSelectedListener implements
			OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (arg2 == 0) {
				EtLen_Write.setEnabled(false);
				EtLen_Write.setText("1");
			} else {
				EtLen_Write.setEnabled(true);
				EtLen_Write.setText("4");
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}

	}

}
