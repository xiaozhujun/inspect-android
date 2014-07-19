package com.csei.inspect;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.csei.service.RFIDService;
import com.example.viewpager.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.view.Window;
@SuppressLint("WorldReadableFiles")
public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//去掉标题栏全屏显示
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         SharedPreferences preferences=getSharedPreferences("count", MODE_WORLD_READABLE); 
         int count=preferences.getInt("count", 0);
         if(count!=0){
        	 Intent intent=new Intent(MainActivity.this,StartInspectActivity.class);
        	 MainActivity.this.startActivity(intent);
        	 MainActivity.this.finish();
//        	 Intent service = new Intent(this, RFIDService.class);
//        	 startService(service);
         }else{
        	 Intent intent=new Intent(MainActivity.this,WelcomeActivity.class);
        	 MainActivity.this.startActivity(intent);
        	 MainActivity.this.finish();
        	 
         }
         Editor editor=preferences.edit();
         editor.putInt("count", ++count);
         editor.commit();
         
    }   
    //因eclipse无法添加含中文字符的文件，只能用土方法，改为英文名，再改为中文名
    class CreateDirThread implements Runnable{
		@Override
		public void run() {
			String configdir=Environment.getExternalStorageDirectory().toString()+"/inspect/config";
			String datadir=Environment.getExternalStorageDirectory().toString()+"/inspect/data";
			File file=new File(configdir);
			if (!file.exists()) {
				file.mkdirs();
			}
			File mFile=new File(configdir+"/RolesTable.xml");
			if (!(mFile.exists())) {
				try {
					InputStream inputStream=MainActivity.this.getAssets().open("RolesTable.xml");
					FileOutputStream fileOutputStream=new FileOutputStream(configdir+"/RolesTable.xml");
					byte[] buffer=new byte[65535];int count=0;
					while ((count=inputStream.read(buffer))>0) {
						fileOutputStream.write(buffer,0,count);
					}
					fileOutputStream.close();inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			mFile=new File(configdir+"/jixiurenyuandianjianbiao.xml");
			if (!(mFile.exists())) {
				try {
					InputStream inputStream=MainActivity.this.getAssets().open("jixiurenyuandianjianbiao.xml");
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
			
			mFile=new File(configdir+"/jixiurenyuandianjianbiao.xml");
			if (!(mFile.exists())) {
				try {
					InputStream inputStream=MainActivity.this.getAssets().open("jixiurenyuandianjianbiao.xml");
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
