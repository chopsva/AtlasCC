package com.atlas.OrderInjection.egrowcery.processor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.atlas.OrderInjection.egrowcery.parser.FileParser;
import com.atlas.OrderInjection.egrowcery.parser.OrderHeader;
import com.atlas.OrderInjection.egrowcery.parser.OrderItem;
import com.atlas.OrderInjection.egrowcery.parser.OrderObject;
import com.atlas.OrderInjection.egrowcery.parser.OrderTax;
import com.atlas.OrderInjection.egrowcery.parser.OrderTender;
import com.atlas.OrderInjection.fileUtils.WriteToTLog;
import com.atlas.OrderInjection.order.OrderInjectionOrder;
import com.atlas.OrderInjection.order.OrderInjectionOrderBuilder;
import com.atlas.cc.utils.AtlasCCLogger;
import com.atlas.cc.utils.AtlasFTPSClient;
import com.atlas.cc.utils.INIFile;

public class eGrowceryProcessor {
	private static eGrowceryProcessor instance=null;
	
	private int termId=626;
	private String op="626";
	private int pluMode=0; //0-tax in PLUs 1-tax in [TaxCodes]
	//eGrowcer spec stuff
	private String fulfilmentPLU="";
	private String tipPLU="";
	private String containerPLU="";
	private String sourceDirectory=".";
	private String sourceExtension=".txt";
	private String processedDirectory="processed";
	private String errorDirectory="error";
	private int defaultDepartment=999;
	
	private String sftpHost="";
	private String sftpUsername="";
	private String sftpPassword="";
	private int sftpPort=21;
	private boolean sftpImplicit=false; //false= expl. 21  true=implicit 990
	private String sftpSourceDir="/";
	private String sftpExtension=".txt";
	private String sftpPrefix="";
	private String sftpRenameExtension=".old";

	private ScheduledExecutorService scheduler;
	private long cycleStartDelay=60000;
	private long cycleInterval=30000;
	
	private Map<String,Integer> taxMap=new HashMap<String,Integer>();
	private int defaultTaxId=0;
	
	private Map<String,Integer> tenderMap=new HashMap<String,Integer>();
	private int defaultTenderType=11;
	
	
//TODO 
//	Need Tender map
//  need tax code map 
//  need fudgefactor plu 
	
	
	public static eGrowceryProcessor getInstance() {
		if (instance==null) {
			 synchronized(eGrowceryProcessor.class) {
				 if (instance==null) {
					 instance=new eGrowceryProcessor();
				 }
			 }
		}
		return instance;
	}
	
	
	private eGrowceryProcessor() {
		readProps();
		//System.out.println(eGrowceryProcessor.instance);
		AtlasCCLogger.getInstance().info(toString());
	}

	@Override
	public String toString() {
		String ret="eGrowceryProcessor\n";
		ret +="=============================\n";
		ret +="termId:"+termId+"\n";
		ret +="op:"+op+"\n";
		ret +="pluMode:"+pluMode+"\n";
		ret +="containerPLU:"+containerPLU+"\n";
		ret +="tipPLU:"+tipPLU+"\n";
		ret +="fulfilmentPLU:"+fulfilmentPLU+"\n";
		ret +="cycleStartDelay:"+cycleStartDelay+"\n";
		ret +="cycleInterval:" + cycleInterval +"\n";
		ret +="sourceDirectory:"+sourceDirectory+"\n";
		ret +="sourceExtension:"+ sourceExtension+"\n";
		ret +="processedDirectory:"+ processedDirectory +"\n";
		ret +="errorDirectory:"+ errorDirectory+"\n";
		ret +="defaultDepartment:"+defaultDepartment+"\n";
		ret +="sftpHost:"+sftpHost+"\n";
		ret +="sftpPort:"+sftpPort+"\n";
		ret +="sftpImplicit:"+sftpImplicit+"\n";
		ret +="sftpUserName:"+sftpUsername+"\n";
		ret +="sftpPassword:"+sftpPassword+"\n";
		ret +="sftpSourceDir:"+sftpSourceDir+"\n";
		ret +="sftpExtension:"+sftpExtension+"\n";
		ret +="sftpPrefix:"+sftpPrefix+"\n";
		ret +="sftpRenameExtension:"+sftpRenameExtension+"\n";
		ret +="defaultTaxId"+defaultTaxId+"\n";
		for(Map.Entry<String,Integer> entry: taxMap.entrySet()) {
			String taxCode=entry.getKey();
			int taxId=entry.getValue();
			ret +=" taxCode '"+taxCode+"'="+taxId+"\n";
		}
		ret +="defaultTenderType:"+ defaultTenderType +"\n";
		for (Map.Entry<String,Integer> entry: tenderMap.entrySet()) {
			String tenderCode=entry.getKey();
			int tendType=entry.getValue();
			ret +=" tenderCode '"+tenderCode+"'="+tendType+"\n";
		}
		
		return ret;
	}
	public String getProcessedDirectory() {
		return processedDirectory;
	}
	public String getErrorDirectory() {
		return errorDirectory;
	}
	public String getSourceDirectory() {
		return sourceDirectory;
	}
	public String getSourceExtension() {
		return sourceExtension;
	}
	
