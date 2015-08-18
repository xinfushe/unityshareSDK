package us.baocai.baocaishop.wxapi;

import us.baocai.baocaishop.LoginActivity;
import us.baocai.baocaishop.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth.Resp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

	private static final String APPID = "wx13952f4a73c86071";

	private IWXAPI api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		api = WXAPIFactory.createWXAPI(this, APPID, false);
		api.handleIntent(getIntent(), this);
	}

	public void onReq(BaseReq arg0) {
		// TODO Auto-generated method stub

	}

	public void onResp(BaseResp resp) {
		String result = "0";
		Bundle bundle = new Bundle();
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			result = "suceess";
			resp.toBundle(bundle);
			Resp sp = new Resp(bundle);
			getToken(sp.code);
			Toast.makeText(this, result + ":"+sp.code, Toast.LENGTH_LONG).show();
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = "errcode_cancel";
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = "R.string.errcode_deny";
			break;
		default:
			result = "R.string.errcode_unknown";
			break;
		}

		
	}

	/**
	 * 跳转
	 * @param code
	 */
	private void getToken(String code) {
		Intent intent = new Intent(this,LoginActivity.class);
		intent.putExtra("code", code);
		startActivity(intent);
		
	}
	

}