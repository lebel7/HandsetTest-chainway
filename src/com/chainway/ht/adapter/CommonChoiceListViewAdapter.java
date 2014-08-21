package com.chainway.ht.adapter;

import java.util.List;
import java.util.Map;

import com.chainway.ht.AppContext;
import com.chainway.ht.R;
import com.chainway.ht.utils.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommonChoiceListViewAdapter extends BaseAdapter {
	private List<Map<String, Object>> listItems; // 信息集合
	private LayoutInflater listContainer; // 视图容器
	private Activity act;
	private AppContext app;
	private LayoutParams para;

	public final class ListItemView { // 自定义控件集合
		public ImageView image;
		public TextView title;
		public ImageView imageChecked;
	}

	public CommonChoiceListViewAdapter(Context context,
			List<Map<String, Object>> listItems) {
		listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
		this.listItems = listItems;
		this.act = (Activity) context;
		app = (AppContext) act.getApplication();
		para = null;
	}

	/**
	 * ListView Item设置
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		// 自定义视图
		ListItemView listItemView = null;
		if (convertView == null) {
			listItemView = new ListItemView();
			// 获取list_item布局文件的视图
			convertView = listContainer.inflate(R.layout.list_item, null);
			// 获取控件对象
			listItemView.image = (ImageView) convertView
					.findViewById(R.id.ListItemImage);
			listItemView.title = (TextView) convertView
					.findViewById(R.id.ListItemTitle);
			listItemView.imageChecked = (ImageView) convertView
					.findViewById(R.id.ListItemCheck);

			// 设置控件集到convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		// 设置文字和图片
		if (StringUtils.isInt(listItems.get(position).get("ListItemImage")
				.toString())) {
			listItemView.image.setBackgroundResource((Integer) listItems.get(
					position).get("ListItemImage"));
		} else {
			if (para == null) {
				para = listItemView.image.getLayoutParams();
				para.height = (int) (46 * app.getDensity(this.act));
				para.width = (int) (46 * app.getDensity(this.act));
			}
			listItemView.image.setLayoutParams(para);
			listItemView.image.setBackgroundDrawable((Drawable) listItems.get(
					position).get("ListItemImage"));
		}
		listItemView.title.setText((String) listItems.get(position).get(
				"ListItemTitle"));
		if (listItems.get(position).get("ListItemCheck") != null) {
			listItemView.imageChecked.setBackgroundResource((Integer) listItems
					.get(position).get("ListItemCheck"));
		} else {
			listItemView.imageChecked.setBackgroundDrawable(null);
		}
		return convertView;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listItems.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return listItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
}