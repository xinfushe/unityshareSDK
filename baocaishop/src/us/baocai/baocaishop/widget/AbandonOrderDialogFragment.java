package us.baocai.baocaishop.widget;

import java.util.List;
import java.util.Map;

import org.json.JSONException;

import us.baocai.baocaishop.R;
import us.baocai.baocaishop.adapter.PeopleAdapter;
import us.baocai.baocaishop.bean.Employee;
import us.baocai.baocaishop.bean.Order;
import us.baocai.baocaishop.bean.OrderDetail;
import us.baocai.baocaishop.bean.Reason;
import us.baocai.baocaishop.net.Api;
import us.baocai.baocaishop.util.StringUtil;
import us.baocai.baocaishop.util.UI;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response.Listener;
import com.umeng.analytics.MobclickAgent;

public class AbandonOrderDialogFragment extends DialogFragment {

	private RadioGroup reason;
	private GridView people;
	private TextView bak;
	private TextView peopleExplain;
	private Button detailPrinter;
	private Activity mActivity;
	private Button cancel;

	private Order order;
	private List<Employee> employee;
	private String explain;

	private TextView contactPeople;

	private OnClickListener onPrinterClickListener;
	private OnItemClickListener onPeopleItemClickListener;

	public static AbandonOrderDialogFragment newInstance(String name) {
		AbandonOrderDialogFragment f = new AbandonOrderDialogFragment();
		Bundle args = new Bundle();
		args.putString("name", name);
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_order_abandon, container);

		getDialog().setTitle(getArguments().getString("name"));
		reason = (RadioGroup) view.findViewById(R.id.abandon_reason);
		people = (GridView) view.findViewById(R.id.detail_people);
		peopleExplain = (TextView) view
				.findViewById(R.id.detail_people_explain);
		detailPrinter = (Button) view.findViewById(R.id.detail_printer);
		contactPeople = (TextView) view
				.findViewById(R.id.detail_contact_people);

		cancel = (Button) view.findViewById(R.id.detail_cancel);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				AbandonOrderDialogFragment.this.dismiss();
			}
		});

		if (employee != null && mActivity != null) {
			PeopleAdapter peopleAdapter = new PeopleAdapter(mActivity);
			peopleAdapter.getDatas().addAll(employee);
			people.setAdapter(peopleAdapter);
		}
		if (explain != null) {
			peopleExplain.setText(explain);
		}

		people.setOnItemClickListener(onPeopleItemClickListener);

		getReson();
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
	public void setOrder_no(List<OrderDetail> ods) {

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
	

	/**
	 * 获取废弃理由
	 * @param position
	 * @param map
	 */
	private void getReson() {
		String result=getActivity().getSharedPreferences("reason", Activity.MODE_PRIVATE).getString("data", "");
		if (StringUtil.isEmpty(result)) {
			Api.jsonGetRequest(getActivity(),
					Api.HTTP_ABANDON_REASON,
					new Listener<String>() {
						@Override
						public void onResponse(String result) {
							JSONObject arg0 = JSON.parseObject(result);
							try {
								boolean errcode = arg0.getBoolean("success");
								if (errcode) {
									List<Reason> data=JSON.parseArray(arg0.getString("data0"),Reason.class);
									for(int i=0;i<data.size();i++){
										Reason r = data.get(i);
										RadioButton radio = new RadioButton(getActivity());
										radio.setText(r.content);
										radio.setId(r.id);
										radio.setHeight(UI.dip2px(getActivity(), 48));
										reason.addView(radio);
										if(i==0){
											radio.setChecked(true);
										}
										
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								MobclickAgent.reportError(getActivity(),
										"作废失败：error:" + arg0.toString());
							}
						}

					});
		} else {
			List<Reason> data=JSON.parseArray(result,Reason.class);
			for(int i=0;i<data.size();i++){
				Reason r = data.get(i);
				RadioButton radio = new RadioButton(getActivity());
				radio.setText(r.content);
				radio.setId(r.id);
				radio.setHeight(UI.dip2px(getActivity(), 48));
				reason.addView(radio);
				if(i==0){
					radio.setChecked(true);
				}
				
			}
		}
		
		
	}
	
	public int getReasonId(){
		return reason.getCheckedRadioButtonId();
	}


}
