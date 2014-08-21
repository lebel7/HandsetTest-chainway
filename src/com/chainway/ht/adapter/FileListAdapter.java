package com.chainway.ht.adapter;

import java.util.List;

import com.chainway.ht.R;
import com.chainway.ht.bean.SFile;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FileListAdapter extends BaseAdapter {

	private List<SFile> listItems;// 数据集合
	private Context mContext;
	private LayoutInflater listContainer;// 视图容器
	private int itemViewResource;// 自定义项视图源

	static class ListItemView {
		public TextView tvTitle;

	}

	/**
	 * 实例化Adapter
	 * 
	 * @param context
	 * @param data
	 * @param resource
	 */
	public FileListAdapter(Context context, List<SFile> data, int resource) {
		this.mContext = context;
		this.listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
		this.itemViewResource = resource;
		this.listItems = data;

	}

	@Override
	public int getCount() {
		return listItems.size();
	}

	@Override
	public Object getItem(int arg0) {
		return listItems.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// 设置文字和图片
		SFile sfile = listItems.get(position);

		// 自定义视图
		ListItemView listItemView = null;

		if (convertView == null) {
			// 获取list_item布局文件的视图
			convertView = listContainer.inflate(this.itemViewResource, null);

			listItemView = new ListItemView();

			listItemView.tvTitle = (TextView) convertView
					.findViewById(R.id.tvTitle);

			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		listItemView.tvTitle.setText(sfile.getName());
		listItemView.tvTitle.setTag(sfile.getPath());

		return convertView;
	}

}
