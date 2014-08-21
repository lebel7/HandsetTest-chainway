package com.chainway.ht.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class SFileList extends Base {
	private int pageSize;

	private List<SFile> list = new ArrayList<SFile>();

	public List<SFile> getFilelist() {
		return list;
	}

	public int getPageSize() {
		return pageSize;
	}

	public static SFileList parse(String jsonString) {
		SFileList locLst = new SFileList();

		try {
			JSONObject json = new JSONObject(jsonString);

			JSONArray jsonArray = json.getJSONArray("list");

			SFile file = null;

			for (int i = 0; i < jsonArray.length(); i++) {
				file = new SFile();
				file.setName(jsonArray.getJSONObject(i).optString("name"));
				file.setPath(jsonArray.getJSONObject(i).optString("path"));

				locLst.getFilelist().add(file);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		locLst.pageSize = locLst.getFilelist().size();

		return locLst;
	}

}
