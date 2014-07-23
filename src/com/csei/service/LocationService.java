package com.csei.service;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.csei.inspect.UserOperationsActivity;
import com.csei.util.LocationInit;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class LocationService extends Service implements BDLocationListener{

	public LocationClient mLocationClient = null;
	public MKSearch mkSearch;

	private double latitude;
	private double longtitude;
	private GeoPoint point;
	
	private String info;

	
	@Override
	public void onReceiveLocation(BDLocation arg0) {
		// TODO Auto-generated method stub
		Log.i("LocationService", "------> onReceiveLocation()");
		
		latitude = arg0.getLatitude();
		longtitude = arg0.getLongitude();
		
		
		point = new GeoPoint((int)(latitude * 1e6),(int)(longtitude * 1e6));
		
		//地址反解析
		mkSearch.reverseGeocode(point);
		
		Message message = Message.obtain();
		
		message.obj = latitude+";"+longtitude;
		message.what = 1;
		
		UserOperationsActivity.handler.sendMessage(message);
		
		mLocationClient.stop();
	}

	@Override
	public void onReceivePoi(BDLocation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("LocationService", "------> onBind()");
		
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i("LocationService", "------> onCreate()");
	
		mLocationClient = new LocationClient(getApplicationContext());
		
		mkSearch = new MKSearch();
		mkSearch.init(UserOperationsActivity.mapManager, new MySearchListener());
		
		//为LocationClient设置监听器，一旦获取了结果，会回调listener中的onReceiveLocation方法
		mLocationClient.registerLocationListener(this);
		//设置定位的参数，包括坐标编码方式，定位方式等等
		mLocationClient.setLocOption(new LocationInit().getOption());
		//启动定位
		mLocationClient.start();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i("LocationService", "------> onDestroy()");
		if(mLocationClient.isStarted()){
			mLocationClient.stop();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.i("LocationService", "------> onStartCommand()");
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("LocationService", "------> onUnbind()");
		return super.onUnbind(intent);
	}

	
	public class MySearchListener implements MKSearchListener{

		@Override
		public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
			// TODO Auto-generated method stub
			if (arg1!= 0 || arg0 == null) {
				Message msg = Message.obtain();
				msg.what = 0;
				UserOperationsActivity.handler.sendMessage(msg);
			} else {
				Log.i("LocationService", "------>onGetAddrResult()");
				info = arg0.strAddr+";"+arg0.addressComponents.city;
				Message msg = Message.obtain();
				msg.what = 2;
				msg.obj = info;
				UserOperationsActivity.handler.sendMessage(msg);
				new Thread(new UserOperationsActivity.SendLocationThread()).start();
			}
		}

		@Override
		public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetPoiDetailSearchResult(int arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetPoiResult(MKPoiResult arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetShareUrlResult(MKShareUrlResult arg0, int arg1,
				int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
