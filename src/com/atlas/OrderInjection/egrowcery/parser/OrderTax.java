package com.atlas.OrderInjection.egrowcery.parser;

public class OrderTax {
	
	public int taxCode;
	public int taxableAmt;
	public int taxAmt;

	public OrderTax() {
		// TODO Auto-generated constructor stub
	}
	public OrderTax(int code,int able,int tax) {
		this.taxCode=code;
		this.taxableAmt=able;
		this.taxAmt=tax;
	}
	public void addTax(int able,int tax) {
		this.taxableAmt+=able;
		this.taxAmt +=tax;
	}

	@Override
	public String toString() {
		String ret="OrderTax\n"+
				"taxCode:"+this.taxCode+"\n"+
				"taxableAmt:"+this.taxableAmt+"\n"+
				"taxAmt:"+this.taxAmt;
		return ret;
		
	}
}
