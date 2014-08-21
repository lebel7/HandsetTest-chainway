package com.chainway.ht.adapter;

import java.util.List;

import com.chainway.ht.R;
import com.chainway.ht.adapter.WiFiAdapter.ListItemView;
import com.chainway.ht.bean.Fingerprint;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FingerprintAdapter extends BaseAdapter {

	static class ListItemView {
		public TextView tvPageId;
		public TextView tvName;
		public TextView tvCreateTime;
	}

	List<Fingerprint> mList;
	private Context mContext;
	private LayoutInflater listContainer;// 视图容器

	public FingerprintAdapter(Context context, List<Fingerprint> data) {
		this.mContext = context;
		this.listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
		this.mList = data;

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListItemView listItemView = null;

		if (convertView == null) {
			convertView = listContainer.inflate(R.layout.fingerprint_list_item,
					null);

			listItemView = new ListItemView();

			listItemView.tvPageId = (TextView) convertView
					.findViewById(R.id.tvPageId);
			listItemView.tvName = (TextView) convertView
					.findViewById(R.id.tvName);

			listItemView.tvCreateTime = (TextView) convertView
					.findViewById(R.id.tvCreateTime);
			convertView.setTag(listItemView);

		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		if (mList != null) {
			Fingerprint f = mList.get(position);

			listItemView.tvPageId.setText(f.getPageId() + "");
			listItemView.tvName.setText(f.getName());
			listItemView.tvCreateTime.setText(f.getCreateTime());

		}

		return convertView;
	}

}
