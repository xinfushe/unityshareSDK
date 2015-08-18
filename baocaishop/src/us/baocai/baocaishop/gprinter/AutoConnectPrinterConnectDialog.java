package us.baocai.baocaishop.gprinter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import us.baocai.baocaishop.R;
import us.baocai.baocaishop.receiver.BluetoothConnectActivityReceiver;
import us.baocai.baocaishop.util.UI;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.gprinter.aidl.GpService;
import com.gprinter.command.GpCom;
import com.gprinter.io.GpDevice;
import com.gprinter.io.PortParameters;
import com.gprinter.save.PortParamDataBase;
import com.gprinter.service.GpPrintService;

public class AutoConnectPrinterConnectDialog extends Activity {
	private final static String DEBUG_TAG = "PrinterConnectDialog";
	public static final String ACTION_CONNECT_STATUS = "action.connect.status";
	private static final int INTENT_PORT_SETTINGS = 0;
	private ListViewAdapter mListViewAdapter = null;
	private List<Map<String, Object>> mList = null;
	private PortParameters mPortParam[] = new PortParameters[GpPrintService.MAX_PRINTER_CNT - 1];
	private int mPrinterId = 0;
	private GpService mGpService;
	private PrinterServiceConnection conn = null;
	private Button oneKeyConnect;
	public static final int REQUEST_ENABLE_BT = 2;
	public static final int REQUEST_OPEN_BLUETOOTH = 4;
	public static final int REQUEST_CONNECT_DEVICE = 3;

	public class PrinterSeial {
		static final int GPIRNTER001 = 0;
		static final int GPIRNTER002 = 1;
		static final int GPIRNTER003 = 2;
		static final int GPIRNTER004 = 3;
		static final int GPIRNTER005 = 4;
		static final int GPIRNTER006 = 5;
	}

	class PrinterServiceConnection implements ServiceConnection {
		@Override
		public void onServiceDisconnected(ComponentName name) {

			Log.i(DEBUG_TAG, "onServiceDisconnected() called");
			mGpService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mGpService = GpService.Stub.asInterface(service);

		}
	};

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.e(DEBUG_TAG, "onResume");
	}

	private void connection() {
		conn = new PrinterServiceConnection();
		Log.i(DEBUG_TAG, "connection");
		Intent intent = new Intent(this,GpPrintService.class);
		startService(intent);
		bindService(intent, conn, Context.BIND_AUTO_CREATE); // bindService
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.dialog_port);

		oneKeyConnect = (Button) findViewById(R.id.oneKeyConnect);
		Log.e(DEBUG_TAG, "onCreate ");
		initPortParam();
		initView();
		registerBroadcast();
		connection();

		oneKeyConnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				autoFindPrinterDevices();
				for (int i = 0; i < mPortParam.length; i++) {
					PortParameters pp = mPortParam[i];
					if (PortParameters.BLUETOOTH == pp.getPortType()) {
						try {
							if (mGpService.getPrinterConnectStatus(i) == GpDevice.STATE_CONNECTED) {
								// int status = mGpService.queryPrinterStatus(i,
								// 1000);

							} else {
								connectOrDisConnectToDevice(i);
							}
						} catch (Exception e) {
						}

					}
				}

			}
		});
	}

	private void initPortParam() {
		Intent intent = getIntent();
		boolean[] state = intent
				.getBooleanArrayExtra(MainActivity.CONNECT_STATUS);
		for (int i = 0; i < GpPrintService.MAX_PRINTER_CNT - 1; i++) {
			PortParamDataBase database = new PortParamDataBase(this);
			mPortParam[i] = new PortParameters();
			mPortParam[i] = database.queryPortParamDataBase("" + i);
			mPortParam[i].setPortOpenState(state[i]);
		}
	}

	@Override
	protected void onDestroy() {

		Log.e(DEBUG_TAG, "onDestroy ");
		super.onDestroy();

		try {
			this.unregisterReceiver(PrinterStatusBroadcastReceiver);
//			this.unregisterReceiver(blueConnectionReceiver);
			this.unregisterReceiver(mFindBlueToothReceiver);
		} catch (Exception e) {
			Log.d(DEBUG_TAG, "unregisterReceiver error:" + e.getMessage());
		}

		try {
			if (conn != null) {
				unbindService(conn); // unBindService
			}
		} catch (Exception e) {
			Log.d(DEBUG_TAG, "unbindService error:" + e.getMessage());

		}
	}

	@Override
	protected void onPause() {

		super.onPause();

	}

	private void initView() {
		ListView list = (ListView) findViewById(R.id.lvOperateList);
		mList = getOperateItemData();
		mListViewAdapter = new ListViewAdapter(this, mList, mHandler);
		list.setAdapter(mListViewAdapter);
		list.setOnItemClickListener(new TitelItemOnClickLisener());
		list.setOnItemLongClickListener(new TitelItemOnLongClickLisener());
	}

	private void registerBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_CONNECT_STATUS);
		this.registerReceiver(PrinterStatusBroadcastReceiver, filter);
