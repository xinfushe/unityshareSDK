package us.baocai.baocaishop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import us.baocai.baocaishop.adapter.ListOrderFinishAdapter;
import us.baocai.baocaishop.bean.Employee;
import us.baocai.baocaishop.bean.Order;
import us.baocai.baocaishop.bean.OrderDetail;
import us.baocai.baocaishop.bean.Store;
import us.baocai.baocaishop.net.Api;
import us.baocai.baocaishop.push.BaocaiPushService;
import us.baocai.baocaishop.push.PushCode;
import us.baocai.baocaishop.util.BaocaiPrinterUtil;
import us.baocai.baocaishop.util.Const;
import us.baocai.baocaishop.util.DeviceUtil;
import us.baocai.baocaishop.util.NetworkUtil;
import us.baocai.baocaishop.util.StringUtil;
import us.baocai.baocaishop.util.UI;
import us.baocai.baocaishop.widget.AbandonOrderDialogFragment;
import us.baocai.baocaishop.widget.OrderDialogFragment;
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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
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
import com.igexin.sdk.PushManager;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

/***
 * 
 * @author stduyjun
 * 
 */
public class FinishOrderActivity extends AppCompatActivity implements OnClickListener {

	private static final String TAG = "LoginActivity";
	
	private OrderListView ordersFinish;
	
	private int finishPagePageNumber = 1;
	private ListOrderFinishAdapter finishAdapter;
	private String shopid;
	private TextView headFinish;
	private List<Employee> employee;
	private int totalCount=0;

	private android.support.v7.app.AlertDialog dealOrderDialog;
	private OrderDialogView odv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_finishlist);
		
		SharedPreferences spf = getSharedPreferences("shop",
				Activity.MODE_PRIVATE);
		shopid = spf.getString("id", "-1");
		
		ordersFinish = (OrderListView) findViewById(R.id.finish_list);
		finishAdapter = new ListOrderFinishAdapter(this);
		ordersFinish.setAdapter(finishAdapter);
		initEmployee();
		ordersFinish.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(final int position, SwipeMenu menu,
					int index) {
				switch (index) {
				case 0:

					abandonFinishOrder(position);

					break;
				}
				return false;
			}

		});
		
		headFinish = (TextView) findViewById(R.id.order_head_finish);

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
		ordersFinish.setMenuCreator(creator);
		ordersFinish.setOnItemClickListener(new OnItemClickListener() { // 制作

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					final int position, long arg3) {
				final Order o = finishAdapter.getDatas().get(position);
				
				
				if(dealOrderDialog==null){
					odv = new OrderDialogView(
							FinishOrderActivity.this);
				
					android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(
							FinishOrderActivity.this);
					builder.setView(odv);
					dealOrderDialog = builder
							.create();
					
					odv.setOnCancleListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							dealOrderDialog.dismiss();
						}
					});
				
				}
				dealOrderDialog.setTitle("订单完成");
				odv.setOrder(o);
				
				if (!dealOrderDialog.isShowing()) {
					dealOrderDialog.show();
				}

			}
		});
		getFinishOrders(true);

	}


	@Override
	protected void onStop() {
		super.onStop();
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


	@Override
	public void onClick(View v) {
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.finish, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		return super.onOptionsItemSelected(item);
	}
	
	
	public void getFinishOrders(final boolean isUpate) {

		final int lastPager = finishPagePageNumber;
		if (isUpate)
			finishPagePageNumber = 1;
		Api.jsonGetRequest(getApplicationContext(), Api.HTTP_GET_ORDER_FINISH
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
								totalCount = resultJson.getIntValue("totalCount");
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
							} else if (resultJson.get("errcode") != null
									&& "100103".equals(resultJson
											.getString("errcode"))) {
								if (isUpate)
									clearList(4);
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
							headFinish.setText("完成" + "("
									+ totalCount + ")");
						}

					}
				});

	}
	
	/**
	 * 转送成功操作
	 * 
	 * @param order
	 */
	private void handleFinishAbandon(Order order) {
		finishAdapter.getDatas().remove(order);

		order.setStatus(5);
		finishAdapter.getDatas().add(0, order);
		finishAdapter.notifyDataSetChanged();
		headFinish.setText("完成" + "(" + totalCount + ")");
	}

	/**
	 * 清除list
	 */
	private void clearList(int status) {
		switch (status) {
		case 4:
			finishAdapter.getDatas().clear();
			finishAdapter.notifyDataSetChanged();
			break;
		
		}
	}
	
	/**
	 * 废弃完成成订单
	 * 
	 * @param position
	 */
	private void abandonFinishOrder(final int position) {

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
				map.put("orderNo", finishAdapter.getDatas().get(position)
						.getOrder_no());
				map.put("employee_id", getEmployee().get(gridIndex).getId()
						+ "");
				map.put("reason_id",(odf.getReasonId()==-1?1:odf.getReasonId()) + "");
				abandonFinishPost(position, map, odf);
			}
		});
	}

	/**
	 * 废弃完成时废弃
	 * 
	 * @param position
	 * @param map
	 */
	private void abandonFinishPost(final int position, Map<String, String> map,
			final AbandonOrderDialogFragment odf) {
		Api.jsonPostRequest(getApplicationContext(),
				Api.HTTP_ORDER_ABANDON_FINISH, map,
				new Listener<org.json.JSONObject>() {

					@Override
					public void onResponse(org.json.JSONObject arg0) {
						try {
							boolean errcode = arg0.getBoolean("success");
							if (errcode) {
								UI.toast(getApplicationContext(), "转送成功");

								handleFinishAbandon(finishAdapter.getDatas()
										.get(position));

							} else {
								UI.toast(getApplicationContext(), "转送失败");
								MobclickAgent.reportError(
										getApplicationContext(), "作废失败：error:"
												+ arg0.toString());

							}

						} catch (JSONException e) {
							e.printStackTrace();
							MobclickAgent.reportError(getApplicationContext(),
									"作废失败：error:" + arg0.toString());

						} finally {
							odf.dismiss();
						}
					}

				});
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
}
