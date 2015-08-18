package us.baocai.push.client.handler.executor;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.baocai.crm.biz.web.dto.MessageDTO;
import us.baocai.push.client.MessageOperator;

/**
 * Created by young on 15-7-30.
 */
public class AckMsgExecutor implements Executor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AckMsgExecutor.class);

    private AckMsgExecutor() {}

    private static class LazyLoader {
        private static final AckMsgExecutor INSTANCE = new AckMsgExecutor();
    }

    public static AckMsgExecutor getInstance() {
        return LazyLoader.INSTANCE;
    }

    @Override
    public void execute(Channel channel, MessageDTO dto, MessageOperator msgOperator) {
        LOGGER.info("PUSH CLIENT 接收到结果确认消息：{}", dto.getData());
    }
}
