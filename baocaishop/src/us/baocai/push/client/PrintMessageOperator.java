package us.baocai.push.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by young on 15-7-31.
 */
public class PrintMessageOperator implements MessageOperator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PrintMessageOperator.class);
    
    @Override
    public void operateOrderMsg(String orderMsg) {
        LOGGER.info("处理消息：{}", orderMsg);
    }

    @Override
    public void operateOrderMakingMsg(String orderMakingMsg) {
        LOGGER.info("处理消息：{}", orderMakingMsg);
    }

    @Override
    public void operateOrderWaitDeliverMsg(String orderWaitDeliverMsg) {
        LOGGER.info("处理消息：{}", orderWaitDeliverMsg);
    }

    @Override
    public void operateOrderDeliveringMsg(String orderDeliveringMsg) {
        LOGGER.info("处理消息：{}", orderDeliveringMsg);
    }

    @Override
    public void operateOrderFinishedMsg(String orderFinishedMsg) {
        LOGGER.info("处理消息：{}", orderFinishedMsg);
    }

    @Override
    public void operateOrderToKfMsg(String orderToKfMsg) {
        LOGGER.info("处理消息：{}", orderToKfMsg);
    }

    @Override
    public void operateCancaledOrderMsg(String cancaledOrderMsg) {
        LOGGER.info("处理消息：{}", cancaledOrderMsg);
    }

    @Override
    public void operateResponseIDRegMsg(String responseIDRegMsg) {
        LOGGER.info("处理消息：{}", responseIDRegMsg);
    }

    @Override
    public void operateResumeCancaledOrderMsg(String resumeCancaledOrderMsg) {
        LOGGER.info("处理消息：{}", resumeCancaledOrderMsg);
    }

    @Override
    public void operatePushOrderToFirstMsg(String pushOrderToFirstMsg) {
        LOGGER.info("处理消息：{}", pushOrderToFirstMsg);
    }
}
