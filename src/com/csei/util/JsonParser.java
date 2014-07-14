package com.csei.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.csei.entity.Employer;

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
	
}
