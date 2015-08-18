package us.baocai.push.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.baocai.crm.biz.push.message.HeartBeatMessage;
import us.baocai.crm.biz.type.PushMessageTypeConst;
import us.baocai.crm.biz.web.dto.MessageDTO;
import us.baocai.push.client.AbstractClient;

import com.alibaba.fastjson.JSON;

/**
 * Created by young on 15-5-12.
 */
public class ClientHeartBeatHandler extends ChannelDuplexHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHeartBeatHandler.class);

    private final AbstractClient client;
    private int heartBeatFailTimes = 0;

    public ClientHeartBeatHandler(AbstractClient client) {
        this.client = client;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if(e.state() == IdleState.ALL_IDLE) {
                sendHertBeatMsg(ctx);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    private void sendHertBeatMsg(ChannelHandlerContext ctx) throws Exception {
        ChannelFuture future = ctx.channel().writeAndFlush(buildHeartBeatMsg());
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    LOGGER.info("捕获到客户端空闲状态，成功发送心跳信息到服务器.");
                    heartBeatFailTimes = 0;
                } else {
                    heartBeatFailTimes++;
                    Channel channel = future.channel();
                    LOGGER.warn("客户端空间，发送心跳信息到服务器失败. channel: {}", channel, future.cause());
                    if(heartBeatFailTimes >= 3) {
                        LOGGER.warn("空闲状态心跳连续三次发送失败，客户端关闭连接");
                        channel.close();
                        heartBeatFailTimes = 0;
                    }
                }
            }
        });
    }

    private String buildHeartBeatMsg() {
        MessageDTO dto = new MessageDTO(PushMessageTypeConst.HEART_BEAT_MSG,
                new HeartBeatMessage(String.valueOf(client.getUid())).toJsonString());
        return String.format("%s%s", dto.toJsonString(), "\r\n");
    }

    public static void main(String[] args) {
        String msg = "{\"data\":\"{\\\"id\\\":\\\"4befd0b3-041c-4db9-8ad4-c718a8c86576\\\",\\\"order\\\":{\\\"addTime\\\":1430378505423,\\\"address\\\":\\\"菜菜菜菜菜包\\\",\\\"bak\\\":\\\"测试自有推送店铺3\\\",\\\"coinsamt\\\":0,\\\"count\\\":1,\\\"deliver_uid\\\":0,\\\"details\\\":[{\\\"addTime\\\":1430378505423,\\\"categoryId\\\":\\\"27\\\",\\\"count\\\":1,\\\"itemId\\\":\\\"132\\\",\\\"name\\\":\\\"EEEE\\\",\\\"orderNo\\\":\\\"\\\",\\\"price\\\":0.01,\\\"sl\\\":\\\"常温\\\",\\\"status\\\":\\\"0\\\"}],\\\"discount\\\":0,\\\"itemId\\\":\\\"132\\\",\\\"maker_uid\\\":0,\\\"name\\\":\\\"包包包包包菜\\\",\\\"orderNo\\\":\\\"FUeGfmYHjNlz3Ti1410uOinY\\\",\\\"pay_status\\\":\\\"0\\\",\\\"payway\\\":\\\"delivery\\\",\\\"phoneNo\\\":\\\"13510992146\\\",\\\"posthash\\\":\\\"hashhashhash\\\",\\\"print_status\\\":\\\"0\\\",\\\"product_name\\\":\\\"EEEE\\\",\\\"serno\\\":32,\\\"shopId\\\":2,\\\"status\\\":\\\"1\\\",\\\"total\\\":0.01,\\\"uid\\\":1989,\\\"voucher_no\\\":\\\"\\\"},\\\"type\\\":\\\"3\\\"}\",\"type\":\"3\"}{\"data\":\"{\\\"id\\\":\\\"4bb0de2e-3459-4bf8-a0cb-8222988321fa\\\",\\\"message\\\":\\\"ping\\\",\\\"type\\\":\\\"2\\\"}\",\"type\":\"2\"}";
        MessageDTO dto = JSON.parseObject(msg, MessageDTO.class);
        System.out.println(dto.getType());
        System.out.println(dto.getData());
    }
}
