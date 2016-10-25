package com.ismar.api;

public class ClipInfo {

	private String adaptive;
	private String high;
	private String low;
	private String medium;
	private String normal;
	private String ultra;
	private String iqiyi_4_0;
    private boolean is_vip;
	public ClipInfo() {
		adaptive = "";
		high = "";
		low = "";
		medium = "";
		normal = "";
		ultra = "";
		iqiyi_4_0 = "";
	}

	public String getAdaptive() {
		return adaptive;
	}

	public void setAdaptive(String adaptive) {
		this.adaptive = adaptive;
	}

	public String getHigh() {
		return high;
	}

	public void setHigh(String high) {
		this.high = high;
	}

	public String getLow() {
		return low;
	}

	public void setLow(String low) {
		this.low = low;
	}

	public String getMedium() {
		return medium;
	}

	public void setMedium(String medium) {
		this.medium = medium;
	}

	public String getNormal() {
		return normal;
	}

	public void setNormal(String normal) {
		this.normal = normal;
	}

	public String getUltra() {
		return ultra;
	}

	public void setUltra(String ultra) {
		this.ultra = ultra;
	}

	public String getIqiyi_4_0() {
		return iqiyi_4_0;
	}

	public void setIqiyi_4_0(String iqiyi_4_0) {
		this.iqiyi_4_0 = iqiyi_4_0;
	}
	public  boolean isIs_vip(){
		return is_vip;
	}
	public  void setIs_vip(boolean value){
		is_vip = value;
	}

}