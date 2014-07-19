package org.csei.database.service.imp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.csei.database.DBHelper;
import org.csei.database.entity.TaskCell;
import org.csei.database.service.TaskCellService;

import com.example.viewpager.R;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TaskCellServiceDao implements TaskCellService{

	private DBHelper mySQLite;
	private SQLiteDatabase db;

	
	public TaskCellServiceDao(Context context) {
		mySQLite = DBHelper.getInstance(context);
		db = mySQLite.getWritableDatabase();
	}


	@Override
	public void addUser(TaskCell user) {
		db.beginTransaction();
		db.execSQL("insert into user(user_id,username,tablename,taskname,devicename,date,timeslot,finishtime,finishflag,uploadflag,tableflag,filesavepath) values(?,?,?,?,?,?,?,?,?,?,?,?)",new Object[]{null,
				user.getUsername(),
				user.getTablename(),
				user.getTaskname(),
				user.getDevicename(),
				user.getDate(),
				user.getTimeslot(),
				user.getFinishtime(),
				user.getFinishflag(),
				user.getUploadflag(),
				user.getTableflag(),null});
		Log.i("msg", "已完成用户添加.");
		db.setTransactionSuccessful();
		db.endTransaction();
	}


	@Override
	public boolean findUser(TaskCell user) throws Exception{
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
	public int getUserID(TaskCell user) throws Exception{
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
	public Cursor QueryHistory1(String username,String date) {
		return db.rawQuery("select user_id AS _id,tablename,devicename,finishtime,uploadflag,filesavepath from user where username=? and date=? and finishflag=?", new String[]{username,date,"已完成"});
	}


	@Override
	public ArrayList<String> GetCurrentProjectNum(String date, String username)
	{
		Cursor cursor = db.rawQuery("select * from user where username=? and date=? ", new String[]{username,date});
		if (cursor.getCount()==0) {
			return null;
		}
		else {
			cursor.moveToFirst();
			ArrayList<String> string=new ArrayList<String>();int i=0;
			do {
				string.add(cursor.getString(cursor.getColumnIndex("tablename")));
			} while (cursor.moveToNext());
			return string;
		}
	}


	@Override
	public Cursor GetCurrentUnuploadNum(String username,String date,String finishflag,String uploadflag)
	{
		Cursor cursor = db.rawQuery("select  user_id AS _id,* from user where username=? and date=? and finishflag=? and uploadflag=?", new String[]{username,date,finishflag,uploadflag});
		return cursor;
	}


	@Override
	public Cursor GetCurrentProject(String username,String date,String finishflag) {
		if (null==finishflag) {
			return db.rawQuery("select user_id AS _id,* from user where username=? and date=? and tableflag in('点检项目','两者')", new String[]{username,date});
		}
		return db.rawQuery("select user_id AS _id,* from user where username=? and date=? and finishflag=? and tableflag in('点检项目','两者')", new String[]{username,date,finishflag});
	}


	@Override
	public void UpdateUserTask(String username, String tablename, String date,
			String devicename, String timeslot,String tableflag) {
		db.beginTransaction();
		db.execSQL("update user set devicename=?,timeslot=? where username=? and date=? and tablename=? and tableflag=?",new Object[]{
				devicename,timeslot,username,date,tablename,tableflag
		});
		Log.i("msg", "已完成更新操作.");
		db.setTransactionSuccessful();
		db.endTransaction();
	}


	@Override
	public void UpdateUserFinishflag(String username, String tablename,
			String date, String finishflag) {
		db.beginTransaction();
		db.execSQL("update user set finishflag=? where username=? and date=? and tablename=?",new Object[]{
				finishflag,username,date,tablename
		});
		Log.i("msg", "已完成更新操作.");
		db.setTransactionSuccessful();
		db.endTransaction();
	}


	@Override
	public void UpdateUserUploadflag(String username, String tablename,
			String date, String finishtime,String finishflag,String uploadflag,String filesavepath) {
		db.beginTransaction();
		db.execSQL("update user set finishtime=?,finishflag=?,uploadflag=?,filesavepath=?  where username=? and date=? and tablename=?",new Object[]{
				finishtime,finishflag,uploadflag,filesavepath,username,date,tablename
		});
		Log.i("msg", "已完成更新操作.");
		db.setTransactionSuccessful();
		db.endTransaction();
	}


	@Override
	public Cursor GetCurrentTask(String username, String date,String finishflag) {
		if (null==finishflag) {
			return db.rawQuery("select user_id AS _id,* from user where username=? and date=? and tableflag in('待做任务','两者')", new String[]{username,date});
		}
		return db.rawQuery("select user_id AS _id,* from user where username=? and date=? and finishflag=? and tableflag in(?,?)", new String[]{username,date,finishflag,"待做任务","两者"});
	}


	@Override
	public void DeleteRecord(String id) {
		db.beginTransaction();
		db.execSQL("delete from user where user_id=? ", new String[]{id});
		Log.i("msg","删除用户"+id);
		db.setTransactionSuccessful();
		db.endTransaction();
	}
}
	

