package us.baocai.push.client;

/**
 * Created by young on 15-8-3.
 */
public class TestClientConfig implements ClientConfig {

    private String serverHost;
    private int serverPort;
    private long uid;
    private long shopId;
    private String clientID;
    private boolean isMaker;
    private MessageOperator msgOperator;
    private int shopGroup;
    private int deliverId;
    private int groupWeight;

    public TestClientConfig() {}

    public TestClientConfig(String host, int port, long uid, long shopId, String id,
                            boolean isMaker, MessageOperator operator, int shopGroup,
                            int deliverId, int groupWeight) {
        this.serverHost = host;
        this.serverPort = port;
        this.uid = uid;
        this.shopId = shopId;
        this.clientID = id;
        this.isMaker = isMaker;
        this.msgOperator = operator;
        this.shopGroup = shopGroup;
        this.deliverId = deliverId;
        this.groupWeight = groupWeight;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setShopId(long shopId) {
        this.shopId = shopId;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public void setMaker(boolean isMaker) {
        this.isMaker = isMaker;
    }

    public void setMsgOperator(MessageOperator msgOperator) {
        this.msgOperator = msgOperator;
    }

    public void setShopGroup(int shopGroup) {
        this.shopGroup = shopGroup;
    }

    public void setDeliverId(int deliverId) {
        this.deliverId = deliverId;
    }

    public void setGroupWeight(int groupWeight) {
        this.groupWeight = groupWeight;
    }

    @Override
    public String getServerHost() {
        return serverHost;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public long getShopId() {
        return shopId;
    }

    @Override
    public long getUid() {
        return uid;
    }

    @Override
    public String getClientID() {
        return clientID;
    }

    @Override
    public boolean isIsMaker() {
        return isMaker;
    }

    @Override
    public MessageOperator getMsgOperator() {
        return msgOperator;
    }

    @Override
    public int getShopGroup() {
        return shopGroup;
    }

    @Override
    public int getDeliverId() {
        return deliverId;
    }

    @Override
    public int getGroupWeight() {
        return groupWeight;
    }
}
