package com.csei.inspect;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.csei.database.entity.User;
import org.csei.database.service.imp.UserServiceDao;
import org.json.JSONException;

import com.cesi.client.CasClient;
import com.csei.util.JsonParser;
import com.csei.util.Tools;
import com.example.viewpager.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class ActionHistoryActivity extends Activity {
	private ListView listView;
	private String SaveHistoryFilePath="/sdcard/inspect/History.dat";
	private SimpleCursorAdapter cursorAdapter;
	private Cursor cursor;
	private ProgressDialog pdDialog;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.activity_history);
		listView=(ListView) findViewById(R.id.actionhistory_lv);
		
		ProgressInit();
		
		LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		View emptyView = inflater.inflate(R.layout.history_empty, (ViewGroup)findViewById(R.id.emptyList));
		emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		((ViewGroup)listView.getParent()).addView(emptyView); 
		listView.setEmptyView(emptyView);
		
		try {
			UserServiceDao userServiceDao=new UserServiceDao(getApplicationContext());
			cursor=userServiceDao.QueryHistory1("赵伟");
			} catch (Exception e) {
			e.printStackTrace();
		}
		cursorAdapter = new SimpleCursorAdapter(
				ActionHistoryActivity.this , R.layout.history_item, cursor 
				, new String[]{"tablename" , "devicename","finishtime","uploadflag"}
				, new int[]{R.id.history_item_tv_tablename , R.id.history_item_tv_devicename,R.id.history_item_tv_finishtime,R.id.history_item_btn_finishflag}){
			//itemlayout点击事件
			@Override
			public View getView(final int position, View convertView,
					final ViewGroup parent) {
				convertView =  super.getView(position, convertView, parent);
				final Button btn_finishflag = (Button) convertView.findViewById(R.id.history_item_btn_finishflag);
				btn_finishflag.setOnTouchListener(new View.OnTouchListener() {
					
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction()==MotionEvent.ACTION_DOWN) {
							btn_finishflag.setBackgroundResource(R.drawable.btn_uploadfile_down);
//							Tools.SetDialogMsg(ActionHistoryActivity.this, getResources().getString(R.string.dialog_uploadfile));
//							Tools.DialogShow();
							pdDialog.show();
							new Thread(new UploadFileThread()).start();
						}
						if (event.getAction()==MotionEvent.ACTION_UP) {
							btn_finishflag.setBackgroundResource(R.drawable.btn_uploadfile);
						}
						return false;
					}
				});
				return convertView;
			}
		};
			//显示数据
		listView.setAdapter(cursorAdapter);
	}
	
	class UploadFileThread implements Runnable
	{
		@Override
		public void run() {
			try {
				final String msg = CasClient.getInstance().doSendFile2(getResources().getString(R.string.UPLOAD_FILE), getResources().getString(R.string.UPLOAD_FILLE_TEST));
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							if (JsonParser.UploadIsSuccess(msg)) {//上传成功
								//插入数据库操作
//								Tools.DialogDismiss();
								pdDialog.dismiss();
								Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
							}
							else{//上传失败
//								Tools.DialogDismiss();
								pdDialog.dismiss();
								Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			} 						
		}
	}
	
	private void ProgressInit() {
		pdDialog = new ProgressDialog(this);
		pdDialog.setMax(100);
		pdDialog.setTitle(getResources().getString(R.string.dialog_title));
		pdDialog.setMessage(getResources().getString(R.string.dialog_loadfile));
		pdDialog.setCancelable(false);
		pdDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pdDialog.setIndeterminate(false);
	}
	
}
