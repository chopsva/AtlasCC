package com.atlas.OrderInjection.objects;

import com.atlas.cc.utils.AtlasCCLogger;

public class OrderInjectionTax extends OrderInjectionObject {
	
	public class TaxValues {
		public int amtTax=0;
		public int taxableAmt=0;
	}
	private TaxValues[] taxes;
	
	public OrderInjectionTax() {
		// TODO Auto-generated constructor stub
		this.taxes=new TaxValues[8];
		for(int i=0;i<taxes.length;i++) {
			this.taxes[i]=new TaxValues();
		}
	}
	
	@Override
	public String getTLogString() {
		return getTLogString(false);
	}
	
	public String getTLogString(boolean refunds) {
		StringBuilder workHexString=new StringBuilder();
		
		//workHexString.append("2207"); //3a added below
		if (refunds) {
			workHexString.append("2208");
		} else {
			workHexString.append("2207");
		}
		int runTaxTotal=0;
		for(int i=1;i<5;i++) {
			workHexString.append("3a");
			//workHexString.append(intToPackedBCD(this.taxes[i].amtTax));
			workHexString.append(intToPackedBCD(getRefundAmtTax(i,refunds)));
			runTaxTotal=getRefundAmtTax(i,refunds);
		}
		for(int i=1;i<5;i++) {
			workHexString.append("3a");
			//workHexString.append(intToPackedBCD(this.taxes[i].taxableAmt));
			workHexString.append(intToPackedBCD(getRefundTaxableAmt(i,refunds)));
		}
		for(int i=4;i<9;i++) {
			workHexString.append("3a");
			//workHexString.append(intToPackedBCD(this.taxes[i].amtTax));
			workHexString.append(intToPackedBCD(getRefundAmtTax(i,refunds)));
			runTaxTotal=getRefundAmtTax(i,refunds);
		}
		for(int i=4;i<9;i++) {
			workHexString.append("3a");
			//workHexString.append(intToPackedBCD(this.taxes[i].taxableAmt));
			workHexString.append(intToPackedBCD(getRefundTaxableAmt(i,refunds)));
		}
		
		workHexString.append("22");
		if (refunds && runTaxTotal==0) {
			workHexString.setLength(0); //clear b/c no refund taxes
		}
		
		return workHexString.toString();
	}
	
	//if refund=false, return only POS tax numbers, if TRUE return only NEG values
	private int getRefundAmtTax(int index, boolean refund) {
		index -=1; //'1'=TAXA but array 0 
		int value=0;
		if (refund) {
			if (this.taxes[index].amtTax<0) {
				value=Math.abs(this.taxes[index].amtTax);
			}
		} else {
			value=this.taxes[index].amtTax;
		}
		return value;
	}
	private int getRefundTaxableAmt(int index, boolean refund) {
		index -=1;
		int value=0;
		if (refund) {
			if (this.taxes[index].taxableAmt<0) {
				value=Math.abs(this.taxes[index].taxableAmt);
			}
		} else {
			value=this.taxes[index].taxableAmt;
		}
		
		
		return value;
	}
	
	
	public void setAmtTax(int index,int amt) {
		index -=1;
		if (index<this.taxes.length)
			this.taxes[index].amtTax=amt;
	}
	public void setTaxableAmt(int index,int amt) {
		index -=1;
		if (index<this.taxes.length)
			this.taxes[index].taxableAmt=amt;
	}
	public void addAmtTax(int index,int amt) {
		AtlasCCLogger.getInstance().debug("addAmtTax called with:"+index);
		index -=1;
		AtlasCCLogger.getInstance().debug("using index:"+index);
		if (index<this.taxes.length)
			this.taxes[index].amtTax +=amt;
	}
	public void addTaxableAmt(int index,int amt) {
		index -=1;
		if (index<this.taxes.length)
			this.taxes[index].taxableAmt +=amt;
	}
	
	public int getAmtTax(int index) {
		AtlasCCLogger.getInstance().debug("getAmtTax called with:"+index);
		index -=1;
		AtlasCCLogger.getInstance().debug("using index:"+index);
		int val=0;
		if (index<this.taxes.length) {
			val=this.taxes[index].amtTax;
		}
		return val;
	}
}
