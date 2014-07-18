package com.csei.inspect;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.csei.database.entity.TaskCell;
import org.csei.database.service.imp.TaskCellServiceDao;
import org.json.JSONException;

import com.cesi.client.CasClient;
import com.csei.adapter.MultipleChoiceCursorAdapter;
import com.csei.entity.Employer;
import com.csei.util.JsonParser;
import com.csei.util.Tools;
import com.example.viewpager.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class ActionHistoryActivity extends Activity {
	private ListView listView;
	private String SaveHistoryFilePath="/sdcard/inspect/History.dat";
	private SimpleCursorAdapter cursorAdapter;
	private Cursor cursor;
	private ProgressDialog pdDialog;
	private Employer employer;
	private TextView textView;
	private FrameLayout fav_bottom_bar1;
	private SimpleCursorAdapter mCursorAdapter;
	private int cb_visible;
	
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.activity_history);
		listView=(ListView) findViewById(R.id.actionhistory_lv);
		employer = (Employer) getIntent().getExtras().getParcelable("employer");
		cb_visible=View.GONE;
		ProgressInit();
		
		LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		View emptyView = inflater.inflate(R.layout.history_empty, (ViewGroup)findViewById(R.id.emptyList));
		emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		((ViewGroup)listView.getParent()).addView(emptyView); 
		listView.setEmptyView(emptyView);
		fav_bottom_bar1 = (FrameLayout) findViewById(R.id.fav_bottom_bar1);
		textView=(TextView) findViewById(R.id.history_tv_edit);
		textView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(textView.getText().toString().equals("编辑")){
					fav_bottom_bar1.setVisibility(View.VISIBLE);
					textView.setText("完成");
					cb_visible=View.VISIBLE;
//					mCursorAdapter.setVisibility(View.VISIBLE);
//					ButtonsOn = true;
					dataChanged();
				}else{
					fav_bottom_bar1.setVisibility(View.GONE);
					textView.setText("编辑");
					cb_visible=View.GONE;
//					mCursorAdapter.setVisibility(View.INVISIBLE);
//					ButtonsOn = false;
					dataChanged();
			}
			}
			});
		
		try {
			TaskCellServiceDao userServiceDao=new TaskCellServiceDao(getApplicationContext());
			cursor=userServiceDao.QueryHistory1(employer.getName(),Tools.GetCurrentDate());
			} catch (Exception e) {
			e.printStackTrace();
		}
		mCursorAdapter = new SimpleCursorAdapter(
				ActionHistoryActivity.this , R.layout.history_item, cursor 
				, new String[]{"tablename" , "devicename","finishtime","uploadflag"}
				, new int[]{R.id.history_item_tv_tablename , R.id.history_item_tv_devicename,R.id.history_item_tv_finishtime,R.id.history_item_btn_finishflag}){
			//itemlayout点击事件
			@Override
			public View getView(final int position, View convertView,
					final ViewGroup parent) {
				convertView =  super.getView(position, convertView, parent);
				
				final Button btn_finishflag = (Button) convertView.findViewById(R.id.history_item_btn_finishflag);
				CheckBox checkBox=(CheckBox) convertView.findViewById(R.id.history_item_cb);
				if (cb_visible==View.GONE) {
					checkBox.setVisibility(cb_visible);
					btn_finishflag.setVisibility(View.VISIBLE);
				}
				else {
					checkBox.setVisibility(cb_visible);
					btn_finishflag.setVisibility(View.GONE);
				}
				if (btn_finishflag.getText().equals("未完成")) {
					btn_finishflag.setBackgroundResource(R.color.myred);
				}
				else btn_finishflag.setBackgroundResource(R.drawable.btn_uploadfile);
				btn_finishflag.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction()==MotionEvent.ACTION_DOWN) {
							v.setBackgroundResource(R.drawable.btn_uploadfile_down);
							pdDialog.show();
							new Thread(new UploadFileThread()).start();
						}
						if (event.getAction()==MotionEvent.ACTION_UP) {
							v.setBackgroundResource(R.drawable.btn_uploadfile);
						}
						return false;
					}
		});
		return convertView;
		};
	};
			//显示数据
		listView.setAdapter(mCursorAdapter);
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
								pdDialog.dismiss();
								Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
							}
							else{//上传失败
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
	
	
	private void dataChanged() {  
		mCursorAdapter.notifyDataSetChanged();  
	}
	
	
}
