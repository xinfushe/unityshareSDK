package us.baocai.baocaishop.gprinter;

import java.util.List;
import java.util.Map;

import us.baocai.baocaishop.R;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ListViewAdapter extends BaseAdapter {
	public final static String DEBUG_TAG = "MyAdapter";
	public static final String IMG = "img";
	public static final String TITEL = "titel";
	public static final String STATUS = "status";
	public static final String INFO = "info";
	public static final String BT_ENABLE = "btenable";
	public static final String ENABLE = "enable";
	public static final String DISABLE = "disable";
	public static final int MESSAGE_CONNECT = 1;
	private Handler mHandler = null;
	private List<Map<String, Object>> listItems; // 商品信息集合
	private LayoutInflater listContainer; // 视图容器

	public final class ListItemView { // 自定义控件集
		public ImageView image;
		public TextView title;
		public TextView info;
		public Button button;
	}

	ListItemView listItemView = null;

	public ListViewAdapter(Context context,
			List<Map<String, Object>> listItems, Handler handler) {
		listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
		this.listItems = listItems;
		mHandler = handler;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listItems.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup container) {
		// ListItemView listItemView = null;
		if (convertView == null) {
			listItemView = new ListItemView();
			convertView = listContainer.inflate(R.layout.main_screen_list_item,
					null);
			listItemView.image = (ImageView) convertView
					.findViewById(R.id.ivOperationItem);
			listItemView.title = (TextView) convertView
					.findViewById(R.id.tvOperationItem);
			listItemView.info = (TextView) convertView
					.findViewById(R.id.tvInfo);
			listItemView.button = (Button) convertView
					.findViewById(R.id.btTestConnect);
			// 设置控件集到convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}
		final int arg = position;
		listItemView.image.setBackgroundResource((Integer) listItems.get(
				position).get(IMG));
		listItemView.title.setText((String) listItems.get(position).get(TITEL));
		listItemView.info.setText((String) listItems.get(position).get(INFO));
		listItemView.button.setText((String) listItems.get(position)
				.get(STATUS));
		String str = (String) listItems.get(position).get(BT_ENABLE);
		if (str.equals(ENABLE)) {
			listItemView.button.setEnabled(true);
		} else {
			listItemView.button.setEnabled(false);
		}
		listItemView.button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(DEBUG_TAG, "arg1 " + arg);
				Message message = new Message();
				message.what = MESSAGE_CONNECT;
				message.arg1 = arg;
				listItemView.button.getTag();
				mHandler.sendMessage(message);
			}
		});
		return convertView;
	}

}
