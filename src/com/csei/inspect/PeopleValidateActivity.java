package com.csei.inspect;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import com.cesi.analysexml.DbModel;
import com.cesi.analysexml.ParseXml;
import com.csei.entity.Employer;
import com.csei.entity.Listable;
import com.csei.service.RFIDService;
import com.csei.util.Tools;
import com.example.viewpager.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
@SuppressLint("HandlerLeak")
public class PeopleValidateActivity extends Activity implements OnClickListener{
	
	String Tag = "PeopleValidateActivity";
	Button backbutton;
	String str = "", result = "";       //result是一个全局变量，用于存放从RolesTable.xml中读取出来的值
	ListView rolestablelist;              //显示RolesTable.xml中读取值的ListView
	int index = 0;                        // 字符索引                                     
	int count = 0;                        // 按钮计数器
	MyRunnable myRunnable;
	Handler handler;
	Thread thread;
	int tag = 0;// 标签
	ProgressBar pb;
	TextView showprocess;                         //显示进度
	String fileDir;                               //文件夹基路径,这里默认为SdCard
	String filename;                              //文件名
	int cur_pos=0;                                  //用于高亮显示
	Button startScan;                               //开始扫描
	private MyBroadcast myBroadcast;				//广播接收者
	public static int cmd_flag = 0;				//操作状态  0为不做其他操作，1为寻卡，2为认证，3为读数据，4为写数据
	public static int authentication_flag = 0;		//认证状态  0为认证失败和未认证  1为认证成功
	private String activity = "com.csei.inspect.PeopleValidateActivity";
	//Debug
	public static String TAG= "M1card";
	volatile boolean Thread_flag = false;
	String username=null;
	int uid;
	String cardType="x1";
	private ProgressDialog shibieDialog; //识别搜索框
	private Timer timerDialog;  //搜索框计时器
	private Timer timeThread;
	private int MSG_FLAG = 1;
	//Dialog结束标识
	private int MSG_OVER = 2;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == MSG_FLAG){	
			}else if(msg.what == MSG_OVER){
				Toast.makeText(getApplicationContext(), "未识别到标签卡，请重试", Toast.LENGTH_SHORT).show();
			}
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.peoplevalidate);
	  init();
	}
	//初始化
	private void init() {
		  fileDir=Environment.getExternalStorageDirectory().toString();
		  rolestablelist = (ListView) findViewById(R.id.rolestablelist);
		  pb = (ProgressBar) findViewById(R.id.pb);
		  showprocess = (TextView) findViewById(R.id.showprocess);
		  backbutton=(Button) this.findViewById(R.id.backbutton);
		  startScan=(Button) this.findViewById(R.id.startScan);
		  startScan.setOnClickListener(this);
		  //返回按钮
		  backbutton.setOnClickListener(new OnClickListener() {
		  public void onClick(View v) {
					backbutton.setBackgroundResource(R.drawable.btn_back_active);
					System.exit(0);
				}
			});
	}
	//点击开启服务
	public void onClick(View v) {
		shibieDialog = new ProgressDialog(PeopleValidateActivity.this, R.style.mProgressDialog);
		shibieDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		shibieDialog.setMessage("识别标签中...");
		shibieDialog.setCancelable(false);
		shibieDialog.show();
		timerDialog = new Timer();
		//7秒后取消搜索
		timerDialog.schedule(new TimerTask() {
			@Override
			public void run() {
				shibieDialog.cancel();
				Intent stopSearch = new Intent();
				stopSearch.setAction("com.csei.service.RFIDService");
				stopSearch.putExtra("stopSearch", true);
				sendBroadcast(stopSearch);  //给服务发送广播,令服务停止
				Message msg = new Message();
				msg.what = MSG_OVER;
				mHandler.sendMessage(msg);
			}
		}, 7000);
   	 Intent service = new Intent(this, RFIDService.class);
   	service.putExtra("cardType", "0x01");
   	 service.putExtra("activity", activity);
   	 startService(service);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		timeThread = new Timer();
		timeThread.schedule(new TimerTask() {
			@Override
			public void run() {
//				String timeStr = Tools.getTime();
				Message msg = new Message();
				msg.what = MSG_FLAG;
				mHandler.sendMessage(msg);
			}
		}, 0 , 1000);
		myBroadcast = new MyBroadcast();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.csei.inspect.PeopleValidateActivity");
		registerReceiver(myBroadcast, filter); 		//注册广播接收者
		super.onResume();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		cmd_flag = 0;  				  //写状态恢复初始状态
		authentication_flag = 0;
		unregisterReceiver(myBroadcast);  //卸载广播接收者
		super.onPause();
		timeThread.cancel();
		Log.e("M1CARDPAUSE", "PAUSE");  	
	}	
	@Override
	protected void onDestroy() {
		Intent stopService = new Intent();
		stopService.setAction("com.csei.service.RFIDService");
		stopService.putExtra("stopflag", true);
		sendBroadcast(stopService);  //给服务发送广播,令服务停止
		Log.e(TAG, "send stop");
		super.onDestroy();
	}
	/**
	 *  广播接收者,接收服务发送过来的数据，并更新UI
	 * @author Administrator
	 *
	 */
	private class MyBroadcast extends BroadcastReceiver {
		
		private void setEmployer(Employer employer){
			shibieDialog.cancel();
			timerDialog.cancel();
			uid= Integer.parseInt(employer.getNumber());
			//获取UserName
			username=employer.getName();
			//读取RolesTable.xml
			getFile(Integer.parseInt(employer.getRoleNum()));
		    pb.setMax(result.length());// 进度条最大值设为文章的长度
			count++;
			    if (count % 2 == 1) {
			     myRunnable = new MyRunnable();
			     thread = new Thread(myRunnable);
			     thread.start();
			     // 启动线程
			     tag = 0;
			     startScan.setText("暂停扫描");
			    }
			    if (count % 2 == 0) {
			     tag = 1;
			     startScan.setText("开始扫描");
			    }   
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Listable listable = intent.getParcelableExtra("listable");
			if(listable == null){
				Toast.makeText(PeopleValidateActivity.this, "读卡失败!", Toast.LENGTH_SHORT).show();
				return;
			}else if(!(listable instanceof Employer)){
				Toast.makeText(PeopleValidateActivity.this, "卡类型错误，请读人员卡!", Toast.LENGTH_SHORT).show();
				return;
			}
			setEmployer((Employer)listable);
			 handler = new Handler() {
				   @Override
				   public void handleMessage(Message msg) {
				    super.handleMessage(msg);
				    switch (msg.what) {
				    case 1:
						drawRolesTableListView();
				       break; 
				    }
				   }
				 //渲染ListView,将根据在卡中读取的rid从RolesTable.xml文件中查找出的内容加载到ListView中
				private void drawRolesTableListView() {
					String[] s=result.split(",");
				      final ArrayList<HashMap<String, Object>> listItem=new ArrayList<HashMap<String,Object>>();
					     for(int i=0;i<s.length;i++){
					    	 HashMap<String, Object> map=new HashMap<String,Object>();
					    	 map.put("ItemImage",R.drawable.item);
					    	 map.put("ItemText", s[i]);
					    	 listItem.add(map);		 
					     }
					     SimpleAdapter listItemAdapter=new SimpleAdapter(PeopleValidateActivity.this,listItem,R.layout.rolestable,new String[]{"ItemImage","ItemText"},new int[]{R.id.ItemImage,R.id.ItemText});		          
					 index++;
				     pb.setProgress(index);
				     showprocess.setText("当前进度:" + index + "/" + result.length());
				     showprocess.setTextSize(15);
				     // 如果读取结束，则重新读取
				    if (index == result.length()) {
				       index = 0;	
						Toast.makeText(PeopleValidateActivity.this, "文件扫描完毕!", Toast.LENGTH_SHORT).show();   
				       rolestablelist.setAdapter(listItemAdapter);	       
				       rolestablelist.setOnItemClickListener(new OnItemClickListener() {
							@SuppressWarnings({ "unchecked" })
							public void onItemClick(AdapterView<?> parent, View view, int position,
									long id) {
								view.setSelected(true);                         
								ListView listview=(ListView) parent;
								HashMap<String,String>map=(HashMap<String, String>) listview.getItemAtPosition(position);
								String tbname=map.get("ItemText");				
								Intent intent=new Intent(PeopleValidateActivity.this,TagValidateActivity.class);
								Bundle bundle=new Bundle();
								bundle.putString("tbname", tbname);
								bundle.putInt("count", 1);
								bundle.putString("username", username);
								bundle.putInt("uid", uid);
								intent.putExtras(bundle);
								startActivityForResult(intent, 0);
							}
						});
				       tag=2;	   
				    }
				}
				  };
		}
	}
	//读取文件线程
	private class MyRunnable implements Runnable {
		  public void run() {
		   while(tag==0) {
		    handler.sendEmptyMessage(1);
		    try {
		     Thread.sleep(100);
		    } catch (InterruptedException e) {
		     e.printStackTrace();
		    }
		   }
		  }
}	      
	/**
	 * 写数据验证,用于验证写入数据
	 * @param src
	 * @return boolean
	 */
	public static boolean checkData(String src){
		boolean flag = false;
		String regString = "[a-f0-9A-F]{32}";
		flag = Pattern.matches(regString, src); //匹配数据，是否为32位的十六进制
		return flag;
	}
	// 得到文件内容的方法，返回一个字符串
		@SuppressWarnings("rawtypes")
		public String getFile(int rid) {          //获取人员点检信息
			Log.e("rid",rid+"");
			result="";
			ParseXml p=new ParseXml();
			filename=fileDir+"/RolesTable.xml";
				List<DbModel> list=p.parseRolesTable(filename,rid);
			    Iterator it=list.iterator();
			    while(it.hasNext()){
			    	DbModel d=(DbModel) it.next();
			    	result+=d.getTableitem()+",";
			    }
			return result;
		}
	}

