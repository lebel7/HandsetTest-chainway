package com.chainway.ht.adapter;

import java.util.List;

import com.chainway.ht.R;
import com.chainway.ht.bean.SFile;
import com.chainway.ht.network.WifiAdmin;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WiFiAdapter extends BaseAdapter {

	List<ScanResult> mWifiList;
	private Context mContext;
	private LayoutInflater listContainer;// 视图容器
	private int itemViewResource;// 自定义项视图源

	static class ListItemView {
		public TextView tvSsid;
		public TextView tvBSSID;
		public ImageView ivLevel;
	}

	/**
	 * 实例化Adapter
	 * 
	 * @param context
	 * @param data
	 * @param resource
	 */
	public WiFiAdapter(Context context, List<ScanResult> data, int resource) {
		this.mContext = context;
		this.listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
		this.itemViewResource = resource;
		this.mWifiList = data;

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mWifiList == null) {
			return 0;
		}
		return mWifiList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		if (mWifiList == null) {
			return null;
		}

		return mWifiList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ListItemView listItemView = null;

		Log.i("MY", "getView() position:" + position);

		if (convertView == null) {
			convertView = listContainer.inflate(this.itemViewResource, null);

			listItemView = new ListItemView();

			listItemView.tvSsid = (TextView) convertView
					.findViewById(R.id.tvSsid);
			listItemView.tvBSSID = (TextView) convertView
					.findViewById(R.id.tvBSSID);

			listItemView.ivLevel = (ImageView) convertView
					.findViewById(R.id.ivLevel);
			convertView.setTag(listItemView);

		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		if (listItemView == null) {
			Log.i("MY", "getView() listItemView==null");
		}

		if (mWifiList != null) {
			ScanResult sResult = mWifiList.get(position);

			listItemView.tvSsid.setText(sResult.SSID + " [" + sResult.BSSID
					+ "]");

			listItemView.tvBSSID.setText("加密："
					+ WifiAdmin.getSecurity(sResult.capabilities) + "\n信道："
					+ WifiAdmin.convertChannel(sResult.frequency) + " * "
					+ sResult.frequency + "\t等级：" + sResult.level + " dBm");

			int iStrong = WifiAdmin.calculateSignalLevel(sResult.level, 5);
			switch (iStrong) {
			case 0:
				listItemView.ivLevel.setImageResource(R.drawable.sterne00);
				break;
			case 1:
				listItemView.ivLevel.setImageResource(R.drawable.sterne01);
				break;
			case 2:
				listItemView.ivLevel.setImageResource(R.drawable.sterne02);
				break;
			case 3:
				listItemView.ivLevel.setImageResource(R.drawable.sterne03);
				break;
			case 4:
				listItemView.ivLevel.setImageResource(R.drawable.sterne04);
				break;

			default:
				listItemView.ivLevel.setImageResource(R.drawable.sterne00);
				break;
			}
		}
		return convertView;
	}

}
