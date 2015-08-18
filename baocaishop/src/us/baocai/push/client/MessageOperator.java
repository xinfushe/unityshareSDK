package us.baocai.push.client;

/**
 * Created by young on 15-7-31.
 */
public interface MessageOperator {
    /**
     * 处理新订单消息
     * 当接收到服务端推送的新订单消息时，会调用此方法的实现
     *
     * @param orderMsg 订单消息JSON字符串
     */
    void operateOrderMsg(String orderMsg);

    /**
     * 处理订单制作中的消息
     * 当接收到服务端推送的制作已在制作中的消息时，会调用此方法的实现
     *
     * @param orderMakingMsg 订单制作中的消息JSON字符串
     */
    void operateOrderMakingMsg(String orderMakingMsg);

    /**
     * 处理订单制作完成，等待配送的消息
     * 当接收到服务端推送的订单制作完成等待配送的消息时，会调用此方法的实现
     *
     * @param orderWaitDeliverMsg 订单制作完成等待配送的消息JSON字符串
     */
    void operateOrderWaitDeliverMsg(String orderWaitDeliverMsg);

    /**
     * 处理订单配送中的消息
     * 当接收到服务端推送的订单已在配送中的消息时，会调用此方法的实现
     *
     * @param orderDeliveringMsg 订单配送中的消息JSON字符串
     */
    void operateOrderDeliveringMsg(String orderDeliveringMsg);

    /**
     * 处理订单配送完成的消息
     * 当接收到服务端推送的订单已配送完成的消息时，会调用此方法的实现
     *
     * @param orderFinishedMsg 订单配送完成的消息JSON字符串
     */
    void operateOrderFinishedMsg(String orderFinishedMsg);

    /**
     * 处理订单作废的消息
     * 当接收到咖啡师将订单作废的消息时，会调用此方法的实现
     *
     * @param orderToKfMsg
     */
    void operateOrderToKfMsg(String orderToKfMsg);

    /**
     * 处理恢复作废订单的消息
     * 当接收到客服恢复作废订单到原来状态的消息时，会调用此方法的实现
     *
     * @param resumeCancaledOrderMsg
     */
    void operateResumeCancaledOrderMsg(String resumeCancaledOrderMsg);

    /**
     * 处理将订单放置到最前面的消息
     * 当接收到客服请求紧急配送的消息时，会调用此方法的实现
     *
     * @param pushOrderToFirstMsg
     */
    void operatePushOrderToFirstMsg(String pushOrderToFirstMsg);

    /**
     * 处理客服取消订单的消息
     * 当接收到客服取消订单的消息时，会调用此方法的实现
     * @param cancaledOrderMsg
     */
    void operateCancaledOrderMsg(String cancaledOrderMsg);
    /**
     * 处理上报ID的响应消息
     * 当接收到服务器返回的响应消息时，会调用此方法的实现
     * @param responseIDRegMsg
     */
    void operateResponseIDRegMsg(String responseIDRegMsg);
}
