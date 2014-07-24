package com.csei.inspect;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.csei.database.entity.TaskCell;
import org.csei.database.service.imp.TaskCellServiceDao;
import org.json.JSONException;
import org.whut.inspect.R;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.cesi.analysexml.DbModel;
import com.cesi.analysexml.ParseXml;
import com.cesi.client.CasClient;
import com.csei.entity.Employer;
import com.csei.util.JsonParser;
import com.csei.util.Tools;
import com.readystatesoftware.viewbadger.BadgeView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupWindow.OnDismissListener;

public class UserOperationsActivity extends Activity {
	private static final String KEY = "item";
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
	private ImageView igv_user;
	private ImageView igv_arrow;
	private PopupWindow window;
	private SharedPreferences preferences;
	private ProgressDialog dialog;
	
	
	public static BMapManager mapManager;
	public static Handler handler;
	private static String latitude;
	private static String longtitude;
	private static String city;
	private static String address;
	private static String user_Id;
	
	private TextView city_location;
	
	
	@SuppressLint("HandlerLeak")
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		
		mapManager = new BMapManager(getApplication());
		mapManager.init(new MKGeneralListener() {

			@Override
			public void onGetPermissionState(int arg0) {

				Toast.makeText(getApplicationContext(),
						"密钥错误", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onGetNetworkState(int arg0) {

				Toast.makeText(getApplicationContext(),

						"网络错误", Toast.LENGTH_SHORT).show();
			}
		});
		
		setContentView(R.layout.activity_useroperations);
		//取得用户ID
		user_Id = getIntent().getExtras().getString("userId");
		Log.i("msg", user_Id);
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if(msg.what==0){
					Toast.makeText(getApplicationContext(), "暂时无法获取地理位置信息！", Toast.LENGTH_SHORT).show();
				}else if(msg.what==1){
					latitude = ((String)(msg.obj)).split(";")[0];
					longtitude = ((String)(msg.obj)).split(";")[1];
				}else if(msg.what==2){
					address = msg.obj.toString().split(";")[0];
					city = msg.obj.toString().split(";")[1];
					city_location.setText("当前城市："+city);
				}else if(msg.what==3){
					Toast.makeText(getApplicationContext(), "已向服务器端发送地理位置信息！\n"+msg.obj.toString(), Toast.LENGTH_SHORT).show();
				}
				
			}
		};
		
		startServer();
		
		city_location = (TextView) findViewById(R.id.textView2);
		
		//获得传递过来的数据
		employer = (Employer) getIntent().getExtras().getParcelable("employer");
		cellServiceDao=new TaskCellServiceDao(getApplicationContext());
		igv_user=(ImageView) findViewById(R.id.useroperation_igv_user);
		igv_arrow=(ImageView) findViewById(R.id.useroperation_igv_arrow);
		igv_arrow.setBackgroundResource(R.drawable.icon_arrow_down);
		items=new ArrayList<Map<String, Object>>();
		preferences=getSharedPreferences("count", Context.MODE_PRIVATE);
		//小图标初始化
		// gridview初始化
		dialog_init();
		new Thread(new GetConfigFileThread()).start();
		//设置gridview点击事件
//		gv_setclick();
		
		igv_user.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				changPopState(v);
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
	
	private boolean isOpenPop = false;
	private ListView list;
	private ArrayList<Map<String, Object>> items;
	private void changPopState(View v) {
		isOpenPop = !isOpenPop;
		if (isOpenPop) {
			igv_arrow.setBackgroundResource(R.drawable.icon_arrow_up);
			popAwindow(v);
		} else {
			igv_arrow.setBackgroundResource(R.drawable.icon_arrow_down);
			if (window != null) {
				window.dismiss();
			}
		}
	}
	
	public ArrayList<Map<String, Object>> CreateData() {		
		Map<String, Object> map;
		map = new HashMap<String, Object>();
		map.put(KEY, employer.getName());
		items.add(map);
		map = new HashMap<String, Object>();
		map.put(KEY, "配置");
		items.add(map);
		map = new HashMap<String, Object>();
		map.put(KEY, "切换账号");
		items.add(map);	
		map = new HashMap<String, Object>();
		map.put(KEY, "退出");
		items.add(map);	
		return items;
	}
	
