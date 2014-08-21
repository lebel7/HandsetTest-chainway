package com.chainway.ht.filebrowser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.chainway.ht.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileManagerActivity extends ListActivity {

	public static String FILE_PATH_ACTION = "com.chainway.download.filepath";

	private List<IconifiedText> directoryEntries = new ArrayList<IconifiedText>();
	private File currentDirectory = new File("/");
	private File myTmpFile = null;
	private int myTmpOpt = -1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		browseToRoot();

		this.setSelection(0);
	}

	// 浏览文件系统的根目录
	private void browseToRoot() {
		browseTo(new File("/"));
	}

	// 返回上一级目录
	private void upOneLevel() {
		if (this.currentDirectory.getParent() != null)
			this.browseTo(this.currentDirectory.getParentFile());
	}

	// 浏览指定的目录,如果是文件则进行打开操作
	private void browseTo(final File file) {
		this.setTitle(file.getAbsolutePath());
		if (file.isDirectory()) {
			this.currentDirectory = file;
			fill(file.listFiles());
		} else {
			fileOptMenu(file);
		}
	}

	// 这里可以理解为设置ListActivity的源
	private void fill(File[] files) {
		if (files == null) {
			Toast.makeText(FileManagerActivity.this,
					getString(R.string.strPermissionDeny), Toast.LENGTH_SHORT)
					.show();
			this.browseTo(this.currentDirectory.getParentFile());
			return;
		}
		// 清空列表
		this.directoryEntries.clear();

		// 添加一个当前目录的选项
		this.directoryEntries.add(new IconifiedText(
				getString(R.string.current_dir), getResources().getDrawable(
						R.drawable.folder)));
		// 如果不是根目录则添加上一级目录项
		if (this.currentDirectory.getParent() != null)
			this.directoryEntries.add(new IconifiedText(
					getString(R.string.up_one_level), getResources()
							.getDrawable(R.drawable.uponelevel)));

		Drawable currentIcon = null;
		for (File currentFile : files) {

			// 确保只显示文件名、不显示路径如：/sdcard/111.txt就只是显示111.txt
			int currentPathStringLenght = this.currentDirectory
					.getAbsolutePath().length();

			// 判断是一个文件夹还是一个文件
			if (currentFile.isDirectory()) {
				currentIcon = getResources().getDrawable(R.drawable.folder);
			} else {
				// 取得文件名
				String fileName = currentFile.getName();
				// 根据文件名来判断文件类型，设置不同的图标
				if (checkEndsWithInStringArray(fileName, getResources()
						.getStringArray(R.array.fileEndingImage))) {
					currentIcon = getResources().getDrawable(R.drawable.image);
				} else if (checkEndsWithInStringArray(fileName, getResources()
						.getStringArray(R.array.fileEndingWebText))) {
					currentIcon = getResources()
							.getDrawable(R.drawable.webtext);
				} else if (checkEndsWithInStringArray(fileName, getResources()
						.getStringArray(R.array.fileEndingPackage))) {
					currentIcon = getResources().getDrawable(R.drawable.packed);
				} else if (checkEndsWithInStringArray(fileName, getResources()
						.getStringArray(R.array.fileEndingAudio))) {
					currentIcon = getResources().getDrawable(R.drawable.audio);
				} else if (checkEndsWithInStringArray(fileName, getResources()
						.getStringArray(R.array.fileEndingVideo))) {
					currentIcon = getResources().getDrawable(R.drawable.video);
				} else {
					currentIcon = getResources().getDrawable(R.drawable.text);
				}

				currentPathStringLenght += 1;// 去除文件前面斜杠
			}

			Log.i("FileManagerActivity",
					"AbsolutePath=" + currentFile.getAbsolutePath());

			this.directoryEntries.add(new IconifiedText(currentFile
					.getAbsolutePath().substring(currentPathStringLenght),
					currentIcon));
		}
		Collections.sort(this.directoryEntries);
		IconifiedTextListAdapter itla = new IconifiedTextListAdapter(this);
		// 将表设置到ListAdapter中
		itla.setListItems(this.directoryEntries);
		// 为ListActivity添加一个ListAdapter
		this.setListAdapter(itla);
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// 取得选中的一项的文件名
		String selectedFileString = this.directoryEntries.get(position)
				.getText();

		if (selectedFileString.equals(getString(R.string.current_dir))) {
			// 如果选中的是刷新
			this.browseTo(this.currentDirectory);
		} else if (selectedFileString.equals(getString(R.string.up_one_level))) {
			// 返回上一级目录
			this.upOneLevel();
		} else {

			File clickedFile = null;
			clickedFile = new File(this.currentDirectory.getAbsolutePath()
					+ "/" + this.directoryEntries.get(position).getText());
			if (clickedFile != null)
				this.browseTo(clickedFile);
		}
	}

	// 通过文件名判断是什么类型的文件
	private boolean checkEndsWithInStringArray(String checkItsEnd,
			String[] fileEndings) {
		for (String aEnd : fileEndings) {
			if (checkItsEnd.endsWith(aEnd))
				return true;
		}
		return false;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 3, 0, "root").setIcon(R.drawable.goroot);
		menu.add(0, 4, 0, "up").setIcon(R.drawable.uponelevel);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case 3:
			this.browseToRoot();
			break;
		case 4:
			this.upOneLevel();
			break;
		}
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	// 处理文件，包括打开，重命名等操作
	public void fileOptMenu(final File file) {

		new AlertDialog.Builder(FileManagerActivity.this)
				.setTitle(R.string.file_title_sel_file)
				.setMessage(R.string.file_title_sel_file_confirm)
				.setPositiveButton(R.string.file_btn_confirm,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								// Toast.makeText(FileManagerActivity.this,
								// "选择文件:" + file.getAbsolutePath(),
								// Toast.LENGTH_SHORT).show();
								Intent intent = new Intent();
								intent.setAction(FILE_PATH_ACTION);
								intent.putExtra("filepath",
										file.getAbsolutePath());
								sendBroadcast(intent);
								FileManagerActivity.this.finish();
							}
						})
				.setNegativeButton(R.string.file_btn_cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Toast.makeText(FileManagerActivity.this,
										R.string.file_msg_cancel,
										Toast.LENGTH_SHORT).show();

							}
						}).show();
	}

	// 得到当前目录的绝对路径
	public String GetCurDirectory() {
		return this.currentDirectory.getAbsolutePath();
	}

	// 移动文件
	public void moveFile(String source, String destination) {
		new File(source).renameTo(new File(destination));
	}

}
