package org.csei.database.entity;

public class User {
	
	private int user_id;
	

	private String username;//姓名 
	private String tablename;//点检表 （多张） 
	private String taskname;//任务名
	private String devicename;// 设备名
	private String date;//任务日期 
	private String timeslot;// 时间段
	private String finishtime;//完成时间
	private String finishflag;//完成标志
	private String uploadflag;//上传否
	
	
	public User(int user_id, String username, String password) {
		super();
		this.user_id = user_id;
		this.username = username;
	}
	
	public User(String... args) {
		super();
		this.username = args[0];
		this.tablename = args[1];
		this.taskname = args[2];
		this.devicename = args[3];
		this.date = args[4];
		this.timeslot = args[5];
		this.finishtime = args[6];
		this.finishflag = args[7];
		this.uploadflag = args[8];
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTablename() {
		return tablename;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	public String getTaskname() {
		return taskname;
	}

	public void setTaskname(String taskname) {
		this.taskname = taskname;
	}

	public String getDevicename() {
		return devicename;
	}

	public void setDevicename(String devicename) {
		this.devicename = devicename;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTimeslot() {
		return timeslot;
	}

	public void setTimeslot(String timeslot) {
		this.timeslot = timeslot;
	}

	public String getFinishtime() {
		return finishtime;
	}

	public void setFinishtime(String finishtime) {
		this.finishtime = finishtime;
	}

	public String getFinishflag() {
		return finishflag;
	}

	public void setFinishflag(String finishflag) {
		this.finishflag = finishflag;
	}

	public String getUploadflag() {
		return uploadflag;
	}

	public void setUploadflag(String uploadflag) {
		this.uploadflag = uploadflag;
	}
	
	
}
