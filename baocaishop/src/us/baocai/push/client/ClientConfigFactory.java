package us.baocai.push.client;


/**
 * Created by young on 15-8-3.
 */
public class ClientConfigFactory {

    public static ClientConfig getMakerClientConfig10() {
        TestClientConfig clientConfig = new TestClientConfig();

//        long uid = RandomUtils.nextInt(200, 299);
        long uid = 10L;
        clientConfig.setServerHost("pushtest.baocai.us");
        clientConfig.setServerHost("localhost");
        clientConfig.setServerPort(10001);
        clientConfig.setUid(uid);
        clientConfig.setClientID("localhost-test-maker-" + uid);
        clientConfig.setMaker(true);
        clientConfig.setShopId(14L);
        clientConfig.setMsgOperator(new PrintMessageOperator());
        clientConfig.setShopGroup(14);
        clientConfig.setDeliverId(1);
        clientConfig.setGroupWeight(2);

        return clientConfig;
    }

    public static ClientConfig getMakerClientConfig11() {
        TestClientConfig clientConfig = new TestClientConfig();

//        long uid = RandomUtils.nextInt(200, 299);
        long uid = 11L;
        clientConfig.setServerHost("pushtest.baocai.us");
        clientConfig.setServerHost("localhost");
        clientConfig.setServerPort(10001);
        clientConfig.setUid(uid);
        clientConfig.setClientID("localhost-test-maker-" + uid);
        clientConfig.setMaker(true);
        clientConfig.setShopId(15L);
        clientConfig.setMsgOperator(new PrintMessageOperator());
        clientConfig.setShopGroup(14);
        clientConfig.setDeliverId(1);
        clientConfig.setGroupWeight(1);

        return clientConfig;
    }

    public static ClientConfig getMakerClientConfig12() {
        TestClientConfig clientConfig = new TestClientConfig();

//        long uid = RandomUtils.nextInt(200, 299);
        long uid = 12L;
        clientConfig.setServerHost("pushtest.baocai.us");
        clientConfig.setServerHost("localhost");
        clientConfig.setServerPort(10001);
        clientConfig.setUid(uid);
        clientConfig.setClientID("localhost-test-maker-" + uid);
        clientConfig.setMaker(true);
        clientConfig.setShopId(16L);
        clientConfig.setMsgOperator(new PrintMessageOperator());
        clientConfig.setShopGroup(14);
        clientConfig.setDeliverId(2);
        clientConfig.setGroupWeight(1);

        return clientConfig;
    }

    public static ClientConfig getDeliverClientConfig() {
        TestClientConfig clientConfig = new TestClientConfig();

//        long uid = RandomUtils.nextInt(300, 399);
        long uid = 13L;
        clientConfig.setServerHost("pushtest.baocai.us");
        clientConfig.setServerHost("localhost");
        clientConfig.setServerPort(10001);
        clientConfig.setUid(uid);
        clientConfig.setClientID("localhost-test-deliver-" + uid);
        clientConfig.setMaker(false);
        clientConfig.setShopId(14L);
        clientConfig.setMsgOperator(new PrintMessageOperator());
        clientConfig.setShopGroup(14);
        clientConfig.setDeliverId(1);
        clientConfig.setGroupWeight(2);

        return clientConfig;
    }

    public static ClientConfig getStatusClientConfig() {
        TestClientConfig clientConfig = new TestClientConfig();

        long uid = 101;
        clientConfig.setServerHost("pushtest.baocai.us");
        clientConfig.setServerHost("localhost");
        clientConfig.setServerPort(10001);
        clientConfig.setUid(uid);
        clientConfig.setClientID("localhost-test-status-" + uid);
        clientConfig.setMaker(false);
        clientConfig.setShopId(0L);
        clientConfig.setMsgOperator(new PrintMessageOperator());

        return clientConfig;
    }
}
