package com.chainway.ht.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chainway.ht.R;
import com.chainway.ht.adapter.CommonChoiceListViewAdapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 *
 */
public class CustomDialog extends Dialog {

	public CustomDialog(Context context, int theme) {
		super(context, theme);
	}

	public CustomDialog(Context context) {
		super(context);
	}

	/**
	 * Helper class for creating a custom dialog
	 */
	public static class Builder {

		private Context context;
		private int icon;
		private String title;
		private String message;
		private String positiveButtonText;
		private String negativeButtonText;
		private String neutralButtonText;
		private View contentView;
		private CharSequence[] items;
		private CharSequence[] muiltItems;
		private String muiltSelected;
		private String itemSelected;

		private DialogInterface.OnClickListener positiveButtonClickListener,
				negativeButtonClickListener, neutralButtonClickListener,
				itemClickListener;

		private OnMultiChoiceClickListener itemChoiceListener;

		public Builder(Context context) {
			this.context = context;
		}

		/**
		 * Set the Dialog message from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		/**
		 * Set the Dialog message from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(int message) {
			this.message = (String) context.getText(message);
			return this;
		}

		/**
		 * Set the Dialog title from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);
			return this;
		}

		/**
		 * Set the Dialog title from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		/**
		 * Set the Dialog icon
		 * 
		 * @param icon
		 * @return
		 */
		public Builder setIcon(int icon) {
			this.icon = icon;
			return this;
		}

		/**
		 * Set a custom content view for the Dialog. If a message is set, the
		 * contentView is not added to the Dialog...
		 * 
		 * @param v
		 * @return
		 */
		public Builder setView(View v) {
			this.contentView = v;
			return this;
		}

