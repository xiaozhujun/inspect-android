package org.csei.database.service.imp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.csei.database.DBHelper;
import org.csei.database.entity.User;
import org.csei.database.service.UserService;

import com.example.viewpager.R;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UserServiceDao implements UserService{

	private DBHelper mySQLite;
	private SQLiteDatabase db;

	
	public UserServiceDao(Context context) throws Exception{
		mySQLite = DBHelper.getInstance(context);
		db = mySQLite.getWritableDatabase();
	}


	@Override
	public void addUser(User user) throws Exception{
		db.beginTransaction();
		db.execSQL("insert into user(user_id,username,tablename,taskname,devicename,date,timeslot,finishtime,finishflag,uploadflag) values(?,?,?,?,?,?,?,?,?,?)",new Object[]{null,
				user.getUsername(),
				user.getTablename(),
				user.getTaskname(),
				user.getDevicename(),
				user.getDate(),
				user.getTimeslot(),
				user.getFinishtime(),
				user.getFinishflag(),
				user.getUploadflag()});
		Log.i("msg", "已完成用户添加.");
		db.setTransactionSuccessful();
		db.endTransaction();
	}


	@Override
	public boolean findUser(User user) throws Exception{
		// TODO Auto-generated method stub
		Cursor cursor = db.rawQuery("select * from user where username=?", new String[]{user.getUsername()});	
		while(cursor.moveToNext()){
			Log.i("msg",cursor.getString(cursor.getColumnIndex("username")));
			Log.i("msg",cursor.getString(cursor.getColumnIndex("password")));
			Log.i("msg",cursor.getString(cursor.getColumnIndex("user_id")));
			return true;
		}
		return false;
	}


	@Override
	public int getUserID(User user) throws Exception{
		Cursor cursor = db.rawQuery("select * from user where username=?", new String[]{user.getUsername()});	
		while(cursor.moveToNext()){
			return cursor.getInt(cursor.getColumnIndex("user_id"));
		}
		return 0;
	}


	@Override
	public List<Map<String, Object>> QueryHistory(String username)
			throws Exception {
		return null;
	}


	@Override
	public Cursor QueryHistory1(String username) throws Exception {
		return db.rawQuery("select user_id AS _id,tablename,devicename,finishtime,uploadflag from user where username=?", new String[]{username});
	}
	
//	@Override
//	public List<Map<String, Object>> QueryHistory(String username) throws Exception
//	{
//		Cursor cursor = db.rawQuery("select tablename,devicename,date,uploadflag from user where username=?", new String[]{username});
//		cursor.moveToFirst();
//		do {
//			Map<String, Object> map=new HashMap<String, Object>();
//			map.put("image", R.drawable.img_record);
////			map.put(key, value);
//		} while (cursor.moveToNext());
//	}
	
	
}
	

