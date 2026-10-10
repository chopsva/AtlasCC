package com.atlas.OrderInjection.order;

import java.util.ArrayList;
import java.util.List;

import com.atlas.OrderInjection.objects.OrderInjectionHeader;
import com.atlas.OrderInjection.objects.OrderInjectionItem;
import com.atlas.OrderInjection.objects.OrderInjectionTax;
import com.atlas.OrderInjection.objects.OrderInjectionTender;
import com.atlas.OrderInjection.objects.OrderInjectionUserData;

public class OrderInjectionOrder {
	
	protected List<OrderInjectionItem> items;
	protected List<OrderInjectionTender> tenders;
	protected List<OrderInjectionUserData> userData;
	protected OrderInjectionHeader header;
	protected OrderInjectionTax taxes;
	protected int currentOrdinal=0;
	protected int numStrings=0;
	
	public OrderInjectionOrder() {
		// TODO Auto-generated constructor stub
		items=new ArrayList<OrderInjectionItem>();
		tenders=new ArrayList<OrderInjectionTender>();
		header=new OrderInjectionHeader(); //create empties
		taxes=new OrderInjectionTax();
		userData=new ArrayList<OrderInjectionUserData>();
	}
	
	public void addItemObject(OrderInjectionItem itm) {
		this.currentOrdinal+=1;
		itm.setOrdinal(this.currentOrdinal);
		this.items.add(itm);
	}

	public void addTenderObject(OrderInjectionTender tnd) {
		tenders.add(tnd);
	}
	
	public void addUserDataObject(OrderInjectionUserData ud) {
		userData.add(ud);
	}
	
	public void setHeaderObject(OrderInjectionHeader hdr) {
		this.header=hdr;
	}
	public OrderInjectionHeader getHeaderObject() {
		return this.header;
	}
	
	public void setTaxObject(OrderInjectionTax tx) {
		this.taxes=tx;
	}
	
	public void addTaxAmts(int index,int amt, int sales) {
		this.taxes.addAmtTax(index,amt);
		this.taxes.addTaxableAmt(index,sales);
	}
	public OrderInjectionTax getTaxObject() {
		return this.taxes;
	}
	
	public void calcGross() {
		int tmpPOS=0;
		int tmpNEG=0;

		for(OrderInjectionItem currentItem: this.items) {
			int itemType=currentItem.getInd3()/10;
			if (itemType!=4 && itemType!=5) { //misc items do NOT affect GROSS
				if (currentItem.getExtPrice()<0) {
					tmpNEG +=Math.abs(currentItem.getExtPrice());
				}else{
					tmpPOS +=currentItem.getExtPrice();
				}
			}
		}
		for(int i=1;i<9;i++) { //taxes referenced by ACE id not index id
			int t=this.taxes.getAmtTax(i);
			if (t<0) {
				tmpNEG +=Math.abs(t);
			} else {
				tmpPOS +=t;
			}
		}
		this.header.setGrossNEG(tmpNEG);
		this.header.setGrossPOS(tmpPOS);
		
	}
	
	//TODO- break out item and tender builds for future extensions
	public String getOrderHexString() {
		this.numStrings=0; //reset 
		
		//build item section
		StringBuilder itemHex=new StringBuilder();
		for(OrderInjectionItem currentItem: this.items) {
			if (itemHex.length()!=0) {
				itemHex.append("2c"); //comma
			}
			itemHex.append(currentItem.getTLogString());
			this.numStrings +=1;
			if (currentItem.getNeedExtRecord()) {
				itemHex.append("2c");
				itemHex.append(currentItem.getItemExtTLogString());
				numStrings +=1;
			}
		}
		
		StringBuilder tenderHex=new StringBuilder();
		for(OrderInjectionTender currentTender: this.tenders) {
			if (tenderHex.length()!=0) {
				tenderHex.append("2c");
			}
			tenderHex.append(currentTender.getTLogString());
			this.numStrings +=1;
		}
		
		StringBuilder userDataHex=new StringBuilder();
		for(OrderInjectionUserData currentUserData: this.userData) {
			if (userDataHex.length()!=0) {
				userDataHex.append("2c");
			}
			userDataHex.append(currentUserData.getTLogString());
			this.numStrings +=1;
		}
		
//		if (numStrings>0) {
//			//we have item/tender -> must have tax
//			this.numStrings +=1;
//		}
		StringBuilder taxHex=new StringBuilder();
		
		taxHex.append(this.taxes.getTLogString(false));
		this.numStrings +=1;
		String taxTemp=this.taxes.getTLogString(true);
		if (taxTemp.length()>0) { //only add tax refund if there is data
			taxHex.append("2c");
			taxHex.append(taxTemp);
			this.numStrings+=1;
		}
		
		
		header.setNumString(this.numStrings); //now we can build the header
		
		StringBuilder result=new StringBuilder();
		result.append(this.header.getTLogString());
		if (numStrings>0) {
			result.append("2c");
			//assume at least an item and a tender and taxes
			result.append(itemHex.toString());
			result.append("2c");
			result.append(tenderHex.toString());
			result.append("2c");
			result.append(this.taxes.getTLogString());
			if (this.userData.size()>0) {
				result.append("2c");
				result.append(userDataHex.toString());
			}
		}
		result.append("0d0a");
		
		return result.toString();
	}
	
}
