package us.baocai.baocaishop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import us.baocai.baocaishop.bean.Store;
import us.baocai.baocaishop.net.Api;
import us.baocai.baocaishop.push.BaocaiPushService;
import us.baocai.baocaishop.push.PushCode;
import us.baocai.baocaishop.util.Const;
import us.baocai.baocaishop.util.DeviceUtil;
import us.baocai.baocaishop.util.NetworkUtil;
import us.baocai.baocaishop.util.StringUtil;
import us.baocai.baocaishop.util.UI;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response.Listener;
import com.gprinter.aidl.GpService;
import com.gprinter.command.GpCom;
import com.gprinter.io.GpDevice;
import com.gprinter.io.PortParameters;
import com.gprinter.save.PortParamDataBase;
import com.gprinter.service.GpPrintService;
import com.igexin.sdk.PushManager;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

/***
 * 
 * @author stduyjun
 * 
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener {

	private static final String TAG = "LoginActivity";
	public static final String CONNECT_STATUS = "connect.status";
	private Button btnGtOrder;
	private TextView userName;
	private TextView btnPrinterStatus;
	private EditText activationCode;
	private Button active;

	public TextView cid;

	public String strCid = "";
	public boolean isStarting = true;

	private String userjson;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		UmengUpdateAgent.update(this); // 检测应用更新

		regToGeTui(); // 注册个推

		btnGtOrder = (Button) findViewById(R.id.gotoorderhome);
		userName = (TextView) findViewById(R.id.username);
		btnPrinterStatus = (TextView) findViewById(R.id.printer_status);
		cid = (TextView) findViewById(R.id.cid);
		activationCode = (EditText) findViewById(R.id.activationCode);
		active = (Button) findViewById(R.id.activation);

		btnGtOrder.setOnClickListener(this);
		active.setOnClickListener(this);

		initPrint();

		checkLogin();
		
		registerBroadcast();

		if (!NetworkUtil.isNetworkConnected(getApplicationContext())) {
			UI.toast(getApplicationContext(), "当前网络未连接");
		} 

		// 线程查询，每隔1秒查询打印机状态
		Thread thread = new Thread() {

			int i = 0;

			@Override
			public void run() {
				super.run();
				try {
					sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				while (flag) {
					try {
						sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (getUseablePrinter().size() < 2) {
						i += 2;
						if (i % 10 == 0) {
							// 打印机重连
							handler.sendEmptyMessage(Const.PRINTER_CONNECTED);

						} else {
							Message msg = Message.obtain();
							msg.obj = (10 - i % 10);
							msg.what = Const.COUNT_SECOND;
							handler.sendMessage(msg);
						}

					} else {

						updateUseablePrinter();
					}

				}
			}
		};

		thread.start();

		DeviceUtil.openBluetooth(this);
	}

	private boolean flag = true;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Const.PRINTER_CONNECTED:
				btnPrinterStatus.setText("打印机重连中...如多次连接失败，请试着手动连接或重启打印机");
				btnPrinterStatus.setBackgroundColor(Color
						.argb(255, 204, 00, 33));
				openPrinter();
				break;
			case Const.COUNT_SECOND:
				btnPrinterStatus.setText("失去打印机," + msg.obj + "秒后重连打印机");
				btnPrinterStatus.setBackgroundColor(Color
						.argb(255, 204, 00, 33));
				break;
			case Const.PRINTER_NO_CONNECTED:
				btnPrinterStatus.setText("打印机连接失败");
				btnPrinterStatus.setBackgroundColor(Color
						.argb(255, 204, 00, 33));
				break;
			case Const.PRINTER_SUCCESS:
				btnPrinterStatus.setText("打印机连接成功");
				btnPrinterStatus
						.setBackgroundColor(Color.argb(255, 00, 99, 00));
				break;

			default:
				break;
			}
		};
	};

	/**
	 * 初始化蓝牙模块
	 */
	private void initPrint() {
		startService();
		connection();
		initPortParam();

	}

	@Override
	protected void onStop() {
		super.onStop();
		flag = false;
	}

	/**
	 * 检测登录
	 */
	private void checkLogin() {

		if(StringUtil.getVersionCode(this)<22){
			SharedPreferences spf = getSharedPreferences("shop",
					Activity.MODE_PRIVATE);
			Editor edit = spf.edit();
			edit.putString("id", "");
			edit.putString("name", "");
			edit.commit();
		}
		
		
		SharedPreferences spf = getSharedPreferences("shop",
				Activity.MODE_PRIVATE);
//		Editor edit = spf.edit();
//		edit.putString("id", "" + 4);
//		edit.putString("name", "多丽");
//		edit.commit();
		
		final String id = spf.getString("id", "");
		String name = spf.getString("name", "");
		if (!StringUtil.isEmpty(id)) {
			userName.setText(name);
			activationCode.setVisibility(View.INVISIBLE);
			active.setVisibility(View.INVISIBLE);
			btnGtOrder.setVisibility(View.VISIBLE);

			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					
					getStoresWeight(id);
				}
			}, 200);
			
			

			startBaocaiPush(id);
			

		} else {
			activationCode.setVisibility(View.VISIBLE);
			active.setVisibility(View.VISIBLE);
			btnGtOrder.setVisibility(View.INVISIBLE);
		}

	}

	private void startBaocaiPush(String shop) {
		if (NetworkUtil.isNetworkConnected(getApplicationContext())) {
			Intent intent = new Intent(this, BaocaiPushService.class);
			intent.putExtra("shopid", shop);
			startService(intent);
		} 
		
	}

	/**
	 * 注册个推
	 */
	private void regToGeTui() {
		regRC();
//		PushManager.getInstance().initialize(this.getApplicationContext());
	}

	/**
	 * 注册透传消息
	 */
	private void regRC() {
		pushReceiver = new PushListenerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(PushCode.ACTION);
		registerReceiver(pushReceiver, filter);
	}

	/**
	 * postCID，绑定用户和设备，用于个推推送
	 */
	private void postCID(String cid, final String strActivationCode) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("regCode", strActivationCode);
		// map.put("regCode", "a333");
		map.put("cid", cid);
		// UI.showLoading(this);
		Api.jsonPostRequest(getApplicationContext(), Api.HTTP_REG_CID, map,
				new Listener<org.json.JSONObject>() {

					@Override
					public void onResponse(org.json.JSONObject result) {
						// UI.dismissLoading(LoginActivity.this);
						try {
							boolean responseStatus = result
									.getBoolean("success");
							if (responseStatus) {
								UI.toast(getApplicationContext(), "激活成功");

								SharedPreferences sdf = getSharedPreferences(
										"activeCode", Activity.MODE_APPEND);
								Editor edit = sdf.edit();
								edit.putString("activeCode", strActivationCode);
								edit.commit();

								activationCode.setVisibility(View.INVISIBLE);
								active.setVisibility(View.INVISIBLE);

								getStores(strActivationCode);

							} else {

								UI.toast(getApplicationContext(),
										result.toString());
							}

						} catch (JSONException e) {
							// UI.dismissLoading(LoginActivity.this);
							e.printStackTrace();
							UI.toast(getApplicationContext(), result.toString());
						}

					}

				});

	}

	@Override
	protected void onResume() {
		super.onResume();
		updateUseablePrinter();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private GpService mGpService = null;

	public List<Integer> getUseablePrinter() {
		List<Integer> uselist = new ArrayList<Integer>();
		for (int i = 0; i < 2; i++) {

			try {
				if (mGpService.getPrinterConnectStatus(i) == GpDevice.STATE_CONNECTED) {
					// int status = mGpService.queryPrinterStatus(i, 1000);
					// if (status == 0) {
					//
					// }
					uselist.add(i);

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return uselist;
	}

	public void updateUseablePrinter() {
		int count = getUseablePrinter().size();
		if (count >= 2) {
			handler.sendEmptyMessage(Const.PRINTER_SUCCESS);
		} else {
			handler.sendEmptyMessage(Const.PRINTER_NO_CONNECTED);
		}

	}

	public boolean[] getConnectState() {
		boolean[] state = new boolean[GpPrintService.MAX_PRINTER_CNT];
		for (int i = 0; i < GpPrintService.MAX_PRINTER_CNT; i++) {
			state[i] = false;
		}
		for (int i = 0; i < GpPrintService.MAX_PRINTER_CNT; i++) {

			try {
				if (mGpService.getPrinterConnectStatus(i) == GpDevice.STATE_CONNECTED) {
					state[i] = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return state;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.action_reboot_push:
			if (isStarting) {

				// 当前为运行状态，停止SDK服务
				Log.d("Loginactivity", "stopping sdk...");
				PushManager.getInstance().stopService(getApplicationContext());

				// UI更新
				cid.setText("cid:停止推送中");

				isStarting = false;
				strCid = "";

			}
			// 当前未运行状态，启动SDK服务
			Log.d("Loginactivity", "reinitializing sdk...");

			// 重新初始化sdk
			PushManager.getInstance().initialize(getApplicationContext());

			// UI更新
			isStarting = true;

			break;
		case R.id.action_printer: // 连接打印机
			Intent intent = new Intent(LoginActivity.this,
					BaocaiPrinterConnectionActivity.class);
			// AutoConnectPrinterConnectDialog.class);
			boolean[] state = getConnectState();
			intent.putExtra(CONNECT_STATUS, state);
			startActivity(intent);
			break;
		case R.id.action_default:
			Builder builder =new AlertDialog.Builder(LoginActivity.this);
			builder.setTitle("你确定要恢复到出厂设置？");
			builder.setMessage("你确定要恢复到出厂设置？");
			builder.setPositiveButton("确定",  new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences spf = getSharedPreferences("shop",
							Activity.MODE_PRIVATE);
					Editor edit = spf.edit();
					edit.putString("id", "" );
					edit.putString("name", "");
					edit.commit();
					UI.toast(getApplicationContext(), "清除成功");
					
				}
			});
			builder.setNegativeButton("取消", null);
			builder.create().show();

			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// 蓝牙打印模块
	private PrinterServiceConnection conn = null;

	class PrinterServiceConnection implements ServiceConnection {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i("ServiceConnection", "onServiceDisconnected() called");
			mGpService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mGpService = GpService.Stub.asInterface(service);
			// UI.toast(getApplicationContext(), "connnnnnnnnnnnnnnnnnn");

			closePrinter();

			openPrinter();
		}

	};

	/**
	 * 关闭打印机
	 */
	private void closePrinter() {
		try {
			mGpService.closePort(0);
			mGpService.closePort(1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 启动佳博蓝牙服务
	 */
	private void startService() {
		Intent i = new Intent(this, GpService.class);
		startService(i);

	}

	private void connection() {
		conn = new PrinterServiceConnection();
		Intent intent = new Intent(this, GpPrintService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE); // bindService
	}

	private PortParameters mPortParam[] = new PortParameters[GpPrintService.MAX_PRINTER_CNT]; // 端口
	private PushListenerReceiver pushReceiver;

	private void initPortParam() {
		boolean[] state = getConnectState();
		for (int i = 0; i < 2; i++) {
			PortParamDataBase database = new PortParamDataBase(this);
			mPortParam[i] = new PortParameters();
			mPortParam[i] = database.queryPortParamDataBase("" + i);
			mPortParam[i].setPortOpenState(state[i]);
		}
	}

	int ttti = 0;

	/**
	 * 打开打印机
	 */
	private void openPrinter() {
		int rel = 0;
		all: for (int i = 0; i < 2; i++) {
			PortParameters pp = mPortParam[i];
			if (PortParameters.BLUETOOTH == pp.getPortType()) {
				try {

					int status = mGpService.getPrinterConnectStatus(i);
					Log.d(TAG, "status:" + status);

					if (status == GpDevice.STATE_CONNECTED) {
						int statusP = mGpService.queryPrinterStatus(i, 1000);

					} else {

						if (i == 0 && isPrinter01CanConnect) {
							rel = mGpService.openPort(i, pp.getPortType(),
									pp.getBluetoothAddr(), 0);
						} else if (i == 1 && isPrinter02CanConnect) {
							rel = mGpService.openPort(i, pp.getPortType(),
									pp.getBluetoothAddr(), 0);
						}

					}
				} catch (Exception e) {
					Log.e(TAG, "pirnter connected:" + e.getMessage());
					// UI.toast(getApplicationContext(), ""+e.getMessage());
				}

				GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
				if (r != GpCom.ERROR_CODE.SUCCESS) {
					if (r == GpCom.ERROR_CODE.DEVICE_ALREADY_OPEN) {
						pp.setPortOpenState(true);
						getConnectState();
					}
				}
			}
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		try {
			unbindService(conn);
			unregisterReceiver(pushReceiver);
			unregisterReceiver(printerStatusBroadcastReceiver);
		} catch (Exception e) {
			Log.e(TAG, "on destory:" + e.getMessage());
		}
	}

	/**
	 * 获取店员详情
	 * 
	 * @param shopId
	 */
	private void getUserInfo(String shopId) {
		Api.jsonGetRequest(this, Api.HTTP_GET_DELIVERY_EMPLOYEE +"?shop_id="+ shopId,
				new Listener<String>() {

					@Override
					public void onResponse(String result) {
						try {
		                    JSONObject jsonObject = JSON.parseObject(result);
		                    if (jsonObject.get("success")!=null&&jsonObject.getBoolean("success")){

		                        SharedPreferences spf = getSharedPreferences(
										"emeployee", Activity.MODE_APPEND);
								Editor edt = spf.edit();
								edt.putString("emeployee", jsonObject.getString("data"));
								edt.commit();

								userjson = result;
		                    }
		                }catch (Exception e){
		                    MobclickAgent.reportError(getApplicationContext(),"获取店铺人员失败"+e.getMessage()+":"+result);
		                }
						

					}
				});

	}

	/**
	 * 获取门店
	 */
	public void getStores(final String activeCode) {
		Api.jsonGetRequest(getApplicationContext(), Api.HTTP_GET_STORES,
				new Listener<String>() {

					@Override
					public void onResponse(String result) {
						UI.toast(getApplicationContext(), "获取店铺成功");
						try {
							List<Store> stores = JSONArray.parseArray(result,
									Store.class);
							for (Store store : stores) {
								if (store.getReg_code().equals(activeCode)) { // 对应激活码
									SharedPreferences spf = getSharedPreferences(
											"shop", Activity.MODE_APPEND);
									Editor edit = spf.edit();
									edit.putString("id", "" + store.getId());
									edit.putString("name", store.getName());
									edit.putInt("shop_group", store.getShop_group());
									edit.putInt("deliver_id", store.getDeliver_id());
									edit.putInt("group_weight", store.getGroup_weight());
									edit.putString("group_name", store.getGroup_name());
									
									edit.commit();

									userName.setText(store.getName());
									activationCode
											.setVisibility(View.INVISIBLE);
									btnGtOrder.setVisibility(View.VISIBLE);

									startBaocaiPush("" + store.getId());
									getUserInfo(store.getId() + "");
								}
							}
						} catch (Exception e) {
							UI.toast(getApplicationContext(), "获取店铺失败");
							activationCode.setVisibility(View.VISIBLE);
							active.setVisibility(View.VISIBLE);
						}

					}

				});
	}
	
	
	/**
	 * 获取门店
	 */
	public void getStoresWeight(final String id) {
		Api.jsonGetRequest(getApplicationContext(), Api.HTTP_GET_STORES,
				new Listener<String>() {

					@Override
					public void onResponse(String result) {
						UI.toast(getApplicationContext(), "获取店铺成功");
						try {
							List<Store> stores = JSONArray.parseArray(result,
									Store.class);
							for (Store store : stores) {
								if (String.valueOf(store.getId()).equals(id)) { // 对应激活码
									SharedPreferences spf = getSharedPreferences(
											"shop", Activity.MODE_APPEND);
									Editor edit = spf.edit();
									edit.putString("id", "" + store.getId());
									edit.putString("name", store.getName());
									edit.putInt("shop_group", store.getShop_group());
									edit.putInt("deliver_id", store.getDeliver_id());
									edit.putInt("group_weight", store.getGroup_weight());
									edit.putString("group_name", store.getGroup_name());
									
								
									edit.commit();

									userName.setText(store.getName());
									activationCode
											.setVisibility(View.INVISIBLE);
									btnGtOrder.setVisibility(View.VISIBLE);

									startBaocaiPush("" + store.getId());
									getUserInfo(store.getId() + "");
								}
							}
						} catch (Exception e) {
							UI.toast(getApplicationContext(), "获取店铺失败");
							activationCode.setVisibility(View.VISIBLE);
							active.setVisibility(View.VISIBLE);
						}

					}

				});
	}

	@Override
	public void onClick(View v) {
		if (v == btnGtOrder) { // 进入订单页面
			if (StringUtil.isEmpty(strCid)) {
				UI.toastTop(getApplicationContext(), "当前推送服务未开启,请开启后推送");
				return;
			}

//			if (getUseableBillPrinter() < 0) {
//				UI.toastTop(getApplicationContext(), "票据打印机没连接");
//
//				return;
//			}
//			if (getUseableLablePrinter() < 0) {
//				UI.toastTop(getApplicationContext(), "标签打印机没连接");
//
//				return;
//			}
			if (StringUtil.isEmpty(userjson)) {
				userjson = getSharedPreferences("emeployee",
						Activity.MODE_PRIVATE).getString("emeployee", "");
			}
			
			Intent intent = new Intent(LoginActivity.this,
					OrderActivity.class);
			startActivity(intent);
			finish();
//			if (!StringUtil.isEmpty(userjson)) {
//				Intent intent = new Intent(LoginActivity.this,
//						OrderActivity.class);
//				startActivity(intent);
//				finish();
//			} else {
//				UI.toast(getApplicationContext(), "店铺人员获取不到");
//			}

		} else if (v == active) {
			String strActivationCode = activationCode.getText().toString();
			SharedPreferences sdf = getSharedPreferences("activeCode",
					Activity.MODE_APPEND);
			Editor edit = sdf.edit();
			edit.putString("activeCode", strActivationCode);
			edit.commit();
			if (StringUtil.isEmpty(strActivationCode)) {
				UI.toast(getApplicationContext(), "cid或者激活码为空");
			} else {
				postCID("tttttttttt", strActivationCode);
			}

		}
	}

	/**
	 * 获取标签打印机序号
	 * 
	 * @return
	 */
	public int getUseableLablePrinter() {
		int result = -1;
		for (int i = 0; i < GpPrintService.MAX_PRINTER_CNT; i++) {

			try {

				if (mGpService.getPrinterConnectStatus(i) == GpDevice.STATE_CONNECTED) {
					int type = mGpService.getPrinterCommandType(i);
					if (type == GpCom.TSC_COMMAND) {
						int status = mGpService.queryPrinterStatus(i, 800);
						if (status == 0) {
							return i;
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 获取票据打印机序号
	 * 
	 * @return
	 */
	public int getUseableBillPrinter() {
		int result = -1;
		for (int i = 0; i < GpPrintService.MAX_PRINTER_CNT; i++) {

			try {

				if (mGpService.getPrinterConnectStatus(i) == GpDevice.STATE_CONNECTED) {
					int type = mGpService.getPrinterCommandType(i);
					if (type == GpCom.ESC_COMMAND) {
						int status = mGpService.queryPrinterStatus(i, 1000);
						if (status == 0) {
							return i;
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 打印机状态监听
	 */
	private void registerBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_CONNECT_STATUS);
		this.registerReceiver(printerStatusBroadcastReceiver, filter);
	}

	public static final String ACTION_CONNECT_STATUS = "action.connect.status";
	private boolean isPrinter01CanConnect = true;
	private boolean isPrinter02CanConnect = true;

	private BroadcastReceiver printerStatusBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (ACTION_CONNECT_STATUS.equals(intent.getAction())) {
				int type = intent.getIntExtra(GpPrintService.CONNECT_STATUS, 0);
				int id = intent.getIntExtra(GpPrintService.PRINTER_ID, 0);
				Log.d(TAG, "connect status " + type);
				if (type == GpDevice.STATE_CONNECTING) { // 正在连接
					// UI.toast(getApplicationContext(), "正在连接");
					if (id == 1) {
						isPrinter02CanConnect = false;
					} else {
						isPrinter01CanConnect = false;
					}
				} else if (type == GpDevice.STATE_NONE) { // 连接断开
					// UI.toast(getApplicationContext(), "连接断开");

					if (id == 1) {
						isPrinter02CanConnect = true;
					} else {
						isPrinter01CanConnect = true;
					}
				} else if (type == GpDevice.STATE_VALID_PRINTER) { // 有效的打印机
					// UI.toast(getApplicationContext(), "有效的打印机");

					if (id == 1) {
						isPrinter02CanConnect = false;
					} else {
						isPrinter01CanConnect = false;
					}
				} else if (type == GpDevice.STATE_INVALID_PRINTER) { // 无效的打印机
					// UI.toast(getApplicationContext(), "无效的打印机");

					if (id == 1) {
						isPrinter02CanConnect = true;
					} else {
						isPrinter01CanConnect = true;
					}
				} else if (type == GpDevice.STATE_CONNECTED) { // 已经连接
					// UI.toast(getApplicationContext(), "已经连接");

					if (id == 1) {
						isPrinter02CanConnect = true;
					} else {
						isPrinter01CanConnect = true;
					}
				}
			}
		}
	};

	/**
	 * 消息透传，个推推送过来订单信息
	 * 
	 * @author studyjun
	 * 
	 */
	class PushListenerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			Log.d(TAG, "onReceive() action=" + bundle.getInt("action"));

			// switch (bundle.getInt(PushConsts.CMD_ACTION)) {

			switch (bundle.getInt("code")) {
			case PushCode.GET_SHOPID_SUCCESS:
				String cid = bundle.getString("clientid");
				LoginActivity.this.strCid = cid;
				LoginActivity.this.cid.setText("开启推送中cid:" + cid);
				break;

			}
		}
	}
}
