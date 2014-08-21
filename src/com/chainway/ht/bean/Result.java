package com.chainway.ht.bean;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import com.chainway.ht.AppException;

/**
 *
 */
public class Result {

	private int errorCode;
	private String errorMessage;

	public boolean OK() {
		return errorCode == 1;
	}

	/**
	 * 
	 * @param
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static Result parse(String jsonString) throws IOException,
			AppException {
		Result result = null;

		try {
			JSONObject json = new JSONObject(jsonString);

			result = new Result();
			result.setErrorCode(json.optInt("errorCode"));
			result.setErrorMessage(json.optString("errorMessage"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;

	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		return String.format("RESULT: CODE:%d,MSG:%s", errorCode, errorMessage);
	}

}
