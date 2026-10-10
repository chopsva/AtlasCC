package com.atlas.OrderInjection.order;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.atlas.OrderInjection.fileUtils.AtlasEAMITEMR;
import com.atlas.OrderInjection.fileUtils.TransNumStorage;
import com.atlas.OrderInjection.objects.OrderInjectionItem;
import com.atlas.OrderInjection.objects.OrderInjectionTax;
import com.atlas.OrderInjection.objects.OrderInjectionTender;
import com.atlas.OrderInjection.objects.OrderInjectionUserData;

//helpers to make building an order simpler
//populates GrossPOS/GrossNEG when items added....
public class OrderInjectionOrderBuilder {
	
	protected OrderInjectionOrder theOrder;
	protected AtlasEAMITEMR atlasEAMITEMR;
	private int defaultDepartment=0;

	public OrderInjectionOrderBuilder() {
		// TODO Auto-generated constructor stub
		this.theOrder=new OrderInjectionOrder();
		this.atlasEAMITEMR=new AtlasEAMITEMR();
	}
	
	public void setDefaultDepartment(int dept) {
		this.defaultDepartment=dept;
	}
	//dept of 0 will result in attempt to lookup the item
	public void addItem(String UPC,int extRetail,int dept) {
		OrderInjectionItem itm=new OrderInjectionItem();
		itm.setItemCode(UPC);
		itm.setExtPrice(extRetail);
		if (dept!=0) {
			itm.setDepartment(dept);
		} else {
			itm.setDepartment(this.atlasEAMITEMR.getDept(UPC));
		}
		this.theOrder.addItemObject(itm);
		if (extRetail>0) {
			this.theOrder.getHeaderObject().addGross(extRetail);
		}
	}
	
	public void addItem(String UPC,int extRetail,int dept, int qty, boolean weight) {
		addItem(UPC,extRetail,dept,qty,weight,null);
	}
	
	public void addItem(String UPC,int extRetail,int dept, int qty, boolean weight, String taxCodes) {
		addItem(UPC,extRetail,dept,qty,weight,taxCodes,null, null);
	}
	
	//taxcodes also sets F/S with "F"- blank clears all flags
	public void addItem(String UPC,int extRetail,int dept, int qty, 
						boolean weight, String taxCodes,String ind3,String famCodes) {
		OrderInjectionItem itm=new OrderInjectionItem();
		itm.setItemCode(UPC);
		itm.setExtPrice(extRetail);
		//itm.setDepartment(dept);
		if (dept!=0) {
			itm.setDepartment(dept);
		} else {
			int tmpDept=this.atlasEAMITEMR.getDept(UPC);
			if (tmpDept==0) {
				tmpDept=this.defaultDepartment;
			}
			itm.setDepartment(tmpDept);
		}
		
		if (ind3==null) {
			int tmpInd3=this.atlasEAMITEMR.getItemType(UPC);
			itm.setInd3(tmpInd3*10);
			//System.out.println("set ind3:"+tmpInd3*10);
		}else{
			itm.setInd3(Integer.parseInt(ind3));
		}
		if (famCodes==null) {
			itm.setFamilyCodes(this.atlasEAMITEMR.getFamilyCodes(UPC));
		} else {
			itm.setFamilyCodes(famCodes);
		}
		
		//TODO qty/weigt
		if (qty!=1) {
			if (weight)
				itm.setWeight(qty);
			else
				itm.setQty(qty);
		}
		if (taxCodes!=null) {
			for(int t=1;t<9;t++) {
				if (taxCodes.contains(String.valueOf(t))) {
					itm.setTaxable(t,true);
				} else {
					itm.setTaxable(t,false);
				}
			}
			if (taxCodes.toLowerCase().contains("f")) {
				itm.setFoodstamp(true);
			} else {
				itm.setFoodstamp(false);
			}
		}
	
			
		
		this.theOrder.addItemObject(itm);
	}
	
	public void addItem(OrderInjectionItem itm) {
		this.theOrder.addItemObject(itm);
	}

	public void addTender(int tendType,int amt) {
		OrderInjectionTender tnd=new OrderInjectionTender();
		tnd.setTendType(tendType);
		tnd.setAmtTender(amt);
		this.theOrder.addTenderObject(tnd);
	}
	public void addTender(int tendType,int amt,String data) {
		OrderInjectionTender tnd=new OrderInjectionTender();
		tnd.setTendType(tendType);
		tnd.setAmtTender(amt);
		tnd.setCustomerId(data);
		this.theOrder.addTenderObject(tnd);
	}
	public void addTender(OrderInjectionTender tnd) {
		this.theOrder.addTenderObject(tnd);
	}
	
	public void setTax(int index,int amt, int sales) {
		this.theOrder.getTaxObject().setAmtTax(index, amt);
		this.theOrder.getTaxObject().setTaxableAmt(index, sales);
	}
	public void setTax(OrderInjectionTax tx) {
		this.theOrder.setTaxObject(tx);
	}
	public void addTax(int index,int amt,int sales) {
		this.theOrder.getTaxObject().addAmtTax(index, amt);
		this.theOrder.getTaxObject().addTaxableAmt(index, sales);
	}

	public void addUserData(int subtype,String[] fields) {
		OrderInjectionUserData oiud=new OrderInjectionUserData(subtype,fields);
		this.theOrder.addUserDataObject(oiud);
	}
	
	//if trans is 0,class will lookup next number. if non-0 that will used but not written to storage
	public void initOrder(int term,int trans) {
		initOrder(term,trans,null,null);
	}
	public void initOrder(int term,int trans,String op) {
		initOrder(term,trans,op,null);
	}
	
	public void initOrder(int term,int trans,String op,String time) {
		this.theOrder.getHeaderObject().setTerminal(term);
		if (trans!=0) {
			this.theOrder.getHeaderObject().setTransNum(trans);
		} else {
			setNextTransactionNumber();
		}
		if(op!=null) { //assume id/pwd match
			this.theOrder.getHeaderObject().setOperator(op);
			this.theOrder.getHeaderObject().setPwd(op);
		}
		if(time!=null) {
			this.theOrder.getHeaderObject().setDateTime(time);
		} else {
			this.theOrder.getHeaderObject().setDateTime(new SimpleDateFormat("yyMMddHHmm").format(new Date()));
		}
	}
	//if using shorcut- calc gross
	public void finalizeOrder() {
		finalizeOrder(true);
	}
	
	public void finalizeOrder(boolean processGross) {
		if (processGross) {
			this.theOrder.calcGross();
		}
	}
	
	public OrderInjectionOrder getOrder() {
		return this.theOrder;
	}
	
	public AtlasEAMITEMR getEAMITEMR() {
		return this.atlasEAMITEMR;
	}
	
	public void setNextTransactionNumber() {
		TransNumStorage tns=new TransNumStorage(); //use default name
		try {
			int trans=(int) tns.incrementAndGet();
			this.theOrder.getHeaderObject().setTransNum(trans);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
