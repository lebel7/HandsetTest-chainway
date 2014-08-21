package com.chainway.ht.dao;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.ab.db.orm.dao.AbDBDaoImpl;
import com.chainway.ht.bean.Fingerprint;
import com.chainway.ht.db.DBSDHelper;

public class FingerprintSDDao extends AbDBDaoImpl<Fingerprint> {

	public FingerprintSDDao(SQLiteOpenHelper dbHelper) {
		super(dbHelper);
	}

	public FingerprintSDDao(Context context) {
		super(new DBSDHelper(context), Fingerprint.class);
	}

	/**
	 * 保存记录到数据库
	 * 
	 * @param f
	 */
	public void saveToDB(Fingerprint f) {
		if (f == null) {
			return;
		}

		insert(f);
	}

	/**
	 * 更新记录到数据库
	 * 
	 * @param f
	 */
	public void updateToDB(Fingerprint f) {
		if (f == null) {
			return;
		}
		update(f);
	}

	/**
	 * 查询记录
	 * 
	 * @param pageId
	 * @return
	 */
	public Fingerprint query(int pageId) {
		// 执行查询
		List<Fingerprint> list = queryList("pageId=?", new String[] { pageId
				+ "" });

		if (list == null || list.size() < 1) {
			return null;
		}

		return list.get(0);
	}
}
