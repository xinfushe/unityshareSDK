package us.baocai.baocaishop.widget;

import java.util.List;

import us.baocai.baocaishop.R;
import us.baocai.baocaishop.adapter.GoodsAdapter;
import us.baocai.baocaishop.adapter.PeopleAdapter;
import us.baocai.baocaishop.bean.Employee;
import us.baocai.baocaishop.bean.Order;
import us.baocai.baocaishop.bean.OrderDetail;
import us.baocai.baocaishop.util.StringUtil;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class OrderDialogFragment extends DialogFragment {

	private ListView goods;
	private GridView people;
	private TextView bak;
	private TextView orderDetail;
	private TextView peopleExplain;
	private Button detailPrinter;
	private TextView totalPrice;
	private TextView payWay;
	private TextView address;
	private Activity mActivity;
	private Button cancel;
	private TextView orderTime;

	private Order order;
	private List<Employee> employee;
	private String explain;

	private TextView contactPeople;

	private OnClickListener onPrinterClickListener;
	private OnItemClickListener onPeopleItemClickListener;
	
	private static OrderDialogFragment f =null;

	public static OrderDialogFragment newInstance(String name) {
		OrderDialogFragment f = new OrderDialogFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putString("name", name);
		f.setArguments(args);

		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_order_detail, container);

		getDialog().setTitle(getArguments().getString("name"));
		goods = (ListView) view.findViewById(R.id.detail_goods);
		people = (GridView) view.findViewById(R.id.detail_people);
		bak = (TextView) view.findViewById(R.id.detail_bak);
		orderDetail = (TextView) view.findViewById(R.id.detail_orderno);
		peopleExplain = (TextView) view
				.findViewById(R.id.detail_people_explain);
		detailPrinter = (Button) view.findViewById(R.id.detail_printer);
		address = (TextView) view.findViewById(R.id.detail_address);
		totalPrice = (TextView) view.findViewById(R.id.detail_totalprice);
		payWay = (TextView) view.findViewById(R.id.detail_payway);
		contactPeople = (TextView) view
				.findViewById(R.id.detail_contact_people);
		orderTime = (TextView) view.findViewById(R.id.detail_order_time);

		cancel = (Button) view.findViewById(R.id.detail_cancel);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				OrderDialogFragment.this.dismiss();
			}
		});

		if (order != null) {
			bak.setText("备注:" + order.getBak());
			bak.setTextColor(Color.RED);
			orderDetail.setText("" + order.getSerno() + "号");
			address.setText("地址:" + order.getContact_address());
			if (order.getPay_way() == 1 || order.getPay_way() == 2
					|| order.getPay_fee() == 0) {
				payWay.setText("已支付");

			} else {
				payWay.setText("货到付款 需收款" + order.getPay_fee() + "元");
			}

			totalPrice.setText("￥" + order.getPay_fee());
			contactPeople.setText("联系人:" + order.getContact_name() + "     "
					+ order.getContact_phone());

			orderTime.setText("下单时间:"
					+ StringUtil.date2String(order.getGmt_create()));

		}

		if (employee != null && mActivity != null) {
			PeopleAdapter peopleAdapter = new PeopleAdapter(mActivity);
			peopleAdapter.getDatas().addAll(employee);
			people.setAdapter(peopleAdapter);
		}
		if (explain != null) {
			peopleExplain.setText(explain);
		}

		GoodsAdapter goodsAdapter = new GoodsAdapter(mActivity);
		if (order != null && order.getDetails() != null) {
			goodsAdapter.getDatas().addAll(order.getDetails());
		}
		goods.setAdapter(goodsAdapter);
		goodsAdapter.notifyDataSetChanged();
		setListViewHeightBasedOnChildren(goods);

		people.setOnItemClickListener(onPeopleItemClickListener);
		detailPrinter.setOnClickListener(onPrinterClickListener);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;

	}

	/**
	 * 设置订单明细
	 * 
	 * @param order
	 */
	public void setDetail(List<OrderDetail> ods) {

		if (mActivity != null) {

		}

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		WindowManager.LayoutParams params = getDialog().getWindow()
				.getAttributes();
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.MATCH_PARENT;
		params.gravity = Gravity.CENTER;
		getDialog().getWindow().setAttributes(params);
	}

	/**
	 * 设置订单详情
	 * 
	 * @param order
	 */
	public void setOrder(Order order) {

		this.order = order;

	}

	/**
	 * 设置处理人员
	 * 
	 * @param employee
	 */
	public void setPeople(List<Employee> employee) {
		this.employee = employee;

	}

	/**
	 * 处理人员说明
	 * 
	 * @param explain
	 */
	public void setPeopleExplain(String explain) {
		this.explain = explain;
	}

	/**
	 * 处理人员监听
	 * 
	 * @param peopleListener
	 */
	public void setOnPeopleClickListener(OnItemClickListener peopleListener) {
		this.onPeopleItemClickListener = peopleListener;
	}

	/**
	 * 补打订单监听
	 * 
	 * @param listener
	 */
	public void setOnOrderPrinterListener(
			android.view.View.OnClickListener listener) {
		this.onPrinterClickListener=listener;
	}

	public void setListViewHeightBasedOnChildren(ListView listView) {
		// 获取ListView对应的Adapter
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
			// listAdapter.getCount()返回数据项的数目
			View listItem = listAdapter.getView(i, null, listView);
			// 计算子项View 的宽高
			listItem.measure(0, 0);
			// 统计所有子项的总高度
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		// listView.getDividerHeight()获取子项间分隔符占用的高度
		// params.height最后得到整个ListView完整显示需要的高度
		listView.setLayoutParams(params);
	}

}
