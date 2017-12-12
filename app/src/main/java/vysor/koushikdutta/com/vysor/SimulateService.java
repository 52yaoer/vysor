package vysor.koushikdutta.com.vysor;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.RemoteViews;

public class SimulateService extends Service {

	private List<String> mHomeNames;
	private boolean isRunning = true;
	private String mLastPackage = "";
	private String mLastActivity = "";
	private boolean isActivityReady;
	
	private int mWidth;
	private int mHeight;
	private int mWidthPace;
	private int mHeightPace;
	
	private NotificationManager mNotificationManager;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		mHomeNames = getHomes();
		mWidth = intent.getIntExtra("width", 0);
		mWidthPace = mWidth / 5;
		mHeight = intent.getIntExtra("height", 0);
		mHeightPace = mHeight / 9;
		
		new CheckRunningActivity().start();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification.Builder(getApplicationContext())
		.setContent(new RemoteViews(getPackageName(), R.layout.notification_simulate))
		.setSmallIcon(R.mipmap.ic_launcher)
		.build();
		notification.flags |= Notification.FLAG_ONGOING_EVENT; 
		mNotificationManager.notify(0, notification);
		return super.onStartCommand(intent, flags, startId);
	}

	private class CheckRunningActivity extends Thread{
	    public void run(){
	        while(isRunning){
	        	String currentTopActivityName = getCurrentPackageName();
	            if (!TextUtils.equals(mLastPackage, currentTopActivityName)) {
	                // show your activity here on top of PACKAGE_NAME.ACTIVITY_NAME
	            	mLastPackage = currentTopActivityName;
	            	isActivityReady = true;
	            } else {
	            	onTouchSimulate();
				}
	            
	            try {
    				Thread.sleep(3000);
    			} catch (Exception e) {
    				// TODO: handle exception
    			}
	        }
	    }

	    /**
	     * 模拟点击，先判断是否稳定
	     */
		private void onTouchSimulate() {
			if (isActivityReady && !mHomeNames.contains(mLastPackage) && !TextUtils.equals(mLastPackage, getPackageName())) {
				isActivityReady = false;
				boolean isPackageChanged = false;
				mLastActivity = getCurrentActivity();
				
				// 执行滑动、点击操作
				for (int height = 10; height < mHeight; height = height + mHeightPace) {
					for (int width = 10; width < mWidth; width = width + mWidthPace) {
						if (TextUtils.equals(mLastPackage, getCurrentPackageName())) {
							AutoTool.execShellCmd("input tap " + width + " " + height);
							try {
			    				Thread.sleep(2000);
			    			} catch (Exception e) {
			    				// TODO: handle exception
			    			}
							
							String currentActivity = getCurrentActivity();
							if (!TextUtils.equals(mLastActivity, currentActivity)) {
								// 按返回键
								AutoTool.execShellCmd("input keyevent 4");
								try {
				    				Thread.sleep(1000);
				    			} catch (Exception e) {
				    				// TODO: handle exception
				    			}
								mLastActivity = currentActivity;
							}
						} else {
							isPackageChanged = true;
						}
						
						if (isPackageChanged) {
							break;
						}
					}
					if (isPackageChanged) {
						break;
					}
				}
			}
		}
	}
	
	/**
	 * 获取当前Package名称
	 * @return
	 */
	private String getCurrentPackageName() {
		ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        String currentRunningActivityName = taskInfo.get(0).topActivity.getPackageName();
        return currentRunningActivityName;
	}
	
	/**
	 * 获取当前Activity名称
	 * @return
	 */
	private String getCurrentActivity() {
		ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        String currentRunningActivityName = taskInfo.get(0).topActivity.getClassName();
        return currentRunningActivityName;
	}

	/**
	 * 获得属于桌面的应用的应用包名称
	 * @return 返回包含所有包名的字符串列表
	 */
	private List<String> getHomes() {
		List<String> names = new ArrayList<String>();
		PackageManager packageManager = this.getPackageManager();
		//属性
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		for(ResolveInfo ri : resolveInfo){
			names.add(ri.activityInfo.packageName);
			System.out.println(ri.activityInfo.packageName);
		}
		
		return names;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isRunning = false;
		if (mNotificationManager != null) {
			mNotificationManager.cancel(0);
		}
	}
}
