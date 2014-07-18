package org.csei.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper{

	private static DBHelper instance;

	
	private DBHelper(Context context) {
		super(context, "db", null, 1);
	}
	
	public static DBHelper getInstance(Context context){
		if(instance==null){
			synchronized(DBHelper.class){
				if(instance==null){
					instance = new DBHelper(context);
				}
			}
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i("msg", "DB正在创建...");
		db.execSQL("CREATE TABLE IF NOT EXISTS USER(user_id integer primary key autoincrement,username varchar(255),tablename varchar(255),taskname varchar(255),devicename varchar(255),date varchar(255),timeslot varchar(255),finishtime varchar(255),finishflag varchar(255),uploadflag varchar(255),tableflag varchar(255))");
		Log.i("msg", "DB创建完成...");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	       	db.execSQL("DROP TABLE IF EXISTS USER");  
	        onCreate(db); 
	}

}
