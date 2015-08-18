package us.baocai.push.client;

/**
 * Created by young on 15-8-3.
 */
public interface ClientConfig {
    /**
     * 获取服务器的IP地址
     * @return
     */
    String getServerHost();
    /**
     * 获取服务器的端口
     * @return
     */
    int getServerPort();
    /**
     * 获取用户ID
     * @return
     */
    long getUid();
    /**
     * 获取要登陆的店铺ID
     * @return
     */
    long getShopId();
    /**
     * 获取客户端的ID标识（全局唯一标识）
     * @return
     */
    String getClientID();
    /**
     * 是否是咖啡师（咖啡师返回true，配送员返回false）
     * @return
     */
    boolean isIsMaker();

    /**
     * 获取推送客户端回调的处理实现
     * @return
     */
    MessageOperator getMsgOperator();

    /**
     * 获取店铺的组别
     * @return
     */
    int getShopGroup();

    /**
     * 获取店铺的配送地址
     * @return
     */
    int getDeliverId();

    /**
     * 获取组权重
     * @return
     */
    int getGroupWeight();
}
