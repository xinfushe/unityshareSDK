package us.baocai.baocaishop;

import java.util.HashMap;

import us.baocai.baocaishop.adapter.BaocaiBluetoothAdapter;
import us.baocai.baocaishop.gprinter.BaocaiPrinter;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.gprinter.aidl.GpService;
import com.gprinter.io.GpDevice;
import com.gprinter.io.PortParameters;
import com.gprinter.save.PortParamDataBase;
import com.gprinter.service.GpPrintService;

public class BaocaiPrinterConnectionActivity extends AppCompatActivity 
		implements OnClickListener {

	public final static String TAG = "BaocaiPrinterConnectionActivity";

	private Button oneKeySearch; // 一键查找
	private Button oneKeyConnect; // 一键连接
	private ListView bluetoothListView; // 蓝牙列表
	private TextView loadding; // 查找设备中....

	private TextView deviceNameOne;
	private TextView deviceAddressOne;
	private TextView deviceNameTwo;
	private TextView deviceAddressTwo;
	private Button deviceBtnOne; // 连接01
	private Button deviceBtnTwo; // 连接02

	private BluetoothAdapter baAdapter; // 蓝牙适配器
	private BaocaiBluetoothAdapter bcbAdapter; // 自定义包菜蓝牙适配器

	private PrinterServiceConnection printerConn; // 打印机Connection
	private GpService printerService; // 打印机服务
	private PortParameters printerPortParameters[] = new PortParameters[MAX_PRINTER_COUNTS]; // 只有两台打印机
	private final static int MAX_PRINTER_COUNTS = 2; // 最大是3

	private int printer01status = BaocaiPrinter.PRINTER_DISCONNECTED; // 打印机1的状态
	private int printer02status = BaocaiPrinter.PRINTER_DISCONNECTED; // 打印机2的状态

	public static final String ACTION_CONNECT_STATUS = "action.connect.status";

	private boolean isFindDevice = false; //是否点击了找设备
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_baocai_blue_connection);

		initUI();

		getDeviceAddress();

		initPrinterComponent();

		regReceiver();

		initPrinterStatus();

		
		/**
		 * 打印机状态监听
		 */
		registerBroadcast();
	}

	/**
	 * 初始化打印机连接状态
	 */
	private void initPrinterStatus() {
		// 设置初始化打印机状态
		boolean[] status = getIntent().getBooleanArrayExtra(
				LoginActivity.CONNECT_STATUS);
		for (int i = 0; i < status.length; i++) {
			if (i == 0 && status[0] == true) {
				printer01status = BaocaiPrinter.PRINTER_CONNECTED; // 打印机1的状态

			} else if (i == 1 && status[1] == true) {
				printer02status = BaocaiPrinter.PRINTER_CONNECTED; // 打印机2的状态
			}
		}
		Message msg = Message.obtain();
		msg.obj = 0;
		msg.what = printer01status;
		handler.sendMessage(msg);

		Message msg1 = Message.obtain();
		msg1.obj = 1;
		msg1.what = printer02status;
		handler.sendMessage(msg1);
	}

	/**
	 * 初始化ui监听之类
	 * 
	 */
	private void initUI() {
		baAdapter = BluetoothAdapter.getDefaultAdapter();
		oneKeySearch = (Button) findViewById(R.id.oneKeySearch);
		oneKeyConnect = (Button) findViewById(R.id.oneKeyConnect);
		deviceBtnOne = (Button) findViewById(R.id.printer_connect01);
		deviceBtnTwo = (Button) findViewById(R.id.printer_connect02);
		loadding = (TextView) findViewById(R.id.loadding);

		deviceNameOne = (TextView) findViewById(R.id.printer_name01);
		deviceAddressOne = (TextView) findViewById(R.id.printer_address01);
		deviceNameTwo = (TextView) findViewById(R.id.printer_name02);
		deviceAddressTwo = (TextView) findViewById(R.id.printer_address02);

		oneKeySearch.setOnClickListener(this);
		oneKeyConnect.setOnClickListener(this);

		deviceBtnOne.setOnClickListener(this);
		deviceBtnTwo.setOnClickListener(this);

		bluetoothListView = (ListView) findViewById(R.id.bluetoothDevicesList);
		bcbAdapter = new BaocaiBluetoothAdapter(this);
		bluetoothListView.setAdapter(bcbAdapter);

		bluetoothListView
				.setOnItemClickListener(new OnBlueDeviceItemClickListener());
	}

	/**
	 * 初始化打印机服务模块
	 */
	private void initPrinterComponent() {

//		Intent intent = new Intent("com.gprinter.aidl.GpPrintService");
		Intent intent = new Intent(this,GpPrintService.class);

		startService(intent);

		printerConn = new PrinterServiceConnection();
		bindService(intent, printerConn, Context.BIND_AUTO_CREATE);

		PortParamDataBase database = new PortParamDataBase(this);
		for (int i = 0; i < MAX_PRINTER_COUNTS; i++) {
			printerPortParameters[i] = database.queryPortParamDataBase(i + "");
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.baocai_blue_connection, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		try {

			if (printerConn != null) {
				unbindService(printerConn);
			}
			unregisterReceiver(findBlueToothReceiver);
			
			if (isFindDevice) { //如果点击了找蓝牙设备，需要溢出监听
				unregisterReceiver(printerStatusBroadcastReceiver);

			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.oneKeyConnect: // 一键连接
			openBluetooth(BaocaiPrinter.REQUEST_SEARCH_BLUETOOTH);
			handler.sendEmptyMessage(BaocaiPrinter.ONE_KEY_CONNECT);

			break;
		case R.id.oneKeySearch: // 一键查找
			openBluetooth(BaocaiPrinter.REQUEST_SEARCH_BLUETOOTH);
			handler.sendEmptyMessage(BaocaiPrinter.ONE_KEY_SEARCH);

			break;
		case R.id.printer_connect01: // 打印机01连接
			switch (printer01status) {
			case BaocaiPrinter.PRINTER_CONNECTED:
				printerClose(0);
				break;
			case BaocaiPrinter.PRINTER_CONNECTING:
				break;
			case BaocaiPrinter.PRINTER_DISCONNECTED:
				printerConnect(0);
				break;
			default:
				break;
			}
			break;
		case R.id.printer_connect02: // 打印机02连接
			switch (printer02status) {
			case BaocaiPrinter.PRINTER_CONNECTED:
				printerClose(1);
				break;
			case BaocaiPrinter.PRINTER_CONNECTING:
				break;
			case BaocaiPrinter.PRINTER_DISCONNECTED:
				printerConnect(1);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 注册广播
	 */
	private void regReceiver() {
		IntentFilter foundFilter = new IntentFilter(
				BluetoothDevice.ACTION_FOUND);
		IntentFilter finishFilter = new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(findBlueToothReceiver, foundFilter);
		registerReceiver(findBlueToothReceiver, finishFilter);

	}

	private final Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case BaocaiPrinter.ONE_KEY_CONNECT:
				printerConnect(0);
				printerConnect(1);

				break;
			case BaocaiPrinter.ONE_KEY_SEARCH:
				regReceiver();
				isFindDevice = true;
				
				bcbAdapter.getDatas().clear();
				if (baAdapter.isDiscovering()) {
					baAdapter.cancelDiscovery();
					bcbAdapter.getDatas().clear();
				}

				baAdapter.startDiscovery();
				loadding.setVisibility(View.VISIBLE);
				break;
			case BaocaiPrinter.PRINTER_CONNECTED:
				int position_connected = (Integer) msg.obj;
				if (position_connected == 0) {
					deviceBtnOne.setText("已连接");
					printer01status = BaocaiPrinter.PRINTER_CONNECTED;
				} else if (position_connected == 1) {
					deviceBtnTwo.setText("已连接");
					printer02status = BaocaiPrinter.PRINTER_CONNECTED;
				}
				break;
			case BaocaiPrinter.PRINTER_CONNECTING:
				int position_connecting = (Integer) msg.obj;
				if (position_connecting == 0) {
					deviceBtnOne.setText("连接中");
					printer01status = BaocaiPrinter.PRINTER_CONNECTING;

				} else if (position_connecting == 1) {
					deviceBtnTwo.setText("连接中");
					printer02status = BaocaiPrinter.PRINTER_CONNECTING;

				}
				break;
			case BaocaiPrinter.PRINTER_DISCONNECTED:
				int position_disconnected = (Integer) msg.obj;
				if (position_disconnected == 0) {
					deviceBtnOne.setText("未连接");
					printer01status = BaocaiPrinter.PRINTER_DISCONNECTED;

				} else if (position_disconnected == 1) {
					deviceBtnTwo.setText("未连接");
					printer02status = BaocaiPrinter.PRINTER_DISCONNECTED;

				}
				break;
			default:
				break;
			}
		};
	};

	/**
	 * 打开蓝牙
	 */
	private void openBluetooth(int requestCode) {
		if (!baAdapter.enable()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, requestCode);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int restultCode,
			Intent data) {
		super.onActivityResult(requestCode, restultCode, data);

		if (BaocaiPrinter.REQUEST_PAIR_BLUETOOTH == requestCode
				&& Activity.RESULT_OK == restultCode) { // 配对蓝牙打印机打开完毕

		} else if (BaocaiPrinter.REQUEST_SEARCH_BLUETOOTH == requestCode // 查找蓝牙打印机
				&& Activity.RESULT_OK == restultCode) {
			handler.sendEmptyMessage(BaocaiPrinter.ONE_KEY_SEARCH);
		}
	}

	private final BroadcastReceiver findBlueToothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//				HashMap<String, Boolean> isUse = new HashMap<String, Boolean>();
//				if (device.getBondState() != BluetoothDevice.BOND_BONDED) { 
//									} else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
//					if (device.getName() != null
//							&& device.getName().equals("Gprinter")) { // 佳博打印机
//						showSelectDialog(device, "找到一台蓝牙打印机，是否连接？");
//					}
//					addDevice(device);
//				}
				if (device.getName() != null
						&& device.getName().equals("Gprinter")) { // 佳博打印机
					showSelectDialog(device, "找到一台蓝牙打印机，是否连接？");
				}
				addDevice(device);


			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				loadding.setVisibility(View.INVISIBLE);
				showFinishDiscovery();
			}
		}

		/**
		 * 是否已经找到了打印机对话框
		 */
		private void showFinishDiscovery() {
			new AlertDialog.Builder(BaocaiPrinterConnectionActivity.this)
					.setTitle("找到了打印机？")
					.setMessage("多次未搜索到请重启打印机试试")
					.setNegativeButton("找到了",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {

								}
							})
					.setPositiveButton("没找到,重新搜索一遍",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									handler.sendEmptyMessage(BaocaiPrinter.ONE_KEY_SEARCH);
								}
							}).create().show();
		}

	};

	/**
	 * 添加设备到打印机
	 * 
	 * @param device
	 */
	private void addDevice(BluetoothDevice device) {
		bcbAdapter.getDatas().add(device);
		bcbAdapter.notifyDataSetChanged();
	}

	public class OnBlueDeviceItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view,
				final int position, long arg3) {

			BluetoothDevice deivice = bcbAdapter.getDatas().get(position);
			showSelectDialog(deivice, "连接上打印机");
		}

	}

	private void showSelectDialog(final BluetoothDevice device, String title) {
		new AlertDialog.Builder(BaocaiPrinterConnectionActivity.this)
				.setTitle(title)
				.setMessage("将此设备连接到哪里？\n")
				.setNegativeButton("打印机1",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {

								deviceNameOne.setText(device.getName());
								deviceAddressOne.setText(device.getAddress());
								saveDevice(device, 0);

							}
						})
				.setPositiveButton("打印机2",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								deviceNameTwo.setText(device.getName());
								deviceAddressTwo.setText(device.getAddress());
								saveDevice(device, 1);
							}
						}).create().show();
	}

	/**
	 * 保存打印机设备
	 * 
	 * @param device
	 * @param position
	 *            保存位置
	 */
	void saveDevice(BluetoothDevice device, int position) {
		// SharedPreferences spf = getPreferences(Activity.MODE_APPEND);
		// Editor edit = spf.edit();
		// edit.putString(position + "", device.getAddress());
		// edit.commit();
		printerPortParameters[position].setBluetoothAddr(device.getAddress());
		printerPortParameters[position].setPortNumber(0);
		printerPortParameters[position].setPortType(PortParameters.BLUETOOTH);
		PortParamDataBase database = new PortParamDataBase(this);
		database.deleteDataBase("" + position);
		database.insertPortParam(position, printerPortParameters[position]);
	}

	void getDeviceAddress() {
		PortParamDataBase database = new PortParamDataBase(this);
		PortParameters param01 = database.queryPortParamDataBase("" + 0);
		PortParameters param02 = database.queryPortParamDataBase("" + 1);

		// SharedPreferences spf = getPreferences(Activity.MODE_APPEND);
		// String address1 = spf.getString("0", "");
		// String address2 = spf.getString("1", "");

		deviceAddressOne.setText("地址:" + param01.getBluetoothAddr());
		deviceAddressTwo.setText("地址:" + param02.getBluetoothAddr());

	}

	// =======================打印服务=========================
	// 初始化打印机连接服务
	private class PrinterServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			printerService = GpService.Stub.asInterface(service); // 初始化完毕

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			printerService = null;
		}

	}

	/**
	 * 打印机连接
	 * 
	 * @param position
	 */
	private void printerConnect(int position) {
		PortParameters device = getPortParameters(position);
		printerConnect(device, position);
	}

	private PortParameters getPortParameters(int position) {
		PortParamDataBase database = new PortParamDataBase(this);
		return database.queryPortParamDataBase("" + position);
	}

	/**
	 * 打印机连接
	 */
	private void printerConnect(PortParameters device, int position) {
		if (printerService != null) {
			try {
				int connStatus = printerService
						.getPrinterConnectStatus(position);
				if (GpDevice.STATE_CONNECTED == connStatus) {
					Message msg = Message.obtain();
					msg.obj = position;
					msg.what = BaocaiPrinter.PRINTER_CONNECTED;
					handler.sendMessage(msg); // 连接上

				} else if (GpDevice.STATE_CONNECTING == connStatus) {
					Message msg = Message.obtain();
					msg.obj = position;
					msg.what = BaocaiPrinter.PRINTER_CONNECTING;
					handler.sendMessage(msg); // 连接中

				} else if (GpDevice.STATE_INVALID_PRINTER == connStatus) {
					Message msg = Message.obtain();
					msg.obj = position;
					msg.what = BaocaiPrinter.PRINTER_CONNECTING;
					handler.sendMessage(msg); // 不可用打印机
					printerService.openPort(position, PortParameters.BLUETOOTH,
							device.getBluetoothAddr(), 0);
				} else if (GpDevice.STATE_NONE == connStatus) {
					Message msg = Message.obtain();
					msg.obj = position;
					msg.what = BaocaiPrinter.PRINTER_CONNECTING;
					handler.sendMessage(msg); // 不可用打印机
					printerService.openPort(position, PortParameters.BLUETOOTH,
							device.getBluetoothAddr(), 0);
				}
				// int printerStatus
				// =printerService.queryPrinterStatus(position,
				// 800);//详细的连接检测可以依靠这个状态判断
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 关闭打印机
	 * 
	 */
	private void printerClose(int position) {
		try {
			printerService.closePort(position);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 打印机状态监听
	 */
	private void registerBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_CONNECT_STATUS);
		this.registerReceiver(printerStatusBroadcastReceiver, filter);
	}

	private BroadcastReceiver printerStatusBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (ACTION_CONNECT_STATUS.equals(intent.getAction())) {
				int type = intent.getIntExtra(GpPrintService.CONNECT_STATUS, 0);
				int id = intent.getIntExtra(GpPrintService.PRINTER_ID, 0);
				Log.d(TAG, "connect status " + type);
				if (type == GpDevice.STATE_CONNECTING) { // 正在连接
					Message msg = Message.obtain();
					msg.obj = id;
					msg.what = BaocaiPrinter.PRINTER_CONNECTING;
					handler.sendMessage(msg); // 连接中
				} else if (type == GpDevice.STATE_NONE) { // 连接断开
					Message msg = Message.obtain();
					msg.obj = id;
					msg.what = BaocaiPrinter.PRINTER_DISCONNECTED;
					handler.sendMessage(msg); // 
				} else if (type == GpDevice.STATE_VALID_PRINTER) { // 有效的打印机
					Message msg = Message.obtain();
					msg.obj = id;
					msg.what = BaocaiPrinter.PRINTER_CONNECTED;
					handler.sendMessage(msg); // 
				} else if (type == GpDevice.STATE_INVALID_PRINTER) { // 无效的打印机
					Message msg = Message.obtain();
					msg.obj = id;
					msg.what = BaocaiPrinter.PRINTER_DISCONNECTED;
					handler.sendMessage(msg); // 
				}
			}
		}
	};

}