		/**
		 * Set the positive button resource and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(int positiveButtonText,
				DialogInterface.OnClickListener listener) {
			this.positiveButtonText = (String) context
					.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the positive button text and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(String positiveButtonText,
				DialogInterface.OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the neutral button resource and it's listener
		 * 
		 * @param neutralButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNeutralButton(int neutralButtonText,
				DialogInterface.OnClickListener listener) {
			this.neutralButtonText = (String) context
					.getText(neutralButtonText);
			this.neutralButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the neutral button text and it's listener
		 * 
		 * @param neutralButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNeutralButton(String neutralButtonText,
				DialogInterface.OnClickListener listener) {
			this.neutralButtonText = neutralButtonText;
			this.neutralButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button resource and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(int negativeButtonText,
				DialogInterface.OnClickListener listener) {
			this.negativeButtonText = (String) context
					.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button text and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(String negativeButtonText,
				DialogInterface.OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		public Builder setItems(CharSequence[] items,
				DialogInterface.OnClickListener listener) {
			this.items = items;
			this.itemClickListener = listener;
			return this;
		}

		public Builder setItems(CharSequence[] items, String itemSelected,
				DialogInterface.OnClickListener listener) {
			this.items = items;
			this.itemClickListener = listener;
			this.itemSelected = itemSelected;
			return this;
		}

		public Builder setMultiChoiceItems(String select, CharSequence[] items,
				boolean[] checkedItems, OnMultiChoiceClickListener listener) {
			this.muiltItems = items;
			this.muiltSelected = select;
			this.itemChoiceListener = listener;
			return this;
		}

		/**
		 * Create the custom dialog
		 */
		public CustomDialog create() {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			final CustomDialog dialog = new CustomDialog(context,
					R.style.AlertDialog);
			View layout = inflater.inflate(R.layout.alert_dialog, null);

			dialog.addContentView(layout, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			if (title == null) {
				((LinearLayout) layout.findViewById(R.id.dialogTopPanel))
						.setVisibility(View.GONE);
			} else {
				// set the dialog title
				((TextView) layout.findViewById(R.id.dialogTitle))
						.setText(title);
				// set the dialog icon
				((ImageView) layout.findViewById(R.id.dialogIcon))
						.setImageResource(icon);
			}
			//

			if (message == null) {
				((LinearLayout) layout.findViewById(R.id.dialogContentPanel))
						.setVisibility(View.GONE);
			}
			if (contentView == null && items == null && muiltItems == null) {
				((LinearLayout) layout.findViewById(R.id.dialogCustomPanel))
						.setVisibility(View.GONE);
			}
			if (negativeButtonText == null && neutralButtonText == null
					&& positiveButtonText == null) {
				((LinearLayout) layout.findViewById(R.id.dialogBottomPanel))
						.setVisibility(View.GONE);
			}
			// set the confirm button
			if (positiveButtonText != null) {
				((Button) layout.findViewById(R.id.dialogButton1))
						.setText(positiveButtonText);
				if (positiveButtonClickListener != null) {
					((Button) layout.findViewById(R.id.dialogButton1))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									positiveButtonClickListener.onClick(dialog,
											DialogInterface.BUTTON_POSITIVE);
									dialog.dismiss();
								}
							});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.dialogButton1)
						.setVisibility(View.GONE);
			}
			if (neutralButtonText != null) {
				((Button) layout.findViewById(R.id.dialogButton2))
						.setText(neutralButtonText);
				if (neutralButtonClickListener != null) {
					((Button) layout.findViewById(R.id.dialogButton2))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									neutralButtonClickListener.onClick(dialog,
											DialogInterface.BUTTON_NEGATIVE);
									dialog.dismiss();
								}
							});
				}
			} else {

				layout.findViewById(R.id.dialogButton2)
						.setVisibility(View.GONE);
			}
			// set the cancel button
			if (negativeButtonText != null) {
				((Button) layout.findViewById(R.id.dialogButton3))
						.setText(negativeButtonText);
				if (negativeButtonClickListener != null) {
					((Button) layout.findViewById(R.id.dialogButton3))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									negativeButtonClickListener.onClick(dialog,
											DialogInterface.BUTTON_NEGATIVE);
									dialog.dismiss();
								}
							});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.dialogButton3)
						.setVisibility(View.GONE);
			}
			// set the content message
			if (message != null) {
				((TextView) layout.findViewById(R.id.dialogMessage))
						.setText(message);
			} else if (contentView != null) {
				// if no message set
				// add the contentView to the dialog body
				((LinearLayout) layout.findViewById(R.id.dialogCustomPanel))
						.removeAllViews();
				((LinearLayout) layout.findViewById(R.id.dialogCustomPanel))
						.addView(contentView, new LayoutParams(
								LayoutParams.FILL_PARENT,
								LayoutParams.WRAP_CONTENT));
			}
			if (items != null && items.length > 0) {
				if (icon == 0) {
					((ImageView) layout.findViewById(R.id.dialogIcon))
							.setImageResource(R.drawable.alert_cmd);
				}

				((LinearLayout) layout.findViewById(R.id.dialogCustomPanel))
						.removeAllViews();
				View listView = inflater.inflate(R.layout.list_view_select,
						null);
				((LinearLayout) layout.findViewById(R.id.dialogCustomPanel))
						.addView(listView, new LayoutParams(
								LayoutParams.FILL_PARENT,
								LayoutParams.WRAP_CONTENT));

				List<Map<String, Object>> listItem = new ArrayList<Map<String, Object>>();
				HashMap<String, Object> map = new HashMap<String, Object>();
				for (int i = 0; i < items.length; i++) {
					map = new HashMap<String, Object>();
					map.put("ListItemImage", R.drawable.cross);// 图像资源的ID
					map.put("ListItemTitle", items[i]);
					map.put("ListItemId", items[i]);
					if (itemSelected != null) {
						if (itemSelected.equals(items[i])) {
							map.put("ListItemCheck", R.drawable.success);
						} else {
							map.put("ListItemCheck", null);
						}
					} else {
						map.put("ListItemCheck", null);
					}
					listItem.add(map);
				}

				ListView list = (ListView) listView.findViewById(R.id.lvSelect);
				CommonChoiceListViewAdapter listItemAdapter = new CommonChoiceListViewAdapter(
						context, listItem); // 创建适配器
				list.setAdapter(listItemAdapter);

				// 添加点击
				list.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						itemClickListener.onClick(dialog, arg2);
						dialog.dismiss();
					}
				});
			}
			if (muiltItems != null && muiltItems.length > 0) {
				((LinearLayout) layout.findViewById(R.id.dialogCustomPanel))
						.removeAllViews();
				View listView = inflater.inflate(R.layout.list_view_select,
						null);
				((LinearLayout) layout.findViewById(R.id.dialogCustomPanel))
						.addView(listView, new LayoutParams(
								LayoutParams.FILL_PARENT,
								LayoutParams.WRAP_CONTENT));

				List<Map<String, Object>> listItem = new ArrayList<Map<String, Object>>();
				HashMap<String, Object> map = new HashMap<String, Object>();
				String[] sels = muiltSelected.split(",");

				List<String> selList = Arrays.asList(sels);
				for (int i = 0; i < muiltItems.length; i++) {
					map = new HashMap<String, Object>();
					map.put("ListItemImage", R.drawable.cross);// 图像资源的ID
					map.put("ListItemTitle", muiltItems[i]);
					map.put("ListItemId", muiltItems[i]);
					if (selList.contains(muiltItems[i])) {
						map.put("ListItemCheck", R.drawable.success);
					} else {
						map.put("ListItemCheck", null);
					}
					listItem.add(map);
				}
				ListView list = (ListView) listView.findViewById(R.id.lvSelect);
				setData(listItem, list, dialog);
			}

			dialog.setContentView(layout);
			return dialog;
		}

		private void setData(final List<Map<String, Object>> listItem,
				final ListView list, final CustomDialog dialog) {
			CommonChoiceListViewAdapter listItemAdapter = new CommonChoiceListViewAdapter(
					context, listItem); // 创建适配器
			list.setAdapter(listItemAdapter);
			// 添加点击
			list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					Map<String, Object> map = listItem.get(arg2);
					if (map.get("ListItemCheck") == null) {
						map.put("ListItemCheck", R.drawable.success);
						itemChoiceListener.onClick(dialog, arg2, true);
					} else {
						map.put("ListItemCheck", null);
						itemChoiceListener.onClick(dialog, arg2, false);
					}
					listItem.set(arg2, map);
					int v = list.getFirstVisiblePosition();
					setData(listItem, list, dialog);
					list.setSelection(v);
				}
			});
		}
	}

}