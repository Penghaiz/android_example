/**
 * 说明：测试
 * 作者：章鹏海
 * 时间：2013-8-8
 */
package com.yd;

import com.yd.Mygps3Activity;
import com.yd.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class LoadingActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        setContentView(R.layout.loading);
        new Thread()
        {
        	public void run()
        	{
        		try {
					sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally {
					 Intent intent = new Intent();
	                 intent.setClass(LoadingActivity.this, Mygps3Activity.class);
	                 startActivity(intent);
	                 LoadingActivity.this.finish();
				}
        	}
        }.start();

    }
}
