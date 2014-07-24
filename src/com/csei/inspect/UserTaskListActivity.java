package com.csei.inspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RunnableFuture;
import java.util.prefs.Preferences;

import org.csei.database.entity.TaskCell;
import org.csei.database.service.imp.TaskCellServiceDao;
import org.json.JSONException;

import com.cesi.client.CasClient;
import com.csei.entity.Employer;
import com.csei.inspect.ActionHistoryActivity.UploadFileThread;
import com.csei.inspect.UserTablesOperationsActivity.MySimpleCursorAdapter;
import com.csei.util.JsonParser;
import com.csei.util.Tools;

import org.whut.inspect.R;

import com.readystatesoftware.viewbadger.BadgeView;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class UserTaskListActivity extends Activity {
	private ListView listView;
	private ProgressDialog dialog;
	private List<Map<String, Object>> list;
	private SimpleAdapter simpleAdapter;
	private Employer employer;
	private Cursor cursor;
	private SharedPreferences preference;  
	private TaskCellServiceDao userServiceDao;
	private SimpleCursorAdapter cursorAdapter;
	private ImageView imageView;
	private BadgeView badge1;
	private int unuploadnum;
	private Builder builder;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.activity_usertasklist);
		com.csei.application.MyApplication.getInstance().addActivity(this);
		listView=(ListView)findViewById(R.id.usertasklist_lv);
		imageView=(ImageView) findViewById(R.id.usertasklist_igv_upload);
		userServiceDao=new TaskCellServiceDao(UserTaskListActivity.this);
		//任务对话框
		builder = new AlertDialog.Builder(this);
		builder.setTitle("提示");
		// 设置对话框显示的内容
		builder.setMessage("是否再次编辑任务？");
		badge1 = new BadgeView(this, imageView);
        badge1.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
        imageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(UserTaskListActivity.this,
						ActionHistoryActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("employer", employer);
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
			}
		});
		
		employer = (Employer) getIntent().getExtras().getParcelable("employer");
		preference=getSharedPreferences("count", Context.MODE_PRIVATE);
		String string=preference.getString("taskflag", "00-00-00");
		//对话框初始化
		dialog_init();
		if (Tools.GetCurrentDate().equals(string)) {//直接从数据库中获得
			//查询未完成的任务，进行UI显示
			//查询已完成但未上传的
			int unuploadnum=((Cursor)userServiceDao.GetCurrentUnuploadNum(employer.getName(), Tools.GetCurrentDate(), "已完成","未上传")).getCount();
			badge1.setText(""+unuploadnum);badge1.show();
			cursor=userServiceDao.GetCurrentTask(employer.getName(), Tools.GetCurrentDate(),null);
			cursorAdapter = new MySimpleCursorAdapter(
					UserTaskListActivity.this , R.layout.usertasklist_lv_item, cursor 
					, new String[]{"taskname" , "devicename","timeslot","finishflag"}
					, new int[]{R.id.usertasklist_item_tv_task, R.id.usertasklist_item_tv_device,R.id.usertasklist_item_tv_deadline,R.id.usertasklist_item_btn_finishflag}){
			};
			listView.setAdapter(cursorAdapter);
			dialog.dismiss();
		}else {
			//获得任务列表
			new Thread(new getTaskThread()).start();
			
		}
