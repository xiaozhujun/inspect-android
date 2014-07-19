package com.csei.inspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.csei.database.service.imp.TaskCellServiceDao;

import com.csei.entity.Employer;
import com.csei.util.Tools;
import com.example.viewpager.R;
import com.readystatesoftware.viewbadger.BadgeView;

import android.R.bool;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class UserOperationsActivity extends Activity {
	private GridView gridView;
	private SimpleAdapter adapterSimple;
	private Employer employer;
	private int projectNum;
	private int taskNum;
	private int unuploadNum;
	private TaskCellServiceDao cellServiceDao;
	private BadgeView bv_project;
	private BadgeView bv_task;
	private BadgeView bv_unupload;
	private boolean firstResume=true;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_useroperations);
		//获得传递过来的数据
		employer = (Employer) getIntent().getExtras().getParcelable("employer");

		cellServiceDao=new TaskCellServiceDao(getApplicationContext());
		//小图标初始化
		// gridview初始化
		gv_init();
		//设置gridview点击事件
//		gv_setclick();

	}

	private void BadgeView_init() {
		
	}

	private void gv_setclick() {

		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					project_click();
					break;
				case 1:
					task_click();
					break;
				case 2:
					history_click();
					break;
				default:
					break;
				}

			}

		});
	}

	private void history_click() {
		Intent intent = new Intent(UserOperationsActivity.this,
				ActionHistoryActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable("employer", employer);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	private void task_click() {
		Intent intent = new Intent(UserOperationsActivity.this,
				UserTaskListActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable("employer", employer);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	private void project_click() {
		Intent intent = new Intent(UserOperationsActivity.this,
				UserTablesOperationsActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable("employer", employer);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	private void gv_init() {
		gridView = (GridView) findViewById(R.id.useroperations_gv);
		ArrayList<HashMap<String, Object>> listItems = new ArrayList<HashMap<String, Object>>();
		// 设置gridview内容
		// 添加点检项目
		HashMap<String, Object> project_map = new HashMap<String, Object>();
		project_map.put("image", R.drawable.img_project);
		project_map
				.put("item", getResources().getString(R.string.text_project));
		listItems.add(project_map);
		// 添加待做任务
		HashMap<String, Object> task_map = new HashMap<String, Object>();
		task_map.put("image", R.drawable.img_task);
		task_map.put("item", getResources().getString(R.string.text_task));
		listItems.add(task_map);
		// 添加点检历史
		HashMap<String, Object> history_map = new HashMap<String, Object>();
		history_map.put("image", R.drawable.img_history);
		history_map
				.put("item", getResources().getString(R.string.text_history));
		listItems.add(history_map);
		//设置适配器
		String[] from = { "image", "item" };
		int[] to = { R.id.useroperations_gv_item_igv,
				R.id.useroperations_gv_item_tv };
		adapterSimple = new SimpleAdapter(this, listItems,
				R.layout.useroperations_gv_item, from, to){
			@Override
			public View getView(final int position, View convertView,
					ViewGroup parent) {
				View view=super.getView(position, convertView, parent);
				TextView textView=(TextView) view.findViewById(R.id.useroperations_gv_item_tv);
				switch ((String)textView.getText()) {
				case "点检历史":
					bv_unupload = new BadgeView(UserOperationsActivity.this, (ImageView) view.findViewById(R.id.useroperations_gv_item_igv));
			        bv_unupload.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
			        unuploadNum=((Cursor)cellServiceDao.GetCurrentUnuploadNum(employer.getName(), Tools.GetCurrentDate(), "已完成","未上传")).getCount();
					bv_unupload.setText(""+unuploadNum);
					bv_unupload.show();
					break;
				case "点检项目":
					bv_project = new BadgeView(UserOperationsActivity.this, (ImageView) view.findViewById(R.id.useroperations_gv_item_igv));
					bv_project.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
					projectNum=((Cursor)cellServiceDao.GetCurrentTask(employer.getName(), Tools.GetCurrentDate(), "未完成")).getCount();
					bv_project.setText(""+projectNum);
					bv_project.show();
					break;
				case "待做任务":
					bv_task = new BadgeView(UserOperationsActivity.this, (ImageView) view.findViewById(R.id.useroperations_gv_item_igv));
					bv_task.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
					taskNum=((Cursor)cellServiceDao.GetCurrentTask(employer.getName(), Tools.GetCurrentDate(), "未完成")).getCount();
					bv_task.setText(""+taskNum);
					bv_task.show();
					break;
				default:
					break;
				}
				
				view.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						switch (position) {
						case 0:
							project_click();
							break;
						case 1:
							task_click();
							break;
						case 2:
							history_click();
							break;
						default:
							break;
						}
					}
				});
				return view;
			}
		};
		gridView.setAdapter(adapterSimple);
		
		
		
	}
	
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
		if(keyCode == KeyEvent.KEYCODE_BACK)  
		{   
				//调用双击退出函数
				exitBy2Click();
		}  
		return false;  
	}  
	
	private static Boolean isExit = false;
	private void exitBy2Click() {  
		Timer tExit = null;  
		if (isExit == false) {  
			isExit = true; // 准备退出  
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();  
			tExit = new Timer();  
			tExit.schedule(new TimerTask() {  
				@Override  
				public void run() {  
					isExit = false; // 取消退出  
				}  
			}, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务  

		} else {  
			this.finish();
		}  
	}
	
	
	class GetUnfinishNum implements Runnable{

		@Override
		public void run() {
			projectNum=((Cursor)cellServiceDao.GetCurrentTask(employer.getName(), Tools.GetCurrentDate(), "未完成")).getCount();
			taskNum=((Cursor)cellServiceDao.GetCurrentTask(employer.getName(), Tools.GetCurrentDate(), "未完成")).getCount();
			unuploadNum=((Cursor)cellServiceDao.GetCurrentUnuploadNum(employer.getName(), Tools.GetCurrentDate(), "已完成","未上传")).getCount();
			
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					bv_project.setText(""+projectNum);bv_unupload.show();
					bv_task.setText(""+taskNum);bv_task.show();
					bv_unupload.setText(""+unuploadNum);bv_project.show();
				}
			});
		}
	}
	@Override
	public void onResume()
	{
		super.onResume();
		if (firstResume) {
			firstResume=false;
		}
		else datachange();
	}

	private void datachange() {
		new Thread(new GetUnfinishNum()).start();
	}
	
}
