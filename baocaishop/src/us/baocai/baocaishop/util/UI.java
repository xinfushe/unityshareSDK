package us.baocai.baocaishop.util;

import us.baocai.baocaishop.R;
import us.baocai.baocaishop.widget.CustomProgressDialog;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class UI {

	public static CustomProgressDialog loadDialog;

	/**
	 * dp转px
	 * 
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static void toast(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	public static void toastTop(Context context, String text) {

		int px = UI.dip2px(context, 24);
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, px * 3);
		TextView textView = new TextView(context);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
		textView.setText(text);
		toast.setView(textView);
		textView.setTextColor(Color.WHITE);
		textView.setPadding(px, px, px, px);
		textView.setBackgroundResource(R.color.half_trans_black);
		toast.show();

	}

	/**
	 * dx转dp
	 * 
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 正在加载对话框
	 * 
	 * @param context
	 */
	public static void showLoading(final Context context) {
		loadDialog = null;
		if (loadDialog == null) {
			loadDialog = CustomProgressDialog.createDialog(context);
		}
		loadDialog.show();
	}

	public static void dismissLoading(Context context) {
		if (loadDialog != null && loadDialog.isShowing()) {
			loadDialog.dismiss();
		}
	}

	public static boolean isShowLoading() {
		if (loadDialog != null && loadDialog.isShowing()) {
			return true;
		} else {
			return false;
		}
	}

	public static int getScreenWidth(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		// 获取屏幕信息
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
//		int screenHeigh = dm.heightPixels;
		return dm.widthPixels;

		
	}
	
	public static int getScreenHeight(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		// 获取屏幕信息
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
//		int screenHeigh = dm.heightPixels;
		return dm.heightPixels;

		
	}
}
