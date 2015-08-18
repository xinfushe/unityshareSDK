package us.baocai.baocaishop.adapter;

import java.util.ArrayList;
import java.util.List;

import us.baocai.baocaishop.R;
import us.baocai.baocaishop.bean.OrderDetail;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GoodsAdapter extends BaseAdapter {
	private List<OrderDetail> datas = new ArrayList<OrderDetail>();
	private LayoutInflater mInflater;
	
	public GoodsAdapter(Context context) {
		super();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {

		return datas.size();
	}

	@Override
	public Object getItem(int arg0) {

		return datas.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {

		return arg0;
	}

	public List<OrderDetail> getDatas() {
		return datas;
	}

	@Override
	public View getView(final int position, View convertView,
			ViewGroup container) {
		final OrderDetail od = getDatas().get(position);

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_orderdetail, null);
			holder = new ViewHolder();
			holder.goodName = (TextView) convertView
					.findViewById(R.id.item_content);
			holder.counts = (TextView) convertView
					.findViewById(R.id.item_counts);
			holder.type = (TextView) convertView
					.findViewById(R.id.item_type);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.goodName.setText(od.getItem_name());
		holder.counts.setText(od.getItem_number()+"");
		holder.type.setText("￥"+od.getItem_price());

		return convertView;
	}

	class ViewHolder {
		public TextView goodName;
		public TextView counts;
		public TextView type;
	}

}
