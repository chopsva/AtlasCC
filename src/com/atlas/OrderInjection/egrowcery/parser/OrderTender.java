package com.atlas.OrderInjection.egrowcery.parser;

public class OrderTender {
	
	public int LineNumber;
	public int PaymentType;
	public String Identifier;
	public int Amount;

	public OrderTender() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String toString() {
		String out="OrderTender\n"+
				"LineNumber:"+this.LineNumber+"\n"+
				"PaymentType:"+this.PaymentType+"\n"+
				"Identifier:"+this.Identifier+"\n"+
				"Amount:"+this.Amount;
		return out;
	}

}
