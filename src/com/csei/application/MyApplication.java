package com.csei.application;


import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;

//MyApplication类实现对所有Activity的统一管理
public class MyApplication extends Application{

	private List<Activity> activities = null;
	private static MyApplication instance;
	
	private MyApplication(){
		activities = new LinkedList<Activity>();		
	}
	
	
	public static MyApplication getInstance(){
		if(instance==null){
			instance = new MyApplication();
		}
		return instance;
	}
	
	public void addActivity(Activity activity){
		if(activities!=null&&activities.size()>0){
			if(!activities.contains(activity)){
				activities.add(activity);
			}
		}else{
			activities.add(activity);
		}
	}
	
	public void exit(){
		if(activities!=null&&activities.size()>0){
			for(Activity activity:activities){
				activity.finish();
			}	
		}
		System.exit(0);
	}
	
	public void exit2(){
		if(activities!=null&&activities.size()>0){
			for(Activity activity:activities){
				activity.finish();
			}	
		}
	}
	
}
