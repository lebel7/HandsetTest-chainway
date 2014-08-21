package com.chainway.ht;

import com.chainway.ht.R;
import com.chainway.ht.filebrowser.FileManagerActivity;
import com.chainway.ht.ui.A14443Activity;
import com.chainway.ht.ui.AppSetActivity;
import com.chainway.ht.ui.BluetoothPrintActivity;
import com.chainway.ht.ui.DownloadActivity;
import com.chainway.ht.ui.GlobalSetActivity;
import com.chainway.ht.ui.GpsActivity;
import com.chainway.ht.ui.ISO15693Activity;
import com.chainway.ht.ui.KeyTestActivity;
import com.chainway.ht.ui.MainActivity;
import com.chainway.ht.ui.NetworkStatusActivity;
import com.chainway.ht.ui.PingActivity;
import com.chainway.ht.ui.SettingsActivity;
import com.chainway.ht.ui.UHFMainActivity;
import com.chainway.ht.ui.UploadActivity;
import com.chainway.ht.ui.YiDActivity;
import com.chainway.ht.view.CustomDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class UIHelper {
	/**
	 * 初始加载
	 */
	public final static int LISTVIEW_ACTION_INIT = 0x01;
	/**
	 * 下拉刷新
	 */
	public final static int LISTVIEW_ACTION_REFRESH = 0x02;
	/**
	 * 滚动加载更多
	 */
	public final static int LISTVIEW_ACTION_SCROLL = 0x03;
	public final static int LISTVIEW_ACTION_CHANGE_CATALOG = 0x04;

	public final static int LISTVIEW_DATA_MORE = 0x01;
	public final static int LISTVIEW_DATA_LOADING = 0x02;
	public final static int LISTVIEW_DATA_FULL = 0x03;
	public final static int LISTVIEW_DATA_EMPTY = 0x04;

	public final static int LISTVIEW_DATATYPE_ORDERLIST = 0x01;

	private static long exitTime = 0;
	private static long showSetTime = 0;// 显示设置时间间隔
	private static long showSetCount = 0;// 显示设置点击次数

	/** 全局web样式 */
	public final static String WEB_STYLE = "<style> #artTitle1 {text-align:center;font-size:14px; color: #666; font-weight:normal; line-height:150%; float:left; width:100%; padding: 5px 0; margin:0 auto;}#artTitle {text-align:center; font-size:20px; color: #009; font-weight:normal; float:left; width:100%; padding:3px 0; margin:0 auto;}#artTitle3 {text-align:center; font-size:14px; color: #666; font-weight:normal; line-height:150%; float:left;width:100%;  padding: 5px 0; margin:0 auto;} p {color:#333;} a {color:#3E62A6;} img {max-width:310px;} "
			+ "img.alignleft {float:left;max-width:120px;margin:0 10px 5px 0;border:1px solid #ccc;background:#fff;padding:2px;} "
			+ "pre {font-size:9pt;line-height:12pt;font-family:Courier New,Arial;border:1px solid #ddd;border-left:5px solid #6CE26C;background:#f6f6f6;padding:5px;} "
			+ "a.tag {font-size:15px;text-decoration:none;background-color:#bbd6f3;border-bottom:2px solid #3E6D8E;border-right:2px solid #7F9FB6;color:#284a7b;margin:2px 2px 2px 0;padding:2px 4px;white-space:nowrap;}</style>";

	/**
	 * 弹出Toast消息
	 * 
	 * @param msg
	 */
	public static void ToastMessage(Context cont, String msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, int msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, String msg, int time) {
		Toast.makeText(cont, msg, time).show();
	}

	/**
	 * 调用拨打电话界面，不直接拨打
	 * 
	 * @param context
	 * @param phoneNum
	 */
	public static void callPhone(Context context, String phoneNum) {
		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
				+ phoneNum));
		context.startActivity(intent);
	}

	public static void showUpload(Context context) {
		Intent intent = new Intent(context, UploadActivity.class);
		context.startActivity(intent);
	}

	public static void showDownload(Context context) {
		Intent intent = new Intent(context, DownloadActivity.class);
		context.startActivity(intent);
	}

	public static void showFileManager(Context context) {
		Intent intent = new Intent(context, FileManagerActivity.class);
		context.startActivity(intent);
	}

	public static void showMain(Context context) {
		Intent intent = new Intent(context, MainActivity.class);
		context.startActivity(intent);
	}

	public static void showPing(Context context) {
		Intent intent = new Intent(context, PingActivity.class);
		context.startActivity(intent);
	}

	public static void showUHF(Context context) {
		Intent intent = new Intent(context, UHFMainActivity.class);
		context.startActivity(intent);
	}

	public static void showYiD(Context context) {
		Intent intent = new Intent(context, YiDActivity.class);
		context.startActivity(intent);
	}

	public static void showBluetoothPrint(Context context) {
		Intent intent = new Intent(context, BluetoothPrintActivity.class);
		context.startActivity(intent);
	}

	public static void showNetworkStatus(Context context) {
		Intent intent = new Intent(context, NetworkStatusActivity.class);
		context.startActivity(intent);
	}

	public static void showGps(Context context) {
		Intent intent = new Intent(context, GpsActivity.class);
		context.startActivity(intent);
	}

	public static void show14443A(Context context) {
		Intent intent = new Intent(context, A14443Activity.class);
		context.startActivity(intent);
	}

	public static void show15693(Context context) {
		Intent intent = new Intent(context, ISO15693Activity.class);
		context.startActivity(intent);
	}

	public static void showKeyTest(Context context) {
		Intent intent = new Intent(context, KeyTestActivity.class);
		context.startActivity(intent);
	}

	/**
	 * 系统设置页面
	 * 
	 * @param context
	 */
	public static void showSet(Context context) {
		Intent intent = new Intent(context, SettingsActivity.class);

		context.startActivity(intent);
	}

	/**
	 * 系统全局设置页面
	 * 
	 * @param context
	 */
	public static void showAppSet(Context context) {
		Intent intent = new Intent(context, GlobalSetActivity.class);

		((MainActivity) context).startActivityForResult(intent, 0);

	}

	/**
	 * 处理显示配置对话框
	 * 
	 * @param cont
	 * @param msg
	 */
	public static void showAppSetDialog(final Context cont) {
		Log.i("MY", "showSetCount=" + showSetCount);
		if ((System.currentTimeMillis() - showSetTime) > 2000) {
			showSetCount = 0;
			showSetTime = System.currentTimeMillis();
		} else if (showSetCount > 5)// 连续点击5次以上
		{
			showSetCount = 0;
			showAppSet(cont);
		} else {
			showSetCount++;// 统计点击次数
		}

	}

	/**
	 * 退出程序
	 * 
	 * @param cont
	 */
	public static void Exit(final Context cont) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cont);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.app_menu_surelogout);
		builder.setPositiveButton(R.string.sure,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 退出
						AppManager.getAppManager().AppExit(cont);
					}
				});
		builder.setNegativeButton(R.string.cancle,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();

	}

	/**
	 * 退出程序
	 * 
	 * @param cont
	 */
	public static void Exit_Toast(final Context cont, String msg) {

		Log.i("UIHelper",
				"Exit_Toast currentTime=" + System.currentTimeMillis()
						+ " exitTime=" + exitTime);

		if ((System.currentTimeMillis() - exitTime) > 2000) {
			ToastMessage(cont, msg);
			exitTime = System.currentTimeMillis();
		} else {
			AppManager.getAppManager().AppExit(cont);
		}

	}

	/**
	 * 打开浏览器
	 * 
	 * @param context
	 * @param url
	 */
	public static void openBrowser(Context context, String url) {
		try {
			Uri uri = Uri.parse(url);
			Intent it = new Intent(Intent.ACTION_VIEW, uri);
			context.startActivity(it);
		} catch (Exception e) {
			e.printStackTrace();
			ToastMessage(context, "无法浏览此网页", 500);
		}
	}

	/**
	 * 获取webviewClient对象
	 * 
	 * @return
	 */
	public static WebViewClient getWebViewClient() {
		return new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				showUrlRedirect(view.getContext(), url);
				return true;
			}

		};
	}

	/**
	 * url跳转
	 * 
	 * @param context
	 * @param url
	 */
	public static void showUrlRedirect(Context context, String url) {

		openBrowser(context, url);
	}

	public static void changeSettingIsLoadImage(Activity activity, boolean b) {
		AppContext ac = (AppContext) activity.getApplication();
		ac.setConfigLoadimage(b);
	}

	/**
	 * 点击返回监听事件
	 * 
	 * @param activity
	 * @return
	 */
	public static View.OnClickListener finish(final Activity activity) {
		return new View.OnClickListener() {
			public void onClick(View v) {
				activity.finish();
			}
		};
	}

	/**
	 * 清除app缓存
	 * 
	 * @param activity
	 */
	public static void clearAppCache(Activity activity) {
		final AppContext ac = (AppContext) activity.getApplication();
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					ToastMessage(ac, "缓存清除成功");
				} else {
					ToastMessage(ac, "缓存清除失败");
				}
			}
		};
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					ac.clearAppCache();
					msg.what = 1;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	/**
	 * 显示提示消息
	 * 
	 * @param act
	 * @param text
	 * @param duration
	 * @param image
	 */
	public static void showMessage(Activity act, String text, int duration,
			int image) {
		showMessage(act, text, duration, image, Gravity.CENTER);
	}

	/**
	 * 显示提示消息
	 * 
	 * @param act
	 * @param text
	 * @param duration
	 * @param image
	 * @param gravity
	 */
	public static void showMessage(Activity act, String text, int duration,
			int image, int gravity) {
		Toast toast = Toast.makeText(act, "   " + text, duration);
		toast.setGravity(gravity, 0, 0);
		LinearLayout toastView = (LinearLayout) toast.getView();
		ImageView imageCodeProject = new ImageView(act);
		imageCodeProject.setImageResource(image);

		toastView.setOrientation(LinearLayout.HORIZONTAL);
		toastView.setGravity(Gravity.CENTER_VERTICAL);
		toastView.addView(imageCodeProject, 0);
		toast.show();
	}

	/**
	 * 显示弹出框消息
	 * 
	 * @param act
	 * @param titleInt
	 * @param message
	 * @param iconInt
	 */
	public static void alert(Activity act, int titleInt, String message,
			int iconInt) {
		try {
			CustomDialog.Builder builder = new CustomDialog.Builder(act);
			builder.setTitle(titleInt);
			builder.setMessage(message);
			builder.setIcon(iconInt);

			builder.setNegativeButton(R.string.close, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 显示弹出框消息
	 * 
	 * @param act
	 * @param titleInt
	 * @param message
	 * @param iconInt
	 */
	public static void alert(Activity act, int titleInt, int messageInt,
			int iconInt) {
		try {
			CustomDialog.Builder builder = new CustomDialog.Builder(act);
			builder.setTitle(titleInt);
			builder.setMessage(messageInt);
			builder.setIcon(iconInt);

			builder.setNegativeButton(R.string.close, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送App异常崩溃报告
	 * 
	 * @param cont
	 * @param crashReport
	 */
	public static void sendAppCrashReport(final Context cont,
			final String crashReport) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cont);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.app_error);
		builder.setMessage(R.string.app_error_message);
		// builder.setPositiveButton(R.string.submit_report,
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int which) {
		// dialog.dismiss();
		// // 发送异常报告
		// Intent i = new Intent(Intent.ACTION_SEND);
		// // i.setType("text/plain"); //模拟器
		// i.setType("message/rfc822"); // 真机
		// i.putExtra(Intent.EXTRA_EMAIL,
		// new String[] { "ljtemail@qq.com" });
		// i.putExtra(Intent.EXTRA_SUBJECT,
		// cont.getString(R.string.app_name)
		// + " Android系统 - 错误报告");
		// i.putExtra(Intent.EXTRA_TEXT, crashReport);
		// cont.startActivity(Intent.createChooser(i, "发送错误报告"));
		// // 退出
		// AppManager.getAppManager().AppExit(cont);
		// }
		// });
		builder.setNegativeButton(R.string.sure,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 退出
						AppManager.getAppManager().AppExit(cont);
					}
				});
		builder.show();
	}

}