	public int getTaxId(String taxCode) {
		AtlasCCLogger.getInstance().info("getTaxId lookup:'"+taxCode+"'");
		Integer id=taxMap.get(taxCode);
		int result=this.defaultTaxId;
		if (id!=null) {
			AtlasCCLogger.getInstance().debug("taxCode found");
			result=id.intValue();
		}
		AtlasCCLogger.getInstance().debug("returning:"+id);
		return result;
	}
	
	public int getTenderId(String tenderCode) {
		AtlasCCLogger.getInstance().debug("tenderCode lookup:'"+tenderCode+"'");
		Integer id=tenderMap.get(tenderCode);
		int result=this.defaultTenderType;
		if (id!=null) {
			result=id.intValue();
		}
		return result;
	}
	private void readProps() {
		INIFile ini; //=new INIFile();
		try {
			ini=INIFile.load("c:\\adx_idt1\\egrow.ini");
			termId=ini.getInt("head", "termId", termId);
			op=ini.get("head", "op", op);
			pluMode=ini.getInt("head", "pluMode", pluMode);
			containerPLU=ini.get("plus", "containerPLU",containerPLU);
			tipPLU=ini.get("plus", "tipPLU",tipPLU);
			fulfilmentPLU=ini.get("plus","fulfilmentPLU",fulfilmentPLU);
			sourceDirectory=ini.get("source", "sourceDirectory",sourceDirectory);
			sourceExtension=ini.get("source", "sourceExtension",sourceExtension);
			processedDirectory=ini.get("source", "processedDirectory",processedDirectory);
			errorDirectory=ini.get("source", "error",errorDirectory);
			defaultDepartment=ini.getInt("head", "defaultDepartment", defaultDepartment);
			sftpHost=ini.get("sftp","sftpHost",sftpHost);
			sftpUsername=ini.get("sftp", "sftpUsername",sftpUsername);
			sftpPassword=ini.get("sftp","sftpPassword",sftpPassword);
			sftpPort=ini.getInt("sftp", "sftpPort",sftpPort);
			sftpImplicit=Boolean.parseBoolean(ini.get("sftp", "sftpImplicit"));
			sftpSourceDir=ini.get("sftp", "sftpSourceDir",sftpSourceDir);
			sftpExtension=ini.get("sftp", "sftpExtension",sftpExtension);
			sftpPrefix=ini.get("sftp", "sftpPrefix",sftpPrefix);
			sftpRenameExtension=ini.get("sftp", "sftpRenameExtension",sftpRenameExtension);
			cycleStartDelay = ini.getInt("head", "cycleStartDelay", (int)cycleStartDelay);
			cycleInterval = ini.getInt("head", "cycleInterval", (int)cycleInterval);
			int numTax=ini.getInt("taxes","numEntries", 0);
			AtlasCCLogger.getInstance().debug("numEntres:"+numTax);
			for(int i=0;i<numTax;i++) {
				AtlasCCLogger.getInstance().debug("lookup "+ "tax"+(i+1));
				String tmp=ini.get("taxes","tax"+(i+1),"");
				if (!tmp.isEmpty()) {
					String[] parts=tmp.split(",");
					taxMap.put(parts[0].trim(), Integer.parseInt(parts[1]));
				}
			}
			defaultTaxId=ini.getInt("taxes","defaultTaxId", defaultTaxId);
			for(int i=1;i<21;i++) {
				String tmp=ini.get("tenders", String.valueOf(i),"");
				if (!tmp.isEmpty()) {
					tenderMap.put(String.valueOf(i), Integer.parseInt(tmp));
				}
			}
			defaultTenderType=ini.getInt("head", "defaultTenderType", defaultTenderType);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public OrderObject parseFile(String filename) {
		FileParser fp=new FileParser(filename,pluMode); //this auto starts parsing 
		System.out.println("PARSE RESULT:"+fp.getParseResult());
		return fp.getOrder();
		
	}
	
	public OrderInjectionOrder buildOrder(OrderObject order) {
		OrderInjectionOrderBuilder builder=new OrderInjectionOrderBuilder();
		OrderHeader head=order.getHeader();
		List<OrderItem> items=order.getItems();
		List<OrderTender> tenders=order.getTenders();
		List<OrderTax> taxes=order.getTaxes();
		
		builder.initOrder(termId, 0,op,null);
		builder.setDefaultDepartment(this.defaultDepartment); //if not found use this dept
		//items
		for(OrderItem item : items) {
			//eGrow does not flag if weight so we need to check the itemfile for scale flag
			if (builder.getEAMITEMR().getScaleFlag(item.UPC)) {
				System.out.println(item.UPC+" scale:true");
				builder.addItem(item.UPC,item.LineExtn,0,item.Quantity,true,builder.getEAMITEMR().getTaxes(item.UPC));
			} else {
				System.out.println(item.UPC+" scale:false");
				builder.addItem(item.UPC,item.LineExtn,0,item.Quantity/100,false,builder.getEAMITEMR().getTaxes(item.UPC));
			}
		}
		if (head.FulfilmentTotal!=0 && !fulfilmentPLU.isEmpty()) {
			builder.addItem(fulfilmentPLU,head.FulfilmentTotal,0,1,false,"");
		}
		if (head.ContainerDepositAmount!=0 && !containerPLU.isEmpty()) {
			builder.addItem(containerPLU,head.ContainerDepositAmount,0,1,false,"");
		}
		if (head.TipAmount!=0 && !tipPLU.isEmpty()) {
			builder.addItem(tipPLU,head.TipAmount,0,1,false,"");
		}

		//tax here
		for(OrderTax tax: taxes) {
			builder.addTax(tax.taxCode, tax.taxAmt,tax.taxableAmt);
		}
		
		//tenders
		for(OrderTender tend: tenders) {
			//builder.addTender(tend.PaymentType,tend.Amount);
			builder.addTender(tend.PaymentType,tend.Amount);
		}
		builder.getOrder().calcGross(); //final step
		
		return builder.getOrder();
	}
	
	public void getFilesFromHost() {
		AtlasFTPSClient client=new AtlasFTPSClient(true);
		try {
			client.connect(this.sftpHost,this.sftpPort,this.sftpUsername, this.sftpPassword);
			String[] files=client.getFiles(this.sftpSourceDir, this.sftpPrefix, this.sftpExtension);
			String workSourceDir=this.sftpSourceDir;
			if (!workSourceDir.endsWith("/")) {
				workSourceDir +="/";
			}
			String workDestDir=this.sourceDirectory;
			if (!workDestDir.endsWith("/")) {
				workDestDir +="/";
			}
			for(String file : files) {
				client.downloadFile(workSourceDir+file, workDestDir+file);
				client.renameFile(workSourceDir+file,workSourceDir+file+".old");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			client.disconnect();
		}
	}
	
	public void startProcessor() {
		scheduler= Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleWithFixedDelay(
				new Runnable() {
					@Override
					public void run() {
						try {
							processCycle();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				},
				cycleStartDelay, cycleInterval, TimeUnit.MILLISECONDS);
		
	}
	
	public void processCycle() {
		System.out.println("In processCycle");
		System.out.println("GETTING FILES");
		eGrowceryProcessor.getInstance().getFilesFromHost();
		System.out.println("CREATING FILE PROCESSOR");
		eGrowceryFileProcessor p=new eGrowceryFileProcessor();
		System.out.println("PROCESSING FILES");
		p.processFiles();
		System.out.println("DONE");

	}

	public static void mainOLD(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(eGrowceryProcessor.getInstance());
		OrderObject order= eGrowceryProcessor.getInstance().parseFile(args[0]);
		System.out.println("eg order:");
		System.out.println(order);
		OrderInjectionOrder ojOrder=eGrowceryProcessor.getInstance().buildOrder(order);
		System.out.println("ORDER:");
		System.out.println(ojOrder.getOrderHexString());
		WriteToTLog.writeHex(ojOrder.getOrderHexString());
	}
	
	public static void mainOLD2(String[] args) {
		System.out.println("GETTING FILES");
		eGrowceryProcessor.getInstance().getFilesFromHost();
		System.out.println("CREATING FILE PROCESSOR");
		eGrowceryFileProcessor p=new eGrowceryFileProcessor();
		System.out.println("PROCESSING FILES");
		p.processFiles();
		System.out.println("DONE");
		
	}
	
	public static void main(String[] args) {
		System.out.println("Starting processor");
		eGrowceryProcessor.getInstance().startProcessor();
	}
}

