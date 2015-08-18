package us.baocai.baocaishop.net;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import us.baocai.baocaishop.util.StringUtil;
import us.baocai.baocaishop.util.UI;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;

public class Api {

//	public static String BASE_HTTP = "http://dev.baocai.us/bc-crm/";
//	public static final String HOST = "pushtest.baocai.us";
//	public static final int PORT = 10001;
//	public static final String SIGN_KEY = "76ddf31d1dab438eae9e5683e740d73c"; // 测试

	 //生产
	 public static final String HOST = "push.baocai.us";
	 public static final int PORT = 10003;
	 public final static String BASE_HTTP = "http://crm.baocai.us/bc-crm/";
	 public static final String SIGN_KEY = "3a20ec2c46e945dfa009ee18920fbefa";


	public static final String SIGN_SCOPE = "android";

	public final static String HTTP_GET_ORDER = BASE_HTTP
			+ "orders/getUserOrders?uid=2&pageNum=1";

	public final static String HTTP_GET_ORDER_DETAIL = BASE_HTTP
			+ "orders/getOrderByNo?orderNo="; // 获取订单明细

	public final static String HTTP_GET_EMPLEOYEE = BASE_HTTP
			+ "employees/findByStoreId?storeId="; // 按店铺获取人员

	public final static String HTTP_UPDATE_ORDER_TO_DELIVERING = BASE_HTTP
			+ "orders/changeOrderToDelivering"; // 更新配送状态

	public final static String HTTP_UPDATE_ORDER_TO_FINISH = BASE_HTTP
			+ "orders/changeOrderToFinishing"; // 更新配送状态;

	public final static String HTTP_UPDATE_ORDER_TO_MAKING = BASE_HTTP
			+ "orders/changeOrderToMaking"; // 更新配送状态;

	public final static String HTTP_REG_CID = BASE_HTTP + "stores/regCid"; // 注册CID

	public final static String HTTP_GET_ORDER_UN_FINISH = BASE_HTTP
			+ "orders/findWaitDealOrderByShopId?shopId="; // 获取未完成订单

	public final static String HTTP_GET_ORDER_TODAY = BASE_HTTP
			+ "orders/findCurrentOrders?shopId="; // 获取今天未成订单

	public final static String HTTP_GET_STORES = BASE_HTTP + "stores/findAll"; // 获取所有门店

//	public final static String HTTP_ORDER_ABANDON = BASE_HTTP
//			+ "orders/cancelOrderByNoAndSn"; // 废弃订单
//	
//	public final static String HTTP_ORDER_ABANDON_FINISH = BASE_HTTP
//			+ "orders/cancelOrderByDelived"; // 废弃订单
	
	public final static String HTTP_ORDER_ABANDON = BASE_HTTP
			+ "orders/transferNotMakedToKF"; // 废弃订单
	
	public final static String HTTP_ORDER_ABANDON_FINISH = BASE_HTTP
			+ "orders/transferDeliveredToKF"; // 废弃订单

	public final static String HTTP_ORDER_OPEN_SHOP = BASE_HTTP
			+ "stores/openStore"; // 开店

	public final static String HTTP_ORDER_CLOSE_SHOP = BASE_HTTP
			+ "stores/closeStore"; // 关店


	public final static String HTTP_GET_QR_TICKET = "http://baocai.us/weixinpay/get_qr_code/"; // 获取微信ticket二维码

	public final static String HTTP_GET_BITMAP = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket="; // 获取微信ticket二维码

	public final static String REFRESH_TOKEN = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=wx08d80bddb80eccba&grant_type=refresh_token&refresh_token=REFRESH_TOKEN";

	public final static String USER_INFO = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID";

	public final static String HTTP_GET_ORDER_UNTREATED = BASE_HTTP
			+ "orders/getCurrWaitDealOrders"; // 获取今天未成订单
	
	public final static String HTTP_GET_ORDER_MAKING = BASE_HTTP
			+ "orders/getCurrMakingOrders"; // 获取今天未成订单
	
	public final static String HTTP_GET_ORDER_DELIVERY = BASE_HTTP
			+ "orders/getCurrDeliveringOrders"; // 获取今天未成订单
	
	public final static String HTTP_GET_ORDER_FINISH = BASE_HTTP
			+ "orders/getCurrFinishedOrders"; // 获取今天未成订单
	
	public static final String HTTP_GET_PROBLEM_ORDER = BASE_HTTP+"orders/getCurrKfDealingOrders"; //获取今天问题订单

	public static final String HTTP_ABANDON_REASON = BASE_HTTP+"reasons/all"; //废弃原因
	
	public static final String HTTP_TODAY_TOTAL_CASH = BASE_HTTP+"orders/calcTodayIncome"; //汇总当天现金收入
	
    public static final String HTTP_GET_DELIVERY_EMPLOYEE = BASE_HTTP + "stores/findStaff"; //获取当前店铺的所有配送员接口


