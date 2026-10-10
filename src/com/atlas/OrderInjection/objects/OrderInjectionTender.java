package com.atlas.OrderInjection.objects;

public class OrderInjectionTender extends OrderInjectionObject {
	private int tendType=0;
	private int amtTender=0;
	private int amtFee=0;
	private String customerId="";
	private int status=0;
	
	
	public OrderInjectionTender() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getTLogString() {
		StringBuilder workHexString=new StringBuilder();
		if (this.amtTender>=0) {
			workHexString.append("22053a");	
		} else {
			workHexString.append("22063a");//make Tender Correction
		}
		
		workHexString.append(intToBCD(this.tendType,2));
		workHexString.append("3a");
		workHexString.append(intToPackedBCD(Math.abs(this.amtTender)));
		workHexString.append("3a");
		workHexString.append(intToPackedBCD(Math.abs(this.amtFee)));
		workHexString.append("3a");
		if (!this.customerId.isEmpty()) {
			workHexString.append(stringToPacked(this.customerId));
		}
		workHexString.append("3a");
		workHexString.append(intToBCD(this.status,2));
		workHexString.append("22");
		return workHexString.toString();
	}
	
	public void setTendType(int v) {
		this.tendType=v;
	}
	public void setAmtTender(int v) {
		this.amtTender=v;
	}
	public void setAmtFee(int v) {
		this.amtFee=v;
	}
	public void setCustomerId(String v) {
		this.customerId=v;
	}
	public void setStatus(int v) {
		this.status=v;
	}

}
