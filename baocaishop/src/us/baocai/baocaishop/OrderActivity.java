package us.baocai.baocaishop;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import us.baocai.baocaishop.adapter.ListOrderAdapter;
import us.baocai.baocaishop.bean.Employee;
import us.baocai.baocaishop.bean.Order;
import us.baocai.baocaishop.bean.OrderDetail;
import us.baocai.baocaishop.bean.Store;
import us.baocai.baocaishop.net.Api;
import us.baocai.baocaishop.push.BaocaiPushService;
import us.baocai.baocaishop.push.PushCode;
import us.baocai.baocaishop.util.BaocaiPrinterUtil;
import us.baocai.baocaishop.util.Const;
import us.baocai.baocaishop.util.StringUtil;
import us.baocai.baocaishop.util.UI;
import us.baocai.baocaishop.widget.AbandonOrderDialogFragment;
import us.baocai.baocaishop.widget.OrderDialogView;
import us.baocai.baocaishop.widget.OrderListView;
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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.gprinter.aidl.GpService;
import com.gprinter.command.GpCom;
import com.gprinter.io.GpDevice;
import com.gprinter.io.PortParameters;
import com.gprinter.save.PortParamDataBase;
import com.gprinter.service.GpPrintService;
import com.umeng.analytics.MobclickAgent;

public class OrderActivity extends AppCompatActivity {

	public static final String TAG = "OrderActivity";

	private OrderListView ordersUntreated;
	private OrderListView ordersMaking;
	private OrderListView ordersDelivery;

	private ListOrderAdapter untreatedAdapter;
	private ListOrderAdapter makingAdapter;
	private ListOrderAdapter deliveryAdapter;
	private ListOrderAdapter finishAdapter;

	private String shopid = "-1"; // 店铺id

	private List<Employee> employee;
	private boolean flag = true;
	public static boolean isPrinting = false;

	private TextView printerStatus;

	private int userablePrinterCount = 0;
	private int lastSize;

	private int unTreatedPagerNumber = 1;
	private int makingPagePageNumber = 1;
	private int devlieringPagePageNumber = 1;
	private int finishPagePageNumber = 1;

