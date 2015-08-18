package us.baocai.push.client;

/**
 * Created by young on 15-5-14.
 */
public class MakerPushClient12 extends AbstractClient {

    public MakerPushClient12(ClientConfig clientConfig) {
        super(clientConfig);
    }

    public static void main(String[] args) throws InterruptedException {
//        new MakerPushClient(ClientConfigFactory.getMakerClientConfig()).startup();
        new MakerPushClient12(ClientConfigFactory.getMakerClientConfig12()).startup();
    }

}
