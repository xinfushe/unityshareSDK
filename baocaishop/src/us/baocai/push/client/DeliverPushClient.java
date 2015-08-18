package us.baocai.push.client;

/**
 * Created by young on 15-5-14.
 */
public class DeliverPushClient extends AbstractClient {

    public DeliverPushClient(ClientConfig clientConfig) {
        super(clientConfig);
    }

    public static void main(String[] args) throws InterruptedException {
        new DeliverPushClient(ClientConfigFactory.getDeliverClientConfig()).startup();
    }
}
