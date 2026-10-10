package com.atlas.OrderInjection.objects;

import com.atlas.cc.utils.AtlasCCLogger;

public class OrderInjectionItem extends OrderInjectionObject {

	private String itemCode="";
	private int extPrice=0;
	private int department=0;
	private int ind1=0;
	private int ind2=0;
	private int ind3=0;
	private int ind4=0;
	private int ordinal=0;
	private int qty=0;
	private int weight=0;
	private int tare=0;
	
	private String familyCodes="000000";
	
	private boolean needExtRecord=false;
	
	//TODO deal with ItemExtension entry
	//     MAke MiscTans item entries
		
	public OrderInjectionItem() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String getTLogString() {
		ind2=setBit(ind2,1,getNeedExtRecord());
		ind2=setBit(ind2,6,(this.extPrice<0)); //set REFUND if neg
		//deal with scale
		if (this.weight!=0) {
			//ind1=setBit(ind1,15,true);
		}
		int workInd3=ind3;
		if (this.extPrice<0) {
			AtlasCCLogger.getInstance().info("flipping "+this.itemCode +" Wind3  b4:"+workInd3);
			if (workInd3/10==5) {
				AtlasCCLogger.getInstance().info("its a 5");
				workInd3 -=10;
			} else {
				if (workInd3/10==4) {
					AtlasCCLogger.getInstance().info("its a 4");
					workInd3 +=10;
				}
			}
			AtlasCCLogger.getInstance().info("  aft:"+workInd3);
		}
		if ((workInd3/10)==0) {
			if (this.extPrice<0) {
				workInd3 +=20; //change to refund item (duh)
			}
		}
		
		StringBuilder workHexString=new StringBuilder();
		workHexString.append("22013a");
		workHexString.append(stringToPacked(this.itemCode));
		workHexString.append("3a");
		workHexString.append(intToPackedBCD(Math.abs(this.extPrice))); ///NEG if refund
		workHexString.append("3a");
		workHexString.append(intToPackedBCD(this.department));
		//workHexString.append("3a0000003a"); //fam code
		workHexString.append("3a"+this.familyCodes+"3a");
		workHexString.append(intToPackedBCD(this.ind1));
		workHexString.append("3a");
		workHexString.append(intToPackedBCD(this.ind2));
		workHexString.append("3a");
		//workHexString.append(intToBCD(this.ind3,2));
		workHexString.append(intToBCD(workInd3,2));
		workHexString.append("3a");
		workHexString.append(intToPackedBCD(this.ind4));
		workHexString.append("3a");
		workHexString.append(intToPacked(this.ordinal));
		workHexString.append("3a22"); //no void ordinal
		
		return workHexString.toString();
	}
	public String getItemExtTLogString() {
		StringBuilder workHexString=new StringBuilder();
		workHexString.append("22023a");
		workHexString.append("003a"); //always use mp=0
		workHexString.append("3a"); //keep DEALQUAN blank
		workHexString.append("F03a"); // force PM=0
		workHexString.append("f13a"); // assume SALEQUAN of 1
		int salePric=this.extPrice;
		if (Math.abs(this.qty)>1) {
			salePric=salePric/Math.abs(this.qty);
		} else {
			if (this.weight!=0) {
				salePric=(salePric*100)/Math.abs(this.weight);
			}
		}
		//to handle neg -> refund
		salePric=Math.abs(salePric);
		workHexString.append(intToPackedBCD(salePric));
		workHexString.append("3a");
		int QWV=Math.abs(this.qty);
		if (this.weight!=0) {
			QWV=Math.abs(this.weight);
		}
		workHexString.append(intToPackedBCD(QWV));
		workHexString.append("3a");
		int tmpInd1=0;
		if (this.qty>1) {
			tmpInd1 |=0x20;
		}
		if (this.weight!=0) {
			tmpInd1 |=0x40;
		}
		if (this.tare>0 ){
			tmpInd1 |=0x10;
		}
		workHexString.append(intToPackedBCD(tmpInd1));
		workHexString.append("3a");
		workHexString.append(intToPacked(this.tare));
		workHexString.append("22");
		
		return workHexString.toString();
	}
	
