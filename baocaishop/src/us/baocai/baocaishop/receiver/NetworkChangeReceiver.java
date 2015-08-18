package us.baocai.baocaishop.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkChangeReceiver extends BroadcastReceiver {
	private ConnectivityManager mConnectivityManager;

	private NetworkInfo netInfo;

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();
		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

			mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			netInfo = mConnectivityManager.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isAvailable()) {

				// 网络连接
				String name = netInfo.getTypeName();

				if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
					// WiFi网络

				} else if (netInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
					// 有线网络

				} else if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
					// 手机网络

				}
			} else {
				//网络断开

			}
		}
	}

}
