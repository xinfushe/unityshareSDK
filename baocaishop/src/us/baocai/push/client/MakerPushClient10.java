package us.baocai.push.client;

/**
 * Created by young on 15-5-14.
 */
public class MakerPushClient10 extends AbstractClient {

    public MakerPushClient10(ClientConfig clientConfig) {
        super(clientConfig);
    }

    public static void main(String[] args) throws InterruptedException {
//        new MakerPushClient(ClientConfigFactory.getMakerClientConfig()).startup();
        new MakerPushClient10(ClientConfigFactory.getMakerClientConfig10()).startup();
    }

}
