package com.csei.inspect;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.csei.service.RFIDService;
import org.whut.inspect.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.view.Window;
@SuppressLint("WorldReadableFiles")
public class MainActivity extends Activity {
	
	private String configdir;
	private String datadir;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//去掉标题栏全屏显示
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        com.csei.application.MyApplication.getInstance().addActivity(this);
        SharedPreferences preferences=getSharedPreferences("count", MODE_WORLD_READABLE); 
         Editor editor=preferences.edit();
         int count=preferences.getInt("count", 0);
         configdir=Environment.getExternalStorageDirectory().toString()+getResources().getString(R.string.default_configsavepath);
		 editor.putString("configsavepath", configdir);
         datadir=getResources().getString(R.string.default_datasavepath);
         editor.putString("datasavepath", datadir);
         if(count!=0){
        	 Intent intent=new Intent(MainActivity.this,LoginActivity.class);
        	 MainActivity.this.startActivity(intent);
        	 MainActivity.this.finish();
//        	 Intent service = new Intent(this, RFIDService.class);
//        	 startService(service);
         }else{
        	 Intent intent=new Intent(MainActivity.this,WelcomeActivity.class);
        	 MainActivity.this.startActivity(intent);
        	 MainActivity.this.finish();
         }
         editor.putInt("count", ++count);
         editor.commit();
         
    }   
}
