package us.baocai.baocaishop.push;

public class PushCode {

    public static final int PORT = 2295;

    public static final String HOST = "10.0.0.9";

    public static final String ACTION = "us.baocai.shop";

    public static final String ACTION_STOP = "us.baocai.shop.stop";

    /**
     * 状态吗20000-20200为推送相关字段 20000-20099为正常状态 20100-20200为异常状态
     */
    public static final int PUSH_OPEN = 20001; // 打开推送

    public static final int PUSH_OPEN_SUCCESS = 20002; // 连接推送成功

    public static final int PUSH_HEART_SUCESS = 20003; // 推送心跳响应成功

    public static final int PUSH_ORDER_SUCCESS = 20004; // 成功接收服务器推送的订单

    public static final int PUSH_RECONNECTED = 20005; // 成功接收服务器推送的订单

    public static final int PUSH_ORDER_REMOVE = 20007; //成功接收服务器推送的需要移除的订单


    public static final int GET_SHOPID_SUCCESS = 20006;


    public static final int PUSH_LOST = 20101; // 推送丢失

    public static final int PUSH_STOP = 20102; // 推送中断

    public static final int SHOPID_NULL = 20103;


    public static final int ORDER_ACCEPT = 20201; //接受订单

    public static final int ORDER_MAKING = 20202; //制作中

    public static final int ORDER_WAIT_DELIVER = 20299; //等待配送

    public static final int ORDER_DELIVERING = 20203; //配送中

    public static final int ORDER_FINISH = 20204; //完成订单

    public static final int ORDER_ABNORMAL_1 = 20205; //异常状态

    public static final int ORDER_ABNORMAL_B = 20206; //异常状态

    public static final int ORDER_ESQ = 20207; //推送客服

    public static final int ORDER_CANCEL  = 20208; //订单取消

    public static final int ORDER_RESUME_CANCEL  = 20209; //订单从取消中恢复

    public static final int ORDER_MOVE_TOP  = 20210; //订单置顶
    
    public static final int CHECK_ALIVE  = 10210; //订单置顶
}
