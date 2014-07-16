package com.csei.inspect;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.csei.database.entity.User;
import org.csei.database.service.imp.UserServiceDao;
import org.json.JSONException;

import com.cesi.analysexml.ParseXml;
import com.cesi.client.CasClient;
import com.csei.adapter.MyexpandableListAdapter;
import com.csei.entity.Employer;
import com.csei.entity.Listable;
import com.csei.entity.Tag;
import com.csei.service.RFIDService;
import com.csei.util.FileUtil;
import com.csei.util.JsonParser;
import com.csei.util.Tools;
import com.example.viewpager.R;
import com.example.viewpager.R.string;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
@SuppressLint("HandlerLeak")
public class TagValidateActivity extends Activity implements ExpandableListView.OnChildClickListener,ExpandableListView.OnGroupClickListener, OnClickListener{            
	RadioGroup inspectResult;          //�Ҳ�ĵ�����б�
    RadioButton checkRadioButton;
	int cur_pos=0;               //��Ҫ�����жϵ�ǰ��position����ʹ��ǰ��listview�е�Item����
	int cur_pos1=0;
	String username=null;         //��ȡ�����Ա
	int uid=0;                    //��ȡ�����ԱID
    String filename=null;          //��ȡ���?��ͬ�ĵ������ֲ�ͬ�ĵ����
	ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
	boolean isInspect=false;            //�Ƿ��ܵ�� ,Ĭ��Ϊfalse,�����ɨ���ǩ֮��,��Ϊtrue
	String fileDir=null;                //ָ�����ļ���ŵ�λ�õĸ�Ŀ¼  /data/data/com.example.viewpager/files/
	TextView inspecttable;              //��ʾ����
	String tag;                         //ɨ���ǩʱ����ѯ����Ӧxml�ļ��е�<location>��ֵ
	ParseXml p=new ParseXml();           //���ý���xml�ļ�����
	boolean Inspect=false;                //
	List<String> ScanedTag=new ArrayList<String>();    //����������ɨ���ı�ǩ
	String tagflag;
	TextView user;
	int isScaned;
	private ExpandableListView inspectItem;
	private ArrayList<String> groupList;
	private ArrayList<List<String>> childList;
	private MyexpandableListAdapter adapter;
	String tname;
	View inspectResultPane;
	RadioButton normal;
	RadioButton abnormal;
	RadioButton nothing;
	String itemItem;
	String groupItem;
	Button backbutton;
	TextView devnum;
	private ImageView arrow;
	private boolean isOpenPop = false;
	private PopupWindow window;
	private ListView list;
	public static final String KEY = "key";
	ArrayList<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
	Context mContext;
	Button beizhu;
	Button startScan;
	private MyBroadcast myBroadcast;				//�㲥������
	public static int cmd_flag = 0;				//����״̬  0Ϊ�������������1ΪѰ����2Ϊ��֤��3Ϊ����ݣ�4Ϊд���
	public static int authentication_flag = 0;		//��֤״̬  0Ϊ��֤ʧ�ܺ�δ��֤  1Ϊ��֤�ɹ�
	private String activity = "com.csei.inspect.TagValidateActivity";
		//Debug
	public static String TAG= "M1card";
	volatile boolean Thread_flag = false;
	String dnum;
	int areaid;
	private TextView title;
	String savefile="����";
	String exit="�˳�";
	String cardType="0x02";
	private ProgressDialog shibieDialog; //ʶ��������
	View view_Group;
	private Timer timerDialog;  //�������ʱ��
	private Timer timeThread;
	private int MSG_FLAG = 1;
	//Dialog�����ʶ
	private int MSG_OVER = 2;
	String beizhustr;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == MSG_FLAG){
				
			}else if(msg.what == MSG_OVER){
				Toast.makeText(getApplicationContext(), "δʶ�𵽱�ǩ����������", Toast.LENGTH_SHORT).show();
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	//��ʼ��
	private void init() {
		TextView textview = new TextView(this);
		setContentView(textview);
		Bundle bundle = getIntent().getExtras();
		//ģ�����ϵ�
		fileDir=Environment.getExternalStorageDirectory().toString();
		int count = bundle.getInt("count");                   //����ScanCardActivity������countֵ
		if (count != 0) {                                  //�����ǵ�һ����ת�����ҳ�棬�������һ�߼���������ʾ�Ƚ��������֤
		tname = bundle.getString("tbname");        
		                    //��ݲ�ͬ��tname���õ�filename
		
		username=bundle.getString("username").trim();
		uid=bundle.getInt("uid");
		
		filename=getFileNameByTableName(tname);  
		p.writeToFormatXml(filename);
		
		setContentView(R.layout.tagvalidate);                              //ʹ��tagvalidate.xml��Դ�ļ�
		//�����ǻ�ȡ��Ӧ����Դ
		inspecttable=(TextView) this.findViewById(R.id.tbname);
		inspecttable.setText(tname);
		inspectItem =  (ExpandableListView) this.findViewById(R.id.inspectItem);
		inspectResult = (RadioGroup) this.findViewById(R.id.insepctResult);
		checkRadioButton=(RadioButton) this.findViewById(inspectResult.getCheckedRadioButtonId());
		inspectResultPane=this.findViewById(R.id.inspectResultPane);
		normal=(RadioButton) this.findViewById(R.id.normal);
		abnormal=(RadioButton) this.findViewById(R.id.abnormal);
		nothing=(RadioButton) this.findViewById(R.id.nothing);
		backbutton=(Button) this.findViewById(R.id.backbutton);
		devnum=(TextView) this.findViewById(R.id.devnum);
		beizhu = (Button) this.findViewById(R.id.beizhu);
		startScan=(Button) this.findViewById(R.id.startScan);
		title=(TextView) this.findViewById(R.id.title);
		startScan.setOnClickListener(this);
		beizhu.setOnClickListener(new ClickEvent());
		user=(TextView) this.findViewById(R.id.username);
		user.setText(username);
		mContext=this;
		arrow = (ImageView) findViewById(R.id.arrow); 
		title.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
				// TODO Auto-generated method stub
		          changPopState(v);

			}
		});
		    backbutton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					backbutton.setBackgroundResource(R.drawable.btn_back_active);
					finish();
				}
			});
		InitData();
		adapter = new MyexpandableListAdapter(TagValidateActivity.this,groupList,childList);
		inspectItem.setAdapter(adapter);
		inspectItem.setOnChildClickListener(this);
		inspectItem.setOnGroupClickListener(this);
}
	}
	// ���?���¼�
		class ClickEvent implements OnClickListener {
			public void onClick(View v) {
				if (v == beizhu) {
					showRoundCornerDialog(TagValidateActivity.this, TagValidateActivity.this
							.findViewById(R.id.beizhu));	
				}
			}
		} 
		// ��ʾԲ�ǶԻ���
		@SuppressWarnings("deprecation")
		public void showRoundCornerDialog(Context context, View parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// ��ȡԲ�ǶԻ��򲼾�View��������ΪԲ��
			final View dialogView = inflater.inflate(R.layout.popupwindow, null,
					false);
			dialogView.setBackgroundResource(R.drawable.rounded_corners_view);
			// ���������Ի������õ����Ի���ı���ΪԲ��
			final PopupWindow pw = new PopupWindow(dialogView,LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT, true);
			pw.setOutsideTouchable(true);
			pw.setAnimationStyle(R.style.PopupAnimation);
			// ע��������豳������Ϊ�ص㲿�֣���������ע�͵�����һ���������豳���������鿴�Ի���Ч��
			final EditText edtUsername = (EditText) dialogView
					.findViewById(R.id.username_edit);
			//������ͨ������ǰ��������͵������������Ӧ�ı�ע
			edtUsername.setHint(beizhustr); // ������ʾ��
			// OK��ť���䴦���¼�
			TextView beizhutitle=(TextView) dialogView.findViewById(R.id.username_view);
			beizhutitle.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					//���������
					 InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
			         imm.hideSoftInputFromWindow(edtUsername.getWindowToken(),0);
				}
			});
			Button btnOK = (Button) dialogView.findViewById(R.id.BtnOK);
			btnOK.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// �����ı�������
					String comment=edtUsername.getText().toString();
					p.writeCommentToXml(filename, itemItem, comment,tag);
					pw.dismiss();
				}
			});
			// Cancel��ť���䴦���¼�
			Button btnCancel = (Button) dialogView.findViewById(R.id.BtnCancel);
			btnCancel.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					pw.dismiss();// �ر�
				}
			});
			// ��ʾRoundCorner�Ի���
			pw.showAtLocation(parent, Gravity.CENTER | Gravity.CENTER, 0, 0);
		}
	/**
	 * ���Pop״̬
	 * */
	public void changPopState(View v) {
		isOpenPop = !isOpenPop;
		if (isOpenPop) {
			arrow.setBackgroundResource(R.drawable.icon_arrow_up);
			popAwindow(v);
		} else {
			arrow.setBackgroundResource(R.drawable.icon_arrow_down);
			if (window != null) {
				window.dismiss();
			}
		}
	}
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
			list.setOnItemClickListener(listClickListener);
			// window = new PopupWindow(v, 260, 300);
			int x = (int) getResources().getDimension(R.dimen.pop_x);
			int y = (int) getResources().getDimension(R.dimen.pop_y);
			window = new PopupWindow(v, x, y);
		}
		window.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.pop_bg));
		window.setFocusable(true);
		window.setOutsideTouchable(false);
		window.setOnDismissListener(new OnDismissListener() {
			public void onDismiss() {
				// TODO Auto-generated method stub
				isOpenPop = false;
				arrow.setBackgroundResource(R.drawable.icon_arrow_down);
			}
		});
		window.update();
		window.showAtLocation(parent, Gravity.RIGHT | Gravity.TOP,
				0, (int) getResources().getDimension(R.dimen.pop_layout_y));
	}
	OnItemClickListener listClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map=(Map<String, Object>) parent.getItemAtPosition(position);
			if(map.get(KEY).equals(savefile)){
				Toast.makeText(mContext,"�ļ���"+map.get(KEY)+"", Toast.LENGTH_SHORT).show();
				writeToXmlUserDateDvnum(filename);
				//2014-7-14-郭知祥 添加上传代码 插入数据库操作
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								//上传功能使用测试文件
								String msg = CasClient.getInstance().doSendFile2(getResources().getString(R.string.UPLOAD_FILE), getResources().getString(R.string.UPLOAD_FILLE_TEST));
								Log.i("msg", msg);
								if (JsonParser.UploadIsSuccess(msg)) {//上传成功
									//插入数据库操作
									User mUser=new User(
											username,tname,null,"门座式起重机",null,null,Tools.GetCurrentTime(),"1","已上传");
									UserServiceDao serviceDao=new UserServiceDao(getApplicationContext());
									serviceDao.addUser(mUser);
									Log.i("msg", "已上传");
								}
								else{//上传失败
									User mUser=new User(
											username,tname,null,"门座式起重机",null,null,Tools.GetCurrentTime(),"1","未上传");
									UserServiceDao serviceDao=new UserServiceDao(getApplicationContext());
									serviceDao.addUser(mUser);
									Log.i("msg", "未上传");
								}
								
//								Looper.prepare();  
//								Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
//								Looper.loop();
							} catch (ClientProtocolException e) {
								e.printStackTrace();
							} catch (NotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							finish();
						}
					}).start();
					//����
			}else if(map.get(KEY).equals(exit)){
				Toast.makeText(mContext,"���!����"+map.get(KEY)+".......", Toast.LENGTH_SHORT).show();
				System.exit(0);
			}
			if (window != null) {
				window.dismiss();
			}
		}
	};
	public ArrayList<Map<String, Object>> CreateData() {		
		Map<String, Object> map;
		map = new HashMap<String, Object>();
		map.put(KEY, savefile);
		items.add(map);
		map = new HashMap<String, Object>();
		map.put(KEY, exit);
		items.add(map);		
		return items;
	}
    @SuppressWarnings("rawtypes")
	private void InitData() {
		// TODO Auto-generated method stub
    	//����֮��
    	List<String> taglist=p.queryLocationFromXml(filename);
    	Iterator t=taglist.iterator();
    	groupList = new ArrayList<String>();
    	String tag = null;
    	List<String> itemlist=new ArrayList<String>();
    	while(t.hasNext()){
    	tag=(String) t.next();
		groupList.add(tag);
    	}
		childList = new ArrayList<List<String>>();
		for (int i = 0; i < groupList.size(); i++) {
			ArrayList<String> childTemp;
				childTemp = new ArrayList<String>();
				itemlist=p.queryItemFromXmlByTag(filename,groupList.get(i));
				Iterator it=itemlist.iterator();
				while(it.hasNext()){
				String item=(String) it.next();
				childTemp.add(item);
				}
				childList.add(childTemp);			
	}
		}
	//ģ�����ϵ�
	private String getFileNameByTableName(String tname) {
		String fileFullName = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
		String newFileName = tname + "-" + username.trim()+"-" + format.format(new Date()) +".xml";
		
		String templateFile = fileDir + "/" +tname +".xml";
		if(FileUtil.prepareInspectFile(templateFile,newFileName)){
			String inspectDir = FileUtil.getInspectDir();
			fileFullName = inspectDir + "/" + newFileName;
		}
		
		return fileFullName;
	}
	//����ϵ�filename�͵��ڱ���
	/*private String getFileNameByTableName(String tname) {
		filename=tname;
		return filename;
	}
*/	
	public List<String> getLocation() {             //��ȡxml�ļ��е�<location>��<field>��ֵ
		List<String> list = new ArrayList<String>();
	    list = p.parseInspect(filename);		
		return list;
	}
	private void writeToXmlUserDateDvnum(String filename) {        //��username,uid,devnum,insepcttimeд��xml�ļ�
		Date d=new Date(System.currentTimeMillis());
		p.writeToXmlUserDateDvnum(filename,tname,username,uid,dnum,d);	
	}   
	public void writeFormatXml(String pathname){         //��ָ����ʽ���ļ�д��
		p.writeToFormatXml(pathname);
	}
	public List<String> queryLocationFromXml(){
		List<String> list=p.queryLocationFromXml(filename);
		return list;
	}
	public boolean onGroupClick(final ExpandableListView parent, final View v,
			int groupPosition, final long id) {
		if(isInspect==false){
			Toast.makeText(TagValidateActivity.this, "��ɨ���ǩ!", Toast.LENGTH_SHORT).show();
		}
		if(isScaned==2){
			inspectItem.collapseGroup(groupPosition);
			Toast.makeText(TagValidateActivity.this, "����ɨ���ǩ����!", Toast.LENGTH_SHORT).show();
			inspectResultPane.setVisibility(View.GONE);
		}
		return false;
	}
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		if(isInspect==false){
			Toast.makeText(TagValidateActivity.this, "��ɨ���ǩ!", Toast.LENGTH_SHORT).show();
		}
		if(isScaned==2){
			Toast.makeText(TagValidateActivity.this, "����ɨ���ǩ����!", Toast.LENGTH_SHORT).show();
		}
		return true;
}
	public void onClick(View v) {
		shibieDialog = new ProgressDialog(TagValidateActivity.this, R.style.mProgressDialog);
		shibieDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		shibieDialog.setMessage("ʶ���ǩ��...");
		shibieDialog.setCancelable(false);
		shibieDialog.show();
		timerDialog = new Timer();
		//7���ȡ������
		timerDialog.schedule(new TimerTask() {
			@Override
			public void run() {
				shibieDialog.cancel();
				Message msg = new Message();
				msg.what = MSG_OVER;
				mHandler.sendMessage(msg);
			}
		}, 7000);
		Intent sendToservice = new Intent(TagValidateActivity.this,RFIDService.class);
		sendToservice.putExtra("cardType", cardType);
		sendToservice.putExtra("activity", activity);
		startService(sendToservice); 
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
		filter.addAction("com.csei.inspect.TagValidateActivity");
		registerReceiver(myBroadcast, filter); 		//ע��㲥������
		super.onResume();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		cmd_flag = 0;  				  //д״̬�ָ���ʼ״̬
		authentication_flag = 0;
		unregisterReceiver(myBroadcast);  //ж�ع㲥������
		super.onPause();
		timeThread.cancel();
		Log.e("M1CARDPAUSE", "PAUSE");  	
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Intent stopService = new Intent();
		stopService.setAction("com.example.service.DeviceService");
		stopService.putExtra("stopflag", true);
		sendBroadcast(stopService);  //������͹㲥,�����ֹͣ
		Log.e(TAG, "send stop");
		super.onDestroy();
	}
	/**
	 *  �㲥������,���շ����͹�������ݣ�������UI
	 * @author Administrator
	 *
	 */
	private class MyBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Listable listable = intent.getParcelableExtra("listable");
			if(listable == null){
				Toast.makeText(TagValidateActivity.this, "����ʧ��!", Toast.LENGTH_SHORT).show();
				return;
			}else if(!(listable instanceof Tag)){
				Toast.makeText(TagValidateActivity.this, "�����ʹ�������豸��!", Toast.LENGTH_SHORT).show();
				return;
			}
			Tag tagRFID = (Tag)listable;

			shibieDialog.cancel();
			timerDialog.cancel();

			dnum=tagRFID.getDeviceNum();
			areaid = Integer.parseInt(tagRFID.getTagAreaNum());
			devnum.setText(dnum);
		    //������DNUM��AreAid��tags.xml�в���������
			String t=tagRFID.getTagArea();
			if(t.equals("˾������")){
				t="˾��������";
			}
			Toast.makeText(TagValidateActivity.this, "��ǩɨ�����!", Toast.LENGTH_SHORT).show();
			   isInspect=true;   
          if(isInspect){
    	     //һˢ��ǩʱ������ɨ��ListView�е�item
    		tag=t;				  
    		
    		for(int i=0;i<groupList.size();i++){
	    	   if(groupList.get(i).equals(tag)){				    
	    		   isScaned=1;
	    		   inspectItem.expandGroup(i);
	    		   for(int j=0;j<groupList.size();j++){
	    			   if(j!=i){
	    				   inspectItem.collapseGroup(j);
	    			   }
	    		   }
	    		   if(isScaned==1){
	    		   inspectItem.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
	    		   inspectItem.setOnChildClickListener(new OnChildClickListener() {
					public boolean onChildClick(ExpandableListView parent, View v,
							int groupPosition, int childPosition, long id) {
						// TODO Auto-generated method stub	
					     itemItem=childList.get(groupPosition).get(childPosition);
					     groupItem=groupList.get(groupPosition);
					     Log.e("groupItem",groupItem);
					     v.setSelected(true); 
					    boolean f=judgeIsBelongToScanTag(filename,itemItem,tag,groupItem);
					    if(f){
						String value=p.getValueFromXmlByItem(filename, itemItem,tag);										
						inspectResultPane.setVisibility(View.VISIBLE);
						if(normal.getText().equals(value)){
							normal.setChecked(true);
						}else if(abnormal.getText().equals(value)){
							abnormal.setChecked(true);
						}else if(nothing.getText().equals(value)){
							nothing.setChecked(true);
						}
						beizhustr=p.getBeiZhuFromInspectXml(filename,itemItem,tag,groupItem);
					     }else{
								Toast.makeText(TagValidateActivity.this, "����������ɨ���ǩ!", Toast.LENGTH_SHORT).show();
							 inspectResultPane.setVisibility(View.GONE);
					     }
					     return false;
					}
                    //�ж�һ��������Ƿ�����ĳ������
					private boolean judgeIsBelongToScanTag(
							String filename, String itemItem,
							String tag,String groupItem) {
						     boolean flag=false;
						    	flag=p.judgeItemIsBelong(filename, tag, itemItem,groupItem);	 
                                 return flag;	
					}
				});
	    		   inspectResult.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(RadioGroup group, int checkedId) {
							// TODO Auto-generated method stub
							checkRadioButton=(RadioButton) inspectResult.findViewById(checkedId);						
							String v=(String) checkRadioButton.getText();
							p.updateInspectXml(filename, itemItem, v,tag);
						}
					});
	    		   }
	    		   }else{
	    			   isScaned=2;
	    		   }
	    	   
	    	   }				        					    	  
             }
			}
		}
	/**
	 * д�����֤,������֤д�����
	 * @param src
	 * @return boolean
	 */
	public static boolean checkData(String src){
		boolean flag = false;
		String regString = "[a-f0-9A-F]{32}";
		flag = Pattern.matches(regString, src); //ƥ����ݣ��Ƿ�Ϊ32λ��ʮ�����
		return flag;
	}
}
