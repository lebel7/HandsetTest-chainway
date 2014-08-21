package com.chainway.ht.db;

import java.io.File;

import android.content.Context;
import android.util.Log;

import com.ab.db.orm.AbSDDBHelper;
import com.chainway.ht.AppConfig;
import com.chainway.ht.bean.Fingerprint;

public class DBSDHelper extends AbSDDBHelper {
	private static final String TAG = "DBSDHelper";
	// 数据库名
	private static final String DBNAME = "chainway.db";
	// 数据库 存放路径
	private static final String DBPATH = "chainway/DB";

	// 当前数据库的版本
	private static final int DBVERSION = 1;
	// 要初始化的表
	private static final Class<?>[] clazz = { Fingerprint.class };

	public DBSDHelper(Context context) {

		super(context, DBPATH, DBNAME, null, DBVERSION, clazz);
		Log.i(TAG, "DBPATH:" + DBPATH);
	}

}
