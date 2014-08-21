package com.chainway.ht.ui;

import com.chainway.ht.R;
import com.chainway.ht.R.layout;
import com.chainway.ht.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class AppSetActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_set);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_set, menu);
		return true;
	}

}
