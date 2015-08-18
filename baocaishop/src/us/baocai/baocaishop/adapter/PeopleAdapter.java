package us.baocai.baocaishop.adapter;

import java.util.ArrayList;
import java.util.List;

import us.baocai.baocaishop.R;
import us.baocai.baocaishop.bean.Employee;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PeopleAdapter extends BaseAdapter {
	private List<Employee> datas = new ArrayList<Employee>();
	private LayoutInflater mInflater;
	
	public PeopleAdapter(Context context) {
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

	public List<Employee> getDatas() {
		return datas;
	}

	@Override
	public View getView(final int position, View convertView,
			ViewGroup container) {
		final Employee emp = getDatas().get(position);

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_employee, null);
			holder = new ViewHolder();
			holder.textView = (TextView) convertView
					.findViewById(R.id.item_content);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.textView.setText(emp.getName());
		if (emp.getColor()==0) {
			holder.textView.setTextColor(Color.BLACK);
		} else {
			holder.textView.setTextColor(emp.getColor());
		}
		
		return convertView;
	}

	class ViewHolder {
		public TextView textView;
	}

}