	private android.support.v7.app.AlertDialog dealOrderDialog;
	private OrderDialogView odv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order);

		SharedPreferences spf = getSharedPreferences("shop",
				Activity.MODE_PRIVATE);
		shopid = spf.getString("id", "0");

		// shopid=17+"";
		init();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 保持屏幕常亮

		// 绑定服务
		Log.e(TAG, "onCreate");
		intiPrinter();

		initEmployee();
		postDelay();

	}

	private void postDelay() {
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// thread.start();

				checkShopStatus();

				regNetworkReceiver();

				regPushServiceReceiver();

				registerBroadcast();

				// 动态注册receiver
				regRC();

				getReson();
			}
		}, 1000);
	}

	private void initEmployee() {
		String userjson = getSharedPreferences("emeployee",
				Activity.MODE_PRIVATE).getString("emeployee", "");
		try {
			employee = JSONArray.parseArray(userjson, Employee.class);
			for (Employee e : employee) {
				int maxID = -1;
				int position = 0;
				for (int i = 0; i < getEmployee().size(); i++) {
					Employee ee = getEmployee().get(i);
					if (maxID < ee.getId()) {
						position = i;
						maxID = ee.getId();
					}
				}

				for (int i = 0; i < getEmployee().size(); i++) {
					if (i == position) {
						getEmployee().get(position).setColor(Color.BLUE);
					} else {
						getEmployee().get(i).setColor(Color.RED);
					}
				}
			}
		} catch (Exception e) {
			UI.toastTop(getApplicationContext(), "未获取到员工信息,请新启动");
		}
	}

	/**
	 * 监听push服务是否存在
	 */
	private void regPushServiceReceiver() {
		pushListenerReceiver = new PushServiceStatusReceiver();
		IntentFilter filter = new IntentFilter(PushCode.ACTION_STOP);
		registerReceiver(pushListenerReceiver, filter);
	}

	class PushServiceStatusReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Intent service = new Intent(OrderActivity.this,
					BaocaiPushService.class);
			service.putExtra("shopid", shopid);
			startService(service);
		}

	}

	private void regNetworkReceiver() {
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(myNetReceiver, mFilter);
	}

	private void intiPrinter() {
		startService();
		connection();
		initPortParam();
	}

	@Override
	protected void onStop() {
		super.onStop();
		flag = false;
	}

	public void updateUseablePrinter() {
		int count = getUseablePrinter().size();
		if (count >= 2) {
			handler.sendEmptyMessage(Const.PRINTER_SUCCESS);
		} else {
			handler.sendEmptyMessage(Const.PRINTER_NO_CONNECTED);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.order, menu);

		 switchShop = (ToggleButton) menu.findItem(R.id.action_open_shop)
	                .getActionView();
	        switchShop.setTextOn("开店中");
	        switchShop.setTextOff("关店中");
	        checkShopStatus();

	        switchShop.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                if (switchShop.isChecked()){
	                    postOpenShop(shopid+"");
	                } else {
	                    postCloseShop(shopid+"");

	                }
	            }
	        });

		return true;
	}

	private boolean isAutoOption = false;

	/**
	 * 关店
	 * 
	 * @param shopeid
	 */
	protected void postCloseShop(String shopeid) {
		switchShop.setVisibility(View.INVISIBLE);
		Map<String, String> map = new HashMap<String, String>();
		map.put("storeId", shopeid);
		Api.jsonPostRequestWithErrorListener(getApplicationContext(),
				Api.HTTP_ORDER_CLOSE_SHOP, map,
				new Listener<org.json.JSONObject>() {

					@Override
					public void onResponse(org.json.JSONObject result) {
						isAutoOption = true;
						try {
							boolean responseStatus = result
									.getBoolean("success");
							if (responseStatus) {
								UI.toast(getApplicationContext(), "关店成功");
								
							} else {
								UI.toast(getApplicationContext(), "关店失败，请重试");
								checkShopStatus();

							}

						} catch (JSONException e) {
							isAutoOption = false;
							e.printStackTrace();
							UI.toast(getApplicationContext(), "关店失败，请重试");
							MobclickAgent.reportError(getApplicationContext(),
									"关店失败" + e.getMessage());

							checkShopStatus();

						} finally {
							switchShop.setVisibility(View.VISIBLE);
						}
					}

				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						UI.toast(getApplicationContext(),
								"关店失败，请重试" + error.toString());
						switchShop.setVisibility(View.VISIBLE);
						MobclickAgent.reportError(getApplicationContext(),
								"关店失败" + error.getMessage());
						checkShopStatus();

					}
				});
	}

	/**
	 * 开店
	 * 
	 * @param shopeid
	 */
	protected void postOpenShop(String shopeid) {
		switchShop.setVisibility(View.INVISIBLE);
		Map<String, String> map = new HashMap<String, String>();
		map.put("storeId", shopeid);
		Api.jsonPostRequestWithErrorListener(getApplicationContext(),
				Api.HTTP_ORDER_OPEN_SHOP, map,
				new Listener<org.json.JSONObject>() {

					@Override
					public void onResponse(org.json.JSONObject result) {
						isAutoOption = true;
						try {
							boolean responseStatus = result
									.getBoolean("success");
							if (responseStatus) {
								UI.toast(getApplicationContext(), "开店成功");
							} else {
								UI.toast(getApplicationContext(), "开店失败，请重试");
								checkShopStatus();
							}

						} catch (JSONException e) {
							isAutoOption = false;
							e.printStackTrace();
							UI.toast(getApplicationContext(), "开店失败，请重试");
							// switchShop.setChecked(false);
							MobclickAgent.reportError(getApplicationContext(),
									"开店失败" + e.getMessage());
							checkShopStatus();

						} finally {
							switchShop.setVisibility(View.VISIBLE);
						}
					}

				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						UI.toast(getApplicationContext(),
								"开店失败，请重试" + error.toString());
						switchShop.setVisibility(View.VISIBLE);
						MobclickAgent.reportError(getApplicationContext(),
								"开店失败" + error.getMessage());
						checkShopStatus();


					}
				});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_refresh_order) {
			getOrders();
			return true;
		}
		if (id == R.id.action_test_printer) {
			printerTest();// 测试打印
			return true;
		}
		if (id == R.id.action_total_cash) {
			getTotalCash();
			return true;
		}

		if (id == R.id.action_turn_finish) {
			Intent intent = new Intent(this, FinishOrderActivity.class);
			startActivity(intent);
			return true;
		}

	
		return super.onOptionsItemSelected(item);
	}

	private void getTotalCash() {
		jsonGetRequest(Api.HTTP_TODAY_TOTAL_CASH + "?shop_id=" + shopid,
				new Listener<String>() {

					@Override
					public void onResponse(String result) {
						JSONObject jsonObject = JSON.parseObject(result);
						if (jsonObject.get("success") != null
								&& jsonObject.getBoolean("success")) {
							AlertDialog.Builder builder = new Builder(
									OrderActivity.this);
							builder.setTitle("当日所收现金");
							builder.setMessage("总金额:"
									+ jsonObject.getString("data")
									+ "元\n以上金额仅供参考");
							builder.setNegativeButton("确定", null);
							builder.show();
						} else {
							AlertDialog.Builder builder = new Builder(
									OrderActivity.this);
							builder.setTitle("当日所收现金");
							builder.setMessage("汇总失败：错误" + result);
							builder.setNegativeButton("确定", null);
							builder.show();
						}
					}
				});

	}

	/**
	 * 打印机测试
	 */
	private void printerTest() {

		Order order = new Order();
		order.setContact_name("微笑");
		// order.setStatus(i % 3);
		order.setGmt_create(new Date());
		order.setContact_address("梦想的终点站");
		order.setPay_fee(99.0f);
		order.setContact_phone("5201314");
		order.setBak("生活就是面对真实的微笑，就是越过障碍注视将来。");
		order.setOrder_no("M6fwDdYx6QGZqE1055i4m7bW ");

		List<OrderDetail> ods = new ArrayList<OrderDetail>();

		for (int i = 0; i < 1; i++) {
			OrderDetail od = new OrderDetail();
			od.setItem_name("微笑");
			od.setItem_number(1);
			od.setItem_price(99f);
			// od.setSl("永远");
			ods.add(od);

		}

		order.setDetails(ods);
		// order.setPayway("wx");
		order.setPay_way(1);
		// printOrderPage(order);

		for (int i = 0; i < order.getDetails().size(); i++) {
			OrderDetail od = order.getDetails().get(i);
			// BaocaiPrinterUtil.printLable(mGpService,
			// getUseableLablePrinter(),
			// getApplicationContext(), od);
			printerQrLable(mGpService, getUseableLablePrinter(),
					getApplicationContext(), od, order.getOrder_no());

		}
		printerQrNote(mGpService, getUseableLablePrinter(),
				getApplicationContext(), order);

		// BaocaiPrinterUtil.printOrder(mGpService, getUseableBillPrinter(),
		// getApplicationContext(), order);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		try {
			if (player != null) {
				player.release();
				player = null;
			}

			if (orderReceiver != null) {
				unregisterReceiver(orderReceiver);
			}
			if (conn != null) {
				unbindService(conn);
			}
			if (myNetReceiver != null) {
				unregisterReceiver(myNetReceiver);
			}
			if (printerStatusBroadcastReceiver != null) {
				unregisterReceiver(printerStatusBroadcastReceiver);
			}
			if (pushListenerReceiver != null) {
				unregisterReceiver(pushListenerReceiver);
			}

			Intent intent = new Intent(this, BaocaiPushService.class);
			stopService(intent);

		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
		printerClose();

	}

	/**
	 * 关闭打印机
	 * 
	 */
	private void printerClose() {
		try {
			mGpService.closePort(0);
			mGpService.closePort(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 注册透传消息
	 */
	private void regRC() {
		orderReceiver = new OrderListenerReceiver();
		IntentFilter filter = new IntentFilter();
		// filter.addAction("com.igexin.sdk.action.f4EGuNnqeeAoBoziJYYVk7");
		filter.addAction(PushCode.ACTION);
		registerReceiver(orderReceiver, filter);
	}

	private void init() {

		ordersUntreated = (OrderListView) findViewById(R.id.order_untreated);
		ordersMaking = (OrderListView) findViewById(R.id.order_making);
		ordersDelivery = (OrderListView) findViewById(R.id.order_delivery);
		printerStatus = (TextView) findViewById(R.id.order_printer_status);
		headFinish = (TextView) findViewById(R.id.order_head_finish);
		ordersFinish = (OrderListView) findViewById(R.id.order_finish);

		untreatedAdapter = new ListOrderAdapter(this);
		makingAdapter = new ListOrderAdapter(this);
		deliveryAdapter = new ListOrderAdapter(this);
		finishAdapter = new ListOrderAdapter(this);

		ordersUntreated.setAdapter(untreatedAdapter);
		ordersMaking.setAdapter(makingAdapter);
		ordersDelivery.setAdapter(deliveryAdapter);
		ordersFinish.setAdapter(finishAdapter);

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// 获取订单
				getOrders();
			}
		}, 1500); // 1.5s后查询数据

		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {
				SwipeMenuItem openItem = new SwipeMenuItem(
						getApplicationContext());
				// set item background
				openItem.setBackground(new ColorDrawable(Color.RED));
				openItem.setWidth(UI.dip2px(getApplicationContext(), 100));
				openItem.setTitle("作废");
				openItem.setTitleSize(18);
				openItem.setTitleColor(Color.WHITE);
				// add to menu
				menu.addMenuItem(openItem);

			}
		};

		ordersUntreated.setMenuCreator(creator);

		ordersUntreated
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(final int position,
							SwipeMenu menu, int index) {
						switch (index) {
						case 0:
							abandonOnAccept(position);
							break;
						}
						return false;
					}

				});

		ordersUntreated.setOnItemClickListener(new OnItemClickListener() { // 未处理

					@Override
					public void onItemClick(AdapterView<?> arg0, View view,
							final int position, long arg3) {
						final Order o = untreatedAdapter.getDatas().get(
								position);
						if (dealOrderDialog == null) {
							odv = new OrderDialogView(OrderActivity.this);

							android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(
									OrderActivity.this);
							builder.setTitle("未处理");
							builder.setView(odv);
							dealOrderDialog = builder.create();

							odv.setOnCancleListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									dealOrderDialog.dismiss();
								}
							});

						}
						dealOrderDialog.setTitle("未处理");
						odv.setOrder(o);
						odv.setPeople(getEmployee());
						odv.setOnPeopleClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								handlerOrder(o, getEmployee().get(position)
										.getId());
//								UI.toast(getApplicationContext(), "订单受理，制作人"
//										+ getEmployee().get(position).getName());
								dealOrderDialog.dismiss();

							}
						});
						odv.setOnOrderPrinterListener(new OnClickListener() { // 订单补打

							@Override
							public void onClick(View view) {
								for (int i = 0; i < o.getDetails().size(); i++) {
									OrderDetail od = o.getDetails().get(i);
									for (int j = 0; j < od.getItem_number(); j++) {
										printerQrLable(mGpService,
												getUseableLablePrinter(),
												getApplicationContext(), od,
												o.getOrder_no());
									}
								}
								
								BaocaiPrinterUtil.printOrder(mGpService,
										getUseableBillPrinter(),
										getApplicationContext(), o);
								showIsPrintSuccess();
							}
						});

						if (!dealOrderDialog.isShowing()) {
							dealOrderDialog.show();
						}
					}
				});

		ordersMaking.setOnItemClickListener(new OnItemClickListener() { // 制作

					@Override
					public void onItemClick(AdapterView<?> arg0, View view,
							final int position, long arg3) {
						final Order o = makingAdapter.getDatas().get(position);

						if (dealOrderDialog == null) {
							odv = new OrderDialogView(OrderActivity.this);

							android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(
									OrderActivity.this);
							builder.setView(odv);
							dealOrderDialog = builder.create();

							odv.setOnCancleListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									dealOrderDialog.dismiss();
								}
							});

						}
						dealOrderDialog.setTitle("制作中");
						odv.setOrder(o);
						odv.setPeople(getEmployee());
						odv.setOnPeopleClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								handleDelivery(o, getEmployee().get(position)
										.getId());
