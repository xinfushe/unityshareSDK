package us.baocai.baocaishop.push;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.Date;

import us.baocai.baocaishop.OrderActivity;
import us.baocai.baocaishop.R;
import us.baocai.baocaishop.net.Api;
import us.baocai.baocaishop.util.DeviceUtil;
import us.baocai.baocaishop.util.StringUtil;
import us.baocai.baocaishop.util.UI;
import us.baocai.push.client.AbstractClient;
import us.baocai.push.client.ClientConfig;
import us.baocai.push.client.MessageOperator;
import android.app.Activity;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.umeng.analytics.MobclickAgent;


public class BaocaiPushService extends Service {

	private NioEventLoopGroup group;
	private Channel mChannel;
	private ChannelFuture cf;
	private static final int notifaId = 10086;
	private BaocaiPushClient client;
	private String shopid =null;
	private Thread thread;
	
	private MediaPlayer player;

	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Builder builder = new Builder(this);
		Notification note = new Notification(R.drawable.ic_launcher,
				"包菜接单助手", System.currentTimeMillis());
		Intent i = new Intent(this, OrderActivity.class);

		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

		note.setLatestEventInfo(this, "包菜接单助手",
				"正在运行中", pi);
		note.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(notifaId, note);

		
		if (intent!=null) {
			shopid = intent.getStringExtra("shopid");
		} 
		final SharedPreferences spf = getSharedPreferences("shop",
				Activity.MODE_PRIVATE);
		if (StringUtil.isEmpty(shopid)) {
			
			shopid = spf.getString("id", "");
		}
		
		if (StringUtil.isEmpty(shopid)) { //获取shopid失败
			mHandler.sendEmptyMessage(PushCode.SHOPID_NULL);
			return super.onStartCommand(intent, flags, startId);
		} else { //获取shopid成功
			Message msg = Message.obtain();
			msg.obj=shopid;
			msg.what=PushCode.GET_SHOPID_SUCCESS;
			mHandler.sendMessage(msg);
		}
		
		if (client == null && shopid!=null) {
			client = new BaocaiPushClient(new ClientConfig() {
				@Override
				public String getServerHost() {
					return Api.HOST;
				}

				@Override
				public int getServerPort() {
					return Api.PORT;
				}

				@Override
				public long getUid() {
					return -Integer.parseInt(shopid);
				}

				@Override
				public long getShopId() {
					return Integer.parseInt(shopid);
				}

				@Override
				public String getClientID() {
					return new DeviceUtil(getApplicationContext()).getDeviceId();
				}

				@Override
				public boolean isIsMaker() {
					return true;
				}

				@Override
				public us.baocai.push.client.MessageOperator getMsgOperator() {
					return new MyMessageOperator();
				}

				@Override
				public int getShopGroup() {
					return spf.getInt("shop_group", 0);
				}

				@Override
				public int getDeliverId() {
					return spf.getInt("deliver_id", 0);
				}

				@Override
				public int getGroupWeight() {
					// TODO Auto-generated method stub
					return spf.getInt("group_weight", 0);
				}
			});
			connected();

		}
	
