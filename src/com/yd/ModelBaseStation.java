/**
 * 说明：基站信息实体类
 * 作者：章鹏海
 * 时间：2013-8-14
 */
package com.yd;

public class ModelBaseStation {
	private String bsId;
	private String bsName;
	private double bsX;
	private double bsY;
	private String info;
	public String getBsName() {
		return bsName;
	}
	public void setBsName(String bsName) {
		this.bsName = bsName;
	}
	public double getBsX() {
		return bsX;
	}
	public void setBsX(double bsX) {
		this.bsX = bsX;
	}
	public double getBsY() {
		return bsY;
	}
	public void setBsY(double bsY) {
		this.bsY = bsY;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getBsId() {
		return bsId;
	}
	public void setBsId(String bsId) {
		this.bsId = bsId;
	}
	
}
