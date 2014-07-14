package com.csei.inspect;



import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;

import com.cesi.analysexml.DbModel;
import com.cesi.analysexml.ParseXml;
import com.cesi.client.CasClient;
import com.csei.entity.Employer;
import com.csei.util.JsonParser;
import com.example.viewpager.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class LoginActivity extends Activity {
	
	private AutoCompleteTextView edt_uname;
	private EditText edt_pwd;
	private CheckBox cb_show_pwd;
	private Button btn_login;
	private static String username;
	private static String password;
//	private MyHandler handler;
	private ProgressDialog dialog;
	protected SharedPreferences preferences;
	protected Editor editor;
	private Employer employer = null;
	
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.activity_login);
		
		preferences = getSharedPreferences("usenamedata",Context.MODE_PRIVATE);
		edt_uname = (AutoCompleteTextView) findViewById(R.id.aedt_uname);
		if (!(null==preferences.getStringSet("username", null))) {
			HashSet<String> temp=(HashSet<String>)preferences.getStringSet("username", null);
			String[] str=(String[])temp.toArray(new String[temp.size()]);
			edt_uname.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,str));
		}

		edt_pwd = (EditText) findViewById(R.id.edt_pwd);		
		cb_show_pwd = (CheckBox) findViewById(R.id.cb_show_pwd);
		btn_login = (Button) findViewById(R.id.btn_login);
		dialog = new ProgressDialog(this);
		dialog.setTitle("提示");
		dialog.setMessage("正在登录，请稍后...");
		dialog.setIndeterminate(false);
		dialog.setCancelable(true);

		cb_show_pwd.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if(cb_show_pwd.isChecked()){
					edt_pwd.setTransformationMethod(HideReturnsTransformationMethod
							.getInstance());
				}else{
					edt_pwd.setTransformationMethod(PasswordTransformationMethod
							.getInstance());
				}
			}

		});

		edt_uname.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				edt_pwd.setText((String)preferences.getString((String)parent.getItemAtPosition(position), null));

			}
		});


		btn_login.setOnTouchListener(new View.OnTouchListener() {

			@SuppressLint("NewApi")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN){     
					//更改为按下时的背景图片     
					((Button)v).setBackgroundResource(R.drawable.button_my_login_down);
				}
				else if(event.getAction() == MotionEvent.ACTION_UP){     
					//改为抬起时的图片     
					((Button)v).setBackgroundResource(R.drawable.button_my_login);     
					//储存用户名代码
					editor = preferences.edit();
					if (null==preferences.getStringSet("username", null)) {
						HashSet<String> tempSet=new HashSet<String>();
						tempSet.add(edt_uname.getText().toString());
						editor.putStringSet("username", tempSet);
						editor.commit();
					}
					else {
						if (!preferences.getStringSet("username", null).contains(edt_uname.getText().toString())) {
							HashSet<String> tempSet=(HashSet<String>) preferences.getStringSet("username", null);
							tempSet.add(edt_uname.getText().toString());
							editor.putStringSet("username", tempSet);
							editor.commit();
						}
					}
					//记住密码功能代码
					if (((CheckBox)findViewById(R.id.cb_rem_pwd)).isChecked()) {
						editor.putString(edt_uname.getText().toString(), edt_pwd.getText().toString());
						editor.commit();
					}
					//线程登录代码
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							username= edt_uname.getText().toString();
							password = edt_pwd.getText().toString();
							final boolean loginResult = CasClient.getInstance().login(username, password, getResources().getString(R.string.LOGIN_SECURITY_CHECK));
							if (loginResult) {//登录成功处理代码}
//								String msg=CasClient.getInstance().doPostNoParams("http://www.cseicms.com/riskManagement/rs/craneinspectreport/getEquipmentVarietyList");
								String msg=CasClient.getInstance().doGet("http://www.cseicms.com/inspectManagement/rs/inspectUser/currentUser");
								try {
//									employer=JsonParser.GetUserData(getResources().getString(R.string.test_jsonstring));
									employer=JsonParser.GetUserData(msg);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
							
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (loginResult) {//登录成功处理代码
										dialog.dismiss();
										Intent intent=new Intent(LoginActivity.this,UserTablesOperationsActivity.class);
										Bundle bundle=new Bundle();
										bundle.putParcelable("employer", employer);
										intent.putExtras(bundle);
										startActivity(intent);
										finish();
									}
									else {//登录失败处理代码
												dialog.dismiss();
												Toast.makeText(LoginActivity.this, getResources().getString(R.string.LOGIN_FAILED), Toast.LENGTH_SHORT).show();
									}
								}
							});
							
						}
					}).start();
					dialog.show();
				}
				return true;
			}
		});
		
	}
	//测试代码
	private void func1()
	{
		dialog.dismiss();
		Intent intent=new Intent(LoginActivity.this,TagValidateActivity.class);
		Bundle bundle=new Bundle();
		bundle.putString("tbname", "机修人员点检表");
		bundle.putInt("count", 1);
		bundle.putString("username", employer.getName());
		bundle.putInt("uid", Integer.parseInt(employer.getNumber()));
		intent.putExtras(bundle);
		startActivity(intent);
		finish();
	}
	
	
//	public String getFile(int rid) {          //获取人员点检信息
//		Log.e("rid",rid+"");
//		result="";
//		ParseXml p=new ParseXml();
//		filename=fileDir+"/RolesTable.xml";
//			List<DbModel> list=p.parseRolesTable(filename,rid);
//		    Iterator it=list.iterator();
//		    while(it.hasNext()){
//		    	DbModel d=(DbModel) it.next();
//		    	result+=d.getTableitem()+",";
//		    }
//		return result;
//	}
	
	
}
