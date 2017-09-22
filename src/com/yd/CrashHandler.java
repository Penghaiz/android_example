package com.yd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

/** 
 * @author Stay 
 *      在Application中统�?��获异常，保存到文件中下次再打�?��上传 
 */
public class CrashHandler implements UncaughtExceptionHandler {   
    /** 是否�?��日志输出,在Debug状�?下开�?  
     * 在Release状�?下关闭以提示程序性能  
     * */  
    public static final boolean DEBUG = true;   
    
    /** 系统默认的UncaughtException处理�?*/  
    private Thread.UncaughtExceptionHandler mDefaultHandler;   
    /** CrashHandler实例 */  
    private static CrashHandler INSTANCE;   
    /** 程序的Context对象 */  
    //private Context mContext;   
    /** 保证只有�?��CrashHandler实例 */  
    private CrashHandler() {}   
    /** 获取CrashHandler实例 ,单例模式*/  
    public static CrashHandler getInstance() {   
        if (INSTANCE == null) {   
            INSTANCE = new CrashHandler();   
        }   
        return INSTANCE;   
    }   
    
    /**  
     * 初始�?注册Context对象,  
     * 获取系统默认的UncaughtException处理�?  
     * 设置该CrashHandler为程序的默认处理�? 
     *   
     * @param ctx  
     */  
    public void init(Context ctx) {   
//        mContext = ctx;   
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler(); 
        Thread.setDefaultUncaughtExceptionHandler(this);   
    }   
   
    
    /**  
     * 当UncaughtException发生时会转入该函数来处理  
     */  
    @Override  
    public void uncaughtException(Thread thread, Throwable ex) {   
    	
    
 
    	
    	
    	
        if (!handleException(ex) && mDefaultHandler != null) {   
            //如果用户没有处理则让系统默认的异常处理器来处�?  
            mDefaultHandler.uncaughtException(thread, ex);   
        } else {  //如果自己处理了异常，则不会弹出错误对话框，则�?��手动�?��app 
            try {   
                Thread.sleep(3000);   
            } catch (InterruptedException e) {   
            }   
            android.os.Process.killProcess(android.os.Process.myPid());   
            System.exit(10);   
        }   
    }   
    
    /**  
     * 自定义错误处�?收集错误信息  
     * 发�?错误报告等操作均在此完成.  
     * �?��者可以根据自己的情况来自定义异常处理逻辑  
     * @return  
     * true代表处理该异常，不再向上抛异常， 
     * false代表不处理该异常(可以将该log信息存储起来)然后交给上层(这里就到了系统的异常处理)去处理， 
     * �?��来说就是true不会弹出那个错误提示框，false就会弹出 
     */  
    private boolean handleException(final Throwable ex) {   
        if (ex == null) {   
            return false;   
        }   
       // final String msg = ex.getLocalizedMessage();   
        final StackTraceElement[] stack = ex.getStackTrace(); 
        final String message = ex.getMessage(); 
        final String b = ex.getCause().toString();
        
        //使用Toast来显示异常信�?  
        new Thread() {   
            @Override  
            public void run() {   
                Looper.prepare();   
//                Toast.makeText(mContext, "程序出错�?" + message, Toast.LENGTH_LONG).show();   
//                可以只创建一个文件，以后全部�?��面append然后发�?，这样就会有重复的信息，个人不推�?
                String fileName = "gps-" + System.currentTimeMillis()  + ".log";  
                File file ;
                if (Environment.getExternalStorageState().equals(
        				Environment.MEDIA_MOUNTED))
                {
                 file=new File(Environment.getExternalStorageDirectory()
        				.getAbsolutePath(),fileName); 
                try { 
                	
        	        StringWriter stackTrace = new StringWriter();  
        	        ex.printStackTrace(new PrintWriter(stackTrace));  
        	       Log.e("异常测试", stackTrace.toString());               	
                    FileOutputStream fos = new FileOutputStream(file,true); 
                    fos.write(message.getBytes()); 
                    
                        fos.write(stackTrace.toString().getBytes());
                        //Log.e("异常测试", b);
                        
                        		
                     
                    fos.flush(); 
                    fos.close(); 
                } catch (Exception e) { 
                }
                }
                Looper.loop();   
            }   
    
        }.start();   
        return false;   
    }   
    
    // TODO 使用HTTP Post 发�?错误报告到服务器  这里不再赘述 
//    private void postReport(File file) {   
//      在上传的时�?还可以将该app的version，该手机的机型等信息�?��发�?的服务器�?
//      Android的兼容�?众所周知，所以可能错误不是每个手机都会报错，还是有针对�?的去debug比较�?
//    }   
}
