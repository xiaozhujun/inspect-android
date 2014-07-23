package com.csei.inspect;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.csei.database.entity.TaskCell;
import org.csei.database.service.imp.TaskCellServiceDao;
import org.json.JSONException;

import com.cesi.analysexml.DbModel;
import com.cesi.analysexml.ParseXml;
import com.cesi.client.CasClient;
import com.csei.entity.Employer;
import com.csei.util.JsonParser;
import com.csei.util.JsonUtils;
import com.csei.util.Tools;
import com.example.viewpager.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
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
	private ProgressDialog dialog;
	protected SharedPreferences preferences;
	protected Editor editor;
	private Employer employer = null;
	private String userId;
	
	@SuppressLint("NewApi")
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

		//创建/inspect/config目录和/inspect/data目录 放置配置文件
		new Thread(new CreateDirThread()).start();
		
		
		
		cb_show_pwd.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
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
					((Button)v).setBackgroundResource(R.drawable.button_my_login_down);
				}
				else if(event.getAction() == MotionEvent.ACTION_UP){     
					((Button)v).setBackgroundResource(R.drawable.button_my_login);     
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
					if (((CheckBox)findViewById(R.id.cb_rem_pwd)).isChecked()) {
						editor.putString(edt_uname.getText().toString(), edt_pwd.getText().toString());
						editor.commit();
					}
					
					new Thread(new Runnable() {
						@Override
						public void run() {
							username= edt_uname.getText().toString();
							password = edt_pwd.getText().toString();
							final boolean loginResult1 = CasClient.getInstance().login(username, password, getResources().getString(R.string.LOGIN_SECURITY_CHECK));
							if (loginResult1) {//��¼�ɹ��������}
								String msg=CasClient.getInstance().doGet(getResources().getString(R.string.USER_GETIMF));
								
								try {
									employer=JsonParser.GetUserData(msg);
									userId = JsonUtils.GetUserId(msg);
								} catch (JSONException e) {
									e.printStackTrace();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (loginResult1) {//登录成功
										dialog.dismiss();
										Intent intent=new Intent(LoginActivity.this,UserOperationsActivity.class);
										Bundle bundle=new Bundle();
										bundle.putParcelable("employer", employer);
										bundle.putString("userId", userId);
										intent.putExtras(bundle);
										startActivity(intent);
										finish();
									}
									else {//登录失败
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
	
	class CreateDirThread implements Runnable{
		@Override
		public void run() {
			String configdir=Environment.getExternalStorageDirectory().toString()+"/inspect/config";
			String datadir=Environment.getExternalStorageDirectory().toString()+"/inspect/data";
			File file=new File(configdir);
			if (!file.exists()) {
				file.mkdirs();
			}
			File mFile=new File(configdir+"/jixiurenyuandianjianbiao.xml");
			if (!(mFile.exists())) {
				try {
					InputStream inputStream=LoginActivity.this.getAssets().open("jixiurenyuandianjianbiao.xml");
					FileOutputStream fileOutputStream=new FileOutputStream(configdir+"/jixiurenyuandianjianbiao.xml");
					byte[] buffer=new byte[65535];int count=0;
					while ((count=inputStream.read(buffer))>0) {
						fileOutputStream.write(buffer,0,count);
					}
					fileOutputStream.close();inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			mFile.renameTo(new File(configdir+"/机修人员点检表.xml"));
		}
	}
	
	
}
