package com.chainway.ht.ui.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import com.chainway.deviceapi.UHFWithRLM.UHFCrcFlagEnum;
import com.chainway.ht.R;
import com.chainway.ht.UIHelper;
import com.chainway.ht.ui.UHFMainActivity;
import com.chainway.ht.utils.StringUtils;
import com.chainway.utility.StringUtility;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class UHFReadTagFragment extends Fragment {

	private static boolean loopFlag = false;
	private static int inventoryFlag = 1;
	private LinearLayout llQValue;
	Handler handler;
	private ArrayList<HashMap<String, String>> tagList;
	SimpleAdapter adapter;

	Button BtClear;
	TextView tv_count;
	RadioGroup RgInventory;
	RadioButton RbInventorySingle;
	RadioButton RbInventoryLoop;
	RadioButton RbInventoryAnti;
	Spinner SpinnerQ;
	Button BtInventory;
	ListView LvTags;
	byte initQ;

	private UHFMainActivity mContext;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i("MY", "UHFReadTagFragment.onCreateView");

		return inflater
				.inflate(R.layout.uhf_readtag_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i("MY", "UHFReadTagFragment.onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		mContext = (UHFMainActivity) getActivity();

		tagList = new ArrayList<HashMap<String, String>>();

		BtClear = (Button) mContext.findViewById(R.id.BtClear);
		tv_count = (TextView) mContext.findViewById(R.id.tv_count);
		RgInventory = (RadioGroup) mContext.findViewById(R.id.RgInventory);
		RbInventorySingle = (RadioButton) mContext
				.findViewById(R.id.RbInventorySingle);
		RbInventoryLoop = (RadioButton) mContext
				.findViewById(R.id.RbInventoryLoop);
		RbInventoryAnti = (RadioButton) mContext
				.findViewById(R.id.RbInventoryAnti);
		SpinnerQ = (Spinner) mContext.findViewById(R.id.SpinnerQ);
		BtInventory = (Button) mContext.findViewById(R.id.BtInventory);
		LvTags = (ListView) mContext.findViewById(R.id.LvTags);

		adapter = new SimpleAdapter(mContext, tagList, R.layout.listtag_items,
				new String[] { "tagUii", "tagLen", "tagCount" }, new int[] {
						R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount });

		BtClear.setOnClickListener(new BtClearClickListener());
		RgInventory
				.setOnCheckedChangeListener(new RgInventoryCheckedListener());
		BtInventory.setOnClickListener(new BtInventoryClickListener());
		SpinnerQ.setEnabled(false);
		SpinnerQ.setOnItemSelectedListener(new QItemSelectedListener());

		llQValue = (LinearLayout) mContext.findViewById(R.id.llQValue);

		LvTags.setAdapter(adapter);
		clearData();

		Log.i("MY", "UHFReadTagFragment.EtCountOfTags=" + tv_count.getText());

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				Bundle bundle = msg.getData();
				String tagEPC = bundle.getString("tagEPC");

				addEPCToList(tagEPC);

			}
		};
	}

	@Override
	public void onPause() {
		Log.i("MY", "UHFReadTagFragment.onPause");
		super.onPause();

		// 停止识别
		stopInventory();

	}

	/**
	 * 添加EPC到列表中
	 * 
	 * @param epc
	 */
	private void addEPCToList(String epc) {
		if (StringUtils.isNotEmpty(epc)) {
			int index = checkIsExist(epc);

			HashMap<String, String> map = new HashMap<String, String>();

			map.put("tagUii", epc);
			map.put("tagCount", String.valueOf(1));

			mContext.getAppContext().uhfQueue.add(epc + "\t 1");

			if (index == -1) {
				tagList.add(map);
				LvTags.setAdapter(adapter);
				tv_count.setText("" + adapter.getCount());
			} else {
				int tagcount = Integer.parseInt(
						tagList.get(index).get("tagCount"), 10) + 1;

				map.put("tagCount", String.valueOf(tagcount));

				tagList.set(index, map);

			}

			adapter.notifyDataSetChanged();

			mContext.playSound(1);

		}
	}

	public class BtClearClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			clearData();

		}
	}

	private void clearData() {
		tv_count.setText("0");

		tagList.clear();

		Log.i("MY", "tagList.size " + tagList.size());

		adapter.notifyDataSetChanged();
	}

	public class RgInventoryCheckedListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {

			llQValue.setVisibility(View.GONE);

			if (checkedId == RbInventorySingle.getId()) {
				// 单步识别
				inventoryFlag = 0;
				SpinnerQ.setEnabled(false);
			} else if (checkedId == RbInventoryLoop.getId()) {
				// 单标签循环识别
				inventoryFlag = 1;
				SpinnerQ.setEnabled(false);
			} else {
				// 防碰撞识别
				inventoryFlag = 2;
				SpinnerQ.setEnabled(true);

				llQValue.setVisibility(View.VISIBLE);
			}
		}
	}

	public class QItemSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {

			initQ = Byte.valueOf((String) SpinnerQ.getSelectedItem(), 10);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}
	}

	public class BtInventoryClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			readTag();
		}
	}

	private void readTag() {
		if (BtInventory.getText().equals(
				mContext.getString(R.string.btInventory)))// 识别标签
		{
			switch (inventoryFlag) {
			case 0:// 单步
			{

				String strUII = mContext.mReader.inventorySingleTag();
				if (StringUtils.isNotEmpty(strUII)) {
					String strEPC = mContext.mReader.convertUiiToEPC(strUII);
					addEPCToList(strEPC);

					tv_count.setText("" + adapter.getCount());
				} else {
					UIHelper.ToastMessage(mContext,
							R.string.uhf_msg_inventory_fail);

					mContext.playSound(2);

				}

			}
				break;
			case 1:// 单标签循环
			{

				if (mContext.mReader.startInventory((byte) 0, (byte) 0)) {
					BtInventory.setText(mContext
							.getString(R.string.title_stop_Inventory));
					loopFlag = true;
					new TagThread().start();
				} else {
					mContext.mReader.stopOperation();
					UIHelper.ToastMessage(mContext,
							R.string.uhf_msg_inventory_open_fail);
					mContext.playSound(2);
				}
			}

				break;
			case 2:// 防碰撞
			{
				if (mContext.mReader.startInventory((byte) 1, initQ)) {
					BtInventory.setText(mContext
							.getString(R.string.title_stop_Inventory));
					loopFlag = true;
					new TagThread().start();
				} else {
					mContext.mReader.stopOperation();
					UIHelper.ToastMessage(mContext,
							R.string.uhf_msg_inventory_open_fail);
					mContext.playSound(2);
				}
			}
				break;

			default:
				break;
			}
		} else {// 停止识别
			stopInventory();
		}
	}

	/**
	 * 停止识别
	 */
	private void stopInventory() {

		if (loopFlag) {

			loopFlag = false;

			if (mContext.mReader.stopOperation()) {
				BtInventory.setText(mContext.getString(R.string.btInventory));
			} else {
				UIHelper.ToastMessage(mContext,
						R.string.uhf_msg_inventory_stop_fail);

			}

		}
	}

	/**
	 * 判断EPC是否在列表中
	 * 
	 * @param strEPC
	 *            索引
	 * @return
	 */
	public int checkIsExist(String strEPC) {
		int existFlag = -1;
		if (StringUtils.isEmpty(strEPC)) {
			return existFlag;
		}

		String tempStr = "";
		for (int i = 0; i < tagList.size(); i++) {
			HashMap<String, String> temp = new HashMap<String, String>();
			temp = tagList.get(i);

			tempStr = temp.get("tagUii");

			if (strEPC.equals(tempStr)) {
				existFlag = i;
				break;
			}
		}

		return existFlag;
	}

	class TagThread extends Thread {

		HashMap<String, String> map;

		public void run() {

			while (loopFlag) {

				String strUII = mContext.mReader.readInventory();

				if (StringUtils.isNotEmpty(strUII)) {
					String strEPC = mContext.mReader.convertUiiToEPC(strUII);

					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					bundle.putString("tagEPC", strEPC);

					msg.setData(bundle);
					handler.sendMessage(msg);

				}

				try {
					sleep(80);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	public void myOnKeyDwon() {
		readTag();
	}

}
