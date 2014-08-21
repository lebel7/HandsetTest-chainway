package com.chainway.ht.network;

import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.chainway.ht.UIHelper;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class HttpMultipartPost extends AsyncTask<String, Integer, String> {
	private Context context;
	private String filePath;
	private String postUrl;
	private ProgressDialog pd;
	protected long totalSize;

	public HttpMultipartPost(Context context, String filePath, String postUrl) {
		this.context = context;
		this.filePath = filePath;
		this.postUrl = postUrl;
	}

	@Override
	protected void onPreExecute() {
		pd = new ProgressDialog(context);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage(" ...");
		pd.setCancelable(false);
		pd.show();
	}

	@Override
	protected String doInBackground(String... params) {
		String serverResponse = null;

		HttpClient httpClient = new DefaultHttpClient();
		HttpContext httpContext = new BasicHttpContext();
		HttpPost httpPost = new HttpPost(postUrl);

		try {

			CustomMultipartEntity multipartContent = new CustomMultipartEntity(
					new CustomMultipartEntity.ProgressListener() {
						@Override
						public void transferred(long num) {
							publishProgress((int) ((num / (float) totalSize) * 100));
						}
					});

			Log.i("MY", "HttpMultipartPost.doInBackground filePath:" + filePath);

			multipartContent.addPart("data", new FileBody(new File(filePath)));
			totalSize = multipartContent.getContentLength();

			// Send it
			httpPost.setEntity(multipartContent);
			HttpResponse response = httpClient.execute(httpPost, httpContext);
			if (response.getStatusLine().getStatusCode() == 200) {
				serverResponse = EntityUtils.toString(response.getEntity());
			} else {
				serverResponse = response.getStatusLine().getStatusCode() + "";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return serverResponse;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		pd.setProgress((int) (progress[0]));
	}

	@Override
	protected void onPostExecute(String result) {
		System.out.println("result: " + result);
		Log.i("MY", "HttpMultipartPost.onPostExecute result：" + result);

		if (!result.toLowerCase().equals("error")) {

			UIHelper.ToastMessage(context, "finish");

			// UIHelper.showMain(context);

		} else {
			UIHelper.ToastMessage(context, "error：" + result);
		}
		pd.dismiss();

	}

	@Override
	protected void onCancelled() {
		System.out.println("cancle");
	}
}
