package com.chainway.ht.ui.fragment;

import com.chainway.deviceapi.UHFWithRLM.UHFCrcFlagEnum;
import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.ui.UHFMainActivity;
import com.chainway.ht.utils.StringUtils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class UHFSetFragment extends Fragment {
	private UHFMainActivity mContext;

	private Button btnSetFre;
	private Button btnGetFre;
	private Spinner spMode;
	private EditText etFreRange;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_uhfset, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mContext = (UHFMainActivity) getActivity();

		btnSetFre = (Button) mContext.findViewById(R.id.BtSetFre);
		btnGetFre = (Button) mContext.findViewById(R.id.BtGetFre);

		spMode = (Spinner) mContext.findViewById(R.id.SpinnerMode);

		etFreRange = (EditText) mContext.findViewById(R.id.EtFreRange);

		etFreRange.setKeyListener(null);

		btnSetFre.setOnClickListener(new SetFreOnclickListener());
		btnGetFre.setOnClickListener(new GetFreOnclickListener());

	}

	public class SetFreOnclickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			byte[] bBaseFre = new byte[2];

			if (mContext.mReader.setFrequency(
					(byte) spMode.getSelectedItemPosition(), (byte) 0,
					bBaseFre, (byte) 0, (byte) 0, (byte) 0)) {
				UIHelper.ToastMessage(mContext,
						R.string.uhf_msg_set_frequency_succ);
			} else {
				UIHelper.ToastMessage(mContext,
						R.string.uhf_msg_set_frequency_fail);
			}

		}
	}

	public class GetFreOnclickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			String strFrequency = mContext.mReader.getFrequency();

			if (StringUtils.isNotEmpty(strFrequency)) {

				etFreRange.setText(strFrequency);

				UIHelper.ToastMessage(mContext,
						R.string.uhf_msg_read_frequency_succ);

			} else {
				UIHelper.ToastMessage(mContext,
						R.string.uhf_msg_read_frequency_fail);
			}
		}

	}

}
