package com.atlas.OrderInjection.egrowcery.parser;

public class OrderItem {
	
	public int LineNumber;
	public String ItemNo;
	public String UPC;
	public String Description;
	public int Sell;
	public int Quantity;
	public int Buy;
	public int TaxRate;
	public int TaxExtn;
	public int LineExtn;
	public int Tare;

	public OrderItem() {
		// TODO Auto-generated constructor stub
	}

	@Override 
	public String toString() {
		String out="OrderItem\n"+
				"LineNumber:"+this.LineNumber+"\n"+
				"ItemNo:"+this.ItemNo+"\n"+
				"UPC:"+this.UPC+"\n"+
				"Descr:"+this.Description+"\n"+
				"Sell:"+this.Sell+"\n"+
				"Qty:"+this.Quantity+"\n"+
				"Buy:"+this.Buy+"\n"+
				"TaxRate:"+this.TaxRate+"\n"+
				"TaxExtn:"+this.TaxExtn+"\n"+
				"LineExtn:"+this.LineExtn+"\n"+
				"Tare:"+this.Tare;
		return out;
	}
}
