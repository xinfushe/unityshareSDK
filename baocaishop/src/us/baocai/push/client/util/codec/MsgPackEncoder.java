package us.baocai.push.client.util.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.msgpack.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by young on 15-5-15.
 */
public class MsgPackEncoder extends MessageToByteEncoder<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MsgPackEncoder.class);

    protected MessagePack messagePack = new MessagePack();

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        LOGGER.debug("MsgPackCodec encode msg: {}", msg);
//        Preconditions.checkNotNull(msg, "Encode msg is null");
//
//        byte[] msgBytes = this.messagePack.write(msg);
//        out.writeBytes(msgBytes);
    }
}
