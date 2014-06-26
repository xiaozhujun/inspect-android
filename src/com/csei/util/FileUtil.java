package com.csei.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.Environment;

public class FileUtil {
	
	//点检项目保存的目录
	private static final String inspectDir = "inspect";
	
	//判断sdcard是否插入
	public static boolean isMounted(){
		String status = Environment.getExternalStorageState();
		if(status.equals(Environment.MEDIA_MOUNTED)){
			return true;
		}
		return false;
	}
	
	//创建文件夹
	public static boolean makeDir(String dir){
		if(isMounted()){
			String rootDir = Environment.getExternalStorageDirectory().toString();
			String dirPath = null;
			if(rootDir.endsWith("/")){
				dirPath = rootDir + dir;
			}else{
				dirPath = rootDir + "/" +dir;
			}
			
			File dirFile = new File(dirPath);
			if(!dirFile.exists()){
				dirFile.mkdirs();
			}
			return true;
		}
		return false;
	}
	
	//获得点检目录路径，如果不存在也不构建
	public static String getInspectDir(){
		String rootDir = Environment.getExternalStorageDirectory().toString();
		String inspectPath = null;
		if(rootDir.endsWith("/")){
			inspectPath = rootDir + inspectDir;
		}else{
			inspectPath = rootDir + "/" +inspectDir;
		}
		return inspectPath;
	}
	
	//构建并返回点检项目目录
	public static String buildInspectDir(){
		String inspectPath = null;
		if(makeDir(inspectDir)){
			inspectPath =  getInspectDir();
		}
		return inspectPath;
	}
	
	//根据完整路径获得文件名
	private static String getFileName(String file){
		if(file.lastIndexOf("/")>0){
			return file.substring(file.lastIndexOf("/"));
		}
		return file;
	}
	
	//准备点检结果文件
	public static boolean prepareInspectFile(String file,String newFileName){
		String inspectPath = buildInspectDir();
		InputStream inStream = null;
        FileOutputStream fs = null;
		try {   
	           int byteread = 0;   
	           File oldfile = new File(file);   
	           if (oldfile.exists()) { //文件存在时   
	               inStream = new FileInputStream(oldfile); //读入原文件   
	               File newFile =  new File(inspectPath+"/"+newFileName);
	               if(!newFile.exists()){
	            	   newFile.createNewFile();
	               }
	               fs = new FileOutputStream(newFile);   
	               byte[] buffer = new byte[1444];   
	               while ( (byteread = inStream.read(buffer)) != -1) {   
	                   fs.write(buffer, 0, byteread);   
	               }   
	           }
	           return true;
	       }   
	       catch (Exception e) {   
	           e.printStackTrace();   
	  
	       }finally{
	    	   try {
		    		  if(fs!=null){
		    			  fs.close();
						  inStream.close();  
		    		  }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	     
	    	   
	       }  
		return false;
	}
	
	

}
