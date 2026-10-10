package com.atlas.OrderInjection.egrowcery.parser;

public class OrderHeader {
	
	public String IDNumber;
	public String OrderSubmitted;
	public int ItemCount;
	public int SaleTotal;
	public int TaxTotal;
	public String LoyaltyID;
	public int FulfilmentTotal;
	public String OrderCompleted;
	public int TipAmount;
	public int ContainerDepositAmount;
	public int CostPlusAmount; //<--this is bad
	
	
	public OrderHeader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		String out="OrderHeader\n"+
				"IDNumber:"+this.IDNumber+"\n"+
				"OrderSubmitted:"+this.OrderSubmitted+"\n"+
				"ItemCount:"+this.ItemCount+"\n"+
				"SaleTotal:"+this.SaleTotal+"\n"+
				"TaxTotal:"+this.TaxTotal+"\n"+
				"LoyaltyID:"+this.LoyaltyID+"\n"+
				"FulfilmentTotal:"+this.FulfilmentTotal+"\n"+
				"OrderCompleted:"+this.OrderCompleted+"\n"+
				"TipAmount:"+this.TipAmount+"\n"+
				"ContainerDepositAmount:"+this.ContainerDepositAmount+"\n"+
				"CostPlusAmount:"+this.CostPlusAmount;
		return out;
	}
	
}
