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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class FinishOrderDialogFragment extends DialogFragment {

	private ListView goods;
	private TextView bak;
	private TextView orderDetail;

	private TextView address;
	private TextView orderStatus;
	private TextView dealEmployee;
	private Activity mActivity;
	private List<Employee> employee;

	public static FinishOrderDialogFragment newInstance(String name) {
		FinishOrderDialogFragment f = new FinishOrderDialogFragment();

		Bundle args = new Bundle();
		args.putString("name", name);
		f.setArguments(args);

		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_order_detail_finish,
				container);

		goods = (ListView) view.findViewById(R.id.detail_goods);
		bak = (TextView) view.findViewById(R.id.detail_bak);
		orderDetail = (TextView) view.findViewById(R.id.detail_orderno);
		address = (TextView) view.findViewById(R.id.detail_address);
		orderStatus = (TextView) view.findViewById(R.id.detail_status);
		dealEmployee = (TextView) view.findViewById(R.id.detail_employee);
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
			GoodsAdapter goodsAdapter = new GoodsAdapter(mActivity);

			if (ods != null) {
				goodsAdapter.getDatas().addAll(ods);

			}
			goods.setAdapter(goodsAdapter);
		}

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
	 * 设置订单详情
	 * 
	 * @param order
	 */
	public void setOrder(Order order) {
		if (order != null) {
			bak.setText("备注:" + order.getBak());
			orderDetail.setText("流水号:" + order.getSerno());
			address.setText("地址:" + order.getContact_address());
			if (5==order.getStatus()) {
				orderStatus.setText("订单状态:废弃");
				orderStatus.setTextColor(Color.RED);
			} else if (4==order.getStatus()) {
				orderStatus.setText("订单状态:完成");
				orderStatus.setTextColor(Color.GREEN);
			}
			
			StringBuffer sb = new StringBuffer();
			if (employee != null && employee.size() > 0) {

				if (order.getMake_employee_id() != 0) {

					for (Employee e : employee) {
						if (order.getMake_employee_id() == e.getId()) {
							sb.append("制作人员:").append(e.getName());
						}

					}
				}
				if (order.getDeliver_employee_id() != 0) {

					for (Employee e : employee) {
						if (order.getMake_employee_id() == e.getId()) {
							sb.append(" 配送人员:").append(e.getName());
						}
					}
				}
			}
			dealEmployee.setText(sb.toString());
		}
	}

}
