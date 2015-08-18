package us.baocai.push.client.handler.executor;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.baocai.crm.biz.push.message.CancaledOrderMessage;
import us.baocai.crm.biz.push.message.MsgBuilder;
import us.baocai.crm.biz.web.dto.MessageDTO;
import us.baocai.push.client.MessageOperator;

/**
 * Created by young on 15-7-30.
 */
public class CancaledOrderMsgExecutor implements Executor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CancaledOrderMsgExecutor.class);

    private CancaledOrderMsgExecutor() {}

    private static class LazyLoader {
        private static final CancaledOrderMsgExecutor INSTANCE = new CancaledOrderMsgExecutor();
    }

    public static CancaledOrderMsgExecutor getInstance() {
        return LazyLoader.INSTANCE;
    }

    @Override
    public void execute(Channel channel, MessageDTO dto, MessageOperator msgOperator) {
        LOGGER.info("PUSH SERVER 接收到取消订单消息：{}", dto.getData());

        CancaledOrderMessage cancaledOrderMessage = null;

        try {
            cancaledOrderMessage = JSON.parseObject(dto.getData(), CancaledOrderMessage.class);
        } catch (Exception e) {
            LOGGER.error("解析消息内容时出错：{}", e.getMessage(), e);
            return;
        }

        if(cancaledOrderMessage == null) {
            LOGGER.error("消息内容为空，返回");
            return;
        }

        final String msgId = cancaledOrderMessage.getId();

        try {
            msgOperator.operateCancaledOrderMsg(cancaledOrderMessage.toJsonString());
        } catch (Exception e) {
            LOGGER.error("PUSH CLIENT 处理消息时出错：{}", e.getMessage(), e);
        }

        LOGGER.debug("向订单来源推送响应结果");
        ChannelFuture future = channel.writeAndFlush(MsgBuilder.buildOKResultMsg(cancaledOrderMessage.getId()));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()) {
                    LOGGER.debug("向订单来源响应消息发送成功.  msgId: {}", msgId);
                } else {
                    LOGGER.error("向订单来源响应消息发送失败.", future.cause());
                }
            }
        });
    }
}
