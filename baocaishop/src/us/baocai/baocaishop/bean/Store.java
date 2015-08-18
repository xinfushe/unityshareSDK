package us.baocai.baocaishop.bean;

/**
 * 东边
 * 
 * @author studyjun
 * 
 */
public class Store {
	// {
	// "id" : 1,
	// "name" : "东边点",
	// "shopId" : 1,
	// "status" : "1",
	// "business" : null,
	// "cid" : null,
	// "reg_code" : "bcgmd",
	// "reg_status" : "0",
	// "createtime" : 1430879693,
	// "regtime" : 0
	// }

	private int id;
	private String name;
	private String shopId;
	private String status;
	private String business;
	private String cid;
	private String reg_code;
	private String reg_status;
	private long createtime;
	private long regtime;
	
	private int shop_group;
	private int deliver_id;
	private int group_weight;
	private String group_name;

	public int getId() {
		return id;
	}

	public int getShop_group() {
		return shop_group;
	}

	public void setShop_group(int shop_group) {
		this.shop_group = shop_group;
	}

	public int getDeliver_id() {
		return deliver_id;
	}

	public void setDeliver_id(int deliver_id) {
		this.deliver_id = deliver_id;
	}

	public int getGroup_weight() {
		return group_weight;
	}

	public void setGroup_weight(int group_weight) {
		this.group_weight = group_weight;
	}

	public String getGroup_name() {
		return group_name;
	}

	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShopId() {
		return shopId;
	}

	public void setShopId(String shopId) {
		this.shopId = shopId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBusiness() {
		return business;
	}

	public void setBusiness(String business) {
		this.business = business;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getReg_code() {
		return reg_code;
	}

	public void setReg_code(String reg_code) {
		this.reg_code = reg_code;
	}

	public String getReg_status() {
		return reg_status;
	}

	public void setReg_status(String reg_status) {
		this.reg_status = reg_status;
	}

	public long getCreatetime() {
		return createtime;
	}

	public void setCreatetime(long createtime) {
		this.createtime = createtime;
	}

	public long getRegtime() {
		return regtime;
	}

	public void setRegtime(long regtime) {
		this.regtime = regtime;
	}

}
