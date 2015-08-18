package us.baocai.baocaishop.util;

import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;

import us.baocai.baocaishop.OrderActivity;
import us.baocai.baocaishop.R;
import us.baocai.baocaishop.bean.Order;
import us.baocai.baocaishop.bean.OrderDetail;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

import com.gprinter.aidl.GpService;
import com.gprinter.command.EscCommand;
import com.gprinter.command.EscCommand.ENABLE;
import com.gprinter.command.EscCommand.FONT;
import com.gprinter.command.EscCommand.JUSTIFICATION;
import com.gprinter.command.GpCom;
import com.gprinter.command.TscCommand;
import com.gprinter.command.TscCommand.BITMAP_MODE;
import com.gprinter.command.TscCommand.DIRECTION;
import com.gprinter.command.TscCommand.EEC;
import com.gprinter.command.TscCommand.FONTMUL;
import com.gprinter.command.TscCommand.FONTTYPE;
import com.gprinter.command.TscCommand.MIRROR;
import com.gprinter.command.TscCommand.ROTATION;

public class BaocaiPrinterUtil {

	/**
	 * 打印订单
	 * 
	 * @param order
	 */
	public static void printOrder(final GpService gpService,
			final int printerPosition, final Context context, final Order order) {
		OrderActivity.isPrinting  = true;
		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				EscCommand esc = new EscCommand();
				esc.addPrintAndFeedLines((byte) 2);
				esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF,ENABLE.ON, ENABLE.ON, ENABLE.OFF);//设置为倍高倍宽
				esc.addSelectJustification(JUSTIFICATION.CENTER);//设置打印居中
				/* 打印文字 */
				String[] address = order.getContact_address().split(" ");
				if(address!=null&&address.length>1){
					StringBuffer strAddress = new StringBuffer();
					for (int i = 1; i < address.length; i++) {
						strAddress.append(address[i]+" ");
						
					}
					esc.addText(strAddress.toString()); // 打印联系人
				}
				esc.addPrintAndLineFeed(); // 打印空白行
				
				esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF, ENABLE.OFF,
						ENABLE.OFF, ENABLE.OFF);// 取消倍高倍宽
				esc.addSelectJustification(JUSTIFICATION.LEFT);// 设置打印左对齐
				esc.addText("---------------------------"); // 打印联系人
				esc.addPrintAndLineFeed(); // 打印空白行
				esc.addText("" + order.getSerno()+"号"); // 打印今日订单数
				
				esc.addPrintAndLineFeed();
				esc.addText("地址:" + order.getContact_address()); // 打印地址
				
				String bak = order.getBak(); // 判断备注是否为空
				if (StringUtil.isEmpty(bak)) {
					bak = "";
				}
				esc.addPrintAndLineFeed();
				esc.addText("备注:" + bak); 
				
				esc.addPrintAndLineFeed(); // 打印空白行
				esc.addText("---------------------------"); // 打印联系人
				esc.addPrintAndLineFeed();
				esc.addText("下单时间:"
						+ StringUtil.date2String(order.getGmt_create())); // 打印文字
				esc.addPrintAndLineFeed();

				/* 打印订单名词 */
				for (OrderDetail od : order.getDetails()) {
					int spacesCounts = 7 - od.getItem_name().length();
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < spacesCounts; i++) {
						sb.append("  ");
					}
