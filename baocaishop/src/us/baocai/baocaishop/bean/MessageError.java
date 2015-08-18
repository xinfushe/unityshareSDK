package us.baocai.baocaishop.bean;

/**
 * {"errcode":40029,"errmsg":"invalid code"}
 * @author studyjun
 *
 */
public class MessageError {
	
	private String errcode;
	private String errmsg;
	public String getErrcode() {
		return errcode;
	}
	public void setErrcode(String errcode) {
		this.errcode = errcode;
	}
	public String getErrmsg() {
		return errmsg;
	}
	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}
}