	OnItemClickListener popwindowlistClickListener=new OnItemClickListener() {
		@SuppressWarnings("unchecked")
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Map<String, Object> map=(Map<String, Object>) parent.getItemAtPosition(position);
			String cas = (String)map.get(KEY);
			if(cas.equals("切换账号")){
				CasClient.getInstance().logout();
				finish();
				startActivity(new Intent(UserOperationsActivity.this,LoginActivity.class));
			}
			else if(cas.equals("退出")){
				finish();
			}
		}
	};
	
	private void popAwindow(View parent) {
		if (window == null) {
			LayoutInflater lay = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = lay.inflate(R.layout.pop, null);
			list = (ListView) v.findViewById(R.id.pop_list);
			SimpleAdapter adapter = new SimpleAdapter(this, CreateData(),
					R.layout.pop_list_item, new String[] { KEY },
					new int[] { R.id.title });
			list.setAdapter(adapter);
			list.setItemsCanFocus(false);
			list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			list.setOnItemClickListener(popwindowlistClickListener);
			int x = (int) getResources().getDimension(R.dimen.pop_x);
			int y = (int) getResources().getDimension(R.dimen.pop_y);
			window = new PopupWindow(v, x, y);
		}
		window.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.main_popupwindow_user_bg));
		window.setFocusable(true);
		window.setOutsideTouchable(false);
		window.setOnDismissListener(new OnDismissListener() {
			public void onDismiss() {
				isOpenPop = false;
				igv_arrow.setBackgroundResource(R.drawable.icon_arrow_down);
			}
		});
		window.update();
		window.showAtLocation(parent, Gravity.RIGHT | Gravity.TOP,
				0, (int) getResources().getDimension(R.dimen.pop_layout_y));		
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
				String cas = (String)textView.getText();
				if(cas.equals("点检历史")){
					bv_unupload = new BadgeView(UserOperationsActivity.this, (ImageView) view.findViewById(R.id.useroperations_gv_item_igv));
			        bv_unupload.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
			        unuploadNum=((Cursor)cellServiceDao.GetCurrentUnuploadNum(employer.getName(), Tools.GetCurrentDate(), "已完成","未上传")).getCount();
					bv_unupload.setText(""+unuploadNum);
					bv_unupload.show();
				}
				else if(cas.equals("点检项目")){
					bv_project = new BadgeView(UserOperationsActivity.this, (ImageView) view.findViewById(R.id.useroperations_gv_item_igv));
					bv_project.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
					bv_project.setText(""+projectNum);
					bv_project.show();
				}
				else if(cas.equals("待做任务")){
					bv_task = new BadgeView(UserOperationsActivity.this, (ImageView) view.findViewById(R.id.useroperations_gv_item_igv));
					bv_task.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
					bv_task.setText(""+taskNum);
					bv_task.show();
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
		dialog.dismiss();
	}
	
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
		if(keyCode == KeyEvent.KEYCODE_BACK)  
		{   //调用双击退出函数
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
			//projectNum=((Cursor)cellServiceDao.GetCurrentProject(employer.getName(), Tools.GetCurrentDate(), "未完成")).getCount();
			taskNum=((Cursor)cellServiceDao.GetCurrentTask(employer.getName(), Tools.GetCurrentDate(), "未完成")).getCount();
			unuploadNum=((Cursor)cellServiceDao.GetCurrentUnuploadNum(employer.getName(), Tools.GetCurrentDate(), "已完成","未上传")).getCount();
			
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					//bv_project.setText(""+projectNum);bv_project.show();
					bv_task.setText(""+taskNum);bv_task.show();
					bv_unupload.setText(""+unuploadNum);bv_unupload.show();
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
	
	public void startServer(){
		Intent intent = new Intent(this,com.csei.service.LocationService.class);
		startService(intent);
	}

	public void stopServer(){
		Intent intent = new Intent(this,com.csei.service.LocationService.class);
		stopService(intent);
	}
	
	
	
	
	class HandleProjectDataThread implements Runnable 
	{
		private String fileDir=preferences.getString("configsavepath", "/sdcard/inspect/config");
		private String[] result;
		
		@Override
		public void run() {//判断是否已创建行数据
			if (((Cursor)cellServiceDao.GetCurrentProject(employer.getName(), Tools.GetCurrentDate(), null)).getCount()>0) {
				projectNum=((Cursor)cellServiceDao.GetCurrentProject(employer.getName(), Tools.GetCurrentDate(), null)).getCount();
				new Thread(new HandleTaskThread()).start();
			}
			else {
				//根据当天时间和用户名字段 查询数据 如何是零则说明还没有创建行数据
				//有创建行数据，但未完成
				ParseXml p = new ParseXml();
				String filename = fileDir + "/RolesTable.xml";
				final List<DbModel> list = p.parseRolesTable(filename, Integer.parseInt(employer.getRoleNum()));
				result = new String[list.size()];int count = 0;
				for (DbModel dbModel : list) {
					result[count++] = dbModel.getTableitem();
				}
				
				final ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
				ArrayList<String> projectnum=cellServiceDao.GetCurrentProjectNum(Tools.GetCurrentDate(), employer.getName());
				int num;
				if (null==projectnum) {
					num=0;
				}
				else num=projectnum.size();
				if (num==0) {//进行插入数据操作
					for (String tablename : result) {
						cellServiceDao.addUser(new TaskCell(employer.getName(),
								tablename,null,null,Tools.GetCurrentDate(),null,null,"未完成","未上传","点检项目"));
					}
				}
				else {//在判断是否有未完成的点检表
//					if (num!=result.length) {//说明表格未完全创建 
						for (String tablename : result) {
							if (!projectnum.contains(tablename)) {//插入操作
								cellServiceDao.addUser(new TaskCell(employer.getName(),
										tablename,null,null,Tools.GetCurrentDate(),null,null,"未完成","未上传","点检项目"));
							}
							else {//更新操作
								
							}
//						}
					}
				}
				//查询未上传
				projectNum=((Cursor)cellServiceDao.GetCurrentProject(employer.getName(), Tools.GetCurrentDate(), null)).getCount();;
				new Thread(new HandleTaskThread()).start();
				
				//标记当天已创建行数据
				Editor editor=preferences.edit();
		        editor.putString("currentProjectflag", Tools.GetCurrentDate());
		        editor.commit();
			}
		}
	
	}
	
	
	class GetConfigFileThread implements Runnable
	{
		private List<Map<String,Object>> confilelist;
		private InputStream inputStream;
		@Override
		public void run() {
			//首先获得文件列表数据，根据列表数据去获得文件地址
			if (!preferences.getString("currentConfigFileflag", "00-00-00").equals(Tools.GetCurrentDate())) {
				String msg=CasClient.getInstance().doPostNoParams(getResources().getString(R.string.GetConfigFileListAddress));
				//Log.i("doget", msg);
				confilelist=new ArrayList<Map<String,Object>>();
				try {
					confilelist=JsonParser.GetConfigFileList(msg);
				} catch (JSONException e) {
					Toast.makeText(getApplicationContext(), "数据解析错误", Toast.LENGTH_SHORT).show();
				}
				inputStream=CasClient.getInstance().DoGetFile(getResources().getString(R.string.GetRoleTableFileAddress));
				if(!Tools.SaveConfigFile(inputStream, "RolesTable.xml", preferences.getString("configsavepath", "/sdcard/inspect/config")))
				{Toast.makeText(getApplicationContext(), "下载配置文件错误", Toast.LENGTH_SHORT).show();}
				for (Map<String,Object> item : confilelist) {//下载文件操作
					inputStream=CasClient.getInstance().DoGetFile(getResources().getString(R.string.GetConfigFileBaseAddress)+(String)item.get("id"));
					if(!Tools.SaveConfigFile(inputStream, item.get("filename")+".xml", preferences.getString("configsavepath", "/sdcard/inspect/config")))
					{Toast.makeText(getApplicationContext(), "下载配置文件错误", Toast.LENGTH_SHORT).show();}
				}
				Editor editor=preferences.edit();
		        editor.putString("currentConfigFileflag", Tools.GetCurrentDate());
		        editor.commit();
			}
			new Thread(new HandleProjectDataThread()).start();
		}
	}
	
class HandleTaskThread implements Runnable{
	private List<Map<String, Object>> list;

		
		@Override
		public void run() {
			if (((Cursor)cellServiceDao.GetCurrentTask(employer.getName(), Tools.GetCurrentDate(), null)).getCount()>0) {
				//查询未完成的任务，进行UI显示
				taskNum=((Cursor)cellServiceDao.GetCurrentTask(employer.getName(), Tools.GetCurrentDate(), "未完成")).getCount();
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						gv_init();
					}
				});
			}
			else {
			String msg=CasClient.getInstance().doGet(getResources().getString(R.string.usertasklist_GETTASKDATA));
			Log.i("msg", msg);
			try {
				list=JsonParser.getTaskList(msg);
				//当天已同步任务数据标志
				Editor editor=preferences.edit();
		        editor.putString("currentTaskflag", Tools.GetCurrentDate());
		        editor.commit();
			} catch (JSONException e) {
				e.printStackTrace();
		}
			//因为用户可以跨越自己的点检任务，所以不能再根据行数据存在与否
			//判断任务是否已在数据库创建行数据，通过点检项目创建的，若有则只需添加任务名、设备名、时间段
			
			ArrayList<String> arrayList=cellServiceDao.GetCurrentProjectNum(Tools.GetCurrentDate(), employer.getName());
			if (null==arrayList) {//没有行数据
				for (Map<String, Object> item : list) {
					cellServiceDao.addUser(new TaskCell(employer.getName(),
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
							cellServiceDao.addUser(new TaskCell(employer.getName(),
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
							cellServiceDao.UpdateUserTask(employer.getName(), (String)item.get("tableName"), Tools.GetCurrentDate(), (String)item.get("deviceName"), (String)item.get("deadline"),"两者");
							Log.i("usertask", "更新行数据");
						}
					}
//				}
			}
			//查询未完成的任务，进行UI显示
			taskNum=((Cursor)cellServiceDao.GetCurrentTask(employer.getName(), Tools.GetCurrentDate(), "未完成")).getCount();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					gv_init();
				}
			});
		}
		}
}

public static class SendLocationThread implements Runnable{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("lng", longtitude);
		params.put("address", address);
		params.put("userId", user_Id);
		params.put("lat", latitude);
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("jsonString",com.csei.util.JsonUtils.HashToJson(params));
		Log.i("msg", com.csei.util.JsonUtils.HashToJson(params));
		String message = CasClient.getInstance().doPost("http://www.cseicms.com/inspectManagement/" +
				"rs/inspectLocate/receiveInspectLocateInfo",
				map);
		Message msg = Message.obtain();
		msg.obj = message;
		msg.what=3;
		handler.sendMessage(msg);
	}
}
	
}
