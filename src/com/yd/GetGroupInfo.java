/**
 * 说明：该类用于获得集团信息并将信息显示在地图上
 * 作者：章鹏海
 * 时间：2013-9-4
 */
package com.yd;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.map.Symbol;
import com.baidu.mapapi.map.TextItem;
import com.baidu.mapapi.map.TextOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.yd.GetBaseStationInfo.MyOverlay;

public class GetGroupInfo {
	// 定义本类用到的mapview和context
	private MapView mMapView = null;
	public Context context = null;

	// 用MapController完成地图控制
	private MapController mMapController = null;
	// 用MyOverlay添加地图覆盖物图层
	public MyOverlay mOverlay = null;
	// 用PopupOverlay弹窗
	private PopupOverlay pop = null;
	// 用ArrayList封装当前所有覆盖物
	private ArrayList<OverlayItem> mItems = null;
	// 用MapView.LayoutParams创建一个地图子布局
	private MapView.LayoutParams layoutParam = null;
	// 用OverlayItem定义一个覆盖物
	private OverlayItem mCurItem = null;
	// 用TextOverlay创建一个文字图层
	private TextOverlay textOverlay = null;
	// 用Symbol创建一个样式类
	private Symbol symbol = null;
	// UI控件
	private TextView popupText = null;
	private View viewCache = null;
	private View popupInfo = null;
	private View popupLeft = null;
	private View popupRight = null;
	private Button button = null;
	private TextView groupInfoText = null;
	// 封装当前经纬度的集团信息
	private ArrayList<String> groupInfoList = null;
	// 封装集团信息的list
	List<ModelGroup> list = new ArrayList<ModelGroup>();

	// 构造函数
	public GetGroupInfo(MapView mMapView, Context context) {
		this.context = context;
		this.mMapView = mMapView;
	}

	/**
	 * 实现取得集团信息
	 * 
	 * @return 1:成功，2未查询到数据，3异常
	 * @param 经纬度
	 *            ，集团名称关键字，查询参数flag：1经纬度查询，2关键字查询
	 * @author 章鹏海
	 */
	public int getGroupInfo(double lat, double lon, String groupName, int flag) {
		try {
			// 获取地图控制器
			mMapController = mMapView.getController();
			// 通过调用WebService取得集团信息并封装在数组里
			if (flag == 3) {
				list = BMapUtil.getGroupInfoByWS(lat, lon);
			} else {
				list = BMapUtil.getGroupInfoByWS2(groupName);
			}

			// 返回结果集大小是0，表示没有数据,结果集是NULL表示获取异常
			if (list == null) {
				return 3;
			}
			if (list.size() == 0) {
				return 2;
			}
			// 将覆盖物添加至地图上
			initOverlay(list);
			// 在mMapView上实例化一个文字图层
			textOverlay = new TextOverlay(mMapView);
			// 将该文字图层添加到mMapView
			mMapView.getOverlays().add(textOverlay);
			setBScenter();
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 3;
		}
	}

	/**
	 * 设置地图当前显示位置以list的第一个集团经纬度为准
	 * 
	 * @author 章鹏海
	 */
	public void setBScenter() {
		GeoPoint p = new GeoPoint((int) (list.get(0).getGroupX() * 1E6),
				(int) (list.get(0).getGroupY() * 1E6));
		mMapController.setCenter(p);
	}

	/**
	 * 根据传入的集团信息绘制文字及文字图层位置，该文字随地图变化有透视效果
	 * 
	 * @author 章鹏海
	 */
	public TextItem drawText(ModelGroup list, String bsDisplayInfo, int count) {
		// 构建文字
		TextItem item = new TextItem();
		// 设置文字位置
		item.pt = new GeoPoint((int) (list.getGroupX() * 1E6),
				(int) (list.getGroupY() * 1E6));
		// 设置文件内容
		item.text = count + "个集团";
		// 设文字大小
		item.fontSize = 18;
		symbol = new Symbol();
		Symbol.Color bgColor = symbol.new Color();
		// 设置文字背景色
		bgColor.red = 0;
		bgColor.blue = 0;
		bgColor.green = 255;
		bgColor.alpha = 50;

		Symbol.Color fontColor = symbol.new Color();
		// 设置文字着色
		fontColor.alpha = 255;
		fontColor.red = 0;
		fontColor.green = 0;
		fontColor.blue = 255;
		// 设置对齐方式
		item.align = TextItem.ALIGN_CENTER;
		// 设置文字颜色和背景颜色
		item.fontColor = fontColor;
		item.bgColor = bgColor;
		return item;
	}

