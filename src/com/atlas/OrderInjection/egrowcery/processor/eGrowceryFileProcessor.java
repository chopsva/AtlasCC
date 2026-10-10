package com.atlas.OrderInjection.egrowcery.processor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.atlas.OrderInjection.egrowcery.parser.OrderObject;
import com.atlas.OrderInjection.fileUtils.WriteToTLog;
import com.atlas.OrderInjection.order.OrderInjectionOrder;
import com.atlas.cc.utils.AtlasCCLogger;

public class eGrowceryFileProcessor {

	private final String path;
	private final String extension;
	
	public eGrowceryFileProcessor() {
		this.path=eGrowceryProcessor.getInstance().getSourceDirectory();
		String extend=eGrowceryProcessor.getInstance().getSourceExtension();
		if (extend.startsWith(".")) {
			this.extension=extend.toLowerCase();
		} else {
			this.extension="."+extend;
		}
	}

	public void processFiles() {
		File directory=new File(path);
		if (!directory.exists()||!directory.isDirectory()) {
			System.out.println("Invalid path:"+path);
			return;
		}
		
		File processedDir=new File(directory,eGrowceryProcessor.getInstance().getProcessedDirectory());
		File errorDir=new File(directory,eGrowceryProcessor.getInstance().getErrorDirectory());
		
		//make dirs if not there
		processedDir.mkdirs();
		errorDir.mkdirs();
		
		File[] files=directory.listFiles();
		if (files==null) {
			System.out.println("Unable to read files:"+path);
			return;
		}
		for(File file : files) {
			if (!file.isFile()) {
				continue;
			}
			if (!file.getName().toLowerCase().endsWith(extension)) {
				continue;
			}
			processFile(file,processedDir,errorDir);
		}
	}
	
	private void processFile(File file,File processedDir,File errorDir) {
		AtlasCCLogger.getInstance().debug("processing:"+file.getAbsolutePath());
		boolean result;
		
		try{
			OrderObject order= eGrowceryProcessor.getInstance().parseFile(file.getAbsolutePath());
			System.out.println("eg order:");
			System.out.println(order);
			OrderInjectionOrder ojOrder=eGrowceryProcessor.getInstance().buildOrder(order);
			System.out.println("ORDER:");
			System.out.println(ojOrder.getOrderHexString());
			WriteToTLog.writeHex(ojOrder.getOrderHexString());
			result=true;
		} catch (Exception e) {
			System.out.println("Exception processing:"+file.getName());
			e.printStackTrace();
			result=false;			
		}
		if (result) {
			moveFile(file,processedDir);
		} else {
			moveFile(file,errorDir);
		}
	}
	
	private void moveFile(File file,File destDir) {
		File destinationFile=new File(destDir,file.getName());
		try {
			Files.move(file.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			System.out.println("moved to:"+destinationFile.getAbsolutePath());
		} catch (Exception e) {
			System.out.println("unable to move file:"+file.getAbsolutePath());
		}
		
	}
	
	public static void main(String[] args) {
		eGrowceryFileProcessor p=new eGrowceryFileProcessor();
		p.processFiles();
	}
}
