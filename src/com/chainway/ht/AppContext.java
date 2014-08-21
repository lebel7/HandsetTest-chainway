package com.chainway.ht;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.UUID;

import com.chainway.ht.services.FileService;
import com.chainway.ht.utils.FileUtils;
import com.chainway.ht.utils.MethodsCompat;
import com.chainway.ht.utils.StringUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

/**
 * 全局应用程序类：用于保存和调用全局应用配置及访问网络数据
 */
public class AppContext extends Application {

	private static final String TAG = "AppContext";

	public static final int NETTYPE_WIFI = 0x01;
	public static final int NETTYPE_CMWAP = 0x02;
	public static final int NETTYPE_CMNET = 0x03;

	public static int PAGE_SIZE = 20;// 默认分页大小
	private static final int CACHE_TIME = 60 * 60000;// 缓存失效时间

	private String saveImagePath;// 保存图片路径

	private Hashtable<String, Object> memCacheRegion = new Hashtable<String, Object>();

	HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
	private SoundPool soundPool;
	private float volumnRatio;
	private AudioManager am;

	// 一维条码数据队列
	public Queue<String> d1Queue = new LinkedList<String>();
	// 14443a标签扫描队列
	public Queue<String> a14443Queue = new LinkedList<String>();
	// 15693标签扫描队列
	public Queue<String> r15693Queue = new LinkedList<String>();
	// UHF标签扫描队列
	public Queue<String> uhfQueue = new LinkedList<String>();

	// Ping队列
	public Queue<String> pingQueue = new LinkedList<String>();
	// Ping服务是否停止
	public boolean pingStop = true;

	@Override
	public void onCreate() {

		super.onCreate();

		Log.i(TAG, "onCreate");

		// 注册App异常崩溃处理器
		Thread.setDefaultUncaughtExceptionHandler(AppException
				.getAppExceptionHandler());
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		// PAGE_SIZE= getResources().getInteger(R.integer.PAGE_SIZE);
		// 设置保存图片的路径
		// saveImagePath = getProperty(AppConfig.SAVE_IMAGE_PATH);
		// if (StringUtils.isEmpty(saveImagePath)) {
		// setProperty(AppConfig.SAVE_IMAGE_PATH,
		// AppConfig.DEFAULT_SAVE_IMAGE_PATH);
		// saveImagePath = AppConfig.DEFAULT_SAVE_IMAGE_PATH;
		// }

		soundPool = new SoundPool(10, AudioManager.STREAM_RING, 5);
		soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
		soundMap.put(2, soundPool.load(this, R.raw.serror, 1));
		am = (AudioManager) this.getSystemService(AUDIO_SERVICE);// 实例化AudioManager对象

		Log.i(TAG, "AppContext init");

		Intent intent = new Intent(this, FileService.class);
		startService(intent);

		FileUtils.createPath(AppConfig.DEFAULT_SAVE_PATH);

	}

	/**
	 * 获取设备型号
	 * 
	 * @return
	 */
	public String getDeviceNum() {
		return android.os.Build.MODEL;
	}

	/**
	 * 获取设备mac地址 此方法需要保证WIFI在本次开机以来曾经是打开过的，否则会返回null。
	 * 
	 * @return
	 */
	public String getLocalMacAddress() {
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return (info == null) ? "NULL" : info.getMacAddress();
	}

	/**
	 * 检测当前系统声音是否为正常模式
	 * 
	 * @return
	 */
	public boolean isAudioNormal() {
		AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
	}

	/**
	 * 应用程序是否发出提示音
	 * 
	 * @return
	 */
	public boolean isAppSound() {
		return isAudioNormal() && isVoice();
	}

	/**
	 * 是否发出提示音
	 * 
	 * @return
	 */
	public boolean isVoice() {
		String perf_voice = getProperty(AppConfig.CONF_VOICE);
		// 默认是开启提示声音
		if (StringUtils.isEmpty(perf_voice))
			return true;
		else
			return StringUtils.toBool(perf_voice);
	}

