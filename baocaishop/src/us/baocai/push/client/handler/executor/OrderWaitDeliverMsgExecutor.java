package us.baocai.push.client.handler.executor;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.baocai.crm.biz.push.message.MsgBuilder;
import us.baocai.crm.biz.push.message.OrderWaitDeliverMessage;
import us.baocai.crm.biz.web.dto.MessageDTO;
import us.baocai.push.client.MessageOperator;

/**
 * Created by young on 15-7-30.
 */
public class OrderWaitDeliverMsgExecutor implements Executor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderWaitDeliverMsgExecutor.class);

    private OrderWaitDeliverMsgExecutor() {}

    private static class LazyLoader {
        private static final OrderWaitDeliverMsgExecutor INSTANCE = new OrderWaitDeliverMsgExecutor();
    }

    public static OrderWaitDeliverMsgExecutor getInstance() {
        return LazyLoader.INSTANCE;
    }

    @Override
    public void execute(Channel channel, MessageDTO dto, MessageOperator msgOperator) {
        LOGGER.info("PUSH CLIENT 接收到订单制作完成等待配送消息：{}", dto.getData());

        OrderWaitDeliverMessage orderWaitDeliverMessage = null;

        try {
            orderWaitDeliverMessage = JSON.parseObject(dto.getData(), OrderWaitDeliverMessage.class);
        } catch (Exception e) {
            LOGGER.error("解析消息内容时出错：{}", e.getMessage(), e);
            return;
        }

        if(orderWaitDeliverMessage == null) {
            LOGGER.error("消息内容为空，返回");
            return;
        }

        final String msgId = orderWaitDeliverMessage.getId();

        try {
            msgOperator.operateOrderWaitDeliverMsg(orderWaitDeliverMessage.toJsonString());
        } catch (Exception e) {
            LOGGER.error("PUSH CLIENT 处理消息时出错：{}", e.getMessage(), e);
        }

        LOGGER.debug("向服务器推送响应结果");
        ChannelFuture future = channel.writeAndFlush(MsgBuilder.buildOKResultMsg(orderWaitDeliverMessage.getId()));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()) {
                    LOGGER.debug("向服务器响应消息发送成功.  msgId: {}", msgId);
                } else {
                    LOGGER.error("向服务器响应消息发送失败.", future.cause());
                }
            }
        });
    }
}