//		//设置listview点击事件
//		listview_setclick();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		datachange();
	}
	
	
	private void listview_setclick() {
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(UserTaskListActivity.this,
						TagValidateActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("tbname", (String)list.get(position).get("tableName"));
				bundle.putInt("count", 1);
				bundle.putString("username", (String)list.get(position).get("userName"));
				bundle.putInt("uid", (Integer)list.get(position).get("userId"));
				intent.putExtras(bundle);
				startActivity(intent);
//				finish();
			}
		});
	}


	private void dialog_init() {
		dialog = new ProgressDialog(this);
		dialog.setTitle(getResources().getString(R.string.dialog_title));
		dialog.setMessage(getResources().getString(R.string.dialog_loadtask));
		dialog.setIndeterminate(false);
		dialog.setCancelable(true);
		dialog.show();
	}
	//获取任务线程
	class getTaskThread implements Runnable{
		

		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			String msg=CasClient.getInstance().doGet(getResources().getString(R.string.usertasklist_GETTASKDATA));
			Log.i("msg", msg);
			try {
				list=JsonParser.getTaskList(msg);
				//当天已同步任务数据标志
				Editor editor=preference.edit();
		        editor.putString("taskflag", Tools.GetCurrentDate());
		        editor.commit();
			} catch (JSONException e) {
				e.printStackTrace();
		}
			//因为用户可以跨越自己的点检任务，所以不能再根据行数据存在与否
			//判断任务是否已在数据库创建行数据，通过点检项目创建的，若有则只需添加任务名、设备名、时间段
			
			ArrayList<String> arrayList=userServiceDao.GetCurrentProjectNum(Tools.GetCurrentDate(), employer.getName());
			if (null==arrayList) {//没有行数据
				for (Map<String, Object> item : list) {
					userServiceDao.addUser(new TaskCell(employer.getName(),
							(String)item.get("tableName"),
							(String)item.get("planName"),
							(String)item.get("deviceName"),
							Tools.GetCurrentDate(),
							(String)item.get("deadline"),
							null,
							"未完成",
							"未上传","待做任务"));
					Log.i("usertask", "插入行数据");
				}
			}
			else {//部分有数据或全部
//				if (list.size()!=arrayList.size()) {//部分有数据
					for (Map<String, Object> item : list) {
						if (!arrayList.contains(item.get("tableName"))) {//插入操作
							userServiceDao.addUser(new TaskCell(employer.getName(),
									(String)item.get("tableName"),
									(String)item.get("planName"),
									(String)item.get("deviceName"),
									Tools.GetCurrentDate(),
									(String)item.get("deadline"),
									null,
									"未完成",
									"未上传","待做任务"));
							Log.i("usertask", "插入行数据");
						}
						else {//更新数据库
							userServiceDao.UpdateUserTask(employer.getName(), (String)item.get("tableName"), Tools.GetCurrentDate(), (String)item.get("deviceName"), (String)item.get("deadline"),"两者");
							Log.i("usertask", "更新行数据");
						}
					}
//				}
			}
			
			//查询未完成的任务，进行UI显示
			cursor=userServiceDao.GetCurrentTask(employer.getName(), Tools.GetCurrentDate(),null);
			//UI操作
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					dialog.dismiss();
					cursorAdapter = new MySimpleCursorAdapter(
							UserTaskListActivity.this , R.layout.usertasklist_lv_item, cursor 
							, new String[]{"taskname" , "devicename","timeslot","finishflag"}
							, new int[]{R.id.usertasklist_item_tv_task, R.id.usertasklist_item_tv_device,R.id.usertasklist_item_tv_deadline,R.id.usertasklist_item_btn_finishflag});
					listView.setAdapter(cursorAdapter);
				}
			});
		}
	}
	
	class MySimpleCursorAdapter extends SimpleCursorAdapter
	{
		public MySimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
		}
		//itemlayout点击事件
		@Override
		public View getView(final int position, View convertView,
				final ViewGroup parent) {
			convertView =  super.getView(position, convertView, parent);
			final Button btn_finishflag = (Button) convertView.findViewById(R.id.usertasklist_item_btn_finishflag);
			if (btn_finishflag.getText().equals("未完成")) {
				btn_finishflag.setBackgroundResource(R.color.myred);
			}
			else btn_finishflag.setBackgroundResource(R.drawable.btn_uploadfile);
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(btn_finishflag.getText().equals("已完成")){
						builder.setPositiveButton("确定"
							, new OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									Intent intent = new Intent(UserTaskListActivity.this,
											TagValidateActivity.class);
									cursor.moveToPosition(position);
									Bundle bundle = new Bundle();
									bundle.putString("tbname", cursor.getString(cursor.getColumnIndex("tablename")));
									bundle.putInt("count", 1);
									bundle.putString("username",employer.getName());
									bundle.putInt("uid",Integer.parseInt(employer.getNumber()));
									intent.putExtras(bundle);
									startActivity(intent);
								}
							});
						builder.setNegativeButton("取消",null);
						builder.create().show();
					}
					else {
					Intent intent = new Intent(UserTaskListActivity.this,
							TagValidateActivity.class);
					cursor.moveToPosition(position);
					Bundle bundle = new Bundle();
					bundle.putString("tbname", cursor.getString(cursor.getColumnIndex("tablename")));
					bundle.putInt("count", 1);
					bundle.putString("username",employer.getName());
					bundle.putInt("uid",Integer.parseInt(employer.getNumber()));
					intent.putExtras(bundle);
					startActivity(intent);
					}
				}
			});
			return convertView;
		}
		
	}
	
	private void datachange(){
		//查询并获得表格
		cursor=userServiceDao.GetCurrentTask( employer.getName(),Tools.GetCurrentDate(), null);
		Log.i("msg", ""+cursor.getCount());
		//查询已完成但未上传的
		unuploadnum=((Cursor)userServiceDao.GetCurrentUnuploadNum(employer.getName(), Tools.GetCurrentDate(), "已完成","未上传")).getCount();
		badge1.setText(""+unuploadnum);badge1.show();
		cursorAdapter = new MySimpleCursorAdapter(
				UserTaskListActivity.this , R.layout.usertasklist_lv_item, cursor 
				, new String[]{"taskname" , "devicename","timeslot","finishflag"}
				, new int[]{R.id.usertasklist_item_tv_task, R.id.usertasklist_item_tv_device,R.id.usertasklist_item_tv_deadline,R.id.usertasklist_item_btn_finishflag});
		listView.setAdapter(cursorAdapter);
	}
}
