package com.chainway.ht;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebSettings.TextSize;

public class AppConfig {
	private final static String APP_CONFIG = "config";
	public final static String TEMP_IMAGE = "temp_image";
	public final static String TEMP_NEWSLIST = "temp_newslist";
	public final static String CONF_APP_UNIQUEID = "APP_UNIQUEID";
	public final static String CONF_COOKIE = "cookie";
	public final static String CONF_LOAD_IMAGE = "perf_loadimage";
	public final static String CONF_CHECKUP = "perf_checkup";
	public final static String CONF_VOICE = "perf_voice";
	public final static String CONF_FONT_SIZE = "font_size";

	public final static String SAVE_IMAGE_PATH = "save_image_path";
	public final static String DEFAULT_SAVE_IMAGE_PATH = Environment
			.getExternalStorageDirectory()
			+ File.separator
			+ "Parking"
			+ File.separator;

	public final static String DEFAULT_SAVE_PATH = Environment
			.getExternalStorageDirectory()
			+ File.separator
			+ "chainway"
			+ File.separator;

	// 与服务器交互时间间隔，毫秒
	public final static int UP_TIME = 5000;
	// 服务器IP
	public static String SERVER_IP = "192.168.1.1";
	// 服务器端口
	public static int SERVER_PORT = 8080;
	// 数据库名称
	public final static String APP_DB_NAME = "ParkDB.db";

	private Context mContext;
	private static AppConfig appConfig;

	public static AppConfig getAppConfig(Context context) {
		if (appConfig == null) {
			appConfig = new AppConfig();
			appConfig.mContext = context;
		}
		return appConfig;
	}

	/**
	 * 获取Preference设置
	 */
	public static SharedPreferences getSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * 判断当前版本是否兼容目标版本的方法
	 * 
	 * @param VersionCode
	 * @return
	 */
	public static boolean isMethodsCompat(int VersionCode) {
		int currentVersion = android.os.Build.VERSION.SDK_INT;
		return currentVersion >= VersionCode;
	}

	/**
	 * 是否加载显示文章图片
	 */
	public static boolean isLoadImage(Context context) {
		return getSharedPreferences(context).getBoolean(CONF_LOAD_IMAGE, true);
	}

	public void setFontSize(TextSize size) {
		set(CONF_FONT_SIZE, size + "");
	}

	public String getCookie() {
		return get(CONF_COOKIE);
	}

	public String get(String key) {
		Properties props = get();
		return (props != null) ? props.getProperty(key) : null;
	}

	public Properties get() {
		FileInputStream fis = null;
		Properties props = new Properties();
		try {

			// 读取app_config目录下的config
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			fis = new FileInputStream(dirConf.getPath() + File.separator
					+ APP_CONFIG);

			props.load(fis);
		} catch (Exception e) {
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		return props;
	}

	private void setProps(Properties p) {
		FileOutputStream fos = null;
		try {

			// 把config建在(自定义)app_config的目录下
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			File conf = new File(dirConf, APP_CONFIG);
			fos = new FileOutputStream(conf);

			p.store(fos, null);
			fos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	public void set(Properties ps) {
		Properties props = get();
		props.putAll(ps);
		setProps(props);
	}

	public void set(String key, String value) {
		Log.i("MY", "AppConfig.set " + value);

		Properties props = get();
		props.setProperty(key, value);
		setProps(props);
	}

	public void remove(String... key) {
		Properties props = get();
		for (String k : key)
			props.remove(k);
		setProps(props);
	}

}
