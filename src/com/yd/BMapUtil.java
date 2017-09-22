package com.yd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class BMapUtil {

	/**
	 * 从view 得到图片
	 * 
	 * @param view
	 * @return
	 */
	public static Bitmap getBitmapFromView(View view) {
		view.destroyDrawingCache();
		view.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.setDrawingCacheEnabled(true);
		Bitmap bitmap = view.getDrawingCache(true);
		return bitmap;
	}

	/**
	 * 首次运行拷贝离线地图
	 * 
	 * @author 章鹏海
	 */
	public static void copyDB(Context context) {

		String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();

		String filePath = sdPath + "/BaiduMapSdk/vmp/h";

		String dbFilePath = filePath + "/longyan_193.dat";

		File dir = new File(sdPath,"/BaiduMapSdk/vmp/h");
		// 判断目录存不存在，不存在就创建
		if (!dir.exists()) {
			dir.mkdirs();
			Log.i("filePath", filePath);
			Log.i("dbFilePath", dbFilePath);
		}
		// 判断文件存不存在，不存在就创建
		File f = new File(filePath,"longyan_193.dat");
		if (!f.exists()) {
			FileOutputStream os = null;
			try {
				os = new FileOutputStream(f,true);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			// 通过字节流拷贝文件
			InputStream is = context.getResources()
					.openRawResource(R.raw.lymap);
			byte[] buffer = new byte[8192];
			int count = 0;
			try {
				while ((count = is.read(buffer)) > 0) {
					os.write(buffer, 0, count);
					os.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				is.close();
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 判断网络连接或者GPS是否可用
	 * 
	 * @author 章鹏海
	 */
	public static boolean checkGpsAndNetSettings(Context context) {
		LocationManager alm = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		boolean isGpsOpen = alm
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
		if (isNetworkConnected(context) || isGpsOpen) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断是否开启网络连接
	 * 
	 * @author 章鹏海
	 */
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 通过WEBSERVICE获取当前经纬度附近基站数据
	 * 
	 * @author 章鹏海
	 */
	public static List<ModelBaseStation> getBsInfoByWS(double lat,double lon) {
		
		List<ModelBaseStation> list = new ArrayList<ModelBaseStation>();
		HttpTransportSE ht = new HttpTransportSE(
//				"http://218.207.179.241:8093/GpsWS/GpsWSForBs?wsdl");
		"http://10.0.2.2:8080/GpsWS/GpsWSForBs?wsdl");
		ht.debug = true;
		// 使用soap1.1协议创建Envelop对象
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		// 实例化SoapObject对象
		SoapObject request = new SoapObject("http://dao.lycmcc/", "getBsList");
		request.addProperty("arg0",(int)(lat * 1E6));
		request.addProperty("arg1",(int)(lon * 1E6));
//		request.addProperty("arg0",wsParamMap);
		envelope.bodyOut = request;
		try {
			// 调用webService
			ht.call(null, envelope);
			// txt1.setText("看看"+envelope.getResponse());
			if (envelope.getResponse() != null) {
				SoapObject result = (SoapObject) envelope.bodyIn;
				SoapObject soapChilds = null;
				for (int i = 0; i < result.getPropertyCount(); i++) {
					soapChilds = (SoapObject) result.getProperty(i);
					ModelBaseStation baseStation = new ModelBaseStation();
					baseStation.setBsId(soapChilds.getProperty("bsId").toString());
					baseStation.setBsName(soapChilds.getProperty("bsName")
							.toString());
					baseStation.setBsX(Double.parseDouble(soapChilds
							.getProperty("bsX").toString()));
					baseStation.setBsY(Double.parseDouble(soapChilds
							.getProperty("bsY").toString()));
					baseStation.setInfo(soapChilds
							.getProperty("info").toString());
					list.add(baseStation);
				}

			} else {
				Log.i("WS", "无返回");
				return list;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("WS", "异常");
			return null;
		}
		return list;
	}
	/**
	 * 通过WEBSERVICE获取当前关键字基站数据
	 * 
	 * @author 章鹏海
	 */
	public static List<ModelBaseStation> getBsInfoByWS2(String bsName) {
		
		List<ModelBaseStation> list = new ArrayList<ModelBaseStation>();
		HttpTransportSE ht = new HttpTransportSE(
//				"http://218.207.179.241:8093/GpsWS/GpsWSForBs?wsdl");
		"http://10.0.2.2:8080/GpsWS/GpsWSForBs?wsdl");
		ht.debug = true;
		// 使用soap1.1协议创建Envelop对象
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		// 实例化SoapObject对象
		SoapObject request = new SoapObject("http://dao.lycmcc/", "getBsListByName");
		request.addProperty("arg0",bsName);
//		request.addProperty("arg0",wsParamMap);
		envelope.bodyOut = request;
		try {
			// 调用webService
			ht.call(null, envelope);
			// txt1.setText("看看"+envelope.getResponse());
			if (envelope.getResponse() != null) {
				SoapObject result = (SoapObject) envelope.bodyIn;
				SoapObject soapChilds = null;
				for (int i = 0; i < result.getPropertyCount(); i++) {
					soapChilds = (SoapObject) result.getProperty(i);
					ModelBaseStation baseStation = new ModelBaseStation();
					baseStation.setBsId(soapChilds.getProperty("bsId").toString());
					baseStation.setBsName(soapChilds.getProperty("bsName")
							.toString());
					baseStation.setBsX(Double.parseDouble(soapChilds
							.getProperty("bsX").toString()));
					baseStation.setBsY(Double.parseDouble(soapChilds
							.getProperty("bsY").toString()));
					baseStation.setInfo(soapChilds
							.getProperty("info").toString());
					list.add(baseStation);
				}

			} else {
				Log.i("WS", "无返回");
				return list;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("WS", "异常");
			return null;
		}
		return list;
	}
	/**
	 * 通过WEBSERVICE获取当前经纬度附近集团数据
	 * 
	 * @author 章鹏海
	 */
	public static List<ModelGroup> getGroupInfoByWS(double lat,double lon) {
		
		List<ModelGroup> list = new ArrayList<ModelGroup>();
		HttpTransportSE ht = new HttpTransportSE(
//				"http://218.207.179.241:8093/GpsWS/GpsWSForGroup?wsdl");
		"http://10.0.2.2:8080/GpsWS/GpsWSForGroup?wsdl");
		ht.debug = true;
		// 使用soap1.1协议创建Envelop对象
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		// 实例化SoapObject对象
		SoapObject request = new SoapObject("http://dao.lycmcc/", "getGroupList");
		request.addProperty("arg0",(int)(lat * 1E6));
		request.addProperty("arg1",(int)(lon * 1E6));
//		request.addProperty("arg0",wsParamMap);
		envelope.bodyOut = request;
		try {
			// 调用webService
			ht.call(null, envelope);
			// txt1.setText("看看"+envelope.getResponse());
			if (envelope.getResponse() != null) {
				SoapObject result = (SoapObject) envelope.bodyIn;
				SoapObject soapChilds = null;
				for (int i = 0; i < result.getPropertyCount(); i++) {
					soapChilds = (SoapObject) result.getProperty(i);
					ModelGroup group = new ModelGroup();
					group.setGroupId(soapChilds.getProperty("groupId").toString());
					group.setGroupName(soapChilds.getProperty("groupName")
							.toString());
					group.setGroupX(Double.parseDouble(soapChilds
							.getProperty("groupX").toString()));
					group.setGroupY(Double.parseDouble(soapChilds
							.getProperty("groupY").toString()));
					group.setInfo(soapChilds
							.getProperty("info").toString());
					list.add(group);
				}

			} else {
				Log.i("WS", "无返回");
				return list;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("WS", "异常");
			return null;
		}
		return list;
	}
	/**
	 * 通过WEBSERVICE获取当前关键字集团数据
	 * 
	 * @author 章鹏海
	 */
	public static List<ModelGroup> getGroupInfoByWS2(String groupName) {
		
		List<ModelGroup> list = new ArrayList<ModelGroup>();
		HttpTransportSE ht = new HttpTransportSE(
//				"http://218.207.179.241:8093/GpsWS/GpsWSForGroup?wsdl");
		"http://10.0.2.2:8080/GpsWS/GpsWSForGroup?wsdl");
		ht.debug = true;
		// 使用soap1.1协议创建Envelop对象
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		// 实例化SoapObject对象
		SoapObject request = new SoapObject("http://dao.lycmcc/", "getGroupListByName");
		request.addProperty("arg0",groupName);
//		request.addProperty("arg0",wsParamMap);
		envelope.bodyOut = request;
		try {
			// 调用webService
			ht.call(null, envelope);
			// txt1.setText("看看"+envelope.getResponse());
			if (envelope.getResponse() != null) {
				SoapObject result = (SoapObject) envelope.bodyIn;
				SoapObject soapChilds = null;
				for (int i = 0; i < result.getPropertyCount(); i++) {
					soapChilds = (SoapObject) result.getProperty(i);
					ModelGroup group = new ModelGroup();
					group.setGroupId(soapChilds.getProperty("groupId").toString());
					group.setGroupName(soapChilds.getProperty("groupName")
							.toString());
					group.setGroupX(Double.parseDouble(soapChilds
							.getProperty("groupX").toString()));
					group.setGroupY(Double.parseDouble(soapChilds
							.getProperty("groupY").toString()));
					group.setInfo(soapChilds
							.getProperty("info").toString());
					list.add(group);
				}

			} else {
				Log.i("WS", "无返回");
				return list;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("WS", "异常");
			return null;
		}
		return list;
	}
}
