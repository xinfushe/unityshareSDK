package us.baocai.baocaishop.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单
 * 
 * @author studyjun
 * 
 */
public class Order {

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
	private int make_employee_id;
	private String make_employee_name;
	private int deliver_employee_id;
	private String deliver_employee_name;
	private Date gmt_create;
	private Date gmt_made;
	private Date gmt_delivered;
	private List<OrderDetail> details = new ArrayList<OrderDetail>();
	
	private int orderColor;
	
	private int showType;
	private int order_status;

	public int getOrder_status() {
		return order_status;
	}

	public void setOrder_status(int order_status) {
		this.order_status = order_status;
	}


	public int getShowType() {
		return showType;
	}

	public void setShowType(int showType) {
		this.showType = showType;
	}

	public int getOrderColor() {
		return orderColor;
	}

	public void setOrderColor(int orderColor) {
		this.orderColor = orderColor;
	}

	public void setDetails(List<OrderDetail> details) {
		this.details = details;
	}



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

	public int getMake_employee_id() {
		return make_employee_id;
	}

	public void setMake_employee_id(int make_employee_id) {
		this.make_employee_id = make_employee_id;
	}

	public String getMake_employee_name() {
		return make_employee_name;
	}

	public void setMake_employee_name(String make_employee_name) {
		this.make_employee_name = make_employee_name;
	}

	public int getDeliver_employee_id() {
		return deliver_employee_id;
	}

	public void setDeliver_employee_id(int deliver_employee_id) {
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

	public List<OrderDetail> getDetails() {
		return details;
	}



	@Override
	public boolean equals(Object o) {

		if (o instanceof Order) {
			if (((Order) o).order_no != null && this.order_no != null) {
				if (this.order_no.equals(((Order) o).order_no)) {
					return true;
				}
			}
		}
		return false;
	}

}