		return super.onStartCommand(intent, flags, startId);
	}

	// 连接
		private void connected() {
			new Thread() {
				public void run() {
					try {
						client.startup();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				};
			}.start();


		}

		@Override
		public void onDestroy() {
			stopForeground(true);
			
			Intent intent = new Intent();
			intent.setAction(PushCode.ACTION_STOP);
			sendBroadcast(intent);
			
			if (player != null) {
				player.release();
				player = null;
			}
			
			super.onDestroy();
			
		}

		  private Handler mHandler = new Handler() {
		        @Override
		        public void handleMessage(Message msg) {
		            Intent intent = new Intent();
		            intent.setAction(PushCode.ACTION);
		            intent.putExtra("code", msg.what);
		            switch (msg.what) {

		                case PushCode.PUSH_HEART_SUCESS: // 心跳响应成功

		                    break;

		                case PushCode.PUSH_OPEN_SUCCESS: // 连接推送成功
		                    intent.putExtra("result", msg.obj.toString());
		                    sendBroadcast(intent);
		                    break;
		                case PushCode.PUSH_ORDER_SUCCESS: // 服务器获取订单成功

		                    intent.putExtra("result", msg.obj.toString());
		                    sendBroadcast(intent);
		                    break;
		                case PushCode.PUSH_STOP: // 中断
		                    sendBroadcast(intent);
		                    break;

		                case PushCode.SHOPID_NULL: // 中断
		                    sendBroadcast(intent);
		                    break;
		                case PushCode.GET_SHOPID_SUCCESS: // 中断
		                    intent.putExtra("clientid", msg.obj.toString());
		                    sendBroadcast(intent);
		                    break;
		                case PushCode.ORDER_DELIVERING:
		                    UI.toastTop(getApplicationContext(), "你有一笔订单移交到配送");
		                    playVoice();
		                    break;
		                case PushCode.ORDER_WAIT_DELIVER:
		                    UI.toastTop(getApplicationContext(), "你有一笔订单制作完毕");
		                    break;
		                case PushCode.ORDER_ACCEPT:
		                    UI.toastTop(getApplicationContext(), "你有一笔新订单");
		                    playAddOrder();
		                    break;
		                case PushCode.ORDER_MAKING:
		                    UI.toastTop(getApplicationContext(), "你有一笔订单制作中");
		                    playVoice();
		                    break;
		                case PushCode.ORDER_FINISH:
		                    UI.toastTop(getApplicationContext(), "你有一笔订单配送完毕");
		                    playVoice();
		                    break;
		                case PushCode.ORDER_CANCEL:
		                    UI.toastTop(getApplicationContext(), "你有一笔订单取消了");
		                    playVoice();
		                    break;
		                case PushCode.ORDER_RESUME_CANCEL:
		                    UI.toastTop(getApplicationContext(), "你有一笔订单恢复了");
		                    playVoice();
		                    break;
		                case PushCode.ORDER_MOVE_TOP:
		                    UI.toastTop(getApplicationContext(), "你有一笔紧急处理订单");
		                    playVoice();
		                    break;
		                case PushCode.PUSH_RECONNECTED:
		                    UI.toastTop(getApplicationContext(), "服务器重连成功");
		                    playVoice();
		                    break;

		                case PushCode.CHECK_ALIVE: // 检查线程时候活着
		                    if (thread != null && !thread.isAlive()) {
		                        connected();
//		                        UI.toast(getApplicationContext(), "recycled");
		                        MobclickAgent.reportError(getApplicationContext(), "baocai push service is recycled");
		                        StringUtil.writeFileToSDAppend("pushthread.log", "recycled " + StringUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss") + "\n");
		                    } else if (thread == null) {
		                        MobclickAgent.reportError(getApplicationContext(), "baocai push service is null");
		                        connected();
		                        StringUtil.writeFileToSDAppend("pushthread.log", "null " + StringUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss") + "\n");
		                    } else {
//		                        UI.toast(getApplicationContext(), "alive");
		                        MobclickAgent.reportError(getApplicationContext(), "baocai push service is alive");
		                        StringUtil.writeFileToSDAppend("pushthread.log", "alive " + StringUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss") + "\n");

		                    }
		                    StringUtil.writeFileToSDAppend("thread.log", "status:" + thread.getState() + " " + StringUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss") + "\n");

		                    sendEmptyMessageDelayed(PushCode.CHECK_ALIVE, 10000);
		                    break;
		                default:
		                    break;
		            }
		        }
		    };

		    /**
		     * 播放系统声音
		     */
		    private void playVoice() {
		        Uri notification = RingtoneManager
		                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
		                notification);
		        r.play();
		    }

		    


		    public class MyMessageOperator implements MessageOperator {

		        @Override
		        public void operateOrderMsg(String orderMsg) {
		            Intent intent = new Intent();
		            intent.setAction(PushCode.ACTION);
		            intent.putExtra("type", PushCode.ORDER_ACCEPT);
		            intent.putExtra("code", PushCode.PUSH_ORDER_SUCCESS);
		            intent.putExtra("result", orderMsg);
		            sendBroadcast(intent);

		            mHandler.sendEmptyMessage(PushCode.ORDER_ACCEPT);

//		            long[] pattern2 = {100, 400, 100, 400};   // 停止 开启 停止 开启
//		            vibrator.vibrate(pattern2, -1);           //重复两次上面的pattern 如果只想震动一次，index
		            StringUtil.writeFileToSDAppend("order.log", "operateOrderMsg order " + orderMsg + "---------------------------------" + StringUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss") + "\n");
		        }

		        @Override
		        public void operateOrderMakingMsg(String orderMakingMsg) {
		            Intent intent = new Intent();
		            intent.setAction(PushCode.ACTION);
		            intent.putExtra("type", PushCode.ORDER_MAKING);
		            intent.putExtra("code", PushCode.PUSH_ORDER_REMOVE);
		            intent.putExtra("status", 2);
		            intent.putExtra("result", orderMakingMsg);
		            sendBroadcast(intent);

		            mHandler.sendEmptyMessage(PushCode.ORDER_MAKING);
		            StringUtil.writeFileToSDAppend("order.log", "operateOrderMakingMsg order " + orderMakingMsg + "---------------------------------" + StringUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss") + "\n");

		        }

		        @Override
		        public void operateOrderWaitDeliverMsg(String orderWaitDeliverMsg) {
		            Intent intent = new Intent();
		            intent.setAction(PushCode.ACTION);
		            intent.putExtra("type", PushCode.ORDER_WAIT_DELIVER);
		            intent.putExtra("code", PushCode.PUSH_ORDER_SUCCESS);
		            intent.putExtra("result", orderWaitDeliverMsg);
		            sendBroadcast(intent);

		            mHandler.sendEmptyMessage(PushCode.ORDER_WAIT_DELIVER);

		            StringUtil.writeFileToSDAppend("order.log", "operateOrderWaitDeliverMsg order " + orderWaitDeliverMsg + "---------------------------------" + StringUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss") + "\n");


		        }

		        @Override
		        public void operateOrderDeliveringMsg(String orderDeliveringMsg) {
		            Intent intent = new Intent();
		            intent.setAction(PushCode.ACTION);
		            intent.putExtra("type", PushCode.ORDER_DELIVERING);
		            intent.putExtra("code", PushCode.PUSH_ORDER_SUCCESS);
		            intent.putExtra("result", orderDeliveringMsg);
		            sendBroadcast(intent);


		            mHandler.sendEmptyMessage(PushCode.ORDER_DELIVERING);
//		            long[] pattern2 = {100, 400, 100, 400};   // 停止 开启 停止 开启
//		            vibrator.vibrate(pattern2, -1);           //重复两次上面的pattern 如果只想震动一次，index

		            StringUtil.writeFileToSDAppend("order.log", "operateOrderWaitDeliverMsg order " + orderDeliveringMsg + "---------------------------------" + StringUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss") + "\n");
		        }

		        @Override
		        public void operateOrderFinishedMsg(String orderFinishedMsg) {
		            Intent intent = new Intent();
		            intent.setAction(PushCode.ACTION);
		            intent.putExtra("type", PushCode.ORDER_FINISH);
		            intent.putExtra("result", orderFinishedMsg);
		            intent.putExtra("status", 4);
		            intent.putExtra("code", PushCode.PUSH_ORDER_REMOVE);
		            sendBroadcast(intent);

		            mHandler.sendEmptyMessage(PushCode.ORDER_FINISH);
		            StringUtil.writeFileToSDAppend("order.log", "operateOrderFinishedMsg order " + orderFinishedMsg + "---------------------------------" + StringUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss") + "\n");
		        }

		        @Override
		        public void operateOrderToKfMsg(String orderToKfMsg) {
		            Intent intent = new Intent();
		            intent.setAction(PushCode.ACTION);
		            intent.putExtra("type", PushCode.ORDER_CANCEL);
		            intent.putExtra("result", orderToKfMsg);
		            intent.putExtra("status", 7);
		            intent.putExtra("code", PushCode.PUSH_ORDER_REMOVE);
		            sendBroadcast(intent);

		            mHandler.sendEmptyMessage(PushCode.ORDER_CANCEL);
		            StringUtil.writeFileToSDAppend("order.log", "operateOrderToKfMsg order " + orderToKfMsg + "---------------------------------" + StringUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss") + "\n");
		        }


		        @Override
		        public void operateResumeCancaledOrderMsg(String resumeCancaledOrderMsg) {
		            Intent intent = new Intent();
		            intent.setAction(PushCode.ACTION);
		            intent.putExtra("type", PushCode.ORDER_RESUME_CANCEL);
		            intent.putExtra("result", resumeCancaledOrderMsg);
		            intent.putExtra("code", PushCode.PUSH_ORDER_SUCCESS);
		            sendBroadcast(intent);

		            mHandler.sendEmptyMessage(PushCode.ORDER_RESUME_CANCEL);

		        }

		        @Override
		        public void operatePushOrderToFirstMsg(String pushOrderToFirstMsg) {
		            Intent intent = new Intent();
		            intent.setAction(PushCode.ACTION);
		            intent.putExtra("type", PushCode.ORDER_MOVE_TOP);
		            intent.putExtra("result", pushOrderToFirstMsg);
		            intent.putExtra("code", PushCode.PUSH_ORDER_SUCCESS);
		            sendBroadcast(intent);

		            mHandler.sendEmptyMessage(PushCode.ORDER_MOVE_TOP);
		        }

		        @Override
		        public void operateCancaledOrderMsg(String cancaledOrderMsg) {
		            Intent intent = new Intent();
		            intent.setAction(PushCode.ACTION);
		            intent.putExtra("type", PushCode.ORDER_CANCEL);
		            intent.putExtra("result", cancaledOrderMsg);
		            intent.putExtra("status", 7);
		            intent.putExtra("code", PushCode.PUSH_ORDER_REMOVE);
		            sendBroadcast(intent);

		            mHandler.sendEmptyMessage(PushCode.PUSH_ORDER_REMOVE);
		        }

		        @Override
		        public void operateResponseIDRegMsg(String responseIDRegMsg) {
		            Intent intent = new Intent();
		            intent.setAction(PushCode.ACTION);
		            intent.putExtra("code", PushCode.PUSH_RECONNECTED);
		            sendBroadcast(intent);

		            mHandler.sendEmptyMessage(PushCode.PUSH_RECONNECTED);
		            StringUtil.writeFileToSDAppend("pushthread.log", "reconn " + StringUtil.date2String(new Date(), "yyyy-MM-dd HH:mm:ss") + "\n");

		        }
		    }

		public class BaocaiPushClient extends AbstractClient{

			public BaocaiPushClient(us.baocai.push.client.ClientConfig clientConfig) {
				super(clientConfig);
			}
		}
		
		/**
		 * 播放MP3声音
		 */
		private void playAddOrder() {

			player = MediaPlayer.create(getApplicationContext(), R.raw.ring);
			player.start();

		}
	}
