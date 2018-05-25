package com.ktc.tvlauncher.fragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ktc.tvlauncher.AppManageActivity;
import com.ktc.tvlauncher.AppSelectActivity;
import com.ktc.tvlauncher.R;
import com.ktc.tvlauncher.dao.bean.AppInfo;
import com.ktc.tvlauncher.db.DatabaseOperator;
import com.ktc.tvlauncher.domain.CarsouselInfo;
import com.ktc.tvlauncher.ui.AppWidget;
import com.ktc.tvlauncher.ui.XCRoundRectImageView;
import com.ktc.tvlauncher.utils.Constant;
import com.ktc.tvlauncher.utils.Logger;
import com.ktc.tvlauncher.utils.ScaleAnimEffect;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import java.util.Locale;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.os.SystemProperties;
import com.ktcp.mta.sdk.KtcpMtaSdk;
/**
 * 应用
 * 
 * @author joychang
 * 
 */
public class AppFragment extends BaseFragment implements OnFocusChangeListener,
		OnClickListener {




    private final String IS_POWER_ON_PROPERTY = "mstar.launcher.1stinit";
    //Hisa 2016.02.19 推荐位图片获得焦点500ms以上进行数据上报 start
  	private int pageIndex = 1;//当前的layout的第几张图片:1,2,3
  	private boolean isFeaturedFocus = false;//判断推荐位是否获得焦点
  	//Hisa 2016.02.19 推荐位图片获得焦点500ms以上进行数据上报 end
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}
		if (null == view) {
			view = inflater.inflate(R.layout.layout_app, container, false);
			init();
		} else {
			((ViewGroup) view.getParent()).removeView(view);
		}

		return view;
	}




	
	@Override
	public void onDetach() {
		super.onDetach();
		try {
			Field childFragmentManager = Fragment.class
					.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(this, null);

		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	// 初始化
	private void init() {
		loadViewLayout();
		findViewById();
		setListener();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		isPlay = false;
		mHandler.removeMessages(NO_CONNECT_DISPLAY_IMAGE);
		super.onPause();
	}

	@Override
	public void onResume() {
		/*
		 * // 查询所有已安装的app home.queryInstalledApp();
		 */
		if (isAppVisible == true)
			isPlay = true;
		if (isChinese()){
			image_app_list.setImageDrawable(context.getResources().getDrawable(R.drawable.app_iv_10));
		}else{
			image_app_list.setImageDrawable(context.getResources().getDrawable(R.drawable.app_iv_10en));
		}
		//mHandler.sendEmptyMessageDelayed(NO_CONNECT_DISPLAY_IMAGE, 0);
		// 获取常用的app,并显示
		queryFavorateApp();
		super.onResume();
	}

	// 获取常用的app
	private void queryFavorateApp() {
		// 先查询数据库，获取所有常用的app
		List<String> lst = dbtools.queryAll();
		// 再获取系统所有已安装的app
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> packLst = home.getPackageManager()
				.queryIntentActivities(intent, 0);
		// applist = new ArrayList<AppInfo>();
		// 把所有常用的app遍历出来
		templovLst.clear();
		for (String pkgname : lst) {
			for (ResolveInfo info : packLst) {
				if (pkgname.equals(info.activityInfo.packageName)) {
					AppInfo ai = new AppInfo();
					ai.setAppicon(info.loadIcon(home.getPackageManager()));
					ai.setAppname(info.loadLabel(home.getPackageManager())
							.toString());
					ai.setApppack(info.activityInfo.packageName);
					ai.setLove(true);
					templovLst.add(ai);
				}
			}
			// 过滤
			// /
		}

		// 更新常用app列表
		mHandler.sendEmptyMessageDelayed(UPDATE_APP_LIST, 0);
	}

	@Override
	public void onStart() {
		//initscheduledExecutorService();
		super.onStart();
	}

	@Override
	public void onStop() {
		//shutdownScheduledExecutorService();
		super.onStop();
	}

	@Override
	public void onDestroy() {
		home.unregisterReceiver(mAppUninstallReceiver);// 解除广播注册
		super.onDestroy();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// TODO Auto-generated method stub
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser){//如果可见
			isPlay = true;
			isAppVisible = true;
			shutdownScheduledExecutorService();
			mHandler.removeMessages(NO_CONNECT_DISPLAY_IMAGE);
			mHandler.sendEmptyMessageDelayed(NO_CONNECT_DISPLAY_IMAGE, 0);
			initscheduledExecutorService();
		}else{
			shutdownScheduledExecutorService();
			mHandler.removeMessages(NO_CONNECT_DISPLAY_IMAGE);
			isAppVisible = false;
			isPlay = false;
		}
	}

	protected void loadViewLayout() {
		app_fls = new FrameLayout[10];
		app_typeLogs = new AppWidget[8];
		appbgs = new ImageView[10];
		animEffect = new ScaleAnimEffect();
		app_tvs = new TextView[10];
		
		SharedPreferences sp = home.getSharedPreferences(
				Constant.SAVE_TENCENT_APPSINFO, Context.MODE_PRIVATE);
		app_valid_url_num = sp.getInt("movie_valid_url_num", 0);
		if (app_valid_url_num > 5){
			app_valid_url_num = 5;
		}
	}

	protected void findViewById() {
		dbtools = new DatabaseOperator(home);
		//判断是第一次开机启动
		
		
		SharedPreferences sp = context.getSharedPreferences("isPowerAppXML", Context.MODE_PRIVATE);
		boolean k = sp.getBoolean("ISPOWERONAPP", true);
		if (k){
			for (int i = 0; i < Constant.miniAppPackageName.length; i++) {
				dbtools.addApp(Constant.miniAppPackageName[i]);
			}
			SharedPreferences.Editor editor = context
					.getSharedPreferences("isPowerAppXML",
							context.MODE_PRIVATE).edit();
			editor.putBoolean("ISPOWERONAPP", false);
			editor.commit();
		}else{
			//
		}
		//判断是否有OTA升级，若有新版本，则显示出来，若没有，则不显示
		boolean isOtaNew = SystemProperties.getBoolean("persist.sys.ota.available", false);
		if (isOtaNew == true){
			dbtools.addApp("android.systemupdate.service");
		}

		IntentFilter mAppFilter = new IntentFilter();
		mAppFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		mAppFilter.addDataScheme("package");
		home.registerReceiver(mAppUninstallReceiver, mAppFilter);

		app_fl = (FrameLayout) view.findViewById(R.id.app_fl);
		// app_fls[0] = (FrameLayout) view.findViewById(R.id.app_fl_0);
		app_fls[0] = (FrameLayout) view.findViewById(R.id.app_fl_0);
		app_fls[1] = (FrameLayout) view.findViewById(R.id.app_fl_1);
		app_fls[2] = (FrameLayout) view.findViewById(R.id.app_fl_2);
		app_fls[3] = (FrameLayout) view.findViewById(R.id.app_fl_3);
		app_fls[4] = (FrameLayout) view.findViewById(R.id.app_fl_4);
		app_fls[5] = (FrameLayout) view.findViewById(R.id.app_fl_5);
		app_fls[6] = (FrameLayout) view.findViewById(R.id.app_fl_6);
		app_fls[7] = (FrameLayout) view.findViewById(R.id.app_fl_7);
		app_fls[8] = (FrameLayout) view.findViewById(R.id.app_fl_8);
		app_fls[9] = (FrameLayout) view.findViewById(R.id.app_fl_9);

		image_carousel = (XCRoundRectImageView) view.findViewById(R.id.app_iv_0);
		app_typeLogs[0] = (AppWidget) view.findViewById(R.id.app_iv_1);
		app_typeLogs[1] = (AppWidget) view.findViewById(R.id.app_iv_2);
		app_typeLogs[2] = (AppWidget) view.findViewById(R.id.app_iv_3);
		app_typeLogs[3] = (AppWidget) view.findViewById(R.id.app_iv_4);
		app_typeLogs[4] = (AppWidget) view.findViewById(R.id.app_iv_5);
		app_typeLogs[5] = (AppWidget) view.findViewById(R.id.app_iv_6);
		app_typeLogs[6] = (AppWidget) view.findViewById(R.id.app_iv_7);
		app_typeLogs[7] = (AppWidget) view.findViewById(R.id.app_iv_8);
		image_app_list = (ImageView) view.findViewById(R.id.app_iv_9);

		// appbgs[0] = (ImageView) view.findViewById(R.id.app_bg_0);
		appbgs[0] = (ImageView) view.findViewById(R.id.app_bg_0);
		appbgs[1] = (ImageView) view.findViewById(R.id.app_bg_1);
		appbgs[2] = (ImageView) view.findViewById(R.id.app_bg_2);
		appbgs[3] = (ImageView) view.findViewById(R.id.app_bg_3);
		appbgs[4] = (ImageView) view.findViewById(R.id.app_bg_4);
		appbgs[5] = (ImageView) view.findViewById(R.id.app_bg_5);
		appbgs[6] = (ImageView) view.findViewById(R.id.app_bg_6);
		appbgs[7] = (ImageView) view.findViewById(R.id.app_bg_7);
		appbgs[8] = (ImageView) view.findViewById(R.id.app_bg_8);
		appbgs[9] = (ImageView) view.findViewById(R.id.app_bg_9);
		
		
		app_tvs[0] = (TextView) view.findViewById(R.id.app_re_0);
		app_tvs[1] = (TextView) view.findViewById(R.id.app_re_1);
		app_tvs[2] = (TextView) view.findViewById(R.id.app_re_2);
		app_tvs[3] = (TextView) view.findViewById(R.id.app_re_3);
		app_tvs[4] = (TextView) view.findViewById(R.id.app_re_4);
		app_tvs[5] = (TextView) view.findViewById(R.id.app_re_5);
		app_tvs[6] = (TextView) view.findViewById(R.id.app_re_6);
		app_tvs[7] = (TextView) view.findViewById(R.id.app_re_7);
		app_tvs[8] = (TextView) view.findViewById(R.id.app_re_8);
		app_tvs[9] = (TextView) view.findViewById(R.id.app_re_9);

		if (isChinese()){
			image_app_list.setImageDrawable(context.getResources().getDrawable(R.drawable.app_iv_10));
		}else{
			image_app_list.setImageDrawable(context.getResources().getDrawable(R.drawable.app_iv_10en));
		}
		
	}
	public boolean isChinese() {  
		Locale locale = context.getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if (language.endsWith("zh"))
		return true;
		else
		return false;
    }
	protected void setListener() {

		for (int i = 0; i < app_typeLogs.length; i++) {
			appbgs[i].setVisibility(View.GONE);
			app_typeLogs[i].setOnClickListener(this);
			// if(ISTV){
			app_typeLogs[i].setOnFocusChangeListener(this);
			//app_typeLogs[i].setOnLongClickListener(new AppLongClickListener());
			// }
		}
		// 图片轮播部分和本地应用部分
		appbgs[0].setVisibility(View.GONE);
		image_carousel.setOnClickListener(this);
		image_carousel.setOnFocusChangeListener(this);
		appbgs[9].setVisibility(View.GONE);
		image_app_list.setOnClickListener(this);
		image_app_list.setOnFocusChangeListener(this);
	}

	public void startActivity(int movieNumber) {	
/*        SharedPreferences sp = context.getSharedPreferences(Constant.SAVE_TENCENT_APPSINFO, Context.MODE_PRIVATE);
        int id = sp.getInt("movie_id_" + movieNumber, 0);
        Intent intent = new Intent("com.wasuali.action.programinfo");
    	intent.putExtra("Id", id);
    	intent.putExtra("Domain", "bs3-auth.sdk.wasu.tv");
    	intent.putExtra("IsFavorite", false);
    	startActivity(intent);*/
    	
    	SharedPreferences sp = context.getSharedPreferences(Constant.SAVE_TENCENT_APPSINFO, Context.MODE_PRIVATE);
    	String uri = sp.getString("app_uri_" + movieNumber,"");
    	
		String itemid = sp.getString("app_item_id_"+movieNumber,"");
		int row = 0;
		int col = 0;
		String number = row + ":" + col;
		String data = "{" +
	    		"\"app_package\":\"com.ktc.tvlauncher\"," + 
	    		"\"event_id\":\"launcher_chosen_clicked\"," +
	    		"\"event_type\":3," + 
	    		"\"data_type\":1," +
	    		"\"pr\":\"LAUNCHER\"," +
	    		"\"data\":{" + 
	    		"\"cid\":\"itemid\"," +
	    		"\"tab\":\"launcher_recom\"," +
	    		"\"pos\":\"number\"" +
	    		"}}";
	    data = data.replace("itemid", itemid);
	    data = data.replace("number", number);
	    Log.d("ktc_tencent","点击推荐位图片app界面:launcher_chosen_clicked:data = " + data.toString());
	    KtcpMtaSdk.mtaReport(context,data);
    	
    	Intent intent = new Intent();
		intent.setData(Uri.parse(uri));
		handleIntent(false, intent);

	}
	
    private void handleIntent(boolean isBroadcast, Intent intent) {
    	if (isBroadcast) {
    				// 广播scheme模式拉起应用
    			context.sendBroadcast(intent);
    	} else {
    				// 隐式调用的方式startActivity
    				intent.setAction("com.tencent.qqlivetv.open");
    				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    				PackageManager packageManager = context.getPackageManager();
    				List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
    				boolean isIntentSafe = activities.size() > 0;
    				if (isIntentSafe) {
    					context.startActivity(intent);
    				} else {
    					//Toast.makeText(this, "未安装腾讯视频 ， 无法跳转", Toast.LENGTH_SHORT).show();
    				}
    			}
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.app_iv_0:
			startActivity(ImageIndex);
			break;
		case R.id.app_iv_1:
			mStartActivity(1);

			break;
		case R.id.app_iv_2:
			mStartActivity(2);
			break;
		case R.id.app_iv_3:
			mStartActivity(3);
			break;
		case R.id.app_iv_4:
			mStartActivity(4);
			break;
		case R.id.app_iv_5:
			mStartActivity(5);
			break;
		case R.id.app_iv_6:
			mStartActivity(6);
			break;
		case R.id.app_iv_7:
			mStartActivity(7);
			break;
		case R.id.app_iv_8:
			mStartActivity(8);
			break;
		case R.id.app_iv_9:// 点击本地应用
			startActivity(new Intent(context, AppManageActivity.class));
			break;

		}
		home.overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}

	private void initAppShortCut() {

	}

	/*
	 * 点击第i个快捷app
	 */
	private void mStartActivity(int index) {
		if (index > templovLst.size())
			return;

		if (templovLst != null) {
			AppInfo appInfo = templovLst.get(index - 1);
			String packName = appInfo.getApppack();
			// Hisa 2016.02.19 tencent video data report start
			if ("com.ktcp.tvvideo".equals(packName)){
				if((packName != null) && "com.ktcp.tvvideo".equals(packName)){ 
				    String data = "{" + 
				    			"\"app_package\":\"com.ktc.tvlauncher\"," + 
				    			"\"event_id\":\"launcher_video_clicked\"," + 
				    			"\"event_type\":3," +
				    			"\"data_type\":1," + 
				    			"\"pr\":\"LAUNCHER\"," +
				    			"\"data\":{}}";
				    Log.d("ktc_tencent","启动腾讯视频app:launcher_video_clicked:data = " + data.toString());
				    KtcpMtaSdk.mtaReport(context.getApplicationContext(),data);
				}
			}
			// Hisa 2016.02.19 tencent video data report end
			doStartApplicationWithPackageName(packName);
		}


	}

	private void doStartApplicationWithPackageName(String packagename) {

		// 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
		PackageInfo packageinfo = null;
		try {
			packageinfo = home.getPackageManager().getPackageInfo(packagename,
					0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packageinfo == null) {
			return;
		}

		// 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(packageinfo.packageName);

		// 通过getPackageManager()的queryIntentActivities方法遍历
		List<ResolveInfo> resolveinfoList = home.getPackageManager()
				.queryIntentActivities(resolveIntent, 0);

		ResolveInfo resolveinfo = resolveinfoList.iterator().next();
		if (resolveinfo != null) {
			// packagename = 参数packname
			String packageName = resolveinfo.activityInfo.packageName;
			// 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
			String className = resolveinfo.activityInfo.name;
			// LAUNCHER Intent
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);

			// 设置ComponentName参数1:packagename参数2:MainActivity路径
			ComponentName cn = new ComponentName(packageName, className);

			intent.setComponent(cn);
			startActivity(intent);
		}
	}
	// Hisa 2016.02.19 推荐位图片获得焦点500ms以上进行数据上报 start
		Runnable tencentReport = new Runnable(){
			public void run() {
				//int uriNumber = curLine * 3 - pageIndex;
				SharedPreferences sp = context.getSharedPreferences(Constant.SAVE_TENCENT_APPSINFO, Context.MODE_PRIVATE);
				String itemid = sp.getString("app_item_id_"+ImageIndex,"");
				int row = 0;
				int col = 0;
				String number = row + ":" + col;
				String data = "{" +
			    		"\"app_package\":\"com.android.mslauncher\"," + 
			    		"\"event_id\":\"launcher_tab_pv\"," +
			    		"\"event_type\":3," + 
			    		"\"data_type\":1," +
			    		"\"pr\":\"LAUNCHER\"," +
			    		"\"data\":{" + 
			    		"\"cid\":\"itemid\"," +
			    		"\"tab\":\"launcher_recom\"," +
			    		"\"pos\":\"number\"" +
			    		"}}";
			    data = data.replace("itemid", itemid);
			    data = data.replace("number", number);
			    Log.d("ktc_tencent","获得推荐位图片焦点app界面:launcher_tab_pv:launcher_tab_pv:data = " + data.toString());
			    KtcpMtaSdk.mtaReport(context,data);			
			}
			
		};
		// Hisa 2016.02.19 推荐位图片获得焦点500ms以上进行数据上报 end
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		int paramInt = 0;

		if (v.getId() == R.id.app_iv_0) {
			Carousel_Focus = true;
		} else {
			Carousel_Focus = false;
		}
		switch (v.getId()) {
		case R.id.app_iv_0:
			isFeaturedFocus = hasFocus;
			
			paramInt = 0;
			// 此处设置不同大小的item的长宽 ,以及框飞入的X和Y轴.

			break;
		case R.id.app_iv_1:
			paramInt = 1;
		
			break;
		case R.id.app_iv_2:
			paramInt = 2;
	
			break;
		case R.id.app_iv_3:
			paramInt = 3;
			break;
		case R.id.app_iv_4:
			paramInt = 4;
			break;
		case R.id.app_iv_5:
			paramInt = 5;
			break;
		case R.id.app_iv_6:
			paramInt = 6;
			break;
		case R.id.app_iv_7:
			paramInt = 7;
			break;
		case R.id.app_iv_8:
			paramInt = 8;
			break;
		case R.id.app_iv_9:
			paramInt = 9;
			break;
		}
		if (hasFocus) {
			showOnFocusAnimation(paramInt);
			flyAnimation(paramInt);
			// 白框动画
		} else {
			showLoseFocusAinimation(paramInt);
			// 将白框隐藏
		}
		
		for (TextView tv : app_tvs) {
			if (tv.getVisibility() != View.GONE) {
				tv.setVisibility(View.GONE);
			}
		}
		
		if (true == isFeaturedFocus){
			handler.removeCallbacks(tencentReport);
			handler.postDelayed(tencentReport, 500);
		}else{
			handler.removeCallbacks(tencentReport);
		}
	}

	public class AppLongClickListener implements OnLongClickListener {

		@Override
		public boolean onLongClick(View v) {
			switch (v.getId()) {
			case R.id.app_iv_1:
				addApp(1);
				break;
			case R.id.app_iv_2:
				addApp(2);
				break;
			case R.id.app_iv_3:
				addApp(3);
				break;
			case R.id.app_iv_4:
				addApp(4);
				break;
			case R.id.app_iv_5:
				addApp(5);
				break;
			case R.id.app_iv_6:
				addApp(6);
				break;
			case R.id.app_iv_7:
				addApp(7);
				break;
			case R.id.app_iv_8:
				addApp(8);
				break;
			}
			return true;
		}

	}

	private void addApp(int i) {
		if (i <= templovLst.size())
			return;
		showPopUp(app_fls[i], null);

	}

	public void showPopUp(View v, String app_layout) {
		int popwin_w = 0;
		int popwin_h = 0;
		int popwin_gap = 0;

		popwin_w = 200;
		popwin_h = 200;
		popwin_gap = 30;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.iapp_popup, null);

		PopupWindow popupWindow = new PopupWindow(view, popwin_w, popwin_h);
		initPopView(view, popupWindow, app_layout);
		popupWindow.setAnimationStyle(R.style.PopupAnimation);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(false);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		int[] location = new int[2];
		v.getLocationOnScreen(location);

		popupWindow.showAtLocation(v, Gravity.NO_GRAVITY,
				location[0] + v.getWidth() - popwin_gap, location[1]);
		popupWindow.update();
	}

	public void initPopView(View view, final PopupWindow popupWindow,
			final String app_layout) {

		popup_delete = (Button) view.findViewById(R.id.popup_delete);
		popup_replace = (Button) view.findViewById(R.id.popup_replace);
		popup_dismiss = (Button) view.findViewById(R.id.popup_dismiss);
		popup_delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

			}
		});
		popup_replace.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context, AppSelectActivity.class);
				intent.putExtra("love_display", true);
				context.startActivity(intent);
				popupWindow.dismiss();
			}
		});
		popup_dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				popupWindow.dismiss();
			}
		});
	}

	/**
	 * 飞框焦点动画
	 * 
	 * @param paramInt
	 */
	private void flyAnimation(int paramInt) {
		int[] location = new int[2];
		int width;
		int height;
		if (paramInt == 0) {
			width = image_carousel.getWidth();
			height = image_carousel.getHeight();
		} else if (paramInt == 9) {
			width = image_app_list.getWidth();
			height = image_app_list.getHeight();
		} else {
			app_typeLogs[paramInt - 1].getLocationOnScreen(location);
			width = app_typeLogs[paramInt - 1].getWidth();
			height = app_typeLogs[paramInt - 1].getHeight();
		}

		float x = (float) location[0];
		float y = (float) location[1];
		if (mHeight > 1000 && mWidth > 1000) {
			switch (paramInt) {
			case 0:
				width = width + 46;
				height = height + 36;
				x = 600;
				y = 360;
				break;
			case 1:
				width = width + 18;
				height = height + 18;
				x = 315;
				y = 731;
				break;
			case 2:
				width = width + 18;
				height = height + 18;
				x = 594;
				y = 732;
				break;
			case 3:
				width = width + 18;
				height = height + 18;
				x = 872;
				y = 732;
				break;
			case 4:
				width = width + 18;
				height = height + 18;
				x = 1158;
				y = 240;
				break;
			case 5:
				width = width + 16;
				height = height + 18;
				x = 1156;
				y = 483;
				break;
			case 6:
				width = width + 18;
				height = height + 18;
				x = 1158;
				y = 731;
				break;
			case 7:
				width = width + 18;
				height = height + 16;
				x = 1441;
				y = 240;
				break;
			case 8:
				width = width + 18;
				height = height + 17;
				x = 1440;
				y = 483;
				break;
			case 9:
				width = width + 10;
				height = height + 8;
				x = 1443;
				y = 732;
				break;

			}
		}
	}

	/**
	 * joychang 设置获取焦点时icon放大凸起
	 * 
	 * @param paramInt
	 */
	private void showOnFocusAnimation(final int paramInt) {
		app_fls[paramInt].bringToFront();// 将当前FrameLayout置为顶层
		float f1 = 1.0F;
		float f2 = 1.07F;
		Animation mtAnimation = null;
		Animation msAnimation = null;
		switch (paramInt) {
		case 0:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 1:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 2:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 3:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 4:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 5:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 6:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 7:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 8:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 9:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;

		default:
			break;
		}
		msAnimation = animEffect.ScaleAnimation(1.0F, 1.15F, 1.0F, 1.15F);
		AnimationSet set = new AnimationSet(true);
		set.addAnimation(msAnimation);
		set.addAnimation(mtAnimation);
		set.setFillAfter(true);
		set.setAnimationListener(new MyOnFocusAnimListenter(paramInt));
		app_fls[paramInt].startAnimation(set);
	}
	/**
	 * 失去焦点缩小
	 * 
	 * @param paramInt
	 */
	private void showLoseFocusAinimation(final int paramInt) {		
		Animation mAnimation = null;
		Animation mtAnimation = null;
		Animation msAnimation = null;
		AnimationSet set = null;
		switch (paramInt) {
		case 0:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			Carousel_Focus = false;
			break;
		case 1:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 2:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 3:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 4:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 5:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 6:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 7:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 8:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;
		case 9:
			mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
			break;


		default:
			break;

		}
		msAnimation = animEffect.ScaleAnimation(1.15F, 1.0F, 1.15F, 1.0F);
		set = new AnimationSet(true);
		set.addAnimation(msAnimation);
		set.addAnimation(mtAnimation);
		set.setFillAfter(true);
		set.setAnimationListener(new MyLooseFocusAnimListenter(paramInt));
		appbgs[paramInt].setVisibility(View.GONE);
		app_fls[paramInt].startAnimation(set);

	}
	/**
	 * 获取焦点时动画监听
	 * 
	 * @author joychang
	 * 
	 */
	public class MyOnFocusAnimListenter implements Animation.AnimationListener {

		private int paramInt;

		public MyOnFocusAnimListenter(int paramInt) {
			this.paramInt = paramInt;
		}

		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			appbgs[paramInt].setVisibility(View.VISIBLE);
			if (paramInt >= 0) {
				app_tvs[paramInt].setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

	}

	/**
	 * 获取焦点时动画监听
	 * 
	 * @author joychang
	 * 
	 */
	public class MyLooseFocusAnimListenter implements
			Animation.AnimationListener {

		private int paramInt;

		public MyLooseFocusAnimListenter(int paramInt) {
			this.paramInt = paramInt;
		}

		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

	}




	private void shutdownScheduledExecutorService() {
		if ((executorService != null ) && (!executorService.isShutdown())) {
			executorService.shutdown();
		}
	}

	private void startScheduledExecutorService() {
		if (executorService.isShutdown()) {
			initscheduledExecutorService();
		}
	}

	private void initscheduledExecutorService() {
		executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleAtFixedRate(mScrollTask_no_avaiable, 0, 10,
				TimeUnit.SECONDS);
	}

	private Runnable mScrollTask_avaiable = new Runnable() {
		@Override
		public void run() {
			mHandler.sendEmptyMessageDelayed(CONNECT_DISPLAY_IMAGE, 0);
		}
	};

	private Runnable mScrollTask_no_avaiable = new Runnable() {
		@Override
		public void run() {
			Log.d("Jason","mScrollTask_no_avaiable:isPlay = " + isPlay);
			if (isPlay == true)
				mHandler.sendEmptyMessageDelayed(NO_CONNECT_DISPLAY_IMAGE, 0);
		}
	};
	Handler handler = new Handler();
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case UPDATE_APP_LIST:
				// 更新常用app列表
				updateAppList();

				break;
			case CONNECT_DISPLAY_IMAGE:

				String drawable = (String) msg.obj;
				if (drawable != null) {
					String url = Constant.WASU_BASE_URL
							+ map.get((Integer) currentIndex).getPicUrl();
					ImageIndex = currentIndex;
					ImageLoader.getInstance().displayImage(url, image_carousel,
							new ImageLoadingListener() {
								public void onLoadingStarted(String arg0,
										View arg1) {

								}

								public void onLoadingFailed(String arg0,
										View arg1, FailReason arg2) {

								}

								public void onLoadingComplete(String arg0,
										View arg1, Bitmap arg2) {

								}

								public void onLoadingCancelled(String arg0,
										View arg1) {

								}
							});

					if (Carousel_Focus == true) {
						showOnFocusAnimation(0);
					}
					currentIndex++;
					if (currentIndex >= map.size()) {
						currentIndex = 0;
					}
				}
				break;
			case NO_CONNECT_DISPLAY_IMAGE:
				if (isPlay == false)
					break;
				Log.d("Jason","app_valid_url_num = " + app_valid_url_num);
				if (index >= app_valid_url_num) {
					index = 0;
				}
				if (Carousel_Focus == true) {
					showOnFocusAnimation(0);
				}
				ImageIndex = index;
				SharedPreferences sp = home.getSharedPreferences(
						Constant.SAVE_TENCENT_APPSINFO, Context.MODE_PRIVATE);
				String imageName = sp.getString("app_picurl_" + index, "");
				String picName = "file://"
						+ home.getApplicationContext().getFilesDir().toString()
						+ "/appsImages/" + index + ".jpg";
				if (sp.getBoolean("tencent_app_update", false)){//如果网络更新成功,那么清空缓存重新加载
					Log.d("Jason","应用清除缓存");
					ImageLoader.getInstance().clearMemoryCache(); 
					ImageLoader.getInstance().clearDiscCache();
					SharedPreferences.Editor editor = context
							.getSharedPreferences(Constant.SAVE_TENCENT_APPSINFO,
									context.MODE_PRIVATE).edit();
					editor.putBoolean("tencent_app_update", false);
					editor.commit();
				}
				Log.d("Jason","应用显示");
				ImageLoader.getInstance().displayImage(picName, image_carousel);

				index++;
				break;
			}

		}

	};

	private BroadcastReceiver mAppUninstallReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {// 监听到卸载应用的广播
				// String packageName =
				// intent.getData().getSchemeSpecificPart();
				// 获取到卸载的应用包名
				// if (my_app_manage_gv != null) {
				queryFavorateApp();// 更新应用列表
				// }
			}
		}

	};

	/*
	 * 更新常用app列表
	 */
	private void updateAppList() {

		if (templovLst != null) {
			int index = 0;


			
			for (int i = 0; i < templovLst.size(); i++) {
				if (index == 8) {// 常用app列表已满
					break;
				}
				index = i;
				AppInfo app = templovLst.get(i);
				app_typeLogs[index].setAppIcon(app.getAppicon());
				if(app.getAppname().endsWith("LocalMM")){
					app_typeLogs[index].setAppName(context.getString(R.string.media_center));
				}else if (app.getAppname().equals("Tvsetting")){
					app_typeLogs[index].setAppName(context.getString(R.string.tv_only));
				}else{	
					app_typeLogs[index].setAppName(app.getAppname());
				}
				index++;
				
			}
			if (index < 8) {// 表示app快捷方式
				for (int j = index; j < 8; j++) {
					app_typeLogs[j].setAppIcon(null);
					app_typeLogs[j].setAppName("");
				}
			}

			index = 0;
		}

	}
	private final int UPDATE_APP_LIST = 1001;
	private final int CONNECT_DISPLAY_IMAGE = 1002;
	private final int NO_CONNECT_DISPLAY_IMAGE = 1003;

	private FrameLayout[] app_fls;
	private FrameLayout app_fl;
	public AppWidget[] app_typeLogs;
	private ImageView[] appbgs;
	private ScaleAnimEffect animEffect;
	private XCRoundRectImageView image_carousel;
	private ImageView image_app_list;
	private View view;
	private TextView[] app_tvs;

	private ImageLoader imageLoader;
	private int bigImageIndexDef = 0;
	private int smallImageIndexDef = 6;
	private ScheduledExecutorService executorService;
	// private ImageSwitcher app_iv_0;
	private int currentIndex = 0;

	private String imgurls[] = new String[6];
	private Map<Integer, CarsouselInfo> map = new HashMap<Integer, CarsouselInfo>();

	private List<CarsouselInfo> data;

	private int ImageIndex = 0;// 用来标记当前显示的图片，为了点击能够准确的打开相应的电影界面
	private boolean Carousel_Focus = false;
	private DatabaseOperator dbtools;
	ArrayList<AppInfo> templovLst = new ArrayList<AppInfo>();
	private PopupWindow menupopupWindow;
	protected static final int MAX_APP_COUNT = 8;
	private int index = 0;
	private int windowsWidth = 0;
	private int windowsHeight = 0;
	private Button popup_dismiss;

	private Button popup_replace;

	private Button popup_delete;
	
	private static boolean isPlay = true;
	private static boolean isAppVisible = false;
	private String TAG = "AppFragment";
	
	private static int app_valid_url_num = 0;
	// {
}
