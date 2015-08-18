package us.baocai.push.client;

import java.util.LinkedList;
import java.util.UUID;

import us.baocai.baocaishop.util.RandomUtils;
import us.baocai.crm.biz.push.message.AckMessage;
import us.baocai.crm.biz.push.message.ChannelStatusMessage;
import us.baocai.crm.biz.push.message.OrderDeliveringMessage;
import us.baocai.crm.biz.push.message.OrderFinishedMessage;
import us.baocai.crm.biz.push.message.OrderMakingMessage;
import us.baocai.crm.biz.push.message.OrderMessage;
import us.baocai.crm.biz.push.message.OrderToKfMessage;
import us.baocai.crm.biz.push.message.OrderWaitDeliverMessage;
import us.baocai.crm.biz.push.message.ResumeCancaledOrderMessage;
import us.baocai.crm.biz.type.PushMessageTypeConst;
import us.baocai.crm.biz.util.OrderHelper;
import us.baocai.crm.biz.web.dto.MessageDTO;
import us.baocai.crm.biz.web.dto.PushOrderDTO;

import com.alibaba.fastjson.JSON;

/**
 * Created by young on 15-8-4.
 */
public class PrintClientMessageFactory {

    private static final long shopId = 14L;
    private static final LinkedList<String> orderNoList = new LinkedList<>();

    private static void putOrderNo(String orderNo) {
        orderNoList.add(orderNo);
    }

    private static String getOrderNo() {
        if(orderNoList.isEmpty()) {
            return null;
        }
        return orderNoList.getFirst();
    }

    public static MessageDTO buildServerStatusMsg() {
        MessageDTO dto = new MessageDTO(PushMessageTypeConst.STATUS_MSG,
                new ChannelStatusMessage().toJsonString());
        return dto;
    }

    public static MessageDTO buildOrderMsg() {
        String str = "{\"address_id\":0,\"bak\":\"\",\"contact_address\":\"东边商务大楼 但是\",\"contact_name\":\"好在\",\"contact_phone\":\"13588888888\",\"deliver_employee_id\":0,\"details\":[{\"category_id\":0,\"gmt_create\":1438308642000,\"item_id\":25,\"item_image_path\":\"http://bcimage.b0.upaiyun.com/item/e2dd3cb9-7660-4206-b132-e0b1149ca806.png\",\"item_name\":\"美式\",\"item_number\":1,\"item_price\":0.01,\"order_id\":0,\"order_no\":\"0892579149452543062\",\"status\":1}],\"fee\":0.01,\"gmt_create\":1438308642000,\"item_id\":25,\"item_image_path\":\"http://bcimage.b0.upaiyun.com/item/e2dd3cb9-7660-4206-b132-e0b1149ca806.png\",\"item_name\":\"美式\",\"make_employee_id\":0,\"number\":1,\"order_no\":\"0892579149452543062\",\"pay_fee\":0,\"pay_status\":1,\"pay_way\":3,\"print_status\":0,\"serno\":4,\"shop_id\":13,\"shop_name\":\"东边店\",\"status\":1,\"user_id\":2891,\"user_name\":\"末底改\"}";
        PushOrderDTO pushOrderDTO = JSON.parseObject(str, PushOrderDTO.class);
        String orderNo = OrderHelper.generateNumbericOrderNo(UUID.randomUUID().toString());
        pushOrderDTO.setOrder_no(orderNo);
        pushOrderDTO.setShop_id(shopId);
        pushOrderDTO.setDeliver_id(1000L);
        putOrderNo(orderNo);
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setOrder(pushOrderDTO);
        orderMessage.setShopGroup(14);
        MessageDTO dto = new MessageDTO();
        dto.setType(PushMessageTypeConst.ORDER_MSG);
        dto.setData(JSON.toJSONString(orderMessage));
        return dto;
    }
    public static MessageDTO buildCancalOrderMsg() {
        return new MessageDTO(PushMessageTypeConst.ORDER_TO_KF_MSG, JSON.toJSONString(new OrderToKfMessage(getOrderNo(), shopId, 0)));
    }

    public static MessageDTO buildOrderMakingMsg() {
        return new MessageDTO(PushMessageTypeConst.ORDER_MAKING_MSG, JSON.toJSONString(new OrderMakingMessage(getOrderNo(), shopId)));
    }

    public static MessageDTO buildOrderWaitDeliverMsg() {
        return new MessageDTO(PushMessageTypeConst.ORDER_WAIT_DELIVER_MSG, JSON.toJSONString(new OrderWaitDeliverMessage(getOrderNo(), shopId)));
    }

    public static MessageDTO buildOrderDeliveringMsg() {
        long deliver_id = RandomUtils.nextInt(200, 399);
        long maker_id = RandomUtils.nextInt(200, 399);
        int identity = deliver_id < 299 ? 1 : 2;
        return new MessageDTO(
                PushMessageTypeConst.ORDER_DELIVERING_MSG,
                JSON.toJSONString(new OrderDeliveringMessage(getOrderNo(), shopId, deliver_id, maker_id, identity, null)));
    }

    public static MessageDTO buildOrderFinishedMsg() {
        long deliver_id = RandomUtils.nextInt(200, 399);
        long maker_id = RandomUtils.nextInt(200, 399);
        int identity = deliver_id < 299 ? 1 : 2;
        return new MessageDTO(
                PushMessageTypeConst.ORDER_FINISHED_MSG,
                JSON.toJSONString(new OrderFinishedMessage(getOrderNo(), shopId, deliver_id, maker_id, identity)));
    }

    public static MessageDTO buildResumeOrderMsg() {
        long uid = RandomUtils.nextInt(200, 399);
        return new MessageDTO(
                PushMessageTypeConst.RESUME_CANCALED_ORDER_MSG,
                JSON.toJSONString(new ResumeCancaledOrderMessage(getOrderNo(), shopId, uid)));
    }

    public static MessageDTO buildAckMsg() {
        MessageDTO dto = new MessageDTO(PushMessageTypeConst.ACK_MSG,
                AckMessage.buildOkMsg(UUID.randomUUID().toString()).toJsonString());
        return dto;
    }
}
