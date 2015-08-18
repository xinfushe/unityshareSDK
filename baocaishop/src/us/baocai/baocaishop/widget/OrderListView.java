package us.baocai.baocaishop.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.baoyz.swipemenulistview.SwipeMenuListView;

public class OrderListView extends SwipeMenuListView{
	
//	private TextView headView;
	
	public OrderListView(Context context) {
		super(context);
		init(context);
	}

	
	public OrderListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}



	public OrderListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}


	private void init(Context context) {
//		headView  = new TextView(context);
//		headView.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
//		headView.setHeight(UI.dip2px(context, 50));
////		headView.setTextSize(unit, size);
//		headView.setGravity(Gravity.CENTER);
//		this.addHeaderView(headView);
	
	}



//	/**
//	 * 设置head文本
//	 * @param text
//	 */
//	public void setHeaderText(String text){
//		headView.setText(text);
//		headView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
//	}
	
//	/**
//	 * 设置head color
//	 * @param text
//	 */
//	public void setHeaderColor(int color){
//		headView.setBackgroundColor(color);
//	}
	
//	/**
//	 * 设置head字体 color
//	 * @param text
//	 */
//	public void setHeaderTextColor(int color){
//		headView.setTextColor(color);
//	}
	
}
