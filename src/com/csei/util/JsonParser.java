package com.csei.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.csei.entity.Employer;
import com.example.nfcdemo.R.string;
import com.example.viewpager.R;

public class JsonParser {

	public static Employer GetUserData(String msg) throws JSONException
	{
		JSONObject jsonObject = new JSONObject(msg);
		JSONObject datajsonObject = jsonObject.getJSONObject("data");
		return new Employer(datajsonObject.getString("role"),
				datajsonObject.getString("roleNum"),
				datajsonObject.getString("name"),
				datajsonObject.getString("number"));
	}
	
	public static List<Map<String,Object>> getTaskList(String msg) throws  JSONException
	{
		Map<String, Object> map=new HashMap<String, Object>();
		List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
		JSONObject datajsonObject = new JSONObject(msg);
		JSONArray jsonArray = datajsonObject.getJSONArray("data");
		for(int i=0;i<jsonArray.length();i++)
		{
			//设置图片 展示相关项
			map.put("image", R.drawable.img_task_clock);
			map.put("planName", jsonArray.getJSONObject(i).getString("planName"));
			map.put("deviceName", jsonArray.getJSONObject(i).getString("deviceName"));
			StringBuilder stringBuilder=new StringBuilder();
			stringBuilder.append(jsonArray.getJSONObject(i).getString("timeStart"));
			stringBuilder.append("-");
			stringBuilder.append(jsonArray.getJSONObject(i).getString("timeEnd"));
			map.put("deadline", stringBuilder.toString());
			//非展示相关项
			map.put("userId", jsonArray.getJSONObject(i).getInt("userId"));
			map.put("userName", jsonArray.getJSONObject(i).getString("userName"));
			map.put("tableName", jsonArray.getJSONObject(i).getString("tableName"));
			list.add(map);
		}
		return list;
	}
	
	public static boolean UploadIsSuccess(String msg) throws JSONException
	{
		JSONObject jsonObject = new JSONObject(msg);
		if (jsonObject.getString("code").equals("200")) {
			return true;
		}
		return false;
	}
	
}
