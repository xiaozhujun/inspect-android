package com.csei.inspect;

import java.util.ArrayList;
import java.util.HashMap;

import com.csei.entity.Employer;
import com.example.viewpager.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

public class UserOperationsActivity extends Activity {
	private GridView gridView;
	private SimpleAdapter adapterSimple;
	private Employer employer;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_useroperations);
		//获得传递过来的数据
		employer = (Employer) getIntent().getExtras().getParcelable("employer");

		// gridview初始化
		gv_init();
		//设置gridview点击事件
		gv_setclick();

	}

	private void gv_setclick() {

		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Log.i("gridview", parent.toString());
				// Log.i("gridview", view.toString());
				// Log.i("gridview", ""+position);
				// Log.i("gridview", ""+id);
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
				R.layout.useroperations_gv_item, from, to);
		gridView.setAdapter(adapterSimple);
	}
}
