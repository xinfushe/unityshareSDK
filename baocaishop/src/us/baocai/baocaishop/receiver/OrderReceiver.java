package us.baocai.baocaishop.receiver;

import java.io.File;
import java.io.FileOutputStream;

import us.baocai.baocaishop.LoginActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.igexin.sdk.PushConsts;

public class OrderReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Log.d("OrderReceiver", "onReceive() action=" + bundle.getInt("action"));

		switch (bundle.getInt(PushConsts.CMD_ACTION)) {

		case PushConsts.GET_CLIENTID:
			// 获取ClientID(CID)
			// 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
//			String cid = bundle.getString("clientid");
//			LoginActivity.strCid=cid;
//			LoginActivity.cid.setText("开启推送中cid:" + cid);

			// writeFileToSD(cid);
			
			break;
		case PushConsts.THIRDPART_FEEDBACK:

			break;
		default:
			break;
		}

	}

	/**
	 * postCid
	 */
	private void postCID() {

	}

	// /**
	// * jsonPost请求
	// * @param url
	// * @param params
	// * @param listener
	// */
	// private void jsonPostRequest(final Context context,String url ,Map<String
	// ,String> params,Response.Listener<org.json.JSONObject> listener){
	//
	// org.json.JSONObject jsonObject = new org.json.JSONObject(params);
	// JsonRequest<org.json.JSONObject> jsonRequest = new JsonObjectRequest(
	// Method.POST,
	// url,
	// jsonObject, listener, new Response.ErrorListener() {
	// @Override
	// public void onErrorResponse(VolleyError error) {
	// UI.toast(context.getApplicationContext(),
	// "失败:" + error.getMessage());
	// }
	// }) {
	// @Override
	// public Map<String, String> getHeaders() {
	// HashMap<String, String> headers = new HashMap<String, String>();
	// headers.put("Accept", "application/json");
	// headers.put("Content-Type", "application/json; charset=UTF-8");
	// return headers;
	// }
	// };
	// VolleyManager.getInstance(context.getApplicationContext()).getRequestQueue().add(jsonRequest);
	// }
}
