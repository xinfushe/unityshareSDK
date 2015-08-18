package us.baocai.push.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.baocai.push.client.initializer.PushClientInitializer;

/**
 * Created by young on 15-5-14.
 */
public abstract class AbstractClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);

    protected final ClientConfig clientConfig;

    public AbstractClient(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public long getUid() {
        return this.clientConfig.getUid();
    }

    public long getShopId() {
        return this.clientConfig.getShopId();
    }

    public String getId() {
        return this.clientConfig.getClientID();
    }

    public boolean isMaker() {
        return this.clientConfig.isIsMaker();
    }

    public int getShopGroup() {
        return this.clientConfig.getShopGroup();
    }

    public int getDeliverId() {
        return this.clientConfig.getDeliverId();
    }

    public int getGroupWeight() {
        return this.clientConfig.getGroupWeight();
    }

    public MessageOperator getMsgOperator() {
        return this.clientConfig.getMsgOperator();
    }

    private void run() throws InterruptedException {
        ChannelFuture future = configureBootstrap(new Bootstrap()).connect();
        future.channel().closeFuture().sync();
    }

    public Bootstrap configureBootstrap(Bootstrap b) {
        return configureBootstrap(b, new NioEventLoopGroup());
    }

    public Bootstrap configureBootstrap(Bootstrap b, EventLoopGroup g) {
        b.group(g)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .remoteAddress(this.clientConfig.getServerHost(), this.clientConfig.getServerPort())
                .handler(new PushClientInitializer(this));

        return b;
    }

    public void startup() throws InterruptedException {
        run();
    }

    public void connect(Bootstrap b) {
        b.connect().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.cause() != null) {
                    LOGGER.info("连接失败", future.cause());
                } else {
                    LOGGER.info("连接成功");
                }
            }
        });
    }
}
