package us.baocai.push.client.util.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import org.msgpack.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by young on 15-5-15.
 */
public class MsgPackDecoder extends ByteToMessageDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MsgPackDecoder.class);

    protected MessagePack messagePack = new MessagePack();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] data = new byte[in.readableBytes()];
        in.readBytes(data);

        if(in.capacity() <= 0)
            return;

        LOGGER.debug("PUSH CLIENT data: {}", data);
        LOGGER.debug("PUSH CLIENT string: {}", new String(data));
        String msg = this.messagePack.read(data, String.class);
        out.add(msg);

        LOGGER.debug("PUSH CLIENT MsgPackDecoder decode msg: {}", msg);
    }
}
