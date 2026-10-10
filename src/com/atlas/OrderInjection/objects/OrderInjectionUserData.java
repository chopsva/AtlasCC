package com.atlas.OrderInjection.objects;

public class OrderInjectionUserData extends OrderInjectionObject {

	protected int origin=90;
	protected int subtype=99;
	protected String[] dataFields;
	protected String fieldDelim="7c"; // "|"
	
	
	public OrderInjectionUserData() {
		// TODO Auto-generated constructor stub
	}
	public OrderInjectionUserData(int subt,String[] fields) {
		this.subtype=subt;
		this.dataFields=fields.clone(); //keep our own copy not just the ref
	}
	
	@Override
	public String getTLogString() {
		StringBuilder workHexString=new StringBuilder();
		workHexString.append("22993a");
		workHexString.append(intToPackedBCD(origin));
		workHexString.append("3a");
		workHexString.append(intToPackedBCD(subtype));
		workHexString.append("3a");
		for(String field: dataFields) {
			workHexString.append(stringToHex(field));
			workHexString.append(fieldDelim);
		}
		workHexString.append("22");
		return workHexString.toString();
	}
		
	

}