//								UI.toast(getApplicationContext(), "制作完成，配送人"
//										+ getEmployee().get(position).getName());
								dealOrderDialog.dismiss();

							}
						});
						odv.setOnOrderPrinterListener(new OnClickListener() { // 订单补打

							@Override
							public void onClick(View view) {
								for (int i = 0; i < o.getDetails().size(); i++) {
									OrderDetail od = o.getDetails().get(i);
									for (int j = 0; j < od.getItem_number(); j++) {

										printerQrLable(mGpService,
												getUseableLablePrinter(),
												getApplicationContext(), od,
												o.getOrder_no());
									}
								}
								BaocaiPrinterUtil.printOrder(mGpService,
										getUseableBillPrinter(),
										getApplicationContext(), o);

								showIsPrintSuccess();
							}
						});

						if (!dealOrderDialog.isShowing()) {
							dealOrderDialog.show();
						}

					}
				});

		ordersDelivery.setOnItemClickListener(new OnItemClickListener() { // 制作

					@Override
					public void onItemClick(AdapterView<?> arg0, View view,
							int position, long arg3) {
						handleOrderFinish(deliveryAdapter.getDatas().get(
								position));
					}
				});

		ordersFinish.setOnItemClickListener(new OnItemClickListener() { // 制作

					@Override
					public void onItemClick(AdapterView<?> arg0, View view,
							final int position, long arg3) {
						final Order o = finishAdapter.getDatas().get(position);

						if (dealOrderDialog == null) {
							odv = new OrderDialogView(OrderActivity.this);

							android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(
									OrderActivity.this);
							builder.setView(odv);
							dealOrderDialog = builder.create();

							odv.setOnCancleListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									dealOrderDialog.dismiss();
								}
							});

						}
						dealOrderDialog.setTitle("问题订单");
						odv.setOrder(o);

						if (!dealOrderDialog.isShowing()) {
							dealOrderDialog.show();
						}

					}
				});

	}

	/**
	 * 接受订单时废弃
	 * 
	 * @param order
	 */
	private void handleAcceptAbandon(Order order) {
		untreatedAdapter.getDatas().remove(order);
		order.setStatus(5);
		untreatedAdapter.notifyDataSetChanged();

		finishAdapter.getDatas().add(0, order);

		finishAdapter.notifyDataSetChanged();
		untreatedAdapter.notifyDataSetChanged();
		headFinish.setText("问题订单" + "(" + finishAdapter.getCount() + ")");

	}

	/**
	 * 打印是否成功
	 */
	private void showIsPrintSuccess() {
		new AlertDialog.Builder(OrderActivity.this)
				.setTitle("打印是否成功")
				.setMessage(Const.PRINTER_ERROR)
				.setNegativeButton("打印成功", null)
				.setPositiveButton("失败重连",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO
								openPrinter();

							}
						}).create().show();
	}

	private void getOrders(final int handlerWhat) {
		UI.showLoading(this);
		SharedPreferences spf = getSharedPreferences("shop",
				Activity.MODE_APPEND);
		jsonGetRequest(Api.HTTP_GET_ORDER_TODAY + shopid,
				new Listener<String>() {

					@Override
					public void onResponse(String result) {

						try {
							List<Order> orders = JSON.parseArray(result,
									Order.class);
							Map<Integer, List<Order>> results = new HashMap<Integer, List<Order>>();
							results.put(1, new ArrayList<Order>());
							results.put(2, new ArrayList<Order>());
							results.put(3, new ArrayList<Order>());
							results.put(4, new ArrayList<Order>());

							for (Order o : orders) {
								if (1 == o.getStatus()) { // 加入订单
									results.get(1).add(o);
								} else if (2 == o.getStatus()) { // 制作中
									results.get(2).add(o);
								} else if (3 == o.getStatus()) { // 配送中
									results.get(3).add(o);
								} else if (4 == o.getStatus()) { // 完成
									results.get(4).add(o);
								} else if (5 == o.getStatus()) { // 推送客服也算是完成
									results.get(4).add(o);
								}

							}

							untreatedAdapter.getDatas().clear();
							makingAdapter.getDatas().clear();
							deliveryAdapter.getDatas().clear();
							finishAdapter.getDatas().clear();

							headFinish.setText("完成" + "("
									+ finishAdapter.getCount() + ")");
							untreatedAdapter.getDatas().addAll(results.get(1));
							makingAdapter.getDatas().addAll(results.get(2));
							deliveryAdapter.getDatas().addAll(results.get(3));
							finishAdapter.getDatas().addAll(results.get(4));
							finishAdapter.notifyDataSetChanged();

							untreatedAdapter.notifyDataSetChanged();
							makingAdapter.notifyDataSetChanged();
							deliveryAdapter.notifyDataSetChanged();

							handler.sendEmptyMessage(handlerWhat);
						} catch (Exception e) {

							try {
								JSONObject jobject = JSON.parseObject(result);
								UI.toast(getApplicationContext(),
										"" + jobject.get("errmsg"));
							} catch (Exception e2) {
								UI.toast(getApplicationContext(), "服务器出错");
							}
						} finally {
							UI.dismissLoading(OrderActivity.this);
						}

					}
				});
	}

	/**
	 * 获取订单
	 */
	private void getOrders() {
		// getOrders(0);
		getUntreatedOrders(true);
		getMakingOrders(true);
		getDeliveryOrders(true);
		getFinishOrders(true);

	}

	void jsonGetRequest(String url, Listener<String> result) {
		Api.jsonGetRequest(this, url, result);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	/**
	 * 受理订单，添加制作，成功则移除未处理状态
	 * 
	 * @param position
	 * @param order
	 */
	private void handlerOrder(final Order order, final int employee) {

		UI.toast(getApplicationContext(), "请在手机上操作该订单");

		// Map<String, String> map = new HashMap<String, String>();
		// map.put("orderNo", order.getOrder_no());
		// map.put("uid", "" + employee);
		// jsonPostRequest(Api.HTTP_UPDATE_ORDER_TO_MAKING, map,
		// new Listener<org.json.JSONObject>() {
		//
		// @Override
		// public void onResponse(org.json.JSONObject result) {
		// JSONObject arg0 = JSON.parseObject(result.toString());
		// try {
		// if (arg0.get("success") != null
		// && arg0.getBoolean("success")) {
		//
		// untreatedAdapter.getDatas().remove(order);
		// untreatedAdapter.notifyDataSetChanged();
		//
		// order.setStatus(2); // 切换到制作状态
		// order.setOrderColor(getEmployeeColor(employee));
		//
		// makingAdapter.getDatas().add(order);
		// makingAdapter.notifyDataSetChanged();
		//
		// playVoice();
		// for (int i = 0; i < order.getDetails().size(); i++) {
		// OrderDetail od = order.getDetails().get(i);
		// for (int j = 0; j < od.getItem_number(); j++) {
		// // BaocaiPrinterUtil.printLable(mGpService,
		// // getUseableLablePrinter(),
		// // getApplicationContext(), od);
		// printerQrLable(mGpService,
		// getUseableLablePrinter(),
		// getApplicationContext(), od,
		// order.getOrder_no());
		// }
		// }
		// BaocaiPrinterUtil.printOrder(mGpService,
		// getUseableBillPrinter(),
		// getApplicationContext(), order);
		// } else {
		// if(arg0.get("errcode")!=null&&"40001".equals((arg0.get("errcode")))){
		// AlertDialog.Builder builder = new
		// AlertDialog.Builder(OrderActivity.this);
		// builder.setTitle("错误").setMessage("你当前的订单已被接单制作,是否清除？").setNegativeButton("取消",
		// null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// untreatedAdapter.getDatas().remove(order);
		// untreatedAdapter.notifyDataSetChanged();
		// }
		// }).show();
		// }else {
		// UI.toast(getApplicationContext(), "accept error:"+arg0.toString());
		// MobclickAgent.reportError(getApplicationContext(),
		// "handlerOrder error:" + arg0.toString());
		// }
		//
		// }
		//
		//
		// } catch (Exception e1) {
		// MobclickAgent.reportError(getApplicationContext(),
		// "handlerOrder error:" + e1.getMessage());
		// e1.printStackTrace();
		// UI.toast(getApplicationContext(), "error:"+e1.getMessage());
		//
		// }
		//
		// }
		// });
	}

	public int getEmployeeColor(int employee) {

		int maxID = -1;
		for (int i = 0; i < getEmployee().size(); i++) {
			Employee e = getEmployee().get(i);
			if (maxID < e.getId()) {
				maxID = e.getId();
			}
		}
		if (maxID > employee) {
			return Color.RED;
		}
		if (maxID == employee) {
			return Color.BLUE;

		}

		return Color.BLACK;

	}

	/**
	 * jsonPost请求
	 * 
	 * @param url
	 * @param params
	 * @param listener
	 */
	private void jsonPostRequest(String url, Map<String, String> params,
			Response.Listener<org.json.JSONObject> listener) {
		Api.jsonPostRequest(this, url, params, listener);

	}

	/**
	 * 请求配送，成功则移除制作状态
	 * 
	 * @param order
	 */
	private void handleDelivery(final Order order, final int employee) {

		UI.toast(getApplicationContext(), "请在手机上操作该订单");
		// Map<String, String> map = new HashMap<String, String>();
		// map.put("orderNo", order.getOrder_no());
		// map.put("uid", employee + "");
		// jsonPostRequest(Api.HTTP_UPDATE_ORDER_TO_DELIVERING, map,
		// new Listener<org.json.JSONObject>() {
		//
		// @Override
		// public void onResponse(org.json.JSONObject result) {
		// JSONObject arg0 = JSON.parseObject(result.toString());
		// try {
		// if (arg0.get("success") != null
		// && arg0.getBoolean("success")) {
		//
		// playVoice();
		// makingAdapter.getDatas().remove(order);
		// makingAdapter.notifyDataSetChanged();
		//
		// order.setStatus(3); // 切换到配送状态
		//
		// order.setOrderColor(getEmployeeColor(employee));
		// deliveryAdapter.getDatas().add(order);
		// deliveryAdapter.notifyDataSetChanged();
		// } else {
		// if(arg0.get("errcode")!=null&&"40001".equals((arg0.get("errcode")))){
		// AlertDialog.Builder builder = new
		// AlertDialog.Builder(OrderActivity.this);
		// builder.setTitle("错误").setMessage("你当前的订单已被配送,是否清除？").setNegativeButton("取消",
		// null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// makingAdapter.getDatas().remove(order);
		// makingAdapter.notifyDataSetChanged();
		// }
		// }).show();
		// }else {
		// UI.toast(getApplicationContext(), "accept error:"+arg0.toString());
		// MobclickAgent.reportError(getApplicationContext(),
		// "handlerOrder error:" + arg0.toString());
		// }
		// }
		// } catch (Exception e) {
		// MobclickAgent.reportError(getApplicationContext(),
		// "handleDelivery error:" + e.getMessage());
		// }
		// }
		// });
	}

	/**
	 * 订单配送完成，移除配送状态，加入到订单完成，消失透传使用
	 * 
	 * @param order
	 */
	private void handleOrderFinish(final Order order) {
		//
		// Map<String, String> map = new HashMap<String, String>();
		// map.put("orderNo", order.getOrder_no());
		// map.put("uid", "0");
		// jsonPostRequest(Api.HTTP_UPDATE_ORDER_TO_FINISH, map,
		// new Listener<org.json.JSONObject>() {
		//
		// @Override
		// public void onResponse(org.json.JSONObject result) {
		// JSONObject arg0 = JSON.parseObject(result.toString());
		// Log.d(TAG, "response -> " + arg0.toString());
		//
		// try {
		// if (arg0.get("success") != null
		// && arg0.getBoolean("success")) {
		// playVoice();
		// deliveryAdapter.getDatas().remove(order);
		// deliveryAdapter.notifyDataSetChanged();
		// order.setStatus(4);
		// } else {
		// if(arg0.get("errcode")!=null&&"40001".equals((arg0.get("errcode")))){
		// AlertDialog.Builder builder = new
		// AlertDialog.Builder(OrderActivity.this);
		// builder.setTitle("错误").setMessage("你当前的订单已配送完毕,是否清除？").setNegativeButton("取消",
		// null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// deliveryAdapter.getDatas().remove(order);
		// deliveryAdapter.notifyDataSetChanged();
		// }
		// }).show();
		// }else {
		// UI.toast(getApplicationContext(), "accept error:"+arg0.toString());
		// MobclickAgent.reportError(getApplicationContext(),
		// "handlerOrder error:" + arg0.toString());
		// }
		//
		// }
		//
		// } catch (Exception e) {
		// MobclickAgent
		// .reportError(
		// getApplicationContext(),
		// "handleOrderFinish error:"
		// + e.getMessage());
		// }
		//
		// }
		// });

		UI.toast(getApplicationContext(), "请在手机上操作该订单");

	}

	/**
	 * 获取工作人员
	 * 
	 * @return
	 */
	public List<Employee> getEmployee() {
		if (employee == null) {
			employee = new ArrayList<Employee>();
		}
		return employee;
	}

	/**
	 * 消息透传，个推推送过来订单信息
	 * 
	 * @author studyjun
	 * 
	 */
	class OrderListenerReceiver extends BroadcastReceiver {

		private AlertDialog lostHostDialog;

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			Log.d(TAG, "onReceive() action=" + bundle.getInt("action"));

			// switch (bundle.getInt(PushConsts.CMD_ACTION)) {

			switch (bundle.getInt("code")) {
			case PushCode.PUSH_RECONNECTED:
				UI.toast(getApplicationContext(), "服务器已经重连");
				lastSize = 0;
				lastSize += untreatedAdapter.getDatas().size();
				lastSize += makingAdapter.getDatas().size();
				lastSize += deliveryAdapter.getDatas().size();

				getOrders(PushCode.PUSH_RECONNECTED);

				break;
			case PushCode.PUSH_STOP:
				showLossHostDialog();
				break;
			case PushCode.PUSH_ORDER_REMOVE:
				String dataRemove = intent.getStringExtra("result");
				JSONObject jsonRemove = JSONObject.parseObject(dataRemove);
				try {
					String orderNo = jsonRemove.getString("orderNo");
					Order order = new Order();
					order.setOrder_no(orderNo);
					int status = intent.getIntExtra("status", 0);
					switch (status) {
					case 2:
						O: for (Order o : untreatedAdapter.getDatas()) {
							if (o.getOrder_no().equals(orderNo)) {
								untreatedAdapter.getDatas().remove(o);
								untreatedAdapter.notifyDataSetChanged();

								if (order == null) {
                                    MobclickAgent.reportError(OrderActivity.this, "订单 为null");
                                }
								  BaocaiPrinterUtil.printOrder(mGpService,
											getUseableBillPrinter(),
											getApplicationContext(), o);
								
								if (!makingAdapter.getDatas().contains(o)) {
									makingAdapter.getDatas().add(o);
									makingAdapter.notifyDataSetChanged();
//									UI.toast(getApplicationContext(), "你的订单"
//											+ o.getSerno() + "移交到制作中");
								}
								
								  

								break O;
							}
						}

						break;
					case 3:
						O: for (Order o : makingAdapter.getDatas()) {
							if (o.getOrder_no().equals(orderNo)) {
								makingAdapter.getDatas().remove(o);
								makingAdapter.notifyDataSetChanged();

								if (!deliveryAdapter.getDatas().contains(o)) {
									deliveryAdapter.getDatas().add(o);
									deliveryAdapter.notifyDataSetChanged();
//									UI.toast(getApplicationContext(), "你的订单"
//											+ o.getSerno() + "移交到配送中");
								}

								break O;
							}
						}

						break;
					case 4:
						if (deliveryAdapter.getDatas().contains(order)) {
							deliveryAdapter.getDatas().remove(order);
							deliveryAdapter.notifyDataSetChanged();
//							UI.toast(getApplicationContext(), "你有一笔订单配送完毕");
						}

						break;
                    case 7:
                        order.setOrder_status(jsonRemove.getIntValue("order_status"));
                        switch (order.getOrder_status()){
                            case 1:
//                            	if (untreatedAdapter.getDatas().contains(order)) {
//                            		untreatedAdapter.getDatas().remove(order);
//                            		untreatedAdapter.notifyDataSetChanged();
//        							}
                            	 Of:
                                     for (Order o : untreatedAdapter.getDatas()) {
                                         if (o.getOrder_no().equals(orderNo)) {
                                        	 untreatedAdapter.getDatas().remove(o);
                                             if(!finishAdapter.getDatas().contains(o)){
                                            	 finishAdapter.getDatas().add(o);
                                            	 finishAdapter.notifyDataSetChanged();
                                             }
                                             untreatedAdapter.notifyDataSetChanged();
                                             break Of;
                                         }
                                     }
                            	
//                                UI.toast(getApplicationContext(), "你有一笔订单被取消");
                                break;
                            case 2:
//                            	if (makingAdapter.getDatas().contains(order)) {
//                            		makingAdapter.getDatas().remove(order);
//                            		makingAdapter.notifyDataSetChanged();
//        							}
                            	 Of:
                                     for (Order o : makingAdapter.getDatas()) {
                                         if (o.getOrder_no().equals(orderNo)) {
                                        	 makingAdapter.getDatas().remove(o);
                                             if(!finishAdapter.getDatas().contains(o)){
                                            	 finishAdapter.getDatas().add(o);
                                            	 finishAdapter.notifyDataSetChanged();
                                             }
                                             makingAdapter.notifyDataSetChanged();
                                             break Of;
                                         }
                                     }
                            	
//                                UI.toast(getApplicationContext(), "你有一笔订单被取消");
                                break;
                            case 3:
//                            	if (deliveryAdapter.getDatas().contains(order)) {
//                            		deliveryAdapter.getDatas().remove(order);
//                            		deliveryAdapter.notifyDataSetChanged();
//        							}
                            	 Of:
                                     for (Order o : finishAdapter.getDatas()) {
                                         if (o.getOrder_no().equals(orderNo)) {
                                        	 finishAdapter.getDatas().remove(o);
                                             if(!finishAdapter.getDatas().contains(o)){
                                            	 finishAdapter.getDatas().add(o);
                                            	 finishAdapter.notifyDataSetChanged();
                                             }
                                             finishAdapter.notifyDataSetChanged();
                                             break Of;
                                         }
                                     }
                            	
//                                UI.toast(getApplicationContext(), "你有一笔订单被取消");
                                break;
                        }

                        break;
					}
				} catch (Exception e) {
					MobclickAgent.reportError(getApplicationContext(), "推送解析失败"
							+ e.getMessage());
				}
				break;
			case PushCode.PUSH_ORDER_SUCCESS:
				// 获取透传数据

				String data = intent.getStringExtra("result");
				int type = intent.getIntExtra("type", 0);
				String dataAdd = intent.getStringExtra("result");
				JSONObject jsonAdd = JSONObject.parseObject(dataAdd);

				if (type == PushCode.ORDER_MOVE_TOP) {
					Order order = JSON.parseObject(jsonAdd.getString("order"),
							Order.class);
					if (!untreatedAdapter.getDatas().contains(order)) {
						untreatedAdapter.getDatas().add(0, order);
					} else {
						untreatedAdapter.getDatas().remove(order);
						untreatedAdapter.getDatas().add(0, order);
					}
//					UI.toastTop(
//							getApplicationContext(),
//							"紧急订单:" + order.getSerno() + "号-"
//									+ order.getItem_name());

				}

				Log.d(TAG, "Got Payload:" + data);
				try {
					// String dataAdd = intent.getStringExtra("result");
					Order order = JSON.parseObject(jsonAdd.getString("order"),
							Order.class);

					// Order order = JSON.parseObject(data, Order.class);

					if (1 == order.getStatus()) { // 加入订单
						if (!untreatedAdapter.getDatas().contains(order)) {
							untreatedAdapter.getDatas().add(order);
							untreatedAdapter.notifyDataSetChanged();
						}
//						if (type == PushCode.ORDER_RESUME_CANCEL) {
//							UI.toastTop(getApplicationContext(),
//									"你有一笔订单恢复了" + order.getSerno() + "号-"
//											+ order.getItem_name());
//						} else {
//							UI.toastTop(getApplicationContext(),
//									"你有一笔新订单:" + order.getSerno() + "号-"
//											+ order.getItem_name());
//						}
//						playAddOrder();
					} else if (2 == order.getStatus()) {
						if (untreatedAdapter.getDatas().contains(order)) {
							untreatedAdapter.getDatas().remove(order);
							untreatedAdapter.notifyDataSetChanged();
						}
						if (!makingAdapter.getDatas().contains(order)) {
							makingAdapter.getDatas().add(order);
							makingAdapter.notifyDataSetChanged();
						}
						
						BaocaiPrinterUtil.printOrder(mGpService,
								getUseableBillPrinter(),
								getApplicationContext(), order);
						
//						if (type == PushCode.ORDER_RESUME_CANCEL) {
//							UI.toastTop(getApplicationContext(),
//									"你有一笔订单恢复了" + order.getSerno() + "号-"
//											+ order.getItem_name());
//						} else {
//							UI.toastTop(getApplicationContext(),
//									"你有一笔订单在制作中:" + order.getSerno() + "号-"
//											+ order.getItem_name());
//						}
					} else if (3 == order.getStatus()) {

						if (makingAdapter.getDatas().contains(order)) {
							makingAdapter.getDatas().remove(order);
							makingAdapter.notifyDataSetChanged();
						}
						if (!deliveryAdapter.getDatas().contains(order)) {
							deliveryAdapter.getDatas().add(order);
							deliveryAdapter.notifyDataSetChanged();

						}
//						if (type == PushCode.ORDER_RESUME_CANCEL) {
//							UI.toastTop(getApplicationContext(),
//									"你有一笔订单恢复了" + order.getSerno() + "号-"
//											+ order.getItem_name());
//						} else {
//							UI.toastTop(getApplicationContext(),
//									"你有一笔订单移交配送:" + order.getSerno() + "号-"
//											+ order.getItem_name());
//						}
					} else if (4 == order.getStatus()) { // 配送完成，移除配送
						if (deliveryAdapter.getDatas().contains(order)) {
							deliveryAdapter.getDatas().remove(order);
							deliveryAdapter.notifyDataSetChanged();
//							Toast.makeText(context,
//									"你有一个订单派送完毕：" + order.getContact_name(),
//									Toast.LENGTH_LONG).show();
						}

					}

				} catch (Exception e) {
					Toast.makeText(context, "error:数据解析失败", Toast.LENGTH_LONG)
							.show();
				}
				break;

			}

		}

		/**
		 * 丢失主机对话框
		 */
		private void showLossHostDialog() {
			if (lostHostDialog == null) {
				lostHostDialog = new AlertDialog.Builder(OrderActivity.this)
						.setTitle("与服务器断开连接")
						.setMessage("请重新刷新信息")
						.setNegativeButton("取消", null)
						.setPositiveButton("确认",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										getOrders();
									}
								}).create();
			}

			if (!lostHostDialog.isShowing()) {
				lostHostDialog.show();
			}
		}

	}

	// ============================打印服务=================================
	/**
	 * 启动佳博蓝牙服务
	 */
	private void startService() {
		Intent i = new Intent(this, GpPrintService.class);
		startService(i);
	}

	private GpService mGpService = null;
	private PrinterServiceConnection conn = null;

	private boolean firstPrint = true;

	class PrinterServiceConnection implements ServiceConnection {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i("ServiceConnection", "onServiceDisconnected() called");
			mGpService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mGpService = GpService.Stub.asInterface(service);

			if (firstPrint) {
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						printerTest();
					}
				}, 2000);
				firstPrint = false;
			}

		}
	};

	private void connection() {
		conn = new PrinterServiceConnection();
		Intent intent = new Intent(this, GpPrintService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE); // bindService
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

				if (mGpService.getPrinterConnectStatus(i) == GpDevice.STATE_CONNECTED
						&& GpCom.TSC_COMMAND == mGpService
								.getPrinterCommandType(i)) {

					// int status = mGpService.queryPrinterStatus(i, 1000);
					// if (status == 0) {
					return i;
					// }
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

				if (mGpService.getPrinterConnectStatus(i) == GpDevice.STATE_CONNECTED
						&& GpCom.ESC_COMMAND == mGpService
								.getPrinterCommandType(i)) {

					// int status = mGpService.queryPrinterStatus(i, 800);
					// if (status == 0) {
					return i;
					// }

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 获取打印机可用列表
	 * 
	 * @return
	 */
	public List<Integer> getUseablePrinter() {
		List<Integer> uselist = new ArrayList<Integer>();
		for (int i = 0; i < GpPrintService.MAX_PRINTER_CNT; i++) {

			try {
				if (mGpService.getPrinterConnectStatus(i) == GpDevice.STATE_CONNECTED) {
					uselist.add(i);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return uselist;
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Const.EXIT_ACTIVITY:
				isExit = false;
				break;
			case Const.PRINTER_CONNECTED:
				printerStatus.setText("打印机重连中...");
				printerStatus.setBackgroundColor(Color.argb(255, 204, 00, 33));
				// openPrinter();
				break;
			case Const.COUNT_SECOND:
				printerStatus.setText("失去打印机," + msg.obj
						+ "秒后重连打印机,多次失败可以重启打印机试试");
				printerStatus.setBackgroundColor(Color.argb(255, 204, 00, 33));
				break;
			case Const.PRINTER_NO_CONNECTED:
				printerStatus.setText("打印机连接失败");
				printerStatus.setBackgroundColor(Color.argb(255, 204, 00, 33));
			case Const.PRINTER_SUCCESS:
				// printerStatus.setText("打印机连接成功");
				// printerStatus.setBackgroundColor(Color.argb(255, 00, 99,
				// 00));
				// handler.sendEmptyMessageDelayed(Const.HIDDEN_PRINTER_STATUS,
				// 1000);
				printerStatus.setText("");
				printerStatus.setBackgroundColor(Color.argb(00, 00, 99, 00));
				break;
			case Const.HIDDEN_PRINTER_STATUS:
				// printerStatus.setText("");
				// printerStatus.setBackgroundColor(Color.argb(00, 00, 99, 00));
				break;
			case PushCode.PUSH_RECONNECTED: // 重新连接上
				int nowSize = 0;
				nowSize += untreatedAdapter.getDatas().size();
				nowSize += makingAdapter.getDatas().size();
				nowSize += deliveryAdapter.getDatas().size();
				if (nowSize != lastSize) { // 当旧的订单数据和新的订单数据不一样，提醒一声
					playVoice();
				}
				break;
			default:
				break;
			}
		};
	};

	// private Timer timer;

	private boolean isExit;

	private MediaPlayer player;

	private OrderListenerReceiver orderReceiver;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 退出
	 */
	private void exit() {
		if (!isExit) {
			isExit = true;
			Toast.makeText(getApplicationContext(), "再按一次退出程序",
					Toast.LENGTH_SHORT).show();
			// 利用handler延迟发送更改状态信息
			handler.sendEmptyMessageDelayed(400, 2000);
		} else {
			finish();
			System.exit(0);
		}
	}

	/**
	 * 播放系统声音
	 */
	private void playVoice() {
		Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
				notification);
		r.play();
	}

//	/**
//	 * 播放MP3声音
//	 */
//	private void playAddOrder() {
//
//		player = MediaPlayer.create(getApplicationContext(), R.raw.ring);
//		player.start();
//
//	}

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

	private PortParameters mPortParam[] = new PortParameters[GpPrintService.MAX_PRINTER_CNT]; // 端口

	private ToggleButton switchShop;

	private void initPortParam() {
		boolean[] state = new boolean[GpPrintService.MAX_PRINTER_CNT];
		for (int i = 0; i < GpPrintService.MAX_PRINTER_CNT; i++) {
			PortParamDataBase database = new PortParamDataBase(this);
			mPortParam[i] = new PortParameters();
			mPortParam[i] = database.queryPortParamDataBase("" + i);
			mPortParam[i].setPortOpenState(state[i]);
		}
	}

	private boolean isCheckShopStatus = false;

	/**
	 * 获取门店
	 */
	public void checkShopStatus() {
		Api.jsonGetRequest(getApplicationContext(), Api.HTTP_GET_STORES,
				new Listener<String>() {

					@Override
					public void onResponse(String result) {
						Log.i("HTTP_GET_STORES", "获取店铺成功");
						try {
							List<Store> stores = JSONArray.parseArray(result,
									Store.class);
							for (Store store : stores) {
								if (String.valueOf(store.getId())
										.equals(shopid)) { // 找到对应店铺
									if (store.getStatus().equals("0")) {
										isCheckShopStatus = false;
										switchShop.setChecked(false);
									} else if (store.getStatus().equals("1")) {
										isCheckShopStatus = true;
										switchShop.setChecked(true);
									}
								}
							}
						} catch (Exception e) {
							UI.toast(getApplicationContext(), "获取店铺状态");
						}

					}

				});
	}

	/**
	 * 打开打印机
	 */
	private void openPrinter() {
		int rel = 0;
		all: for (int i = 0; i < mPortParam.length; i++) {
			PortParameters pp = mPortParam[i];
			try {
				if (PortParameters.BLUETOOTH == pp.getPortType()
						&& mGpService.getPrinterConnectStatus(i) != GpDevice.STATE_CONNECTED) {

					if (i == 0 && isPrinter01CanConnect) {
						rel = mGpService.openPort(i, pp.getPortType(),
								pp.getBluetoothAddr(), 0);
					} else if (i == 1 && isPrinter02CanConnect) {
						rel = mGpService.openPort(i, pp.getPortType(),
								pp.getBluetoothAddr(), 0);
					}

					GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
					if (r != GpCom.ERROR_CODE.SUCCESS
							&& r == GpCom.ERROR_CODE.DEVICE_ALREADY_OPEN) {
						pp.setPortOpenState(true);
						getConnectState();
					}
				}
			} catch (Exception e) {
				// UI.toast(getApplicationContext(), ""+e.getMessage());
				Log.e(TAG, "Connected printer:" + e.getMessage());
			}
		}
		userablePrinterCount = getUseablePrinter().size();
	}

	private ConnectivityManager mConnectivityManager;

	private NetworkInfo netInfo;

	// 监听网络状态变化的广播接收器

	private BroadcastReceiver myNetReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			netInfo = mConnectivityManager.getActiveNetworkInfo();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)
					&& netInfo != null && netInfo.isAvailable()) {

				switch (netInfo.getType()) {
				case ConnectivityManager.TYPE_WIFI:
					// WiFi网络
					UI.toast(getApplicationContext(), "当前WiFi网络网络连接");
					break;
				case ConnectivityManager.TYPE_ETHERNET:
					// 有线网络
					UI.toast(getApplicationContext(), "当前有线网络连接");
					break;
				case ConnectivityManager.TYPE_MOBILE:
					// 手机网络
					UI.toast(getApplicationContext(), "当前手机网络连接");
					break;

				default:
					break;
				}

			} else {
				// 网络断开
				UI.toast(getApplicationContext(), "当前无网络连接");
			}

		}
	};

	private PushServiceStatusReceiver pushListenerReceiver;

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

				switch (type) {
				case GpDevice.STATE_CONNECTING:// 正在连接
					// id ==
					// 0?isPrinter01CanConnect=false:isPrinter02CanConnect=false;
					if (id == 1) {
						isPrinter02CanConnect = false;
					} else {
						isPrinter01CanConnect = false;
					}
					break;
				case GpDevice.STATE_NONE:// 连接断开
					if (id == 1) {
						isPrinter02CanConnect = true;
					} else {
						isPrinter01CanConnect = true;
					}
					break;

				case GpDevice.STATE_VALID_PRINTER:// 有效的打印机
					if (id == 1) {
						isPrinter02CanConnect = false;
					} else {
						isPrinter01CanConnect = false;
					}
					break;

				case GpDevice.STATE_INVALID_PRINTER: // 无效的打印机
					if (id == 1) {
						isPrinter02CanConnect = true;
					} else {
						isPrinter01CanConnect = true;
					}

					break;
				case GpDevice.STATE_CONNECTED:// 已经连接
					if (id == 1) {
						isPrinter02CanConnect = true;
					} else {
						isPrinter01CanConnect = true;
					}
					break;

				default:
					break;
				}

			}
		}
	};

	private TextView headFinish;

	private OrderListView ordersFinish;

	/**
	 * 活动打印接口
	 * 
	 * @param od
	 */
	private void printerQrLable(final GpService gpService,
			final int printerPosition, final Context context,
			final OrderDetail od, String orderNo) {

		Map<String, String> params = new HashMap<String, String>();
		params.put("orderNo", orderNo);
		Api.jsonPostRequest(getApplicationContext(), Api.HTTP_GET_QR_TICKET,
				params, new Listener<org.json.JSONObject>() {

					@Override
					public void onResponse(org.json.JSONObject result) {
						JSONObject ob = JSONObject.parseObject(result
								.toString());

						if (StringUtil.isEmail(ob.getString("errcode"))) { // 如果不是活动，正常打印
							BaocaiPrinterUtil.printLable(gpService,
									printerPosition, context, od);

						}

						Api.getBitmap(getApplicationContext(),
								Api.HTTP_GET_BITMAP + ob.getString("ticket"),
								new Listener<Bitmap>() {

									@Override
									public void onResponse(Bitmap bitmap) {
										// BaocaiPrinterUtil.printLable(gpService,
										// printerPosition, context, od);
										BaocaiPrinterUtil.printBitmapLable(
												gpService, printerPosition,
												context, od, bitmap);
									}
								}, new ErrorListener() {

									@Override
									public void onErrorResponse(VolleyError arg0) {
										BaocaiPrinterUtil.printLable(gpService,
												printerPosition, context, od);

									}
								});
					}

				});
	}

	/**
	 * 活动打印接口
	 * 
	 * @param od
	 */
	private void printerQrNote(final GpService gpService,
			final int printerPosition, final Context context, final Order order) {

		Map<String, String> params = new HashMap<String, String>();
		params.put("orderNo", order.getOrder_no());
		Api.jsonPostRequest(getApplicationContext(), Api.HTTP_GET_QR_TICKET,
				params, new Listener<org.json.JSONObject>() {

					@Override
					public void onResponse(org.json.JSONObject result) {
						JSONObject ob = JSONObject.parseObject(result
								.toString());

						if (StringUtil.isEmail(ob.getString("errcode"))) { // 如果不是活动，正常打印
							BaocaiPrinterUtil.printOrder(gpService,
									printerPosition, context, order);

						}

						Api.getBitmap(getApplicationContext(),
								ob.getString("ticket"), new Listener<Bitmap>() {

									@Override
									public void onResponse(Bitmap bitmap) {
										BaocaiPrinterUtil.printBitmapOrder(
												gpService, printerPosition,
												context, order, bitmap);
									}
								}, new ErrorListener() {

									@Override
									public void onErrorResponse(VolleyError arg0) {
										BaocaiPrinterUtil
												.printOrder(gpService,
														printerPosition,
														context, order);

									}
								});
					}

				});
	}

	public void getUntreatedOrders(final boolean isUpate) {

		final int lastPager = unTreatedPagerNumber;
		if (isUpate)
			unTreatedPagerNumber = 1;
		Api.jsonGetRequest(getApplicationContext(),
				Api.HTTP_GET_ORDER_UNTREATED + "?shopId=" + shopid
						+ "&pageNum=" + unTreatedPagerNumber,
				new Listener<String>() {

					@Override
					public void onResponse(String result) {
						try {
							JSONObject resultJson = JSON.parseObject(result);
							if (resultJson.get("totalPageNum") != null
									&& resultJson.get("pageContent") != null) {
								List<Order> orders = JSON.parseArray(
										resultJson.getString("pageContent"),
										Order.class);
								
								
								if (orders == null
										|| orders.size() == 0
										|| resultJson
												.getIntValue("totalPageNum") <= unTreatedPagerNumber) {
									UI.toast(getApplicationContext(),
											"没有更多的数据了");
								}

								if (orders != null && !isUpate) {
									untreatedAdapter.getDatas().removeAll(
											orders);
									untreatedAdapter.getDatas().addAll(orders);
									unTreatedPagerNumber++;
								}
								if (orders != null && isUpate) {

									clearList(1);
									untreatedAdapter.getDatas().addAll(orders);
									unTreatedPagerNumber = 2;
								}
								if (resultJson.getIntValue("totalPageNum") >= unTreatedPagerNumber) {
									getUntreatedOrders(false);
								}
							} else if (resultJson.get("errcode") != null
									&& "100103".equals(resultJson
											.getString("errcode"))) {
								if (isUpate||unTreatedPagerNumber==1)
									clearList(1);
								UI.toast(getApplicationContext(), "没找到数据");
							} else {
								UI.toast(getApplicationContext(), "查询错误："
										+ resultJson);
							}

						} catch (com.alibaba.fastjson.JSONException e) {

							try {
								JSONObject jobject = JSON.parseObject(result);
								UI.toast(getApplicationContext(),
										"" + jobject.get("errmsg"));
							} catch (Exception e2) {
								UI.toast(getApplicationContext(), "服务器出错");
							} finally {
								if (isUpate)
									unTreatedPagerNumber = lastPager;
							}
						} finally {
							// TODO 通知那个fagament刷新回去
							untreatedAdapter.notifyDataSetChanged();
						}

					}
				});

	}

	public void getMakingOrders(final boolean isUpate) {
		final int lastPager = makingPagePageNumber;
		if (isUpate)
			makingPagePageNumber = 1;
		Api.jsonGetRequest(getApplicationContext(), Api.HTTP_GET_ORDER_MAKING
				+ "?shopId=" + shopid + "&pageNum=" + makingPagePageNumber,
				new Listener<String>() {

					@Override
					public void onResponse(String result) {
						try {
							JSONObject resultJson = JSON.parseObject(result);
							if (resultJson.get("totalPageNum") != null
									&& resultJson.get("pageContent") != null) {
								List<Order> orders = JSON.parseArray(
										resultJson.getString("pageContent"),
										Order.class);
								if (orders == null
										|| orders.size() == 0
										|| resultJson
												.getIntValue("totalPageNum") <= makingPagePageNumber) {
									UI.toast(getApplicationContext(),
											"没有更多的数据了");
								}
								if (orders != null && !isUpate) {

									makingAdapter.getDatas().removeAll(orders);
									makingAdapter.getDatas().addAll(orders);
									makingPagePageNumber++;

								}
								if (orders != null && isUpate) {
									clearList(2);
									makingAdapter.getDatas().addAll(orders);
									makingPagePageNumber = 2;

								}
								if (resultJson.getIntValue("totalPageNum") >= makingPagePageNumber) {
									getMakingOrders(false);
								}
							} else if (resultJson.get("errcode") != null
									&& "100103".equals(resultJson
											.getString("errcode"))) {
								if (isUpate||makingPagePageNumber==1)
									clearList(2);
								UI.toast(getApplicationContext(), "没找到数据");
							} else {
								UI.toast(getApplicationContext(), "查询错误："
										+ resultJson);
							}

						} catch (com.alibaba.fastjson.JSONException e) {

							try {
								JSONObject jobject = JSON.parseObject(result);
								UI.toast(getApplicationContext(),
										"" + jobject.get("errmsg"));
							} catch (Exception e2) {
								UI.toast(getApplicationContext(), "服务器出错");
							} finally {
								if (isUpate)
									makingPagePageNumber = lastPager;
							}
						} finally {
							makingAdapter.notifyDataSetChanged();

						}

					}
				});

	}

	public void getDeliveryOrders(final boolean isUpate) {

		final int lastPager = devlieringPagePageNumber;
		if (isUpate)
			devlieringPagePageNumber = 1;
		Api.jsonGetRequest(getApplicationContext(), Api.HTTP_GET_ORDER_DELIVERY
				+ "?shopId=" + shopid + "&pageNum=" + devlieringPagePageNumber,
				new Listener<String>() {

					@Override
					public void onResponse(String result) {
						try {
							JSONObject resultJson = JSON.parseObject(result);
							if (resultJson.get("totalPageNum") != null
									&& resultJson.get("pageContent") != null) {
								List<Order> orders = JSON.parseArray(
										resultJson.getString("pageContent"),
										Order.class);
								if (orders == null
										|| orders.size() == 0
										|| resultJson
												.getIntValue("totalPageNum") <= devlieringPagePageNumber) {
									UI.toast(getApplicationContext(),
											"没有更多的数据了");
								}
								if (orders != null && !isUpate) {
									deliveryAdapter.getDatas()
											.removeAll(orders);
									deliveryAdapter.getDatas().addAll(orders);
									devlieringPagePageNumber++;
								}
								if (orders != null && isUpate) {
									clearList(3);
									deliveryAdapter.getDatas().addAll(orders);
									devlieringPagePageNumber = 2;

								}
								if (resultJson.getIntValue("totalPageNum") >= devlieringPagePageNumber) {
									getDeliveryOrders(false);
								}
							} else if (resultJson.get("errcode") != null
									&& "100103".equals(resultJson
											.getString("errcode"))) {
								if (isUpate||devlieringPagePageNumber==1)
									clearList(3);
								UI.toast(getApplicationContext(), "没找到数据");
							} else {
								UI.toast(getApplicationContext(), "查询错误："
										+ resultJson);
							}

						} catch (com.alibaba.fastjson.JSONException e) {

							try {
								JSONObject jobject = JSON.parseObject(result);
								UI.toast(getApplicationContext(),
										"" + jobject.get("errmsg"));
							} catch (Exception e2) {
								UI.toast(getApplicationContext(), "服务器出错");
							} finally {
								if (isUpate)
									devlieringPagePageNumber = lastPager;
							}
						} finally {
							// TODO 通知那个fagament刷新回去

							deliveryAdapter.notifyDataSetChanged();

						}

					}
				});

	}

	/**
	 * 清除list
	 */
	private void clearList(int status) {
		switch (status) {
		case 1:
			untreatedAdapter.getDatas().clear();
			untreatedAdapter.notifyDataSetChanged();
			break;
		case 2:
			makingAdapter.getDatas().clear();
			makingAdapter.notifyDataSetChanged();
			break;
		case 3:
			deliveryAdapter.getDatas().clear();
			deliveryAdapter.notifyDataSetChanged();
			break;
		case 4:
			finishAdapter.getDatas().clear();
			finishAdapter.notifyDataSetChanged();
			break;
		}
	}

	private void abandonOnAccept(final int position) {

		final AbandonOrderDialogFragment odf = AbandonOrderDialogFragment
				.newInstance("推送客服");

		odf.setPeople(getEmployee());
		odf.setPeopleExplain("处理人员");

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag(
				"finishAbandonDialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		odf.show(ft, "finishAbandonDialog");
		odf.setOnPeopleClickListener(new OnItemClickListener() { // 人员选择

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int gridIndex, long arg3) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("orderNo", untreatedAdapter.getDatas().get(position)
						.getOrder_no());
				map.put("employee_id", getEmployee().get(gridIndex).getId()
						+ "");
				map.put("reason_id", odf.getReasonId() + "");
				orderAbandon2Making(position, map, odf);
			}
		});

	}

	private void orderAbandon2Making(final int position,
			Map<String, String> map, final AbandonOrderDialogFragment odf) {
		Api.jsonPostRequest(getApplicationContext(), Api.HTTP_ORDER_ABANDON,
				map, new Listener<org.json.JSONObject>() {

					@Override
					public void onResponse(org.json.JSONObject result) {
						JSONObject arg0 = JSON.parseObject(result.toString());
						try {
							boolean errcode = arg0.getBoolean("success");
							if (errcode) {
								UI.toast(getApplicationContext(), "转送成功");
								handleAcceptAbandon(untreatedAdapter.getDatas()
										.get(position));

							} else {
								UI.toast(getApplicationContext(), "转送失败");
								MobclickAgent.reportError(
										getApplicationContext(), "接受时推送客服错误:"
												+ arg0.toString());

							}

						} catch (Exception e) {
							e.printStackTrace();
							MobclickAgent.reportError(getApplicationContext(),
									"接受时推送客服错误:" + e.getMessage());
						} finally {
							odf.dismiss();
						}
					}

				});
	}

	/**
	 * 获取废弃理由
	 * 
	 * @param position
	 * @param map
	 */
	private void getReson() {
		Api.jsonGetRequest(getApplicationContext(), Api.HTTP_ABANDON_REASON,
				new Listener<String>() {

					@Override
					public void onResponse(String result) {
						JSONObject arg0 = JSON.parseObject(result);
						try {
							boolean errcode = arg0.getBoolean("success");
							if (errcode) {
								SharedPreferences sdf = getSharedPreferences(
										"reason", MODE_PRIVATE);
								Editor editor = sdf.edit();
								editor.putString("data", arg0.getString("data"));
								editor.commit();

							}
						} catch (Exception e) {
							e.printStackTrace();
							MobclickAgent.reportError(getApplicationContext(),
									"作废失败：error:" + arg0.toString());
						}
					}

				});
	}

	public void getFinishOrders(final boolean isUpate) {

		final int lastPager = finishPagePageNumber;
		if (isUpate)
			finishPagePageNumber = 1;
		Api.jsonGetRequest(getApplicationContext(), Api.HTTP_GET_PROBLEM_ORDER
				+ "?shopId=" + shopid + "&pageNum=" + finishPagePageNumber,
				new Listener<String>() {

					@Override
					public void onResponse(String result) {
						try {
							JSONObject resultJson = JSON.parseObject(result);
							if (resultJson.get("totalPageNum") != null
									&& resultJson.get("pageContent") != null) {
								List<Order> orders = JSON.parseArray(
										resultJson.getString("pageContent"),
										Order.class);
								if (orders == null
										|| orders.size() == 0
										|| resultJson
												.getIntValue("totalPageNum") < finishPagePageNumber) {
									UI.toast(getApplicationContext(),
											"没有更多的数据了");
								}
								if (orders != null && !isUpate) {
									finishAdapter.getDatas().remove(orders);
									finishAdapter.getDatas().addAll(orders);
									finishPagePageNumber++;
								}
								if (orders != null && isUpate) {
									clearList(4);
									finishAdapter.getDatas().addAll(orders);
									finishPagePageNumber = 2;
								}
								if (resultJson.getIntValue("totalPageNum") >= finishPagePageNumber) {
									getFinishOrders(false);
								}
								headFinish.setText("问题订单" + "("
										+ resultJson.getIntValue("totalCount")
										+ ")");
							} else if (resultJson.get("errcode") != null
									&& "100103".equals(resultJson
											.getString("errcode"))) {
								if (isUpate||finishPagePageNumber==1){
									clearList(4);
								} 
								
								UI.toast(getApplicationContext(), "没找到数据");
							} else {
								UI.toast(getApplicationContext(), "查询错误："
										+ resultJson);
							}

						} catch (com.alibaba.fastjson.JSONException e) {

							try {
								JSONObject jobject = JSON.parseObject(result);
								UI.toast(getApplicationContext(),
										"" + jobject.get("errmsg"));
							} catch (Exception e2) {
								UI.toast(getApplicationContext(), "服务器出错");
							} finally {
								if (isUpate)
									finishPagePageNumber = lastPager;
							}

						} finally {
							// TODO 通知那个fagament刷新回去
							finishAdapter.notifyDataSetChanged();

						}

					}
				});

	}

}
