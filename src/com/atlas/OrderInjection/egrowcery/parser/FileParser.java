package com.atlas.OrderInjection.egrowcery.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;

import com.atlas.OrderInjection.egrowcery.processor.eGrowceryProcessor;
import com.ibm.jsse2.util.e;

public class FileParser {

	private String filename="";
	private String lastSection="";
	private OrderObject theOrder;
	static private int pluFormat=0; //0- tax in plu, 1- tax in [TAX CODES]
	private boolean parseResult=true;
	
	
	public FileParser() {
		// TODO Auto-generated constructor stub
	}
	
	public FileParser(String filename) {
		this(filename,0);
	}
	
	public FileParser(String filename,int pluFormat) {
		this.filename=filename;
		this.pluFormat=pluFormat;
		parseResult=parseFile();
	}
	
	public boolean getParseResult() {
		return this.parseResult;
	}
	
	
	public boolean parseFile() {
		boolean result=false;
		this.theOrder=new OrderObject();
		BufferedReader reader;
		try {
			reader=new BufferedReader(new FileReader(this.filename));
		} catch (IOException e) {
			e.printStackTrace();
			return result;
		}
		
		try (BufferedReader autoCloseReader = reader) {
			String line;
			
			while ((line=autoCloseReader.readLine())!=null) {
				processLine(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return result;
		}
			
		result=true; //made it this far w/o error
		
		
		return result;
	}
	
	public void processLine(String line) {
		if (line.startsWith("[")) {
			processSection(line);
		} else {
			line=line+" ";
			switch (this.lastSection.toUpperCase()) {
			case "HEADER":
				processHeader(line);
				break;
			case "TAXCODES":
				processTaxCodes(line);
				break;
			case "PLUS":
				processPLUs(line);
				break;
			case "FINALISERS":
				processFinals(line);
				break;
			default:
				System.out.println("Unknown section line");
				System.out.println(line);
				break;
			}
		}
	}

	private int DecimalToInt(String val) {
		System.out.println("DecimalToInt:"+val.trim()+":");
		int out=0;
		try {
			out=new BigDecimal(val.trim()).movePointRight(2).intValueExact();	
		} catch (Exception e) {
			//
		}
		return out; 
	}
	
	private int StringToInt(String val) {
		int out=0;
		try {
			out=Integer.parseInt(val);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return out;
	}
	
	public void processSection(String line) {
		System.out.println("processSection:"+line);
		int open=line.indexOf('[');
		int close=line.indexOf(']');
		if (open>=0 && close>open) {
			this.lastSection=line.substring(open+1,close);
		}
		System.out.println("Section:"+this.lastSection);
	}
	
	public void processHeader(String line) {
		System.out.println("processHeader:"+line);
		String[] fields=line.split(",");
		OrderHeader head=new OrderHeader();
		head.IDNumber=fields[0];
		head.OrderSubmitted=fields[1];
		head.ItemCount=StringToInt(fields[2].trim());
		head.SaleTotal=DecimalToInt(fields[3]);
		head.TaxTotal=DecimalToInt(fields[4]);
		head.LoyaltyID=fields[5];
		head.FulfilmentTotal=DecimalToInt(fields[6]);
		head.OrderCompleted=fields[7];
		head.TipAmount=DecimalToInt(fields[8]);
		head.ContainerDepositAmount=DecimalToInt(fields[9]);
		head.CostPlusAmount=DecimalToInt(fields[10]);
		this.theOrder.setHeader(head);
	}
	
	public void processPLUs(String line) {
		System.out.println("processPLUs:"+line);
		String[] fields=line.split(",");
		//need to set pluMode ourself 
		if (fields.length<10) {
			pluFormat=1;
		} else {
			pluFormat=0;
		}
		
		OrderItem item=new OrderItem();
		item.LineNumber=StringToInt(fields[0].trim());
		item.ItemNo=fields[1];
		//item.UPC=fields[2];
		item.UPC=fields[2].replaceFirst("^0+","");
		item.Description=fields[3];
		item.Sell=DecimalToInt(fields[4]);
		item.Quantity=DecimalToInt(fields[5]);
		item.Buy=DecimalToInt(fields[6]);
		if (pluFormat==0) {
			item.TaxRate=DecimalToInt(fields[7]);
			item.TaxExtn=DecimalToInt(fields[8]);
			item.LineExtn=DecimalToInt(fields[9]);
			item.Tare=DecimalToInt(fields[10].trim());
		}
		if (pluFormat==1) { //no item level taxes
			item.LineExtn=DecimalToInt(fields[7].trim());
			item.Tare=DecimalToInt(fields[8].trim());
		}
		this.theOrder.addItem(item);
	}
	
	public void processFinals(String line) {
		System.out.println("processFinals:"+line);
		String[] fields=line.split(",");
		OrderTender tend=new OrderTender();
		tend.LineNumber=StringToInt(fields[0].trim());
		tend.PaymentType=eGrowceryProcessor.getInstance().getTenderId(fields[1].trim());
		//tend.PaymentType=StringToInt(fields[1].trim());
		tend.Identifier=fields[2];
		tend.Amount=DecimalToInt(fields[3]);
		this.theOrder.addTender(tend);
	}
	
	public void processTaxCodes(String line) {
		System.out.println("processTaxCodes:"+line);
		String[] fields=line.split(",");
		//int code=StringToInt(fields[0].trim());
		int code=eGrowceryProcessor.getInstance().getTaxId(fields[0].trim());
		int able=DecimalToInt(fields[1].trim());
		int amt=DecimalToInt(fields[2].trim());
		if (code!=0) {
			this.theOrder.addTax(code, able, amt);
		}
	}
	
	public OrderObject getOrder() {
		return this.theOrder;
	}
	

	public static void main(String[] args) {
		if (args.length<1) {
			System.out.println("to test, give filename");
			System.exit(0);
		}
		System.out.println("arg len:"+args.length);
		if (args.length>1) {
			pluFormat=Integer.parseInt(args[1]);
			System.out.println("PLU FORMAT:"+pluFormat);
		}		
		FileParser fp=new FileParser(args[0]);
		

		System.out.println(fp.theOrder);
		
	}
}
