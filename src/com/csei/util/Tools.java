package com.csei.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.whut.inspect.R;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/*import com.zhouzhi.es.content.OperatorInfo;
import com.zhouzhi.es.content.XunjianInfoCache;*/

@SuppressLint({ "DefaultLocale", "SimpleDateFormat" })
public class Tools {

	/**
	 * byte תʮ�����
	 * @param b
	 * @param size
	 * @return
	 */
		@SuppressLint("DefaultLocale")
		public static String Bytes2HexString(byte[] b, int size) {
		    String ret = "";
		    for (int i = 0; i < size; i++) {
		      String hex = Integer.toHexString(b[i] & 0xFF);
		      if (hex.length() == 1) {
		        hex = "0" + hex;
		      }
		      ret += hex.toUpperCase();
		    }
		    return ret;
		  }
		
		public static byte uniteBytes(byte src0, byte src1) {
		    byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
		    _b0 = (byte)(_b0 << 4);
		    byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
		    byte ret = (byte)(_b0 ^ _b1);
		    return ret;
		  }
		
		/**
		 * ʮ�����תbyte
		 * @param src
		 * @return
		 */
		public static byte[] HexString2Bytes(String src) {
			int len = src.length() / 2;
			byte[] ret = new byte[len];
			byte[] tmp = src.getBytes();

			for (int i = 0; i < len; i++) {
				ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
			}
			return ret;
		}
		
		/**
		 * ��ȡϵͳʱ�䣬ʱ���ʽΪ�� ��-��-��   ʱ���� ��
		 * @return
		 */
		public static String getTime(){
			String model = "yyyy-MM-dd HH:mm ss";
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat(model);		
			String dateTime = format.format(date);
			return  dateTime;
		}
		
		/**
		 * ������յ���ݳ���
		 * @param dataLen ������ݵĳ���
		 * @param data ���
		 * @return
		 */
		public static boolean checkData(String dataLen, String data){
			int length = Integer.parseInt(dataLen, 16);
			if(length == (data.length()/2 - 5)){
				return true;
			}
			return false;
		}
		
		/**
		 * ����Ѳ��㿨�����
		 * @param AreaCarddata
		 */
		public static void resolveAreaCard(String AreaCarddata){
//			OperatorInfo.ELEVATOR_ID = AreaCarddata.substring(0, 20);
//			OperatorInfo.AREA_TYPE = AreaCarddata.substring(20, 26);
//			OperatorInfo.ELEVATOR_TYPE = AreaCarddata.substring(26, 28);
			/*XunjianInfoCache.DIANTI_ID = AreaCarddata.substring(0, 20);
			XunjianInfoCache.QUYU_ID = Integer.parseInt(AreaCarddata.substring(20, 26));
			XunjianInfoCache.DIANTI_TYPE = AreaCarddata.substring(26, 28);*/
		}
		
		/**
		 * ���������Ա��Ƭ�е���Ϣ
		 * @param cardData
		 */
		public static void getOperatorInfo(String cardData){
			/*OperatorInfo.TEL_AREA = cardData.substring(0, 6);
			OperatorInfo.OPID = cardData.substring(6, 8);
			OperatorInfo.ID = cardData.substring(8, 16);
			OperatorInfo.OPNAME = hex2Chinese(cardData.substring(16, 32));
			///
			XunjianInfoCache.RENYUAN_TYPE = Integer.parseInt(cardData.substring(6, 8));
			XunjianInfoCache.RENYUAN_ID = cardData.substring(8, 16);
			XunjianInfoCache.RENYUAN_NAME = hex2Chinese(cardData.substring(16, 32));*/
			
		}
		
		/**
		 * ʮ�����ת��Ϊ����
		 * @param hex
		 * @return
		 */
		public static String hex2Chinese(String hex){
			String name = null;
			byte[] temp = Tools.HexString2Bytes(hex);
			try {
				name = new String(temp,"GB2312");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return name;
		}
		
		public static void playMedia(){
			MediaPlayer mPlayer = new MediaPlayer();
			try {
				mPlayer.setDataSource("/system/media/audio/ui/VideoRecord.ogg");  //ѡ��ϵͳ�����ļ�
				mPlayer.prepare();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mPlayer.start();
		}
		
		//2014-07-16 郭知祥添加代码
		public static String GetCurrentTime() {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return format.format(new Date());
		}
		public static String GetCurrentDate() {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			return format.format(new Date());
		}
		/**
		 * @param inputStream 文件输入流
		 * @param filename 文件名
		 * @param filepath 文件保存路径
		 * @return true保存成功
		 */
		public static boolean SaveConfigFile(InputStream inputStream,String filename,String filepath)
		{
			OutputStream os;
			File file=new File(filepath);
			if (!file.exists()) {
				file.mkdirs();
			}
			file=new File(filepath+"/"+filename);
			if (file.exists()) {
				file.delete();
			}
			byte[] bs = new byte[1024];
	           int len;
	           try {
	           os = new FileOutputStream(file);
	           while ((len = inputStream.read(bs)) != -1) {
	               os.write(bs, 0, len);
	           }
				os.close();
				inputStream.close();
				Log.i("保存配置文件", filename+"保存在"+filepath);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				Log.i("保存配置文件", filename+"保存失败");
				return false;
			}
		}
}
