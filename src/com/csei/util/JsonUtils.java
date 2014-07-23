package com.csei.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONObject;


public class JsonUtils {

	public static String HashToJson(HashMap<String,Object> params){
		  String string = "{";  
	        for (Iterator<Entry<String, Object>> it = params.entrySet().iterator(); it.hasNext();) {  
	            Entry<String,Object> e = (Entry<String,Object>) it.next();  
	            string += "\"" + e.getKey() + "\":";  
	            string += "\"" + e.getValue() + "\",";  
	        }  
	        string = string.substring(0, string.lastIndexOf(","));  
	        string += "}";  
	        return string;
	}
	
	public static String GetUserId(String message) throws Exception{
		JSONObject jsonObject = new JSONObject(message);
		JSONObject jsonItem = jsonObject.getJSONObject("data");
		return jsonItem.getInt("id")+"";

	}
}