	/**
	 * 
	 * jsonpostRequest 请求
	 * 
	 * @param context
	 * @param url
	 * @param params
	 * @param listener
	 */
	public static void jsonPostRequest(final Context context, String url,
			Map<String, String> params,
			Response.Listener<org.json.JSONObject> listener) {
		int nonce = random6();
		Long timestamp = new Date().getTime();
		String sign = Api
				.md5(Api.SIGN_KEY + Api.SIGN_SCOPE + nonce + timestamp);

		StringBuffer sb = new StringBuffer();
		if (url.contains("?")) {
			sb.append("&");
		} else {
			sb.append("?");
		}

		sb.append("signature=");
		sb.append(sign);
		sb.append("&scope=");
		sb.append(Api.SIGN_SCOPE);
		sb.append("&nonce=");
		sb.append(nonce);
		sb.append("&timestamp=");
		sb.append(timestamp);
		url += sb.toString();
		org.json.JSONObject jsonObject = new org.json.JSONObject(params);
		JsonRequest<org.json.JSONObject> jsonRequest = new JsonObjectRequest(
				Method.POST, url, jsonObject, listener,
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e("JSON_POST_REQUEST", error.getMessage(), error);
						UI.toast(context.getApplicationContext(),
								"失败:" + error.getMessage());
					}
				}) {
			@Override
			public Map<String, String> getHeaders() {
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Accept", "application/json");
				headers.put("Content-Type", "application/json; charset=UTF-8");
				return headers;
			}
		};

		VolleyManager.getInstance(context.getApplicationContext())
				.getRequestQueue().add(jsonRequest);
	}

	/**
	 * 
	 * jsonpostRequest 请求
	 * 
	 * @param context
	 * @param url
	 * @param params
	 * @param listener
	 */
	public static void jsonPostRequestWithErrorListener(final Context context,
			String url, Map<String, String> params,
			Response.Listener<org.json.JSONObject> listener,
			Response.ErrorListener error) {
		int nonce = random6();
		Long timestamp = new Date().getTime();
		String sign = Api
				.md5(Api.SIGN_KEY + Api.SIGN_SCOPE + nonce + timestamp);

		StringBuffer sb = new StringBuffer();
		if (url.contains("?")) {
			sb.append("&");
		} else {
			sb.append("?");
		}

		sb.append("signature=");
		sb.append(sign);
		sb.append("&scope=");
		sb.append(Api.SIGN_SCOPE);
		sb.append("&nonce=");
		sb.append(nonce);
		sb.append("&timestamp=");
		sb.append(timestamp);
		url += sb.toString();

		org.json.JSONObject jsonObject = new org.json.JSONObject(params);
		JsonRequest<org.json.JSONObject> jsonRequest = new JsonObjectRequest(
				Method.POST, url, jsonObject, listener, error) {
			@Override
			public Map<String, String> getHeaders() {
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Accept", "application/json");
				headers.put("Content-Type", "application/json; charset=UTF-8");
				return headers;
			}
		};

		VolleyManager.getInstance(context.getApplicationContext())
				.getRequestQueue().add(jsonRequest);
	}

	/**
	 * 
	 * jsonpostRequest 请求
	 * 
	 * @param context
	 * @param url
	 * @param params
	 * @param listener
	 */
	public static void jsonGetRequest(final Context context, String url,
			Listener<String> result) {

		int nonce = random6();
		Long timestamp = new Date().getTime();
		String sign = Api
				.md5(Api.SIGN_KEY + Api.SIGN_SCOPE + nonce + timestamp);

		StringBuffer sb = new StringBuffer();
		if (url.contains("?")) {
			sb.append("&");
		} else {
			sb.append("?");
		}

		sb.append("signature=");
		sb.append(sign);
		sb.append("&scope=");
		sb.append(Api.SIGN_SCOPE);
		sb.append("&nonce=");
		sb.append(nonce);
		sb.append("&timestamp=");
		sb.append(timestamp);
		url += sb.toString();

		UTF8StringRequest stringRequest = new UTF8StringRequest(Method.GET,
				url, result, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						if (UI.isShowLoading()) {
							UI.dismissLoading(context);
						}
						UI.toast(context.getApplicationContext(),
								"失败" + error.getMessage());
					}
				});

		VolleyManager.getInstance(context.getApplicationContext())
				.getRequestQueue().add(stringRequest);
	}

	public static int random6() {
		Random random = new Random();
		int x = random.nextInt(899999);
		x = x + 100000;
		return x;
	}

	/**
	 * 字符串md5
	 * 
	 * @param string
	 * @return
	 */
	public static String md5(String string) {
		byte[] hash;
		try {
			hash = MessageDigest.getInstance("MD5").digest(
					string.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Huh, MD5 should be supported?", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Huh, UTF-8 should be supported?", e);
		}

		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10)
				hex.append("0");
			hex.append(Integer.toHexString(b & 0xFF));
		}
		return hex.toString();
	}

	public static void getBitmap(Context context, String ticket,
			Listener<Bitmap> result, Response.ErrorListener error) {
		@SuppressWarnings("deprecation")
		ImageRequest imageRequest = new ImageRequest(ticket, result, 200, 200,
				Config.RGB_565, error);

		VolleyManager.getInstance(context.getApplicationContext())
				.getRequestQueue().add(imageRequest);
	}

}
