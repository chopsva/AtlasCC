package com.atlas.OrderInjection.egrowcery.parser;

import java.util.ArrayList;
import java.util.List;

public class OrderObject {

	private OrderHeader theHeader;
	private List<OrderItem> theItems=new ArrayList<OrderItem>();
	private List<OrderTender> theTenders=new ArrayList<OrderTender>();
	private List<OrderTax> theTaxes=new ArrayList<OrderTax>();
	
	public OrderObject() {
		// TODO Auto-generated constructor stub
		
	}
	
	public void setHeader(OrderHeader oh) {
		this.theHeader=oh;
	}
	public OrderHeader getHeader() {
		return this.theHeader;
	}
	public void addItem(OrderItem oi){
		this.theItems.add(oi);
	}
	public List<OrderItem> getItems() {
		return this.theItems;
	}
	public void addTender(OrderTender ot) {
		this.theTenders.add(ot);
	}
	public List<OrderTender> getTenders() {
		return this.theTenders;
	}
	
	public void addTax(int code,int able,int amt) {
		for(OrderTax ot : this.theTaxes) {
			if (ot.taxCode==code) {
				ot.addTax(able, amt);
				return;
			}
		}
		//did not find it
		this.theTaxes.add(new OrderTax(code,able,amt));
	}
	public List<OrderTax> getTaxes(){
		return this.theTaxes;
	}
	
	@Override
	public String toString() {
		String out="OrderObject\n"+this.theHeader+"\n";
		for(OrderItem oi : this.theItems) {
			out+=oi +"\n";
		}
		for(OrderTender ot :this.theTenders) {
			out+=ot +"\n";
		}
		for(OrderTax otx: this.theTaxes) {
			out+=otx +"\n";
		}
		
		return out;
	}
	
	
}
