package us.baocai.baocaishop.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by young on 15-4-28.
 */
public class OrderDTO {

    private long serno;
    private String order_no;
    private long user_id;
    private String user_name;
    private String contact_name;
    private String contact_address;
    private String contact_phone;
    private double fee;
    private double pay_fee;
    private int status;
    private int pay_way;
    private int pay_status;
    private String bak;
    private int number;
    private long shop_id;
    private String shop_name;
    private int print_status;
    private long make_employee_id;
    private String make_employee_name;
    private long deliver_employee_id;
    private String deliver_employee_name;
    private Date gmt_create;
    private Date gmt_made;
    private Date gmt_delivered;
    private List<OrderDetailDTO> details = new ArrayList<OrderDetailDTO>();

    private long item_id;
    private String item_name;
    
    
    
    public long getItem_id() {
		return item_id;
	}

	public void setItem_id(long item_id) {
		this.item_id = item_id;
	}

	public String getItem_name() {
		return item_name;
	}

	public void setItem_name(String item_name) {
		this.item_name = item_name;
	}

	public long getSerno() {
        return serno;
    }

    public void setSerno(long serno) {
        this.serno = serno;
    }

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getContact_address() {
        return contact_address;
    }

    public void setContact_address(String contact_address) {
        this.contact_address = contact_address;
    }

    public String getContact_phone() {
        return contact_phone;
    }

