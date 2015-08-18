package us.baocai.baocaishop.adapter;

import java.util.ArrayList;
import java.util.List;

import us.baocai.baocaishop.R;
import us.baocai.baocaishop.adapter.StoreAdapter.ViewHolder;
import us.baocai.baocaishop.bean.Store;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BaocaiBluetoothAdapter extends BaseAdapter {

	private List<BluetoothDevice> datas = new ArrayList<BluetoothDevice>();
	private LayoutInflater mInflater;

	public BaocaiBluetoothAdapter(Context context) {
		super();
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

	public List<BluetoothDevice> getDatas() {
		return datas;
	}

	@Override
	public View getView(final int position, View convertView,
			ViewGroup container) {
		final BluetoothDevice device = getDatas().get(position);

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_device, null);
			holder = new ViewHolder();
			holder.deviceName = (TextView) convertView
					.findViewById(R.id.item_deivce_name);
			holder.deviceMac = (TextView) convertView
					.findViewById(R.id.item_deivce_mac);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (device.getName() != null
				&& device.getName().equals("Gprinter")) {
			holder.deviceName.setText("名称:"+"包菜打印机");
			holder.deviceName.setTextColor(Color.RED);
		} else {
			holder.deviceName.setText("名称:"+device.getName());
			holder.deviceName.setTextColor(Color.BLACK);
		}
		holder.deviceMac.setText("地址:"+device.getAddress());
		return convertView;
	}

	class ViewHolder {
		public TextView deviceName;
		public TextView deviceMac;
	}
}
