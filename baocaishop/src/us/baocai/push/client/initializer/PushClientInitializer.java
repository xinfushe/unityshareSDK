package us.baocai.push.client.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import us.baocai.push.client.AbstractClient;
import us.baocai.push.client.handler.ClientHeartBeatHandler;
import us.baocai.push.client.handler.PushClientHandler;
import us.baocai.push.client.handler.ReconnecHandler;

import java.util.concurrent.TimeUnit;

/**
 * Created by young on 15-5-14.
 */
public class PushClientInitializer extends ChannelInitializer<SocketChannel> {

    private AbstractClient client;
    private EventExecutorGroup executorGroup;
    private boolean compression = true;
    private int client_idle_timeout = 30;

    public PushClientInitializer() {
        this.executorGroup = new NioEventLoopGroup();
    }

    public PushClientInitializer(AbstractClient client) {
        this();
        this.client = client;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        if(compression) {
            pipeline.addLast(this.executorGroup, "deflaterEncoder", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
            pipeline.addLast(this.executorGroup, "inflaterDecoder", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
        }

        pipeline.addLast(this.executorGroup, "delimiterFramerDecoder", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast(this.executorGroup, "stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
        pipeline.addLast(this.executorGroup, "stringDecoder", new StringDecoder(CharsetUtil.UTF_8));

        pipeline.addLast(this.executorGroup, "idleStateHandler", new IdleStateHandler(0, 0, client_idle_timeout, TimeUnit.SECONDS));
        pipeline.addLast(this.executorGroup, "heartBeatHandler", new ClientHeartBeatHandler(client));
        pipeline.addLast(this.executorGroup, "reconnectHandler", new ReconnecHandler(client));
        pipeline.addLast(this.executorGroup, "pushClientHandler", new PushClientHandler(client));
    }
}
