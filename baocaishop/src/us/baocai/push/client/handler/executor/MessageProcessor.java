package us.baocai.push.client.handler.executor;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.baocai.crm.biz.type.PushMessageTypeConst;
import us.baocai.crm.biz.web.dto.MessageDTO;
import us.baocai.push.client.MessageOperator;

/**
 * Created by young on 15-7-30.
 */
public class MessageProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);

    private MessageProcessor() {}

    private static class LazyLoader {
        private static final MessageProcessor INSTANCE = new MessageProcessor();
    }

    public static MessageProcessor getInstance() {
        return LazyLoader.INSTANCE;
    }

    public void process(final Channel channel, final MessageDTO dto, final MessageOperator msgOperator) {

        switch (dto.getType()) {
            case PushMessageTypeConst.ORDER_MSG:
                // 处理新认单推送消息
                OrderMsgExecutor.getInstance().execute(channel, dto, msgOperator);
                break;
            case PushMessageTypeConst.ORDER_MAKING_MSG:
                // 处理订单制作中的消息
                OrderMakingMsgExecutor.getInstance().execute(channel, dto, msgOperator);
                break;
            case PushMessageTypeConst.ORDER_WAIT_DELIVER_MSG:
                // 处理订单制作完成等待配送的消息
                OrderWaitDeliverMsgExecutor.getInstance().execute(channel, dto, msgOperator);
                break;
            case PushMessageTypeConst.ORDER_DELIVERING_MSG:
                // 处理订单配送中的消息
                OrderDeliveringMsgExecutor.getInstance().execute(channel, dto, msgOperator);
                break;
            case PushMessageTypeConst.ORDER_FINISHED_MSG:
                // 处理订单配送完成的消息
                OrderFinishedMsgExecutor.getInstance().execute(channel, dto, msgOperator);
                break;
            case PushMessageTypeConst.ACK_MSG:
                // 处理确认消息
                AckMsgExecutor.getInstance().execute(channel, dto, msgOperator);
                break;
            case PushMessageTypeConst.ORDER_TO_KF_MSG:
                // 处理作废订单的消息
                OrderToKfMsgExecutor.getInstance().execute(channel, dto, msgOperator);
                break;
            case PushMessageTypeConst.RESUME_CANCALED_ORDER_MSG:
                // 处理恢复作废订单的消息
                ResumeCancaledOrderMsgExecutor.getInstance().execute(channel, dto, msgOperator);
                break;
            case PushMessageTypeConst.CANCALED_ORDER_MSG:
                // 处理客服取消订单的消息
                CancaledOrderMsgExecutor.getInstance().execute(channel, dto, msgOperator);
                break;
            case PushMessageTypeConst.RESPONSE_ID_REG_MSG:
                // 处理上报ID成功的消息
                ResponseIDRegMsgExecutor.getInstance().execute(channel, dto, msgOperator);
                break;
            default:
                LOGGER.info("PUSH SERVER 未知的消息类型：{}", dto.toJsonString());
        }
    }
}
