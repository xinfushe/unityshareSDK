package us.baocai.baocaishop.adapter;

import java.util.ArrayList;
import java.util.List;

import us.baocai.baocaishop.R;
import us.baocai.baocaishop.bean.Order;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListOrderFinishAdapter extends BaseAdapter {
	private List<Order> datas = new ArrayList<Order>();
	private LayoutInflater mInflater;
	private Context context;
	
	
	public ListOrderFinishAdapter(Context context) {
		super();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			
			convertView = mInflater.inflate(R.layout.item_order_finish, null);
			holder = new ViewHolder();
			holder.textView = (TextView) convertView
					.findViewById(R.id.item_content);
			
			holder.textViewName = (TextView) convertView
					.findViewById(R.id.item_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		/// 这一步必须要做,否则不会显示.
		     
		if (order.getPay_way()==1 ||order.getPay_way()==2|| order.getPay_fee() == 0) {
			Drawable drawable= context.getResources().getDrawable(R.drawable.online_pay);
			 drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			 holder.textView.setCompoundDrawables(drawable,null,null,null);

		} else {
			Drawable drawable= context.getResources().getDrawable(R.drawable.pay_cash);
			 drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			 holder.textView.setCompoundDrawables(drawable,null,null,null);
		}
		
		holder.textView.setText(order.getSerno()+"号");

		return convertView;
	}

	class ViewHolder {
		public TextView textViewName;
		public TextView textView;
	}

}
