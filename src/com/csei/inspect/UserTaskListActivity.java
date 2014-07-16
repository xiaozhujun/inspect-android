package com.csei.inspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RunnableFuture;

import org.json.JSONException;

import com.cesi.client.CasClient;
import com.csei.util.JsonParser;
import com.example.viewpager.R;

import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class UserTaskListActivity extends Activity {
	private ListView listView;
	private ProgressDialog dialog;
	private List<Map<String, Object>> list;
	private SimpleAdapter simpleAdapter;
	
	
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.activity_usertasklist);
		listView=(ListView)findViewById(R.id.usertasklist_lv);
		//对话框初始化
		dialog_init();
		//获得任务列表
		new Thread(new getTaskThread()).start();
		//设置listview点击事件
		listview_setclick();
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
		@Override
		public void run() {
			String msg=CasClient.getInstance().doGet(getResources().getString(R.string.usertasklist_GETTASKDATA));
			Log.i("msg", msg);
			try {
				list=JsonParser.getTaskList(msg);
			} catch (JSONException e) {
				e.printStackTrace();
		}
		String[] from = { "image", "planName" , "deviceName", "deadline"};
			int[] to = { R.id.usertasklist_item_igv,
					R.id.usertasklist_item_tv_task, R.id.usertasklist_item_tv_device,R.id.usertasklist_item_tv_deadline};
			simpleAdapter = new SimpleAdapter(UserTaskListActivity.this, list,
					R.layout.usertasklist_lv_item, from, to);
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					dialog.dismiss();
					listView.setAdapter(simpleAdapter);
				}
			});
		}
	}
	
}