//		blueConnectionReceiver = new BluetoothConnectActivityReceiver();
//		IntentFilter itFilter = new IntentFilter(
//				"android.bluetooth.device.action.PAIRING_REQUEST");
//		this.registerReceiver(blueConnectionReceiver, itFilter);
	}

	private BroadcastReceiver PrinterStatusBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (ACTION_CONNECT_STATUS.equals(intent.getAction())) {
				int type = intent.getIntExtra(GpPrintService.CONNECT_STATUS, 0);
				int id = intent.getIntExtra(GpPrintService.PRINTER_ID, 0);
				Log.d(DEBUG_TAG, "connect status " + type);
				if (type == GpDevice.STATE_CONNECTING) {
					setProgressBarIndeterminateVisibility(true);
					SetLinkButtonEnable(ListViewAdapter.DISABLE);
					mPortParam[id].setPortOpenState(false);
					Map<String, Object> map;
					map = mList.get(id);
					map.put(ListViewAdapter.STATUS,
							getString(R.string.connecting));
					mList.set(id, map);
					mListViewAdapter.notifyDataSetChanged();

				} else if (type == GpDevice.STATE_NONE) {
					setProgressBarIndeterminateVisibility(false);
					SetLinkButtonEnable(ListViewAdapter.ENABLE);
					mPortParam[id].setPortOpenState(false);
					Map<String, Object> map;
					map = mList.get(id);
					map.put(ListViewAdapter.STATUS, getString(R.string.connect));
					mList.set(id, map);
					mListViewAdapter.notifyDataSetChanged();
				} else if (type == GpDevice.STATE_VALID_PRINTER) {
					setProgressBarIndeterminateVisibility(false);
					SetLinkButtonEnable(ListViewAdapter.ENABLE);
					mPortParam[id].setPortOpenState(true);
					Map<String, Object> map;
					map = mList.get(id);
					map.put(ListViewAdapter.STATUS, getString(R.string.cut));

					int printerType;
					try {
						printerType = mGpService.getPrinterCommandType(id);
						if (printerType == GpCom.TSC_COMMAND) {
							map.put(ListViewAdapter.TITEL, "标签打印机");
						} else if (printerType == GpCom.ESC_COMMAND) {
							map.put(ListViewAdapter.TITEL, "票据打印机");
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					mList.set(id, map);
					mListViewAdapter.notifyDataSetChanged();
				} else if (type == GpDevice.STATE_INVALID_PRINTER) {
					setProgressBarIndeterminateVisibility(false);
					SetLinkButtonEnable(ListViewAdapter.ENABLE);
					messageBox("请确认连接的是佳博打印机");
				}
			}
		}
	};

	private String getPortParamInfoString(PortParameters Param) {
		String info = new String();
		info = getString(R.string.port);
		int type = Param.getPortType();
		Log.d(DEBUG_TAG, "Param.getPortType() " + type);
		if (type == PortParameters.BLUETOOTH) {
			info += getString(R.string.bluetooth);
			info += "  " + getString(R.string.address);
			info += Param.getBluetoothAddr();
		} else if (type == PortParameters.USB) {
			info += getString(R.string.usb);
			info += "  " + getString(R.string.address);
			info += Param.getUsbDeviceName();
		} else if (type == PortParameters.ETHERNET) {
			info += getString(R.string.ethernet);
			info += "  " + getString(R.string.ip_address);
			info += Param.getIpAddr();
			info += "  " + getString(R.string.port_number);
			info += Param.getPortNumber();
		} else {
			info = getString(R.string.init_port_info);
		}

		return info;
	}

	void SetPortParamToView(PortParameters Param) {
		Map<String, Object> map;
		map = mList.get(mPrinterId);
		String info = getPortParamInfoString(Param);
		map.put(ListViewAdapter.INFO, info);
		mList.set(mPrinterId, map);
		mListViewAdapter.notifyDataSetChanged();
	}

	void SetPortParamToView(PortParameters Param, int position) {
		Map<String, Object> map;
		map = mList.get(position);
		String info = getPortParamInfoString(Param);
		map.put(ListViewAdapter.INFO, info);
		mList.set(position, map);
		mListViewAdapter.notifyDataSetChanged();
	}

	void SetLinkButtonEnable(String s) {
		Map<String, Object> map;
		for (int i = 0; i < GpPrintService.MAX_PRINTER_CNT - 1; i++) {
			map = mList.get(i);
			map.put(ListViewAdapter.BT_ENABLE, s);
			mList.set(i, map);
		}
		mListViewAdapter.notifyDataSetChanged();
	}

	private List<Map<String, Object>> getOperateItemData() {
		int[] PrinterID = new int[] { R.string.gprinter001,
				R.string.gprinter002, R.string.gprinter003,
				R.string.gprinter004, R.string.gprinter005 };
		int[] PrinterImage = new int[] { R.drawable.ic_printer,
				R.drawable.ic_printer, R.drawable.ic_printer,
				R.drawable.ic_printer, R.drawable.ic_printer };
		Map<String, Object> map;
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < GpPrintService.MAX_PRINTER_CNT - 1; i++) {
			map = new HashMap<String, Object>();
			map.put(ListViewAdapter.IMG, PrinterImage[i]);
			map.put(ListViewAdapter.TITEL, getString(PrinterID[i]));
			if (mPortParam[i].getPortOpenState() == false)
				map.put(ListViewAdapter.STATUS, getString(R.string.connect));
			else {
				map.put(ListViewAdapter.STATUS, getString(R.string.cut));
			}
			String str = getPortParamInfoString(mPortParam[i]);
			map.put(ListViewAdapter.INFO, str);
			map.put(ListViewAdapter.BT_ENABLE, "enable");
			list.add(map);
		}
		return list;
	}

	class TitelItemOnLongClickLisener implements OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			// TODO Auto-generated method stub
			Log.d(DEBUG_TAG, "TitelItemOnLongClickLisener " + arg2);
			Intent intent = new Intent(GpPrintService.ACTION_PRINT_TESTPAGE);
			intent.putExtra(GpPrintService.PRINTER_ID, arg2);
			sendBroadcast(intent);
			return true;
		}
	}

	class TitelItemOnClickLisener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub]
			mPrinterId = arg2;
			if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
				Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			} else {
				Intent intent = new Intent(
						AutoConnectPrinterConnectDialog.this,
						BluetoothDeviceList.class);
				startActivityForResult(intent, REQUEST_OPEN_BLUETOOTH);
			}
		}
	}

	void connectOrDisConnectToDevice(int PrinterId) {
		mPrinterId = PrinterId;
		int rel = 0;
		if (mPortParam[PrinterId].getPortOpenState() == false) {
			if (CheckPortParamters(mPortParam[PrinterId])) {
				switch (mPortParam[PrinterId].getPortType()) {

				case PortParameters.BLUETOOTH:
					try {
						rel = mGpService.openPort(PrinterId,
								mPortParam[PrinterId].getPortType(),
								mPortParam[PrinterId].getBluetoothAddr(), 0);
					} catch (Exception e) {
						Log.e(DEBUG_TAG, "printer error" + e.getMessage());
						e.printStackTrace();
					}
					break;
				}
				GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
				if (r != GpCom.ERROR_CODE.SUCCESS) {
					if (r == GpCom.ERROR_CODE.DEVICE_ALREADY_OPEN) {
						mPortParam[PrinterId].setPortOpenState(true);
						Map<String, Object> map;
						map = mList.get(PrinterId);
						map.put(ListViewAdapter.STATUS, getString(R.string.cut));
						mList.set(PrinterId, map);
						mListViewAdapter.notifyDataSetChanged();
					} else {
						messageBox(GpCom.getErrorText(r));
					}
				}
			} else {
				messageBox(getString(R.string.port_parameters_wrong));
			}
		} else {
			Log.d(DEBUG_TAG, "DisconnectToDevice ");
			setProgressBarIndeterminateVisibility(true);
			SetLinkButtonEnable(ListViewAdapter.DISABLE);
			Map<String, Object> map;
			map = mList.get(PrinterId);
			map.put(ListViewAdapter.STATUS, getString(R.string.cutting));
			mList.set(PrinterId, map);
			mListViewAdapter.notifyDataSetChanged();
			try {
				mGpService.closePort(PrinterId);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ListViewAdapter.MESSAGE_CONNECT:
				connectOrDisConnectToDevice(msg.arg1);
			}
			super.handleMessage(msg);
		}
	};

	Boolean CheckPortParamters(PortParameters param) {
		boolean rel = false;
		int type = param.getPortType();
		if (type == PortParameters.BLUETOOTH) {
			if (!param.getBluetoothAddr().equals("")) {
				rel = true;
			}
		}
		return rel;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "requestCode" + requestCode + '\n' + "resultCode"
				+ resultCode);
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == INTENT_PORT_SETTINGS) {
			// getIP settings info from IP settings dialog
			if (resultCode == RESULT_OK) {

				if (CheckPortParamters(mPortParam[mPrinterId])) {
					PortParamDataBase database = new PortParamDataBase(this);
					database.deleteDataBase("" + mPrinterId);
					database.insertPortParam(mPrinterId, mPortParam[mPrinterId]);
				} else {
					messageBox(getString(R.string.port_parameters_wrong));
				}

			} else if (requestCode == REQUEST_ENABLE_BT) {
				if (resultCode == Activity.RESULT_OK) {
					Intent intent = new Intent(
							AutoConnectPrinterConnectDialog.this,
							BluetoothDeviceList.class);
					startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
				} else {
					// bluetooth is not open
					Toast.makeText(this, R.string.bluetooth_is_not_enabled,
							Toast.LENGTH_SHORT).show();
				}
			} else {
				messageBox(getString(R.string.port_parameters_is_not_save));
			}
		} else if (requestCode == REQUEST_OPEN_BLUETOOTH
				&& RESULT_OK == resultCode) {

			mPortParam[mPrinterId].setPortType(PortParameters.BLUETOOTH);

			String str = data
					.getStringExtra(PortConfigurationActivity.EXTRA_DEVICE_ADDRESS);
			mPortParam[mPrinterId].setBluetoothAddr(str);
			Log.d(DEBUG_TAG, "BluetoothAddr " + str);
			SetPortParamToView(mPortParam[mPrinterId]);
			if (CheckPortParamters(mPortParam[mPrinterId])) {
				PortParamDataBase database = new PortParamDataBase(this);
				database.deleteDataBase("" + mPrinterId);
				database.insertPortParam(mPrinterId, mPortParam[mPrinterId]);
			} else {
				messageBox(getString(R.string.port_parameters_wrong));
			}
		}
	}

	private void messageBox(String err) {
		Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 自动找寻打印机设备，根据名称判断
	 */
	public void autoFindPrinterDevices() {
		boolean isReturn = true;
		for (int i = 0; i < 2; i++) { // 两个连接口 判断是否已经设置了蓝牙端口了
			if (PortParameters.BLUETOOTH != mPortParam[i].getPortType()) {
				isReturn = false;
			}

		}
		if (isReturn) {
			return;
		}

		if (!UI.isShowLoading()) {
			UI.showLoading(AutoConnectPrinterConnectDialog.this);
		}

		BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> bonded = bAdapter.getBondedDevices();

		IntentFilter foundFilter = new IntentFilter(
				BluetoothDevice.ACTION_FOUND);
		IntentFilter finishFilter = new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		HashMap<String, Boolean> isUse = new HashMap<String, Boolean>();
		for (BluetoothDevice device : bonded) {
			if (device.getName().equals("Gprinter")) {
				a: for (int i = 0; i < 2; i++) { // 两个连接口 判断是否已经设置了蓝牙端口了
					if (PortParameters.BLUETOOTH != mPortParam[i].getPortType()) {
						if (isUse.get(device.getAddress()) != null
								&& isUse.get(device.getAddress()) == true) {
							continue a;
						}
						mPortParam[i].setPortType(PortParameters.BLUETOOTH);
						String str = device.getAddress();
						mPortParam[i].setBluetoothAddr(str);
						Log.d(DEBUG_TAG, "BluetoothAddr " + str);
						SetPortParamToView(mPortParam[i], i);
						if (CheckPortParamters(mPortParam[i])) {
							PortParamDataBase database = new PortParamDataBase(
									this);
							database.deleteDataBase("" + i);
							database.insertPortParam(i, mPortParam[i]);
						}
						isUse.put(mPortParam[i].getBluetoothAddr(), true);

						// mListViewAdapter.notifyDataSetChanged();
					} else {
						isUse.put(mPortParam[i].getBluetoothAddr(), true);
					}

				}
			}

		}

		boolean isShowing = true;
		for (int i = 0; i < 2; i++) { // 两个连接口 判断是否已经设置了蓝牙端口了
			if (PortParameters.BLUETOOTH != mPortParam[i].getPortType()) {
				isShowing = false;
			}

		}
		if (isShowing && UI.isShowLoading()) {
			UI.dismissLoading(AutoConnectPrinterConnectDialog.this);
			return; // 已经配对完毕
		}
		registerReceiver(mFindBlueToothReceiver, foundFilter);
		registerReceiver(mFindBlueToothReceiver, finishFilter);

		// If we're already discovering, stop it
		if (bAdapter.isDiscovering()) {
			bAdapter.cancelDiscovery();
		}
		// Request discover from BluetoothAdapter
		bAdapter.startDiscovery();
	}

	// changes the title when discovery is finished
	private final BroadcastReceiver mFindBlueToothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed

				HashMap<String, Boolean> isUse = new HashMap<String, Boolean>();
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					if (device.getName() != null
							&& device.getName().equals("Gprinter")) { // 佳博打印机
						for (int i = 0; i < 2; i++) { // 两个连接口 判断是否已经设置了蓝牙端口了
							if (isUse.get(device.getAddress()) != null
									&& isUse.get(device.getAddress()) == true) {
								continue;
							}

							if (PortParameters.BLUETOOTH != mPortParam[i]
									.getPortType()) {
								mPortParam[i]
										.setPortType(PortParameters.BLUETOOTH);
								String str = device.getAddress();
								mPortParam[i].setBluetoothAddr(str);
								Log.d(DEBUG_TAG, "BluetoothAddr " + str);
								SetPortParamToView(mPortParam[i], i);
								if (CheckPortParamters(mPortParam[i])) {
									PortParamDataBase database = new PortParamDataBase(
											AutoConnectPrinterConnectDialog.this);
									database.deleteDataBase("" + i);
									database.insertPortParam(i, mPortParam[i]);
								}
								isUse.put(mPortParam[i].getBluetoothAddr(),
										true);

								// mListViewAdapter.notifyDataSetChanged();
							} else {
								isUse.put(mPortParam[i].getBluetoothAddr(),
										true);

							}

						}
					} else if (device.getName() == null) { // 有可能是
						if (UI.isShowLoading()) {
							UI.dismissLoading(AutoConnectPrinterConnectDialog.this);
						}
					}

					boolean isShowing = false;
					for (int i = 0; i < 2; i++) { // 两个连接口 判断是否已经设置了蓝牙端口了
						if (PortParameters.BLUETOOTH != mPortParam[i]
								.getPortType()) {
							isShowing = true;
						}

					}
					if (isShowing && UI.isShowLoading()) {
						UI.dismissLoading(AutoConnectPrinterConnectDialog.this);
					}

				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
			}
		}
	};
//	private BluetoothConnectActivityReceiver blueConnectionReceiver;

}
