package org.csei.database.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.csei.database.entity.TaskCell;

import com.example.viewpager.R.string;

import android.database.Cursor;

public interface TaskCellService {
	
	public void addUser(TaskCell user) ;
	
	public boolean findUser(TaskCell user) throws Exception;
	
	public int getUserID(TaskCell user) throws Exception;
	
	public List<Map<String, Object>> QueryHistory(String username) throws Exception;
	//查询点检记录
	public Cursor QueryHistory1(String username,String date) throws Exception;
	//得到当天已存在的项目
	public ArrayList<String> GetCurrentProjectNum(String date,String username) ;
	//得到未上传的数目
	public Cursor GetCurrentUnuploadNum(String username,String date,String finishflag,String uploadflag) ;
	//得到当天点检项目的表格名 finishflag="未完成"||"已完成"||null
	public Cursor GetCurrentProject(String date,String username,String finishflag);
	//更新任务行数据操作
	public void UpdateUserTask(String username,String tablename,String date,String devicename,String timeslot,String tableflag);
	//更新完成标志行数据操作
	public void UpdateUserFinishflag(String username,String tablename,String date,String finishflag);
	//更新上传标志行数据操作
	public void UpdateUserUploadflag(String username, String tablename,
			String date, String finishtime,String finishflag,String uploadflag,String filesavepath);
	//得到当天未完成的任务
	public Cursor GetCurrentTask(String username,String date,String finishflag);
	//删除操作 id=行号
	public void DeleteRecord(String id);
	
}
