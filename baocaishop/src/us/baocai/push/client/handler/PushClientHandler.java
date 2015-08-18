package us.baocai.push.client.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.baocai.crm.biz.push.message.AckMessage;
import us.baocai.crm.biz.push.message.OrderMessage;
import us.baocai.crm.biz.type.PushMessageType;
import us.baocai.crm.biz.type.PushMessageTypeConst;
import us.baocai.crm.biz.web.dto.MessageDTO;
import us.baocai.push.client.AbstractClient;
import us.baocai.push.client.handler.executor.MessageProcessor;

/**
 * Created by young on 15-5-14.
 */
public class PushClientHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushClientHandler.class);

    private final AbstractClient client;

    public PushClientHandler(AbstractClient client) {
        this.client = client;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        LOGGER.debug("PUSH CLIENT PushClientHandler exceptionCaught");
        LOGGER.error(cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg)
            throws Exception {
        LOGGER.debug("PUSH CLIENT 客户端接收到消息: {}", msg);

        MessageDTO dto = null;

        try {
            dto = JSON.parseObject(msg, MessageDTO.class);
        } catch (Exception e) {
            LOGGER.error("解析MessageDTO时出错");
            LOGGER.error(e.getMessage(), e);
            return;
        }

        if(dto == null) {
            LOGGER.error("消息内容为空，返回");
            return;
        }

        MessageProcessor.getInstance().process(ctx.channel(), dto, client.getMsgOperator());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.debug("PUSH CLIENT 连接被中断");
    }
}
