package com.csei.util;

import com.baidu.location.LocationClientOption;

public class LocationInit {
	LocationClientOption option = new LocationClientOption();
	private boolean isOpenGPS = true;
	private String  addrType = "all";
	private String  coorType = "bd09ll";
	private boolean isDisableCache = true;
	private int     scanSpan = 30000;
	private int     poiNumber = 5;
	private int		poiDistance = 1000;
	private boolean	poiExtraInfo =true;
	
	public void initOption(){
		option.setOpenGps(isOpenGPS);
		option.setAddrType(addrType);//返回的定�?结果包�?�地�?�信�?�
		option.setCoorType(coorType);//返回的定�?结果是百度�?纬度,默认值gcj02
		option.setScanSpan(scanSpan);//设置�?�起定�?请求的间隔时间为5000ms
		option.disableCache(isDisableCache);//�?止�?�用缓存定�?
		option.setPoiNumber(poiNumber);	//最多返回POI个数	
		option.setPoiDistance(poiDistance); //poi查询�?离		
		option.setPoiExtraInfo(poiExtraInfo); //是�?�需�?POI的电�?和地�?�等详细信�?�	
	}
	
	public LocationClientOption getOption() {
		initOption();
		return option;
	}

	public boolean isOpenGPS() {
		return isOpenGPS;
	}

	public void setOpenGPS(boolean isOpenGPS) {
		this.isOpenGPS = isOpenGPS;
	}

	public String getAddrType() {
		return addrType;
	}

	public void setAddrType(String addrType) {
		this.addrType = addrType;
	}

	public String getCoorType() {
		return coorType;
	}

	public void setCoorType(String coorType) {
		this.coorType = coorType;
	}

	public boolean isDisableCache() {
		return isDisableCache;
	}

	public void setDisableCache(boolean isDisableCache) {
		this.isDisableCache = isDisableCache;
	}

	public int getScanSpan() {
		return scanSpan;
	}

	public void setScanSpan(int scanSpan) {
		this.scanSpan = scanSpan;
	}

	public int getPoiNumber() {
		return poiNumber;
	}

	public void setPoiNumber(int poiNumber) {
		this.poiNumber = poiNumber;
	}

	public int getPoiDistance() {
		return poiDistance;
	}

	public void setPoiDistance(int poiDistance) {
		this.poiDistance = poiDistance;
	}

	public boolean isPoiExtraInfo() {
		return poiExtraInfo;
	}

	public void setPoiExtraInfo(boolean poiExtraInfo) {
		this.poiExtraInfo = poiExtraInfo;
	}
	
}

