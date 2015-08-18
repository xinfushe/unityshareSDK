package us.baocai.push.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import us.baocai.crm.biz.push.message.ChannelStatusMessage;
import us.baocai.crm.biz.type.PushMessageTypeConst;
import us.baocai.crm.biz.web.dto.MessageDTO;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by young on 15-5-14.
 */
public class PrintChannelStatusClient extends AbstractClient {

    public PrintChannelStatusClient(ClientConfig clientConfig) {
        super(clientConfig);
    }

    public void run() {
        try {
            Channel channel = this.configureBootstrap(new Bootstrap())
                    .connect(clientConfig.getServerHost(), clientConfig.getServerPort()).sync().channel();

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            printHint();

            while (true) {
                String line = in.readLine();
                if (line == null) continue;
                MessageDTO dto = null;
                switch (line.trim()) {
                    case "1":
                        dto = PrintClientMessageFactory.buildServerStatusMsg();
                        break;
                    case "2":
                        dto = PrintClientMessageFactory.buildOrderMsg();
                        break;
                    case "3":
                        dto = PrintClientMessageFactory.buildOrderMakingMsg();
                        break;
                    case "4":
                        dto = PrintClientMessageFactory.buildOrderWaitDeliverMsg();
                        break;
                    case "5":
                        dto = PrintClientMessageFactory.buildOrderDeliveringMsg();
                        break;
                    case "6":
                        dto = PrintClientMessageFactory.buildOrderFinishedMsg();
                        break;
                    case "7":
                        dto = PrintClientMessageFactory.buildCancalOrderMsg();
                        break;
                    case "8":
                        dto = PrintClientMessageFactory.buildAckMsg();
                        break;
                    case "9":
                        dto = PrintClientMessageFactory.buildResumeOrderMsg();
                        break;
                    default:
                        printHint();
                }
                if(dto != null) {
                    channel.writeAndFlush(dto.toJsonString() + "\r\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printHint() {
        System.out.println("############################");
        System.out.println("查看服务器状态，请输入：        1");
        System.out.println("发送新订单消息，请输入：        2");
        System.out.println("发送订单制作中消息，请输入：     3");
        System.out.println("发送订单制作完成消息，请输入：   4");
        System.out.println("发送订单配送中消息，请输入：     5");
        System.out.println("发送订单配送完成消息，请输入：   6");
        System.out.println("发送订单作废消息，请输入：      7");
        System.out.println("发送确认消息，请输入：         8");
        System.out.println("发送恢复作废订单消息，请输入：   9");
        System.out.println("############################");
    }

    @Override
    public void startup() throws InterruptedException {
        run();
    }

    public static void main(String [] args) throws Exception {
        new PrintChannelStatusClient(ClientConfigFactory.getStatusClientConfig()).startup();
    }
}
