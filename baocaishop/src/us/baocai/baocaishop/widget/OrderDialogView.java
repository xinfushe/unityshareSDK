package us.baocai.baocaishop.widget;

import java.util.List;

import us.baocai.baocaishop.R;
import us.baocai.baocaishop.adapter.GoodsAdapter;
import us.baocai.baocaishop.adapter.PeopleAdapter;
import us.baocai.baocaishop.bean.Employee;
import us.baocai.baocaishop.bean.Order;
import us.baocai.baocaishop.util.StringUtil;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class OrderDialogView extends FrameLayout {

	private ListView goods;
	private GridView people;
	private TextView bak;
	private TextView orderDetail;
	private TextView peopleExplain;
	private Button detailPrinter;
	private TextView totalPrice;
	private TextView payWay;
	private TextView address;
	private Button cancel;
	private TextView orderTime;

	private List<Employee> employee;
	private String explain;

	private TextView contactPeople;

	private OnClickListener onPrinterClickListener;
	private OnItemClickListener onPeopleItemClickListener;

	public OrderDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public OrderDialogView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();

	}

	public OrderDialogView(Context context) {
		super(context);
		init();

	}

	void init() {
		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.dialog_order_detail, this);

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
		

		people.setOnItemClickListener(onPeopleItemClickListener);
		detailPrinter.setOnClickListener(onPrinterClickListener);

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

	/**
	 * 设置订单详情
	 * 
	 * @param order
	 */
	public void setOrder(Order order) {

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

		switch (order.getStatus()) {
		case 1:
			peopleExplain.setText("制作");
			break;
		case 2:
			peopleExplain.setText("配送");
			break;
		case 3:
			peopleExplain.setText("完成");
			break;
		case 4:
			peopleExplain.setText("完成");
			break;
		case 5:
			peopleExplain.setText("废弃");
			break;
		case 6:
			peopleExplain.setText("废弃");
			break;
		case 7:
			peopleExplain.setText("废弃");
			break;
		default:
			break;
		}

		GoodsAdapter goodsAdapter = new GoodsAdapter(getContext());
		if (order != null && order.getDetails() != null) {
			goodsAdapter.getDatas().addAll(order.getDetails());
		}
		goods.setAdapter(goodsAdapter);
		goodsAdapter.notifyDataSetChanged();
		setListViewHeightBasedOnChildren(goods);

	}

	/**
	 * 设置处理人员
	 * 
	 * @param employee
	 */
	public void setPeople(List<Employee> employee) {
		if (employee != null) {
			PeopleAdapter peopleAdapter = new PeopleAdapter(getContext());
			peopleAdapter.getDatas().addAll(employee);
			people.setAdapter(peopleAdapter);
		}

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
		people.setOnItemClickListener(peopleListener);
	}

	/**
	 * 补打订单监听
	 * 
	 * @param listener
	 */
	public void setOnOrderPrinterListener(
			android.view.View.OnClickListener listener) {
		detailPrinter.setOnClickListener(listener);
	}
	
	/**
	 * 
	 * @param listener
	 */
	public void setOnCancleListener(android.view.View.OnClickListener listener){
		cancel.setOnClickListener(listener);
	}

}
