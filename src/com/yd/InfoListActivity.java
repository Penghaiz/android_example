/**
 * 说明：测试
 * 作者：章鹏海
 * 时间：2013-9-3
 */
package com.yd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.yd.Mygps3Activity.UiThread;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class InfoListActivity extends Activity {

	private ListView listView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.info_list);
		Bundle bundle = this.getIntent().getExtras();
		ArrayList<String> bsInfoList = bundle.getStringArrayList("list");
		listView = (ListView) findViewById(R.id.listView1);
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				InfoListActivity.this, android.R.layout.simple_list_item_1,
				bsInfoList);
		listView.setAdapter(arrayAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Builder replyGroupInfoDialog = new AlertDialog.Builder(InfoListActivity.this);
				replyGroupInfoDialog.setTitle("服务日志反馈");
				replyGroupInfoDialog.setIcon(R.drawable.ic_launcher);
				LayoutInflater groupInflater = (LayoutInflater) InfoListActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				LinearLayout groupLayout = (LinearLayout) groupInflater.inflate(
						R.layout.dialogreplytext, null);
				replyGroupInfoDialog.setView(groupLayout);
				EditText editText = (EditText) groupLayout.findViewById(R.id.et2);
				replyGroupInfoDialog.setPositiveButton("确定 ",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}
						});

				replyGroupInfoDialog.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}

						});
				replyGroupInfoDialog.show();
			}
		});
	}

}
