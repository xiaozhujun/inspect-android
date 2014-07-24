package com.csei.inspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.csei.database.entity.TaskCell;
import org.csei.database.service.imp.TaskCellServiceDao;

import com.cesi.analysexml.DbModel;
import com.cesi.analysexml.ParseXml;
import com.csei.entity.Employer;
import com.csei.inspect.ActionHistoryActivity.UploadFileThread;
import com.csei.inspect.UserOperationsActivity.HandleTaskThread;
import com.csei.util.Tools;
import org.whut.inspect.R;
import com.readystatesoftware.viewbadger.BadgeView;

import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class UserTablesOperationsActivity extends Activity {
	private ProgressDialog pdDialog;
	private Employer employer;
	private String fileDir = Environment.getExternalStorageDirectory()
			.toString();
	private String[] result;
	private ListView rolestablelist;
	private int count;
	private ImageView imageView;
	private BadgeView badge1;
	private MySimpleCursorAdapter listItemAdapter;
	private Cursor cursor;
	private int unuploadnum;
	private TaskCellServiceDao serviceDao;
	public SharedPreferences preferences;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_usertablesoperation);
		rolestablelist = (ListView) findViewById(R.id.activity_usertables_lv);
		imageView=(ImageView) findViewById(R.id.usertableoperation_igv_upload);
		badge1 = new BadgeView(this, imageView);
        badge1.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
        serviceDao=new TaskCellServiceDao(UserTablesOperationsActivity.this);
        preferences=getSharedPreferences("count", Context.MODE_PRIVATE);
		// 加载文件对话框初始化
		ProgressInit();
		// 处理数据
		try {
			handledata();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		imageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(UserTablesOperationsActivity.this,
						ActionHistoryActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("employer", employer);
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
			}
		});

	}

	@Override
	public void onResume()
	{
		super.onResume();
		datachange();
		
		
	}
	
	
	private void ProgressInit() {
		pdDialog = new ProgressDialog(this);
		pdDialog.setMax(100);
		pdDialog.setTitle(getResources().getString(R.string.dialog_title));
		pdDialog.setMessage(getResources().getString(R.string.dialog_loadfile));
		pdDialog.setCancelable(false);
		pdDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pdDialog.setIndeterminate(false);
		pdDialog.show();
	}


	private void handledata() throws Exception {
		employer = (Employer) getIntent().getExtras().getParcelable("employer");
		new Thread(new HandleDataThread()).start();
	}

	private void datachange(){
		//查询未完成的表格
		cursor=serviceDao.GetCurrentProject( employer.getName(),Tools.GetCurrentDate(), null);
		//查询已完成但未上传的
		unuploadnum=((Cursor)serviceDao.GetCurrentUnuploadNum(employer.getName(), Tools.GetCurrentDate(), "已完成","未上传")).getCount();;
		badge1.setText(""+unuploadnum);
		badge1.show();
		listItemAdapter = new MySimpleCursorAdapter(
				UserTablesOperationsActivity.this, R.layout.rolestable,
				cursor, new String[] {"tablename","finishflag" },
				new int[] { R.id.ItemText,R.id.usertableoperation_item_btn_finishflag });
//		listItemAdapter.notifyDataSetChanged();
		rolestablelist.setAdapter(listItemAdapter);
	}
	
	class MySimpleCursorAdapter extends SimpleCursorAdapter{

		public MySimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
		}
		
		//itemlayout点击事件
		@Override
		public View getView(final int position, View convertView,
				final ViewGroup parent) {
			convertView =  super.getView(position, convertView, parent);
			final TextView textView=(TextView)convertView.findViewById(R.id.ItemText);
			convertView.setOnClickListener(new View.OnClickListener() {
				//item点检事件
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(UserTablesOperationsActivity.this,
							TagValidateActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("tbname", (String)textView.getText());
					bundle.putInt("count", 1);
					bundle.putString("username", employer.getName());
					bundle.putInt("uid", Integer.parseInt(employer.getNumber()));
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
			final Button btn_finishflag = (Button) convertView.findViewById(R.id.usertableoperation_item_btn_finishflag);
			if (btn_finishflag.getText().equals("未完成")) {
				btn_finishflag.setBackgroundResource(R.color.myred);
				convertView.setEnabled(true);
			}
			else {
				btn_finishflag.setBackgroundResource(R.drawable.btn_uploadfile);
				convertView.setEnabled(false);
			}
			return convertView;
		}
	}
	
	
	class HandleDataThread implements Runnable 
	{
		@Override
		public void run() {
			if (!preferences.getString("currentProjectflag", "00-00-00").equals(Tools.GetCurrentDate())) {
				//根据当天时间和用户名字段 查询数据 如何是零则说明还没有创建行数据
				//有创建行数据，但未完成
				ParseXml p = new ParseXml();
				String filename = fileDir + "/RolesTable.xml";
				final List<DbModel> list = p.parseRolesTable(filename, Integer.parseInt(employer.getRoleNum()));
				result = new String[list.size()];count = 0;
				for (DbModel dbModel : list) {
					result[count++] = dbModel.getTableitem();
				}
				
				final ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
				ArrayList<String> projectnum=serviceDao.GetCurrentProjectNum(Tools.GetCurrentDate(), employer.getName());
				int num;
				if (null==projectnum) {
					num=0;
				}
				else num=projectnum.size();
				if (num==0) {//进行插入数据操作
					for (String tablename : result) {
						serviceDao.addUser(new TaskCell(employer.getName(),
								tablename,null,null,Tools.GetCurrentDate(),null,null,"未完成","未上传","点检项目"));
					}
				}
				else {//在判断是否有未完成的点检表
//					if (num!=result.length) {//说明表格未完全创建 
						for (String tablename : result) {
							if (!projectnum.contains(tablename)) {//插入操作
								serviceDao.addUser(new TaskCell(employer.getName(),
										tablename,null,null,Tools.GetCurrentDate(),null,null,"未完成","未上传","点检项目"));
							}
							else {//更新操作
								
							}
//						}
					}
				}
			}
			
		
				//查询未完成的表格
				cursor=serviceDao.GetCurrentProject( employer.getName(),Tools.GetCurrentDate(), null);
				//查询已完成但未上传的
				unuploadnum=((Cursor)serviceDao.GetCurrentUnuploadNum(employer.getName(), Tools.GetCurrentDate(), "已完成","未上传")).getCount();
				runOnUiThread(new Runnable() {
					@SuppressWarnings("deprecation")
					@Override
					public void run() {
						pdDialog.dismiss();
						badge1.setText(""+unuploadnum);badge1.show();
						listItemAdapter = new MySimpleCursorAdapter(
								UserTablesOperationsActivity.this, R.layout.rolestable,
								cursor, new String[] {"tablename","finishflag" },
								new int[] { R.id.ItemText,R.id.usertableoperation_item_btn_finishflag });
						rolestablelist.setAdapter(listItemAdapter);
						rolestablelist.setOnItemClickListener(new OnItemClickListener() {
							@SuppressWarnings({ "unchecked" })
							public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
								view.setSelected(true);
								ListView listview = (ListView) parent;
								HashMap<String, String> map = (HashMap<String, String>) listview
										.getItemAtPosition(position);
								String tbname = map.get("ItemText");
								Intent intent = new Intent(UserTablesOperationsActivity.this,
										TagValidateActivity.class);
								Bundle bundle = new Bundle();
								bundle.putString("tbname", tbname);
								bundle.putInt("count", 1);
								bundle.putString("username", employer.getName());
								bundle.putInt("uid", Integer.parseInt(employer.getNumber()));
								intent.putExtras(bundle);
								startActivity(intent);
							}
						});
					}
				});
		}
}
}
