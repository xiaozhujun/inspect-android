package org.csei.database.service;

import java.util.List;
import java.util.Map;

import org.csei.database.entity.User;

import android.database.Cursor;

public interface UserService {
	
	public void addUser(User user) throws Exception;
	
	public boolean findUser(User user) throws Exception;
	
	public int getUserID(User user) throws Exception;
	
	public List<Map<String, Object>> QueryHistory(String username) throws Exception;
	
	public Cursor QueryHistory1(String username) throws Exception;
}