	public void setItemCode(String itemCode) {
	    this.itemCode = itemCode;
	}

	public void setExtPrice(int extPrice) {
	    this.extPrice = extPrice;
	}

	public void setDepartment(int department) {
	    this.department = department;
	}

	public void setInd1(int ind1) {
	    this.ind1 = ind1;
	}

	public void setInd2(int ind2) {
	    this.ind2 = ind2;
	}

	public void setInd3(int ind3) {
	    this.ind3 = ind3;
	}

	public void setInd4(int ind4) {
	    this.ind4 = ind4;
	}
	public int getInd1() {
		return this.ind1;
	}
	public int getInd2() {
		return this.ind2;
	}
	public int getInd3() {
		return this.ind3;
	}
	public int getInd4() {
		return this.ind4;
	}

	public void setOrdinal(int ordinal) {
	    this.ordinal = ordinal;
	}	
	
	public void setFamilyCodes(String famCode) {
		if (famCode!=null) {
			this.familyCodes=String.format("%6s", famCode).replace(' ', '0');
		}
	}
	
	public boolean getNeedExtRecord() {
		if (Math.abs(this.qty)>1 || this.weight!=0) {
			this.needExtRecord=true;
		} else {
			this.needExtRecord=false;
		}
		return this.needExtRecord;
	}
	
	public int getExtPrice() {
		return this.extPrice;
	}

	public int setBit(int flagInt,int index,boolean value) {
		if (index<0 || index>31) {
			throw  new IllegalArgumentException("Bit index must be between 0 and 31");
	    }

	    int mask = 1 << index;

	    if (value) {
	        return flagInt | mask;   // Set bit to 1
	    } else {
	        return flagInt & ~mask;  // Clear bit to 0
	    }
	}
		
	
	//this index is 1-8 not 0-7
	public void setTaxable(int index,boolean value) {
		System.out.println("setTax "+index+":"+value);
		
		if (index>4) {
			setTaxableExt(index,value);
		}
		System.out.println("ind1 b4:"+this.ind1);
		int workInd1=this.ind1;
		switch (index) {
			case 1:
				workInd1=setBit(workInd1,7,value);
				break;
			case 2:
				workInd1=setBit(workInd1,6,value);
				break;
			case 3:
				workInd1=setBit(workInd1,5,value);
				break;
			case 4:
				workInd1=setBit(workInd1,4,value);
				break;
		}
		this.ind1=workInd1;
		System.out.println("ind1 aft:"+this.ind1);
	}
	
	public void setFoodstamp(boolean value) {
		System.out.println("setfs "+value+"  ind1 b4:"+this.ind1);
		this.ind1=setBit(this.ind1,3,value);
		System.out.println("      ind1 aft:"+this.ind1);
	}
	
	protected void setTaxableExt(int index,boolean value) {
		int workInd4=this.ind4;
		switch (index) {
			case 1:
				workInd4=setBit(workInd4,7,value);
				break;
			case 2:
				workInd4=setBit(workInd4,6,value);
				break;
			case 3:
				workInd4 =setBit(workInd4,5,value);
				break;
			case 4:
				workInd4=setBit(workInd4,4,value);
				break;
		}
		this.ind4=workInd4;
	}
	
	public void setQty(int q) {
		this.qty=q;
	}
	public void setWeight(int w) {
		System.out.println("set WEIGHT!");
		int workInd1=this.ind1;
		this.weight=w;
		System.out.println("ind1 b4:"+this.ind1);
		workInd1=setBit(workInd1,14,true);
		System.out.println("ind1 aft:"+workInd1);
		this.ind1=workInd1;
	}
	
}
