package us.baocai.baocaishop.adapter;

import java.util.ArrayList;
import java.util.List;

import us.baocai.baocaishop.OrderActivity;
import us.baocai.baocaishop.R;
import us.baocai.baocaishop.bean.Order;
import us.baocai.baocaishop.util.StringUtil;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListOrderAdapter extends BaseAdapter {
	private List<Order> datas = new ArrayList<Order>();
	private LayoutInflater mInflater;
	private Context context;
	
	public ListOrderAdapter(Context context) {
		super();
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
	}

	@Override
	public int getCount() {

		return datas.size();
	}

	@Override
	public int getItemViewType(int position) {

		return getDatas().get(position).getShowType();
	}

	@Override
	public Object getItem(int arg0) {

		return datas.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {

		return arg0;
	}

	public List<Order> getDatas() {
		return datas;
	}

	@Override
	public View getView(final int position, View convertView,
			ViewGroup container) {
		final Order order = getDatas().get(position);

		ViewHolder holder = null;
		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.item_order, null);
			holder = new ViewHolder();
			holder.textView = (TextView) convertView
					.findViewById(R.id.item_content);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (1==order.getStatus()||2==order.getStatus()) {
			String fooler = StringUtil.getLastNum(order.getContact_address());
			if (!StringUtil.isEmpty(fooler) && fooler.length() > 2) {
				fooler = fooler.substring(0, 2);
			}
			
			if(order.getNumber()!=0){
				holder.textView.setText( order.getSerno()+ "号-"
						+ order.getItem_name()+"("+order.getNumber()+")" + "-" + fooler+"楼");
			} else {
				holder.textView.setText( order.getSerno()+ "号-"
						+ order.getItem_name() + "-" + fooler+"楼");
			}
			
		} else {
			holder.textView.setText( order.getSerno()+ "号-"+order.getItem_name()
					);

		}

		  if (order.getOrderColor() == 0 && order.getStatus()!=5&& order.getStatus()!=1){
	            switch (order.getStatus()){
	                case 2:
	                   order.setOrderColor(((OrderActivity)context).getEmployeeColor(order.getMake_employee_id()));
	                    break;

	                case 3:
	                    order.setOrderColor( ((OrderActivity)context).getEmployeeColor(order.getDeliver_employee_id()));

	                    break;
	                case 4:
	                    order.setOrderColor( ((OrderActivity)context).getEmployeeColor(order.getDeliver_employee_id()));

	                    break;
	                case 5:
//	                    order.setOrderColor( ((OrderActivity)context).getEmployeeColor(order.getDeliver_employee_id()));
	                    break;
	                case 6:
	                    order.setOrderColor( ((OrderActivity)context).getEmployeeColor(order.getDeliver_employee_id()));

	                    break;
	            }
	        }


		
		
		if (order.getOrderColor()!=0) {
			holder.textView.setTextColor(order.getOrderColor());
		} else {
			holder.textView.setTextColor(Color.BLACK);
		} 

		return convertView;
	}

	class ViewHolder {
		public TextView textView;
	}

}
