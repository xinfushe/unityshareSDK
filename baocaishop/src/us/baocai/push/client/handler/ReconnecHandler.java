package us.baocai.push.client.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.baocai.crm.biz.push.message.DeliverRegisteIDMessage;
import us.baocai.crm.biz.push.message.MakerRegisteIDMessage;
import us.baocai.crm.biz.push.message.RegisteIDMessage;
import us.baocai.crm.biz.push.nettypush.NettyPusher;
import us.baocai.crm.biz.type.PushMessageTypeConst;
import us.baocai.crm.biz.web.dto.MessageDTO;
import us.baocai.push.client.AbstractClient;
import us.baocai.push.client.util.Constants;

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

/**
 * Created by young on 15-5-14.
 */
public class ReconnecHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReconnecHandler.class);

    private AbstractClient client;

    public ReconnecHandler(AbstractClient clieht) {
        this.client = clieht;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.debug("PUSH CLIENT 连接到: " + ctx.channel().remoteAddress());
        // 连接成功后，发送上报ID消息
        sendIdMsg(ctx.channel());
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // Discard received data
        LOGGER.debug("PUSH CLIENT 接收到消息：{}", msg);
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("与服务器断开连接: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx)
            throws Exception {
        LOGGER.info("等待: " + Constants.RECONNECT_DELAY + 's');

        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("PUSH CLIENT 重新连接到: " + ctx.channel().remoteAddress());
                client.connect(client.configureBootstrap(new Bootstrap(), loop));
            }
        }, Constants.RECONNECT_DELAY, TimeUnit.SECONDS);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.debug("PUSH CLIENT ReconnecHandler 发生异常");
        if (cause instanceof ConnectException) {
            LOGGER.error("PUSH CLIENT 连接失败: " + cause.getMessage());
        }
        LOGGER.error(cause.getMessage(), cause);
        ctx.close();
    }

    private void sendIdMsg(Channel channel) {
        RegisteIDMessage regIDMsg = null;
        if(client.isMaker()) {
            regIDMsg = new MakerRegisteIDMessage(client.getUid(), client.getId(),
                    client.getShopId(), client.getShopGroup(), client.getDeliverId(), client.getGroupWeight());
        } else {
            regIDMsg = new DeliverRegisteIDMessage(client.getUid(), client.getId(),
                    client.getShopId(), client.getShopGroup(), client.getDeliverId(), client.getGroupWeight());
        }
        MessageDTO dto = new MessageDTO(PushMessageTypeConst.ID_MSG, regIDMsg.toJsonString());
        final String msgId = regIDMsg.getId();
        ChannelFuture future = channel.writeAndFlush(String.format("%s\r\n", dto.toJsonString()));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()) {
                    LOGGER.debug("PUSH CLIENT 上报ID完成.  msgId: {}", msgId);
                } else {
                    LOGGER.error("PUSH CLIENT 上报ID失败.", future.cause());
                }
            }
        });
    }
}
