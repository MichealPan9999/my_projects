
package com.ktc.tvlauncher.update;



import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.ktc.tvlauncher.utils.Constant;
import com.ktc.tvlauncher.utils.HttpUtil;
import com.ktc.tvlauncher.utils.Logger;
import com.ktc.tvlauncher.utils.StringTool;
import com.ktcp.mta.sdk.KtcpMtaSdk;
import java.net.URL;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import android.util.Log;
import com.ktc.tvlauncher.tencent.bean.TencentTeleplaysReInfo;

public class UpdateTeleplayData implements Runnable {
	private Context context;
	private Handler handler;
	private boolean isBootUpdate;

	public UpdateTeleplayData(Context context, Handler handler, boolean isBootUpdate) {
		this.context = context;
		this.handler = handler;
		this.isBootUpdate = isBootUpdate;
	}

	@Override
	public void run() {

		SharedPreferences sp = context.getSharedPreferences(
				Constant.SAVE_TENCENT_TELEPLAYSINFO, context.MODE_PRIVATE);

		HttpUtil httpUtil = new HttpUtil();
		
		String guid = KtcpMtaSdk.getBoxGuid(context);
	      String qua = KtcpMtaSdk.getBoxQua(context,"LAUNCHER");
	      String guidEncode = Constant.DEFAULT_TENCENT_GUID;
	      String quaEncode = Constant.DEFAULT_TENCENT_QUA;
	        try {
				if ((guid != null) && !("".equals(guid))){
					guidEncode = URLEncoder.encode(guid, "UTF-8");
					//Log.d("Jason","guidEncode = " + guidEncode);
				}
				if ((qua != null) && !("".equals(qua))){
					quaEncode = URLEncoder.encode(qua, "UTF-8");
					//Log.d("Jason","quaEncode = " + quaEncode);
				}
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        
	       // String baseurl = "http://tv.t002.ottcn.com/i-tvbin/qtv_video/special_channel/get_special_channel?tv_cgi_ver=1.0&format=json&req_from=PHILIPS_LAUNCHER&channel_selector=launcher&content_selector=all&pictures=pic_354x354%2bpic_496x722";
	        String teleplayurl = Constant.TELEPLAY_BASE_URL + "&Q-UA=" + quaEncode + "&guid=" + guidEncode;
	        Log.d("Jason","teleplayurl = " + teleplayurl);
		String content = httpUtil
				.getJsonContent(teleplayurl);
		if (content != null) {
			TencentTeleplaysReInfo tencentTeleplayInfo = StringTool.getTencentTeleplayInfo(content);
			if (tencentTeleplayInfo != null) {
				String[] titile=tencentTeleplayInfo.getTitle();
	            String[] uri=tencentTeleplayInfo.getUri();
	            String[] description=tencentTeleplayInfo.getDescription();
	            String[] pic_470_630=tencentTeleplayInfo.getPic_470_630();
	            String[] year=tencentTeleplayInfo.getPresent_year();
	            String[] item_id=tencentTeleplayInfo.getItem_id();
	            String[] director=tencentTeleplayInfo.getDirectors();
	            String[] viewPoint=tencentTeleplayInfo.getS_title();
	            String[] actor=tencentTeleplayInfo.getActor();
	            String[] s_title=tencentTeleplayInfo.getS_title();
		
				boolean isSucees = true;
				for (int i = 0; i < 6; i++) {
					try {
						if (pic_470_630[i] != null) {
/*							FileOutputStream fos = context
									.openFileOutput(getPicName(picUrl[i]),
											context.MODE_PRIVATE);*/
							// FileOutputStream fos=new
							// FileOutputStream("data/data/com.ktc.launcher/files/"+getPicName(pic[i]));
							 FileOutputStream fos=new FileOutputStream("data/data/com.ktc.tvlauncher/files/teleplaysImages/"+i+".jpg");
							isSucees = downImage(fos, pic_470_630[i]);
						}
					} catch (FileNotFoundException e) {
						isSucees = false;
						// Log.e(Constants.TAG,
						// "downImage false!!!!!!!!!!!!!!!");
						break;
					}
				}
				if (isSucees) {
					// Log.i(Constants.TAG, "downImage Sucess");
					SharedPreferences.Editor editor = context
							.getSharedPreferences(Constant.SAVE_TENCENT_TELEPLAYSINFO,
									context.MODE_PRIVATE).edit();
					for (int i = 0, j = 0; i < pic_470_630.length; i++) {
						if (pic_470_630[i] != null) {
			                   
							editor.putString("teleplay_image_"+j, i + ".jpg");
			                editor.putString("teleplay_uri_"+j, uri[i]);
			                editor.putString("teleplay_name_"+j, s_title[i]);
			                editor.putString("teleplay_year_" + j, year[i]);
			                editor.putString("teleplay_item_id_" + j, item_id[i]);
			                editor.putString("teleplay_director_"+j, director[i]);
			                editor.putString("teleplay_description_"+j, description[i]);
			                editor.putString("teleplay_actor_"+j, actor[i]);
			                editor.putString("teleplay_director_"+j, director[i]);
			                editor.putString("teleplay_title_"+j, titile[i]);
			                editor.putString("teleplay_s_title_"+j, s_title[i]);

					j++;
						}
					}
					SharedPreferences.Editor tvconfig = context
							.getSharedPreferences(Constant.TV_CONFIG,
									context.MODE_PRIVATE).edit();
					// 设置刷新周期为1天
					long validTime = System.currentTimeMillis();
					validTime = validTime + 1 * 24 * 60 * 60 * 1000;
					//validTime = validTime + 1;
					Log.d("Jason","更新App数据成功validTime = " + validTime);
					tvconfig.putLong("movie_validTime", validTime);
					tvconfig.commit();
					editor.putBoolean("tencent_teleplay_update", true);
					editor.commit();
				}
				else{
					//没有更新成功
					Log.d("Jason","更新Teleplay数据没有成功validTime ");
					SharedPreferences.Editor editor = context
							.getSharedPreferences(Constant.SAVE_TENCENT_TELEPLAYSINFO,
									context.MODE_PRIVATE).edit();
					editor.putBoolean("tencent_teleplay_update", false);
					editor.commit();
				}
			}
		}
		if (content == null){
			if (isBootUpdate == false){
			  Message msg=handler.obtainMessage(Constant.NETWORK_TIMEOUT);
			  handler.sendMessage(msg);
			}
		}
		else{
			  if (isBootUpdate == false){
			  	Message msg=handler.obtainMessage(Constant.NETWORK_SUCESS);
			  	handler.sendMessage(msg);	
			  }
		}
	}

	public String getPicName(String pic) {
		String[] picStr = pic.split("/");
		return picStr[picStr.length - 1];
	}

	public boolean downImage(FileOutputStream fos, String imageUrl) {
		try {
			URL myFileUrl = new URL(imageUrl);
			InputStream in = myFileUrl.openStream();
			int len = -1;
			byte[] data = new byte[1024];
			while ((len = in.read(data)) != -1) {
				fos.write(data, 0, len);
				fos.flush();
			}
			in.close();
			fos.close();
			return true;
		} catch (IOException e) {
			// Log.i(Constants.TAG,
			// "--------------downImage false-------------");
			return false;
		}
	}
}