    public void setContact_phone(String contact_phone) {
        this.contact_phone = contact_phone;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public double getPay_fee() {
        return pay_fee;
    }

    public void setPay_fee(double pay_fee) {
        this.pay_fee = pay_fee;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPay_way() {
        return pay_way;
    }

    public void setPay_way(int pay_way) {
        this.pay_way = pay_way;
    }

    public int getPay_status() {
        return pay_status;
    }

    public void setPay_status(int pay_status) {
        this.pay_status = pay_status;
    }

    public String getBak() {
        return bak;
    }

    public void setBak(String bak) {
        this.bak = bak;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getShop_id() {
        return shop_id;
    }

    public void setShop_id(long shop_id) {
        this.shop_id = shop_id;
    }

    public String getShop_name() {
        return shop_name;
    }

    public void setShop_name(String shop_name) {
        this.shop_name = shop_name;
    }

    public int getPrint_status() {
        return print_status;
    }

    public void setPrint_status(int print_status) {
        this.print_status = print_status;
    }

    public long getMake_employee_id() {
        return make_employee_id;
    }

    public void setMake_employee_id(long make_employee_id) {
        this.make_employee_id = make_employee_id;
    }

    public String getMake_employee_name() {
        return make_employee_name;
    }

    public void setMake_employee_name(String make_employee_name) {
        this.make_employee_name = make_employee_name;
    }

    public long getDeliver_employee_id() {
        return deliver_employee_id;
    }

    public void setDeliver_employee_id(long deliver_employee_id) {
        this.deliver_employee_id = deliver_employee_id;
    }

    public String getDeliver_employee_name() {
        return deliver_employee_name;
    }

    public void setDeliver_employee_name(String deliver_employee_name) {
        this.deliver_employee_name = deliver_employee_name;
    }

    public Date getGmt_create() {
        return gmt_create;
    }

    public void setGmt_create(Date gmt_create) {
        this.gmt_create = gmt_create;
    }

    public Date getGmt_made() {
        return gmt_made;
    }

    public void setGmt_made(Date gmt_made) {
        this.gmt_made = gmt_made;
    }

    public Date getGmt_delivered() {
        return gmt_delivered;
    }

    public void setGmt_delivered(Date gmt_delivered) {
        this.gmt_delivered = gmt_delivered;
    }

    public List<OrderDetailDTO> getDetails() {
        return details;
    }

    public void setDetails(List<OrderDetailDTO> details) {
        this.details = details;
    }

    //    private String orderNo;
//    private long serno;
//    private String address;
//    private String name;
//    private String phoneNo;
//    private double total;
//    private double discount;
//    private double coinsamt;
//    private long addTime;
//    private String status;
//    private long uid;
//    private long maker_uid;
//    private long deliver_uid;
//    private long shopId;
//    private String bak;
//    private String payway;
//    private int count;
//    private String posthash;
//    private String product_name;
//    private String voucher_no;
//    private String print_status;
//    private String pay_status;
//    private String itemId;
//    private List<OrderDetailDTO> details = new ArrayList<OrderDetailDTO>();
//
//    public List<OrderDetailDTO> getDetails() {
//        return details;
//    }
//
//    public void setDetails(List<OrderDetailDTO> details) {
//        this.details = details;
//    }
//
//    public String getOrderNo() {
//        return orderNo;
//    }
//
//    public void setOrderNo(String orderNo) {
//        this.orderNo = orderNo;
//    }
//
//    public String getAddress() {
//        return address;
//    }
//
//    public void setAddress(String address) {
//        this.address = address;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getPhoneNo() {
//        return phoneNo;
//    }
//
//    public void setPhoneNo(String phoneNo) {
//        this.phoneNo = phoneNo;
//    }
//
//    public double getTotal() {
//        return total;
//    }
//
//    public void setTotal(double total) {
//        this.total = total;
//    }
//
//    public double getDiscount() {
//        return discount;
//    }
//
//    public void setDiscount(double discount) {
//        this.discount = discount;
//    }
//
//    public long getAddTime() {
//        return addTime;
//    }
//
//    public void setAddTime(long addTime) {
//        this.addTime = addTime;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public long getUid() {
//        return uid;
//    }
//
//    public void setUid(long uid) {
//        this.uid = uid;
//    }
//
//    public long getShopId() {
//        return shopId;
//    }
//
//    public void setShopId(long shopId) {
//        this.shopId = shopId;
//    }
//
//    public String getBak() {
//        return bak;
//    }
//
//    public void setBak(String bak) {
//        this.bak = bak;
//    }
//
//    public String getPayway() {
//        return payway;
//    }
//
//    public void setPayway(String payway) {
//        this.payway = payway;
//    }
//
//    public int getCount() {
//        return count;
//    }
//
//    public void setCount(int count) {
//        this.count = count;
//    }
//
//    public String getPosthash() {
//        return posthash;
//    }
//
//    public void setPosthash(String posthash) {
//        this.posthash = posthash;
//    }
//
//    public String getProduct_name() {
//        return product_name;
//    }
//
//    public void setProduct_name(String product_name) {
//        this.product_name = product_name;
//    }
//
//    public String getVoucher_no() {
//        return voucher_no;
//    }
//
//    public void setVoucher_no(String voucher_no) {
//        this.voucher_no = voucher_no;
//    }
//
//    public String getPrint_status() {
//        return print_status;
//    }
//
//    public void setPrint_status(String print_status) {
//        this.print_status = print_status;
//    }
//
//    public String getPay_status() {
//        return pay_status;
//    }
//
//    public void setPay_status(String pay_status) {
//        this.pay_status = pay_status;
//    }
//
//    public String getItemId() {
//        return itemId;
//    }
//
//    public void setItemId(String itemId) {
//        this.itemId = itemId;
//    }
//
//    public long getSerno() {
//        return serno;
//    }
//
//    public void setSerno(long serno) {
//        this.serno = serno;
//    }
//
//    public double getCoinsamt() {
//        return coinsamt;
//    }
//
//    public void setCoinsamt(double coinsamt) {
//        this.coinsamt = coinsamt;
//    }
//
//    public long getMaker_uid() {
//        return maker_uid;
//    }
//
//    public void setMaker_uid(long maker_uid) {
//        this.maker_uid = maker_uid;
//    }
//
//    public long getDeliver_uid() {
//        return deliver_uid;
//    }
//
//    public void setDeliver_uid(long deliver_uid) {
//        this.deliver_uid = deliver_uid;
//    }


//    /**
//     * 璁＄畻璁㈠崟鍘熷鎬讳环
//     * @return 璁㈠崟鐨勫師濮嬫�讳环锛屾暟鎹紓甯告椂杩斿洖-1
//     */
//    @Transient
//    public double getTotalAmt(List<ShopItem> shopItemList) {
//        double totalAmt = 0.0D;
//
//        // TODO 濡傛灉鏈潵鍟嗗搧浠锋牸浠庤揣鏋舵嬁锛岃繖閲岄渶瑕佷慨鏀�
//        for(OrderDetailDTO detailDTO : getDetails()) {
//            totalAmt += DoubleUtil.multi(detailDTO.getPrice(), detailDTO.getCount());
//        }
//
//        return totalAmt;
//    }

  

//    public static void main(String[] args) throws JsonProcessingException {
//        String orderNo = OrderHelper.generateOrderNo("123");
//        long addTime = System.currentTimeMillis();
//
//        List<OrderDetailDTO> details = new ArrayList<OrderDetailDTO>();
//
//        for(int i = 0; i < 3; i++) {
//            OrderDetailDTO detail = new OrderDetailDTO();
//
//            detail.setAddTime(addTime);
//            detail.setCount(1);
//            detail.setOrderNo(orderNo);
//            detail.setItemId(String.valueOf(new Random(1).nextInt(10)));
//            detail.setName("鍟嗗搧鏄庣粏" + i);
//            detail.setSl("甯告俯");
//            detail.setStatus("0");
//            detail.setPrice(1.2+i);
//            detail.setCategoryId("" + i);
//
//            details.add(detail);
//        }
//
//        OrderDTO dto = new OrderDTO();
//        dto.setAddTime(addTime);
//        dto.setOrderNo(orderNo);
//        dto.setSerno(1234);
//        dto.setAddress("for test 鍦板潃");
//        dto.setBak("澶囨敞涓�涓俊鎭惂");
//        dto.setCount(3);
//        dto.setCoinsamt(0);
//        dto.setDetails(details);
//        dto.setPayway("delivery");
//        dto.setPhoneNo("18888888888");
//        dto.setPosthash("hashhashhash");
//        dto.setShopId(123);
//        dto.setTotal(100);
//        dto.setUid(12);
//        dto.setVoucher_no("abc");
//        dto.setName("灏忔槑");
//        dto.setPrint_status("0");
//        dto.setStatus("0");
//        dto.setPrint_status("0");
//
//        System.out.println(MapperHelper.genJson(dto));
//    }
}
