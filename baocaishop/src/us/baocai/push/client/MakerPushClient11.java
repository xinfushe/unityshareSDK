package us.baocai.push.client;

/**
 * Created by young on 15-5-14.
 */
public class MakerPushClient11 extends AbstractClient {

    public MakerPushClient11(ClientConfig clientConfig) {
        super(clientConfig);
    }

    public static void main(String[] args) throws InterruptedException {
//        new MakerPushClient(ClientConfigFactory.getMakerClientConfig()).startup();
        new MakerPushClient11(ClientConfigFactory.getMakerClientConfig11()).startup();
    }

}
