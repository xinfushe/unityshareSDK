package us.baocai.push.client.handler.executor;

import io.netty.channel.Channel;
import us.baocai.crm.biz.web.dto.MessageDTO;
import us.baocai.push.client.MessageOperator;

/**
 * Created by young on 15-7-30.
 */
public interface Executor {

    void execute(Channel channel, MessageDTO dto, MessageOperator msgOperator);
}