//					sb.append("x");
					sb.append("   ");
					esc.addText(od.getItem_name() + sb.toString() 
							+ "  "  + "   x" + od.getItem_number()
							); // 商品名
												// 数量 // 单价
					esc.addPrintAndLineFeed();
				}

				

				esc.addPrintAndLineFeed();
				esc.addText("抵扣券抵扣:          ￥" + (DoubleUtil.minus(order.getFee(),order.getPay_fee()))); // 优惠
				esc.addPrintAndLineFeed();
				
				esc.addText("总计:             ￥" + order.getPay_fee()); // 总价
				esc.addPrintAndLineFeed(); // 打印空白行
				esc.addText("---------------------------"); // 打印联系人
				esc.addPrintAndLineFeed(); // 打印空白行
				esc.addPrintAndLineFeed();
				esc.addText("联系人:" + order.getContact_name()+"   "+order.getContact_phone()); // 打印联系人
				esc.addPrintAndLineFeed(); // 打印空白行
				
				if (order.getPay_status()==1|| (order.getPay_fee() == 0 && order.getPay_status()==0)) {
					esc.addText("已付款"); // 支付方式
				} else {
					esc.addText("货到付款 请收款:");
					esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF,ENABLE.ON, ENABLE.ON, ENABLE.OFF);//设置为倍高倍宽
					esc.addText(""+order.getPay_fee());
					esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF, ENABLE.OFF,
							ENABLE.OFF, ENABLE.OFF);// 取消倍高倍宽
					esc.addText("元"); // 打印文字
				}
				esc.addPrintAndLineFeed();
				esc.addPrintAndLineFeed();
				esc.addPrintAndLineFeed();
				esc.addPrintAndLineFeed();
				esc.addPrintAndLineFeed();
				Vector<Byte> datas = esc.getCommand(); // 发送数据
				Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
				byte[] bytes = ArrayUtils.toPrimitive(Bytes);
				String str = Base64.encodeToString(bytes, Base64.DEFAULT);
				int rel;
				try {
					rel = gpService.sendEscCommand(printerPosition, str);
					GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
					if (r != GpCom.ERROR_CODE.SUCCESS) {
						Log.e("Printer error", "Printer error:"+GpCom.getErrorText(r));
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				} finally {
					OrderActivity.isPrinting  = false;
				}
				
			}
		};

		thread.start();
	}

	/**
	 * 打印订单
	 * 
	 * @param order
	 */
	public static void printLable(final GpService gpService,
			final int printerPosition, final Context context,
			final OrderDetail orderDetail) {
		OrderActivity.isPrinting  = true;
		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				TscCommand tsc = new TscCommand();
				tsc.addSize(40, 30); // 设置标签尺寸，按照实际尺寸设置
				tsc.addGap(1); // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
				tsc.addDirection(DIRECTION.BACKWARD, MIRROR.NORMAL);// 设置打印方向
				tsc.addReference(0, 0);// 设置原点坐标
				tsc.addTear(ENABLE.ON); // 撕纸模式开启
				tsc.addCls();// 清除打印缓冲区
				// 绘制简体中文
				tsc.addText(
						10,
						20,
						FONTTYPE.SIMPLIFIED_CHINESE,
						ROTATION.ROTATION_0,
						FONTMUL.MUL_1,
						FONTMUL.MUL_1,
						"现磨:"
								+ orderDetail.getItem_name()
								+ " "
								+ StringUtil
										.date2StringDDHHmm(new java.util.Date()));
				// 绘制图片
				Bitmap b = BitmapFactory.decodeResource(context.getResources(),
						R.drawable.showqrcode);
				tsc.addBitmap(60, 50, BITMAP_MODE.OVERWRITE,200,
						b);

				tsc.addPrint(1, 1); // 打印标签
//				tsc.addSound(2, 100); // 打印标签后 蜂鸣器响
				Vector<Byte> datas = tsc.getCommand(); // 发送数据
				Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
				byte[] bytes = ArrayUtils.toPrimitive(Bytes);
				String str = Base64.encodeToString(bytes, Base64.DEFAULT);
				int rel;
				try {
					rel = gpService.sendTscCommand(printerPosition, str);
					GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
					if (r != GpCom.ERROR_CODE.SUCCESS) {
//						Toast.makeText(context.getApplicationContext(),
//								GpCom.getErrorText(r), Toast.LENGTH_SHORT)
//								.show();
						Log.e("Printer error", "Printer error:"+GpCom.getErrorText(r));
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					OrderActivity.isPrinting  = false;
				}
			}
		};

		thread.start();
	}

	/**
	 * 打印订单
	 * 
	 * @param order
	 */
	public static void printQrLable(final GpService gpService,
			final int printerPosition, final Context context,
			final OrderDetail orderDetail) {

		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				TscCommand tsc = new TscCommand();
				tsc.addSize(40, 30); // 设置标签尺寸，按照实际尺寸设置
				tsc.addGap(1); // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
				tsc.addDirection(DIRECTION.BACKWARD, MIRROR.NORMAL);// 设置打印方向
				tsc.addReference(0, 0);// 设置原点坐标
				tsc.addTear(ENABLE.ON); // 撕纸模式开启
				tsc.addCls();// 清除打印缓冲区
				// 绘制简体中文
				tsc.addText(
						10,
						30,
						FONTTYPE.SIMPLIFIED_CHINESE,
						ROTATION.ROTATION_0,
						FONTMUL.MUL_1,
						FONTMUL.MUL_1,
						"现做:"
								+ orderDetail.getItem_name()
								+ " "
								+ StringUtil
										.date2StringDDHHmm(new java.util.Date()));
				// 绘制图片
				// Bitmap b =
				// BitmapFactory.decodeResource(context.getResources(),
				// R.drawable.showqrcode);
				// tsc.addBitmap(60, 60, BITMAP_MODE.OVERWRITE, b.getWidth() /
				// 2, b);
				// tsc.addQRCode(0, 0,EEC.LEVEL_Q , 150, ROTATION.ROTATION_0,
				// "http://www.baocai.us");
				tsc.addQRCode(20, 50, EEC.LEVEL_L, 5, ROTATION.ROTATION_0,
						" www.gprinter.com.cn");
				// tsc.addQRCode(x, y, level, cellwidth, rotation, data)

				tsc.addPrint(1, 1); // 打印标签
//				tsc.addSound(2, 100); // 打印标签后 蜂鸣器响
				Vector<Byte> datas = tsc.getCommand(); // 发送数据
				Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
				byte[] bytes = ArrayUtils.toPrimitive(Bytes);
				String str = Base64.encodeToString(bytes, Base64.DEFAULT);
				int rel;
				try {
					rel = gpService.sendTscCommand(printerPosition, str);
					GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
					if (r != GpCom.ERROR_CODE.SUCCESS) {
						// Toast.makeText(context.getApplicationContext(),
						// GpCom.getErrorText(r), Toast.LENGTH_SHORT).show();
						Log.e("Printer error", "Printer error:"+GpCom.getErrorText(r));
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

	}

	/**
	 * 打印订单
	 * 
	 * @param order
	 */
	public static void printBitmapLable(final GpService gpService,
			final int printerPosition, final Context context,
			final OrderDetail orderDetail,final Bitmap bitmap) {

		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				TscCommand tsc = new TscCommand();
				tsc.addSize(40, 30); // 设置标签尺寸，按照实际尺寸设置
				tsc.addGap(1); // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
				tsc.addDirection(DIRECTION.BACKWARD, MIRROR.NORMAL);// 设置打印方向
				tsc.addReference(0, 0);// 设置原点坐标
				tsc.addTear(ENABLE.ON); // 撕纸模式开启
				tsc.addCls();// 清除打印缓冲区
				// 绘制简体中文
				tsc.addText(
						10,
						20,
						FONTTYPE.SIMPLIFIED_CHINESE,
						ROTATION.ROTATION_0,
						FONTMUL.MUL_1,
						FONTMUL.MUL_1,
						"    "
								+ orderDetail.getItem_name()
								+ " "
								+ StringUtil
										.date2StringDDHHmm(new java.util.Date()));
				// 绘制图片
//				 Bitmap b =
//				 BitmapFactory.decodeResource(context.getResources(),
//				 R.drawable.showqrcode);
//				 tsc.addBitmap(60, 60, BITMAP_MODE.OVERWRITE, b.getWidth() /
//				 2, b);
				 
				tsc.addBitmap(60, 50, BITMAP_MODE.OVERWRITE, bitmap.getWidth(), bitmap);
				
				tsc.addPrint(1, 1); // 打印标签
				Vector<Byte> datas = tsc.getCommand(); // 发送数据
				Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
				byte[] bytes = ArrayUtils.toPrimitive(Bytes);
				String str = Base64.encodeToString(bytes, Base64.DEFAULT);
				int rel;
				try {
					rel = gpService.sendTscCommand(printerPosition, str);
					GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
					if (r != GpCom.ERROR_CODE.SUCCESS) {
						// Toast.makeText(context.getApplicationContext(),
						// GpCom.getErrorText(r), Toast.LENGTH_SHORT).show();
						Log.e("Printer error", "Printer error:"+GpCom.getErrorText(r));
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};


		thread.start();
	}

	
	
	/**
	 * 打印订单
	 * 
	 * @param order
	 */
	public static void printTESTLable(GpService gpService, int printerPosition,
			Context context) {

		TscCommand tsc = new TscCommand();
		tsc.addSize(40, 30); // 设置标签尺寸，按照实际尺寸设置
		tsc.addGap(1); // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
		tsc.addDirection(DIRECTION.BACKWARD, MIRROR.NORMAL);// 设置打印方向
		tsc.addReference(0, 0);// 设置原点坐标
		tsc.addTear(ENABLE.ON); // 撕纸模式开启
		tsc.addCls();// 清除打印缓冲区
		// 绘制简体中文
		tsc.addText(10, 30, FONTTYPE.SIMPLIFIED_CHINESE, ROTATION.ROTATION_0,
				FONTMUL.MUL_1, FONTMUL.MUL_1, "     包菜东边同楼社群");
		// 绘制图片
		Bitmap b = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.qrcccc);
		tsc.addBitmap(60, 60, BITMAP_MODE.OVERWRITE, b.getWidth() / 2, b);

		tsc.addPrint(1, 1); // 打印标签
//		tsc.addSound(2, 100); // 打印标签后 蜂鸣器响
		Vector<Byte> datas = tsc.getCommand(); // 发送数据
		Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
		byte[] bytes = ArrayUtils.toPrimitive(Bytes);
		String str = Base64.encodeToString(bytes, Base64.DEFAULT);
		int rel;
		try {
			rel = gpService.sendTscCommand(printerPosition, str);
			GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
			if (r != GpCom.ERROR_CODE.SUCCESS) {
				// Toast.makeText(context.getApplicationContext(),
				// GpCom.getErrorText(r), Toast.LENGTH_SHORT).show();
				Log.e("Printer error", "Printer error:"+GpCom.getErrorText(r));
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 打印订单
	 * 
	 * @param order
	 */
	public static void printBitmapOrder(final GpService gpService,
			final int printerPosition, final Context context, final Order order,final Bitmap bitmap) {
		OrderActivity.isPrinting  = true;
		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				EscCommand esc = new EscCommand();
				esc.addPrintAndFeedLines((byte) 2);
				esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF,ENABLE.ON, ENABLE.ON, ENABLE.OFF);//设置为倍高倍宽
				esc.addSelectJustification(JUSTIFICATION.CENTER);//设置打印居中
				/* 打印文字 */
				String[] address = order.getContact_address().split(" ");
				if(address!=null&&address.length>1){
					StringBuffer strAddress = new StringBuffer();
					for (int i = 1; i < address.length; i++) {
						strAddress.append(address[i]+" ");
						
					}
					esc.addText(strAddress.toString()); // 打印联系人
				}
				esc.addPrintAndLineFeed(); // 打印空白行
				esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF, ENABLE.OFF,
						ENABLE.OFF, ENABLE.OFF);// 取消倍高倍宽
				esc.addSelectJustification(JUSTIFICATION.LEFT);// 设置打印左对齐
				esc.addText("---------------------------"); // 打印联系人
				esc.addPrintAndLineFeed(); // 打印空白行
				esc.addText("" + order.getSerno()+"号"); // 打印今日订单数
			
				esc.addPrintAndLineFeed();
				String bak = order.getBak(); // 判断备注是否为空
				if (StringUtil.isEmpty(bak)) {
					bak = "";
				}
				esc.addPrintAndLineFeed();
				esc.addText("备注:" + bak); // 打印地址
				
				esc.addText("地址:" + order.getContact_address()); // 打印地址
				esc.addPrintAndLineFeed(); // 打印空白行
				esc.addText("---------------------------"); // 打印联系人
				esc.addPrintAndLineFeed();
				esc.addText("下单时间:"
						+ StringUtil.date2String(order.getGmt_create())); // 打印文字
				esc.addPrintAndLineFeed();

				/* 打印订单名词 */
				for (OrderDetail od : order.getDetails()) {
					int spacesCounts = 7 - od.getItem_name().length();
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < spacesCounts; i++) {
						sb.append("  ");
					}
					sb.append("   ");
					esc.addText(od.getItem_name() + sb.toString() 
							+ "  "  + "   x" + od.getItem_number()
							); // 商品名
												// 数量
												// 单价
					esc.addPrintAndLineFeed();
				}

				
				esc.addPrintAndLineFeed();
				esc.addText("抵扣券抵扣:          ￥" + (DoubleUtil.minus(order.getFee(),order.getPay_fee()))); // 优惠
				esc.addPrintAndLineFeed();
				
				esc.addText("总计:             ￥" + order.getPay_fee()); // 总价
				esc.addPrintAndLineFeed(); // 打印空白行
				esc.addText("---------------------------"); // 打印联系人
				esc.addPrintAndLineFeed(); // 打印空白行
				esc.addPrintAndLineFeed();
				esc.addText("联系人:" + order.getContact_name()+"    "+order.getContact_phone()); // 打印联系人
				esc.addPrintAndLineFeed(); // 打印空白行
				if (order.getPay_status()==1|| (order.getPay_fee() == 0 && order.getPay_status()==0)) {
					esc.addText("已付款"); // 支付方式
				} else {
//					esc.addText("---------------------------"); //
//					esc.addPrintAndLineFeed();
					esc.addText("货到付款 请收款:");
					esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF,ENABLE.ON, ENABLE.ON, ENABLE.OFF);//设置为倍高倍宽
					esc.addText(""+order.getPay_fee());
					esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF, ENABLE.OFF,
							ENABLE.OFF, ENABLE.OFF);// 取消倍高倍宽
					esc.addText("元"); // 打印文字
//					esc.addPrintAndLineFeed();
//					esc.addText("---------------------------"); //
//					esc.addPrintAndLineFeed();
				}
				esc.addPrintAndLineFeed();
				esc.addPrintAndLineFeed();
			
				esc.addRastBitImage(bitmap, bitmap.getWidth(),  bitmap.getHeight());
				esc.addPrintAndLineFeed();
				esc.addPrintAndLineFeed();
				
				esc.addPrintAndLineFeed();
				esc.addPrintAndLineFeed();
				esc.addPrintAndLineFeed();
				Vector<Byte> datas = esc.getCommand(); // 发送数据
				Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
				byte[] bytes = ArrayUtils.toPrimitive(Bytes);
				String str = Base64.encodeToString(bytes, Base64.DEFAULT);
				int rel;
				try {
					rel = gpService.sendEscCommand(printerPosition, str);
					GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
					if (r != GpCom.ERROR_CODE.SUCCESS) {
						// Toast.makeText(context, GpCom.getErrorText(r),
						// Toast.LENGTH_SHORT).show();
						Log.e("Printer error", "Printer error:"+GpCom.getErrorText(r));
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				} finally {
					OrderActivity.isPrinting  = false;
				}
				
			}
		};

		thread.start();
	}
	
	public static int getVersionCode(Context context)//获取版本号(内部识别号)  
	{  
	    try {  
	        PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);  
	        return pi.versionCode;  
	    } catch (NameNotFoundException e) {  
	        // TODO Auto-generated catch block  
	        e.printStackTrace();  
	        return 0;  
	    }  
	}
	
}