	/**
	 * 创建覆盖物（OverlayItem），并添加至覆盖物图层，再把覆盖物图层添加到mapview
	 * 
	 * @author 章鹏海
	 */
	public void initOverlay(List<ModelGroup> list) {

		/**
		 * 创建自定义overlay
		 */
		mOverlay = new MyOverlay(context.getResources().getDrawable(
				R.drawable.icon_gcoding), mMapView);
		/**
		 * 准备overlay 数据
		 */

		OverlayItem item = null;
		GeoPoint p;
		for (int i = 0; i < list.size(); i++) {

			if (i > 0) {
				if ((list.get(i).getGroupX() == list.get(i - 1).getGroupX())
						&& (list.get(i).getGroupY() == list.get(i - 1)
								.getGroupY())) {
					continue;
				}
			}
			p = new GeoPoint((int) (list.get(i).getGroupX() * 1E6), (int) (list
					.get(i).getGroupY() * 1E6));
			item = new OverlayItem(p, "集团", "集团");
			item.setTitle(list.get(i).getGroupName());
			item.setMarker(context.getResources().getDrawable(
					R.drawable.icon_gcoding));
			mOverlay.addItem(item);
		}

		/**
		 * 保存所有item，以便overlay在reset后重新添加
		 */
		mItems = new ArrayList<OverlayItem>();
		mItems.addAll(mOverlay.getAllItem());
		/**
		 * 将overlay 添加至MapView中
		 */
		mMapView.getOverlays().add(mOverlay);
		/**
		 * 刷新地图
		 */
		mMapView.refresh();

		/**
		 * 向地图添加自定义View.
		 */
		LayoutInflater inflater = LayoutInflater.from(context);
		viewCache = inflater.inflate(R.layout.custom_text_view, null);
		popupInfo = (View) viewCache.findViewById(R.id.popinfo);
		popupLeft = (View) viewCache.findViewById(R.id.popleft);
		popupRight = (View) viewCache.findViewById(R.id.popright);
		popupText = (TextView) viewCache.findViewById(R.id.textcache);
		button = new Button(context);
		groupInfoText = new TextView(context);
		button.setBackgroundResource(R.drawable.popup);

		/**
		 * 创建一个popupoverlay
		 */
		PopupClickListener popListener = new PopupClickListener() {
			@Override
			public void onClickedPopup(int index) {

				Intent intent = new Intent();
				intent.setClass(context, InfoListActivity.class);
				Bundle bundle = new Bundle();
				bundle.putStringArrayList("list", groupInfoList);
				intent.putExtras(bundle);
				// 从context启动必须添加下面一行代码
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);

				// if (index == 0) {
				// 更新item位置
				// pop.hidePop();
				// Toast.makeText(context, "测试数据1",
				// Toast.LENGTH_SHORT).show();//
				// } else if (index == 2) {
				// 更新图标
				// mCurItem.setMarker(context.getResources().getDrawable(R.drawable.nav_turn_via_1));
				// mOverlay.updateItem(mCurItem);
				// mMapView.refresh();
				// pop.hidePop();
				// Toast.makeText(context, "测试数据2", Toast.LENGTH_SHORT).show();
				// }
			}
		};
		pop = new PopupOverlay(mMapView, popListener);
	}

	/**
	 * 清除所有Overlay
	 * 
	 * @param view
	 */
	public void clearOverlay(View view) {
		if (mOverlay != null) {
			mOverlay.removeAll();
			if (pop != null) {
				pop.hidePop();
			}
			mMapView.removeView(groupInfoText);
			mMapView.getOverlays().remove(textOverlay);
			mMapView.refresh();
		}
	}

	/**
	 * 继承ItemizedOverlay来添加覆盖物图层
	 * 
	 * @author 章鹏海
	 */
	public class MyOverlay extends ItemizedOverlay {

		public MyOverlay(Drawable defaultMarker, MapView mapView) {
			super(defaultMarker, mapView);
		}

		@Override
		public boolean onTap(int index) {
			try{
			OverlayItem item = getItem(index);
			mCurItem = item;
			item.getPoint().getLatitudeE6();
			// String result = "";
			String itemLat = Double.toString(item.getPoint().getLatitudeE6());
			String itemLng = Double.toString(item.getPoint().getLongitudeE6());
			mMapController.setCenter(new GeoPoint(item.getPoint()
					.getLatitudeE6(), item.getPoint().getLongitudeE6()));
			// 当前经纬度集团数
			int nowLatLanCount = 0;
			// 当前经纬度集团名称汇总
			StringBuffer groupNameTotal = new StringBuffer();
			groupInfoList = new ArrayList<String>();
			for (int i = 0; i < list.size(); i++) {
				String listLat = Double.toString(list.get(i).getGroupX() * 1E6);
				String listLng = Double.toString(list.get(i).getGroupY() * 1E6);
				if (itemLat.equals(listLat) && itemLng.equals(listLng)) {
					String result = list.get(i).getGroupName() + "\n"
							+ list.get(i).getInfo() + "\n";
					groupInfoList.add(result);
					nowLatLanCount++;
					groupNameTotal.append(list.get(i).getGroupName() + "\n");
				} else {
				}
			}
			SpannableString sp1 = new SpannableString("此位置有" + nowLatLanCount
					+ "个集团:" + "\n");
			SpannableString sp2 = new SpannableString(groupNameTotal);
			SpannableString sp3 = new SpannableString("点击查看集团详细信息");
			sp1.setSpan(new ForegroundColorSpan(Color.RED), 4, 5,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			sp3.setSpan(new UnderlineSpan(), 0, 4,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			sp3.setSpan(new ForegroundColorSpan(Color.RED), 0, 4,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			SpannableStringBuilder sp = new SpannableStringBuilder();
			sp.append(sp1);
			sp.append(sp2);
			sp.append(sp3);
			popupText.setText(sp);
			Bitmap[] bitMaps = { BMapUtil.getBitmapFromView(popupInfo) };
			pop.showPopup(bitMaps, item.getPoint(), 32);
			// }
			}
			catch(Exception e){
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		public boolean onTap(GeoPoint pt, MapView mMapView) {
			if (pop != null) {
				pop.hidePop();
				mMapView.removeView(groupInfoText);
			}
			return false;
		}

	}
}
