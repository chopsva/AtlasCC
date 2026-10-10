package com.atlas.OrderInjection.objects;

public class OrderInjectionHeader extends OrderInjectionObject {

	private int terminal=0;
	private int transNum=0;
	private String dateTime="2601010800"; //temp holder 1/1/26 8am
	private int tranType=0;
	private int numString=0;
	private String operator="1"; //need better defaults
	private String pwd="1";
	private int grossPOS=0;
	private int grossNEG=0;
	private int ringTime=1;
	private int tenderTime=1;
	private int specialTime=1;
	private int inactiveTime=1;
	private int ind1=0;
	private int altPrice=0;
	private int voidTRC=0;
	
	public OrderInjectionHeader() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String getTLogString() {
		StringBuilder workHexString=new StringBuilder();
		workHexString.append("22003a");
		workHexString.append(intToBCD(this.terminal,4));
		workHexString.append("3a");
		workHexString.append(intToBCD(this.transNum,4));
		workHexString.append("3a");
		workHexString.append(this.dateTime);
		workHexString.append("3a");
		workHexString.append(intToBCD(this.tranType,2));
		workHexString.append("3a");
		workHexString.append(intToPackedBCD(this.numString));
		workHexString.append("3a");
		workHexString.append(stringToPacked(this.operator));
		workHexString.append("3a");
		workHexString.append(stringToPacked(this.pwd));
		workHexString.append("3a");
		workHexString.append(intToPackedBCD(this.grossPOS));
		workHexString.append("3a");
		workHexString.append(intToPackedBCD(this.grossNEG));
		workHexString.append("3a");
		workHexString.append(intToPackedBCD(this.ringTime));
		workHexString.append("3a");
		workHexString.append(intToPackedBCD(this.tenderTime));
		workHexString.append("3a");
		workHexString.append(intToPackedBCD(this.specialTime));
		workHexString.append("3a");
		workHexString.append(intToPackedBCD(this.inactiveTime));
		workHexString.append("3a");
		workHexString.append(intToPacked(this.ind1));
		workHexString.append("3a");
		workHexString.append(intToBCD(this.altPrice,2));
		workHexString.append("3a");
		if (this.voidTRC!=0) {
			workHexString.append(intToPackedBCD(this.voidTRC));
		}
		workHexString.append("22");
		return workHexString.toString();
	}
	
	public void setTerminal(int terminal) {
	    this.terminal = terminal;
	}

	public void setTransNum(int transNum) {
	    this.transNum = transNum;
	}

	public void setDateTime(String dateTime) {
	    this.dateTime = dateTime;
	}

	public void setTranType(int tranType) {
	    this.tranType = tranType;
	}

	public void setNumString(int numString) {
	    this.numString = numString;
	}

	public void setOperator(String operator) {
	    this.operator = operator;
	}

	public void setPwd(String pwd) {
	    this.pwd = pwd;
	}

	public void setGrossPOS(int grossPOS) {
	    this.grossPOS = grossPOS;
	}

	public void setGrossNEG(int grossNEG) {
	    this.grossNEG = grossNEG;
	}

	public void addGross(int val) {
		if (val>0) {
			this.grossPOS+=val;
		}else {
			this.grossNEG+=Math.abs(val);
		}
	}
	
	public void setRingTime(int ringTime) {
	    this.ringTime = ringTime;
	}

	public void setTenderTime(int tenderTime) {
	    this.tenderTime = tenderTime;
	}

	public void setSpecialTime(int specialTime) {
	    this.specialTime = specialTime;
	}

	public void setInactiveTime(int inactiveTime) {
	    this.inactiveTime = inactiveTime;
	}

	public void setInd1(int ind1) {
	    this.ind1 = ind1;
	}

	public void setAltPrice(int altPrice) {
	    this.altPrice = altPrice;
	}

	public void setVoidTRC(int voidTRC) {
	    this.voidTRC = voidTRC;
	}	
}	