	/**
	 * 设置是否发出提示音
	 * 
	 * @param b
	 */
	public void setConfigVoice(boolean b) {
		setProperty(AppConfig.CONF_VOICE, String.valueOf(b));
	}

	/**
	 * 播放提示音
	 * 
	 * @param id
	 *            成功1，失败2
	 */
	public void playSound(int id) {

		float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_RING); // 返回当前AudioManager对象的最大音量值
		float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_RING);// 返回当前AudioManager对象的音量值
		volumnRatio = audioCurrentVolumn / audioMaxVolumn;

		if (isAppSound()) {

			try {
				soundPool.play(soundMap.get(id), volumnRatio, // 左声道音量
						volumnRatio, // 右声道音量
						1, // 优先级，0为最低
						0, // 循环次数，0无不循环，-1无永远循环
						1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
						);
			} catch (Exception e) {
				e.printStackTrace();

				UIHelper.ToastMessage(this, "playSound error");

			}

			Log.i(TAG, "playSound volumnRatio：" + volumnRatio
					+ " audioCurrentVolumn:" + audioCurrentVolumn
					+ " audioMaxVolumn:" + audioMaxVolumn + " soundMap:"
					+ soundMap.get(id));

		}
	}

	/**
	 * 检测网络是否可用
	 * 
	 * @return
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	/**
	 * 获取当前网络类型
	 * 
	 * @return 0：没有网络 1：WIFI网络 2：WAP网络 3：NET网络
	 */
	public int getNetworkType() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = networkInfo.getExtraInfo();
			if (!StringUtils.isEmpty(extraInfo)) {
				if (extraInfo.toLowerCase().equals("cmnet")) {
					netType = NETTYPE_CMNET;
				} else {
					netType = NETTYPE_CMWAP;
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = NETTYPE_WIFI;
		}
		return netType;
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
	 * 获取App安装包信息
	 * 
	 * @return
	 */
	public PackageInfo getPackageInfo() {
		PackageInfo info = null;
		try {
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace(System.err);
		}
		if (info == null)
			info = new PackageInfo();
		return info;
	}

	/**
	 * 获取App唯一标识
	 * 
	 * @return
	 */
	public String getAppId() {
		String uniqueID = getProperty(AppConfig.CONF_APP_UNIQUEID);
		if (StringUtils.isEmpty(uniqueID)) {
			uniqueID = UUID.randomUUID().toString();
			setProperty(AppConfig.CONF_APP_UNIQUEID, uniqueID);
		}
		return uniqueID;
	}

	/**
	 * 是否加载显示文章图片
	 * 
	 * @return
	 */
	public boolean isLoadImage() {
		String perf_loadimage = getProperty(AppConfig.CONF_LOAD_IMAGE);
		// 默认是加载的
		if (StringUtils.isEmpty(perf_loadimage))
			return true;
		else
			return StringUtils.toBool(perf_loadimage);
	}

	/**
	 * 设置是否加载文章图片
	 * 
	 * @param b
	 */
	public void setConfigLoadimage(boolean b) {
		setProperty(AppConfig.CONF_LOAD_IMAGE, String.valueOf(b));
	}

	/**
	 * 是否启动检查更新
	 * 
	 * @return
	 */
	public boolean isCheckUp() {
		String perf_checkup = getProperty(AppConfig.CONF_CHECKUP);
		// 默认是开启
		if (StringUtils.isEmpty(perf_checkup))
			return true;
		else
			return StringUtils.toBool(perf_checkup);
	}

	/**
	 * 设置启动检查更新
	 * 
	 * @param b
	 */
	public void setConfigCheckUp(boolean b) {
		setProperty(AppConfig.CONF_CHECKUP, String.valueOf(b));
	}

	/**
	 * 清除保存的缓存
	 */
	public void cleanCookie() {
		removeProperty(AppConfig.CONF_COOKIE);
	}

	/**
	 * 判断缓存数据是否可读
	 * 
	 * @param cachefile
	 * @return
	 */
	private boolean isReadDataCache(String cachefile) {
		return readObject(cachefile) != null;
	}

	/**
	 * 判断缓存是否存在
	 * 
	 * @param cachefile
	 * @return
	 */
	private boolean isExistDataCache(String cachefile) {
		boolean exist = false;
		File data = getFileStreamPath(cachefile);
		if (data.exists())
			exist = true;
		return exist;
	}

	/**
	 * 判断缓存是否失效
	 * 
	 * @param cachefile
	 * @return
	 */
	public boolean isCacheDataFailure(String cachefile) {
		boolean failure = false;
		File data = getFileStreamPath(cachefile);
		if (data.exists()
				&& (System.currentTimeMillis() - data.lastModified()) > CACHE_TIME)
			failure = true;
		else if (!data.exists())
			failure = true;
		return failure;
	}

	/**
	 * 清除app缓存
	 */
	public void clearAppCache() {
		// // 清除webview缓存
		// File file = CacheManager.getCacheFileBaseDir();
		// if (file != null && file.exists() && file.isDirectory()) {
		// for (File item : file.listFiles()) {
		// item.delete();
		// }
		// file.delete();
		// }
		deleteDatabase("webview.db");
		deleteDatabase("webview.db-shm");
		deleteDatabase("webview.db-wal");
		deleteDatabase("webviewCache.db");
		deleteDatabase("webviewCache.db-shm");
		deleteDatabase("webviewCache.db-wal");
		// 清除数据缓存
		clearCacheFolder(getFilesDir(), System.currentTimeMillis());
		clearCacheFolder(getCacheDir(), System.currentTimeMillis());
		// 2.2版本才有将应用缓存转移到sd卡的功能
		if (isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)) {
			clearCacheFolder(MethodsCompat.getExternalCacheDir(this),
					System.currentTimeMillis());
		}
		// 清除编辑器保存的临时内容
		Properties props = getProperties();
		for (Object key : props.keySet()) {
			String _key = key.toString();
			if (_key.startsWith("temp"))
				removeProperty(_key);
		}
	}

	/**
	 * 清除缓存目录
	 * 
	 * @param dir
	 *            目录
	 * @param numDays
	 *            当前系统时间
	 * @return
	 */
	private int clearCacheFolder(File dir, long curTime) {
		int deletedFiles = 0;
		if (dir != null && dir.isDirectory()) {
			try {
				for (File child : dir.listFiles()) {
					if (child.isDirectory()) {
						deletedFiles += clearCacheFolder(child, curTime);
					}
					if (child.lastModified() < curTime) {
						if (child.delete()) {
							deletedFiles++;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return deletedFiles;
	}

	/**
	 * 将对象保存到内存缓存中
	 * 
	 * @param key
	 * @param value
	 */
	public void setMemCache(String key, Object value) {
		memCacheRegion.put(key, value);
	}

	/**
	 * 从内存缓存中获取对象
	 * 
	 * @param key
	 * @return
	 */
	public Object getMemCache(String key) {
		return memCacheRegion.get(key);
	}

	/**
	 * 保存磁盘缓存
	 * 
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void setDiskCache(String key, String value) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = openFileOutput("cache_" + key + ".data", Context.MODE_PRIVATE);
			fos.write(value.getBytes());
			fos.flush();
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 获取磁盘缓存数据
	 * 
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public String getDiskCache(String key) throws IOException {
		FileInputStream fis = null;
		try {
			fis = openFileInput("cache_" + key + ".data");
			byte[] datas = new byte[fis.available()];
			fis.read(datas);
			return new String(datas);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 保存对象
	 * 
	 * @param ser
	 * @param file
	 * @throws IOException
	 */
	public boolean saveObject(Serializable ser, String file) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = openFileOutput(file, MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ser);
			oos.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				oos.close();
			} catch (Exception e) {
			}
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 读取对象
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public Serializable readObject(String file) {
		if (!isExistDataCache(file))
			return null;

		Log.i("MY", "readObject " + file);

		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = openFileInput(file);
			ois = new ObjectInputStream(fis);
			return (Serializable) ois.readObject();
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			e.printStackTrace();
			// 反序列化失败 - 删除缓存文件
			if (e instanceof InvalidClassException) {
				File data = getFileStreamPath(file);
				data.delete();
			}
		} finally {
			try {
				ois.close();
			} catch (Exception e) {
			}
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * 获取内存中保存图片的路径
	 * 
	 * @return
	 */
	public String getSaveImagePath() {
		return saveImagePath;
	}

	public boolean containsProperty(String key) {
		Properties props = getProperties();
		return props.containsKey(key);
	}

	public void setProperties(Properties ps) {
		AppConfig.getAppConfig(this).set(ps);
	}

	public Properties getProperties() {
		return AppConfig.getAppConfig(this).get();
	}

	public void setProperty(String key, String value) {
		AppConfig.getAppConfig(this).set(key, value);
	}

	public String getProperty(String key) {
		return AppConfig.getAppConfig(this).get(key);
	}

	public void removeProperty(String... key) {
		AppConfig.getAppConfig(this).remove(key);
	}

	public double getDensity(Activity act) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = act.getResources().getDisplayMetrics();
		return dm.density;
	}

	/**
	 * 打开输入法
	 * 
	 * @param act
	 */
	public void showInputMethod(final Activity act) {
		Handler mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) act
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 500);
	}

	/**
	 * 关闭输入法
	 * 
	 * @param act
	 */
	public void closeInputMethod(final Activity act) {
		Handler mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) act
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				// if (imm.isActive()) { //无效
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				// }
			}
		}, 500);
	}

	/**
	 * 保存扫描数据
	 * 
	 * @param fileName
	 * @param data
	 */
	public void saveRecords(String fileName, String data) {
		if (StringUtils.isEmpty(fileName) || StringUtils.isEmpty(data)) {
			return;
		}

		if (!FileUtils.checkSaveLocationExists()) {
			return;
		}

		boolean isCreate = false;

		if (!TextUtils.isEmpty(getProperty(fileName))) {
			long between_days = (System.currentTimeMillis() - Long
					.parseLong(getProperty(fileName))) / (1000 * 3600 * 24);

			if (between_days > 1) {
				isCreate = true;
			}

			Log.i(TAG, "saveRecords between_days:" + between_days);
		}

		setProperty(fileName, System.currentTimeMillis() + "");

		String path = AppConfig.DEFAULT_SAVE_PATH + "ScanLog/";

		if (!FileUtils.checkFilePathExists(path)) {
			FileUtils.createPath(path);
		}

		File logFile = new File(path + fileName);

		FileWriter fw = null;

		try {

			Log.i(TAG, "logFile.exists()=" + logFile.exists());

			if (!logFile.exists()) {

				logFile.createNewFile();

			}

			if (isCreate) {
				fw = new FileWriter(logFile, false);
			} else {
				fw = new FileWriter(logFile, true);
			}

			fw.append("\n******" + getLocalMacAddress() + "@"
					+ StringUtils.getTimeFormat(System.currentTimeMillis())
					+ "******\n" + data);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
				}
			}
		}

	}

	/**
	 * 用来判断服务是否运行.
	 * 
	 * @param context
	 * @param className
	 *            判断的服务名字
	 * @return true 在运行 false 不在运行
	 */
	public static boolean isServiceRunning(Context mContext, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(30);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {

			Log.i(TAG, "cls1=" + serviceList.get(i).service.getClassName()
					+ " cls2=" + className);

			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}

}
