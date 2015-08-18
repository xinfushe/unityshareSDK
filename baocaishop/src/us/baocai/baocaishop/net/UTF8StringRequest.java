package us.baocai.baocaishop.net;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.lidroid.xutils.http.RequestParams;

/**
 * utf8编码请求
 * 
 * @author studyjun
 * 
 */
public class UTF8StringRequest extends StringRequest {

	public UTF8StringRequest(int method, String url,
			Response.Listener<String> listener,
			Response.ErrorListener errorListener) {
		super(method, url, listener, errorListener);
	}

	/**
	 * 防止乱码
	 * 
	 * @param response
	 * @return
	 */
	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {

		String str = null;
		try {
			str = new String(response.data, "utf-8");

		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		}
		return Response.success(str,
				HttpHeaderParser.parseCacheHeaders(response));
	}

	@Override
	protected VolleyError parseNetworkError(VolleyError volleyError) {
		return super.parseNetworkError(volleyError);
	}

//	@Override
//	protected Map<String, String> getParams() throws AuthFailureError {
//		
//		Map<String, String> params = super.getParams();
//		if(params==null){
//			params = new HashMap<String ,String>();
//		}
//		
//		int nonce = Api.random6();
//		Long timestamp = new Date().getTime();
//		String sign = Api
//				.md5(Api.SIGN_KEY + Api.SIGN_SCOPE + nonce + timestamp);
//		RequestParams requestParams = new RequestParams();
//		params.put("signature", sign);
//		params.put("scope", Api.SIGN_SCOPE);
//		params.put("nonce", nonce + "");
//		params.put("timestamp", timestamp + "");
//		return params;
//	}
}