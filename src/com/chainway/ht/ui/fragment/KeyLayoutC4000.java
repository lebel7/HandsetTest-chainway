package com.chainway.ht.ui.fragment;

import com.chainway.ht.R;
import com.chainway.ht.ui.KeyTestActivity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class KeyLayoutC4000 extends KeyLayoutFragment {

	private static final String TAG = "KeyLayoutC4000";

	private KeyTestActivity mContext;

	private TextView tvKey1;
	private TextView tvKey2;
	private TextView tvKey3;
	private TextView tvKey4;
	private TextView tvKey5;
	private TextView tvKey6;
	private TextView tvKey7;
	private TextView tvKey8;
	private TextView tvKey9;
	private TextView tvKey10;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = (KeyTestActivity) getActivity();
		init();
	}

	private void init() {
		tvKey1 = (TextView) mContext.findViewById(R.id.tv_key1);
		tvKey2 = (TextView) mContext.findViewById(R.id.tv_key2);
		tvKey3 = (TextView) mContext.findViewById(R.id.tv_key3);
		tvKey4 = (TextView) mContext.findViewById(R.id.tv_key4);
		tvKey5 = (TextView) mContext.findViewById(R.id.tv_key5);
		tvKey6 = (TextView) mContext.findViewById(R.id.tv_key6);
		tvKey7 = (TextView) mContext.findViewById(R.id.tv_key7);
		tvKey8 = (TextView) mContext.findViewById(R.id.tv_key8);
		tvKey9 = (TextView) mContext.findViewById(R.id.tv_key9);
		tvKey10 = (TextView) mContext.findViewById(R.id.tv_key10);

	}

	public void onKeyDown(int keyCode) {
		super.onKeyDown(keyCode);

		Log.i(TAG, "keyCode:" + keyCode);

		switch (keyCode) {
		case 25:
			tvKey1.setText("" + keyCode);
			tvKey1.setBackgroundResource(R.drawable.textfield_pressed);
			mContext.playSound(1);
			break;
		case 24:
			tvKey3.setText("" + keyCode);
			tvKey3.setBackgroundResource(R.drawable.textfield_pressed);
			mContext.playSound(1);
			break;
		case 82:
			tvKey4.setText("" + keyCode);
			tvKey4.setBackgroundResource(R.drawable.textfield_pressed);
			mContext.playSound(1);
			break;
		// case 4:
		// tvKey6.setText("" + keyCode);
		// tvKey6.setBackgroundResource(R.drawable.textfield_pressed);
		// mContext.playSound();
		// break;
		case 84:
			tvKey7.setText("" + keyCode);
			tvKey7.setBackgroundResource(R.drawable.textfield_pressed);
			mContext.playSound(1);
			break;
		case 131:
			tvKey8.setText("" + keyCode);
			tvKey8.setBackgroundResource(R.drawable.textfield_pressed);
			mContext.playSound(1);
			break;
		case 140:
			tvKey9.setText("" + keyCode);
			tvKey9.setBackgroundResource(R.drawable.textfield_pressed);
			mContext.playSound(1);
			break;
		case 132:
			tvKey10.setText("" + keyCode);
			tvKey10.setBackgroundResource(R.drawable.textfield_pressed);
			mContext.playSound(1);
			break;

		default:
			break;
		}
	}

	public void onKeyUp(int keyCode) {
		super.onKeyUp(keyCode);

		Log.i(TAG, "keyCode:" + keyCode);

		switch (keyCode) {
		case 25:

			tvKey1.setBackgroundResource(R.drawable.textfield_disabled_selected);

			break;
		case 24:

			tvKey3.setBackgroundResource(R.drawable.textfield_disabled_selected);

			break;
		case 82:

			tvKey4.setBackgroundResource(R.drawable.textfield_disabled_selected);

			break;
		// case 4:

		// tvKey6.setBackgroundResource(R.drawable.textfield_disabled_selected);
		//
		// break;
		case 84:

			tvKey7.setBackgroundResource(R.drawable.textfield_disabled_selected);

			break;
		case 131:

			tvKey8.setBackgroundResource(R.drawable.textfield_disabled_selected);

			break;
		case 140:

			tvKey9.setBackgroundResource(R.drawable.textfield_disabled_selected);

			break;
		case 132:

			tvKey10.setBackgroundResource(R.drawable.textfield_disabled_selected);

			break;

		default:
			break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.keylayout_c4000, container, false);
	}

}
