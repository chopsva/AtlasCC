package com.atlas.OrderInjection.tests;

import com.atlas.OrderInjection.fileUtils.WriteToTLog;
import com.atlas.OrderInjection.objects.OrderInjectionHeader;
import com.atlas.OrderInjection.objects.OrderInjectionItem;
import com.atlas.OrderInjection.objects.OrderInjectionTax;
import com.atlas.OrderInjection.objects.OrderInjectionTender;
import com.atlas.OrderInjection.order.OrderInjectionOrder;
import com.atlas.OrderInjection.order.OrderInjectionOrderBuilder;

public class BuildTransacitonHexTest {

	public BuildTransacitonHexTest() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OrderInjectionItem item=new OrderInjectionItem();
		item.setItemCode("100");
		item.setDepartment(10);
		item.setExtPrice(395);
		item.setInd3(1); // normal-keyed
		item.setOrdinal(1);
		item.setInd1(0x80); //tax A
		item.setInd2(8); //fs
		System.out.println("ITEM:");
		System.out.println(item.getTLogString());
		
		OrderInjectionTender tnd=new OrderInjectionTender();
		tnd.setTendType(11);
		tnd.setAmtTender(400);
		System.out.println("TEND:");
		System.out.println(tnd.getTLogString());
			
		OrderInjectionTax tax=new OrderInjectionTax();
		tax.setAmtTax(0, 5);
		tax.setTaxableAmt(0, 395);
		System.out.println("TAX:");
		System.out.println(tax.getTLogString());
		
		OrderInjectionHeader head=new OrderInjectionHeader();
		head.setTerminal(600);
		head.setTransNum(42);
		head.setDateTime("2607242345");
		head.setTranType(0);
		head.setNumString(3);
		head.setOperator("626");
		head.setPwd("626");
		head.setGrossPOS(395);
		System.out.println("HEADER:");
		System.out.println(head.getTLogString());
		
		//part 2 of test 
		OrderInjectionOrder theOrder=new OrderInjectionOrder();
		theOrder.setHeaderObject(head);
		theOrder.addItemObject(item);
		theOrder.addTenderObject(tnd);
		theOrder.setTaxObject(tax);
		System.out.println("ORDER:");
		System.out.println(theOrder.getOrderHexString());
		
		//part 3 of test
		OrderInjectionOrderBuilder builder=new OrderInjectionOrderBuilder();
		builder.initOrder(600, 0,"626",null); //trans of 0 means next trans, null for time->current
		builder.addItem("100",790,10,2,false,"2F");
		builder.addTax(1,10,790);
		String taxCodes=builder.getEAMITEMR().getTaxes("101"); //this gets the item taxes from ace for upc 101
		builder.addItem("101",395,0,1,false,taxCodes); //leave dept 0 to have lookup 
		builder.addTax(1,5,395);
		builder.addItem("4011",100,0,400,true,"");
		
		builder.addTender(11,1300);
		String[] ud={"FIELD1","8675309","ou812"};
		builder.addUserData(99, ud);
		
		//builder.addTax(1, 10, 790);
		builder.finalizeOrder();
		System.out.println("build:");
		System.out.println(builder.getOrder().getOrderHexString());
		
		//part 4
		WriteToTLog.writeHex(builder.getOrder().getOrderHexString());
		
	}

}
