/**
 * 说明：主界面
 * 作者：章鹏海
 * 时间：2013-8-7
 */
package com.yd;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKOLUpdateElement;
import com.baidu.mapapi.map.MKOfflineMap;
import com.baidu.mapapi.map.MKOfflineMapListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class Mygps3Activity extends Activity implements MKOfflineMapListener {
	// 管理百度地图（验证KEY）
	BMapManager mBMapMan = null;
	// 定位相关
	LocationClient mLocClient;
	LocationData locData = null;
	MyLocationListenner myListener = new MyLocationListenner();
	// 定位图层
	locationOverlay myLocationOverlay = null;
	// 地图相关
	MapView mMapView = null; // 地图View
	MapController mMapController = null;
	// UI相关
	OnCheckedChangeListener radioButtonListener = null;
	Button requestLocButton = null;
	Button getBaseStationInfo = null;
	Button getGroupnInfo = null;
	TextView tv1 = null;
	TextView tv2 = null;
	boolean isRequest = false;// 是否手动触发请求定位
	boolean isFirstLoc = true;// 是否首次定位
	boolean isDisplayBaseStationInfo = false;// 当前是否已经显示基站信息
	ProgressDialog waitDialog;// 查询基站信息进度条
	EditText editText;// 查询关键字输入框

	// 离线地图
	MKOfflineMap mOffline = null;
	// 基站信息类
	GetBaseStationInfo getBsInfo = null;
	GetGroupInfo getGroupInfo = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 必须先验证再设置布局
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init("939bd2d61022ea589e809290cfbec786", null);
		setContentView(R.layout.main);
		// 检测当前手机的GPS和网络连接状态
		getGpsNetStatus();
		requestLocButton = (Button) findViewById(R.id.button1);
		getBaseStationInfo = (Button) findViewById(R.id.button2);
		getGroupnInfo = (Button) findViewById(R.id.button3);
		tv1 = (TextView) findViewById(R.id.tv1);
		tv2 = (TextView) findViewById(R.id.tv2);

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapsView);
		mMapController = mMapView.getController();
		mMapView.getController().setZoom(17);
		mMapView.getController().enableClick(true);
		mMapView.setBuiltInZoomControls(true);

		// 定位初始化
		mLocClient = new LocationClient(this);
		locData = new LocationData();
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(5000);// 每5秒定位一次
		option.setPriority(LocationClientOption.NetWorkFirst);// 优先使用网络定位
		mLocClient.setLocOption(option);
		mLocClient.start();
		GeoPoint point = new GeoPoint((int) (39.915 * 1E6), (int) (116 * 1E6));// 用给定的经纬度构建一个GeoPoint
																				// //
																				// 单位是微度（度*1E6）
		mMapController.setCenter(point);// 设置地图中心点
		// 定位图层初始化
		myLocationOverlay = new locationOverlay(mMapView);
		// 设置定位数据
		myLocationOverlay.setData(locData);
		// 添加定位图层
		mMapView.getOverlays().add(myLocationOverlay);
		// 开启定位图层接受方向数据功能，当定位数据中有方向时，定位图标会旋转至该方向
		myLocationOverlay.enableCompass();
		// 修改定位数据后刷新图层生效
		mMapView.refresh();

		// 实例化GetBaseStationInfo类
		getBsInfo = new GetBaseStationInfo(mMapView,
				Mygps3Activity.this.getApplicationContext());
		// 实例化GetBaseStationInfo类
		getGroupInfo = new GetGroupInfo(mMapView,
				Mygps3Activity.this.getApplicationContext());
		// 点击当前位置按钮,执行手动定位
		requestLocButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 手动定位
				requestLocClick();
			}
		});
		// 点击获取基站信息
		getBaseStationInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMapView.getOverlays().clear();
				mMapView.getOverlays().add(myLocationOverlay);
				mMapView.refresh();
				setPrecessBar();
				// 在新的线程中执行查询基站信息操作
				UiThread uiThread = new UiThread(1);
				uiThread.start();
				
			}
		});
		// 点击获取集团信息
		getGroupnInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMapView.getOverlays().clear();
				mMapView.getOverlays().add(myLocationOverlay);
				mMapView.refresh();
				setPrecessBar();
				// 在新的线程中执行查询基站信息操作
				UiThread uiThread = new UiThread(3);
				uiThread.start();
			}
		});

		/** 离线地图初始化 **/
		BMapUtil.copyDB(this.getApplicationContext());
		mOffline = new MKOfflineMap();
		mOffline.init(mMapController, this);
		/** 离线地图导入离线包 **/
		int num = mOffline.scan();
	}

	/**
	 * 手动触发一次定位请求
	 * 
	 * @author 章鹏海
	 */
	public void requestLocClick() {
		isRequest = true;
		mLocClient.requestLocation();
		Toast.makeText(Mygps3Activity.this, "正在定位……", Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * 定位SDK监听函数
	 * 
	 * 当前经纬度有变化时，在MAIN布局的TEXTVIEW设置显示当前经纬度,同时刷新地图图层数据
	 * 
	 * 如果是首次定位或者手动触发定位，则地图中心移动到当前位置
	 * 
	 * @author 章鹏海
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			StringBuffer wd = new StringBuffer(256);
			StringBuffer jd = new StringBuffer(256);
			wd.append(location.getLatitude());
			jd.append(location.getLongitude());
			tv1.setText("25.079388");
			tv2.setText("117.027152");
			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();
			locData.latitude = 25.079388;
			locData.longitude = 117.027152;
			// 如果不显示定位精度圈，将accuracy赋值为0即可
			locData.accuracy = location.getRadius();
			locData.direction = location.getDerect();
			// 更新定位数据
			myLocationOverlay.setData(locData);
			// 更新图层数据执行刷新后生效
			mMapView.refresh();
			// 是手动触发请求或首次定位时，移动到定位点
			if (isRequest || isFirstLoc) {
				// 移动地图到定位点
				mMapController.setCenter(new GeoPoint(
						(int) (locData.latitude * 1e6),
						(int) (locData.longitude * 1e6)));
				isRequest = false;
			}
			// 首次定位完成
			isFirstLoc = false;
		}

		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}

	/**
	 * 继承MyLocationOverlay,显示当前位置的图层
	 * 
	 * 重写dispatchTap实现点击当前位置时做出处理
	 * 
	 * @author 章鹏海
	 */
	public class locationOverlay extends MyLocationOverlay {

		public locationOverlay(MapView mapView) {
			super(mapView);
		}

		@Override
		protected boolean dispatchTap() {
			// 处理点击事件,弹出泡泡
			// popupText.setBackgroundResource(R.drawable.popup);
			// popupText.setText("我的位置");
			// pop.showPopup(BMapUtil.getBitmapFromView(popupText), new
			// GeoPoint(
			// (int) (locData.latitude * 1e6),
			// (int) (locData.longitude * 1e6)), 8);
			return true;
		}
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		mBMapMan.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		mBMapMan.start();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (mLocClient != null)
			mLocClient.stop();
		mOffline.destroy();
		mMapView.destroy();
		mBMapMan.destroy();

		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mMapView.onRestoreInstanceState(savedInstanceState);
	}

	/**
	 * 重写按菜单键的触发事件
	 * 
	 * @author 章鹏海
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "查询基站信息");
		menu.add(0, 1, 1, "查询集团信息");
		menu.add(0, 2, 2, "清除显示信息");
		menu.add(0, 3, 3, "系统说明");
		menu.add(0, 4, 4, "退出系统");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * 重写按菜单键后选择内容的触发事件
	 * 
	 * @author 章鹏海
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int item_id = item.getItemId();
		switch (item_id) {
		case 0:
			Builder groupDialog = new AlertDialog.Builder(Mygps3Activity.this);
			groupDialog.setTitle("查询基站信息");
			groupDialog.setIcon(R.drawable.ic_launcher);
			LayoutInflater groupInflater = (LayoutInflater) Mygps3Activity.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout groupLayout = (LinearLayout) groupInflater.inflate(
					R.layout.dialogtext, null);
			groupDialog.setView(groupLayout);
			editText = (EditText) groupLayout.findViewById(R.id.et2);
			editText.setHint("例如：龙岩一中");
			groupDialog.setPositiveButton("确定 ",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mMapView.getOverlays().clear();
							mMapView.getOverlays().add(myLocationOverlay);
							mMapView.refresh();
							setPrecessBar();
							UiThread uiThread = new UiThread(2);
							uiThread.start();
						}
					});

			groupDialog.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}

					});
			groupDialog.show();
			break;
		case 1:
			Builder bsDialog = new AlertDialog.Builder(Mygps3Activity.this);
			bsDialog.setTitle("查询集团信息");
			bsDialog.setIcon(R.drawable.ic_launcher);
			LayoutInflater bsInflater = (LayoutInflater) Mygps3Activity.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout bsLayout = (LinearLayout) bsInflater.inflate(
					R.layout.dialogtext, null);
			bsDialog.setView(bsLayout);
			editText = (EditText) bsLayout.findViewById(R.id.et2);
			editText.setHint("龙岩市气象局");
			bsDialog.setPositiveButton("确定 ",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mMapView.getOverlays().clear();
							mMapView.getOverlays().add(myLocationOverlay);
							mMapView.refresh();
							setPrecessBar();
							UiThread uiThread = new UiThread(4);
							uiThread.start();
						}
					});
			bsDialog.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}

					});
			bsDialog.show();
			break;
		case 2:
			mMapView.getOverlays().clear();
			mMapView.getOverlays().add(myLocationOverlay);
			mMapView.refresh();
			break;
		}
		return true;
	}

	/**
	 * 重写按下返回键的触发事件
	 * 
	 * @author 章鹏海
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(Mygps3Activity.this)
					.setTitle("龙岩移动GPS系统")
					.setMessage("是否退出系统？")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 退出时销毁定位
									finish();
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							}).show();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 离线地图状态
	 * 
	 * @author 章鹏海
	 */
	@Override
	public void onGetOfflineMapState(int arg0, int arg1) {
		try {
			Log.i("离线地图状态", "正常");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取手机的GPS和NET状态
	 * 
	 * @author 章鹏海
	 */
	public void getGpsNetStatus() {
		if (!BMapUtil.checkGpsAndNetSettings(this.getApplicationContext())) {
			new AlertDialog.Builder(Mygps3Activity.this)
					.setTitle("龙岩移动GPS系统")
					.setMessage("检测到您的手机没有开启网络连接和GPS，建议您优先开启网络连接功能！")
					.setPositiveButton("确定开启",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent(
											Settings.ACTION_DATA_ROAMING_SETTINGS);
									startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面
								}
							})
					.setNegativeButton("关闭系统",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
									Mygps3Activity.this.finish();
								}
							}).show();
		}
		;
	}

	/**
	 * 接受设置GPS或者NET后的返回值
	 * 
	 * @author 章鹏海
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0) {
			if (!BMapUtil.checkGpsAndNetSettings(this.getApplicationContext())) {
				// 设置后如果还是没有GPS或者网络连接
				Toast.makeText(Mygps3Activity.this,
						"对不起，当前无可用的网络连接或者GPS功能,系统无法工作。", Toast.LENGTH_SHORT)
						.show();

			}
			;
		}
	};

	/**
	 * 设置进度条参数
	 * 
	 * @author 章鹏海
	 */
	public void setPrecessBar() {
		waitDialog = new ProgressDialog(Mygps3Activity.this); // 等待进度条
		waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		waitDialog.setMessage("正在查询，请稍等...");
		waitDialog.setIndeterminate(false);// 设置进度条是否为不显示
		waitDialog.setCancelable(false);// 设置进度条是否可以按返回键取消
		waitDialog.show();
	}

	/**
	 * 创建一个新线程，用来处理取得基站信息的工作
	 * 
	 * @author 章鹏海
	 */
	class UiThread extends Thread {
		int flag = 0;

		public UiThread(int flag) {
			this.flag = flag;
		}

		@Override
		public void run() {
			try {
				Looper.prepare();
				// boolean isGetedInfo = false;
				int isGetedInfo = 0;
				int leftTime = 10;// 查询基站xin
				switch (flag) {
				case 1:
					isGetedInfo = getBsInfo.getBaseStationInfo(
							locData.latitude, locData.longitude, null, flag);
					break;
				case 2:
					isGetedInfo = getBsInfo.getBaseStationInfo(0, 0, editText
							.getText().toString(), flag);
					break;
				case 3:
					isGetedInfo = getGroupInfo.getGroupInfo(
							locData.latitude, locData.longitude, null, flag);
					break;
				case 4:
					isGetedInfo = getGroupInfo.getGroupInfo(0, 0, editText
							.getText().toString(), flag);
					break;
				}
				while (leftTime > 0) {
					sleep(1000);
					leftTime = leftTime - 1;
					switch (isGetedInfo) {
					case 1: {
						waitDialog.dismiss();
						// 设置为已经显示
						isDisplayBaseStationInfo = true;
						Log.i("获取", "1");
						leftTime = 0;
						break;
					}
					case 2: {
						waitDialog.dismiss();
						// 设置为已经显示
						isDisplayBaseStationInfo = false;
						Toast.makeText(Mygps3Activity.this,
								"未查询到周围的基站信息，建议降低查询精度后重试！", Toast.LENGTH_LONG)
								.show();
						Log.i("获取", "2");
						leftTime = 0;
						break;

					}
					case 3: {
						waitDialog.dismiss();
						// 设置为已经显示
						isDisplayBaseStationInfo = false;
						Log.i("获取", "3");
						Toast.makeText(Mygps3Activity.this, "获取基站信息失败，请稍后重试！",
								Toast.LENGTH_LONG).show();
						leftTime = 0;
						break;
					}
					}
				}

				Looper.loop();
			} catch (InterruptedException e) {
				e.printStackTrace();
				Toast.makeText(Mygps3Activity.this, "获取基站信息异常，请稍后重试！",
						Toast.LENGTH_LONG).show();
				isDisplayBaseStationInfo = false;
			}
		}
	}
}
