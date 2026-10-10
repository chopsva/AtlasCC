package com.atlas.cc.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;

public class AtlasFTPSClient {

	private final FTPSClient ftp;
	
	public AtlasFTPSClient(boolean implicit) {
		//true- implicit port 990, false- explicit port 21
		ftp=new FTPSClient("TLS",implicit);
	}

	@SuppressWarnings("deprecation")
	public void connect(String host, int port, String username, String password) throws IOException {
		ftp.setConnectTimeout(30000);
		ftp.setDefaultTimeout(30000);
		ftp.setDataTimeout(30000);
		
		ftp.setEndpointCheckingEnabled(true);
		ftp.connect(host,port);

		if (!ftp.login(username, password)) {
			throw new IOException("FTP Login failed:"+ftp.getReplyString());
		}
		
		ftp.execPBSZ(0);
		ftp.execPROT("P");
		ftp.enterLocalPassiveMode();
		ftp.setFileType(FTP.BINARY_FILE_TYPE);
	}
	
	public void listFiles(String remoteDirectory) throws IOException {
		FTPFile[] files=ftp.listFiles(remoteDirectory);
		for (int i=0;i<files.length;i++) {
			FTPFile file=files[i];
			if (file.isFile()) {
				System.out.println("FILE:"+file.getName()+" size:"+file.getSize());
			} else if (file.isDirectory()) {
				System.out.println("DIR:"+file.getName());
			}
		}
	}
	
	public String[] getFiles(String remoteDirectory,String filePrefix,String fileExtension) throws IOException {
		List<String> resultList=new ArrayList<String>();
		System.out.println("getFiles");
		FTPFile[] files=ftp.listFiles(remoteDirectory);
		for(int i=0;i<files.length;i++) {
			FTPFile file=files[i];
			if (file.isFile()) {
				String name=file.getName();
				if (filePrefix.isEmpty() || name.startsWith(filePrefix) ) {
					if (fileExtension.isEmpty() || name.endsWith(fileExtension)) {
						resultList.add(name);
						System.out.println("file:"+name);
					}
				}
				
			}
		}
		return resultList.toArray(new String[0]);
	}
	
	public boolean downloadFile(String remoteFile,String localFile) throws IOException {
		
		FileOutputStream output=null;
		
		try{
			output=new FileOutputStream(localFile);
			boolean success=ftp.retrieveFile(remoteFile,output);
			if (!success) {
				System.out.println("DL Failed: "+ftp.getReplyString());
			}
			return success;
		} finally {
			if (output !=null) {
				output.close();
			}
		}
		
	}
	
	public boolean renameFile(String oldRemoteName,String newRemoteName)  throws IOException {
		boolean success=ftp.rename(oldRemoteName, newRemoteName);
		if (!success) {
			System.out.println("REN Failed: "+ftp.getReplyString());
		}
		return success;
	}
	
	
	//download file
	//rename file
	public void disconnect() {
		if (ftp.isConnected()) {
			try {
				ftp.logout();
			} catch (IOException e) {
				//ignore
			}
			try {
				ftp.disconnect();
			} catch (IOException e) {
				//igore
			}
		}
		
	}
	
	public static void main(String[] args) {
		AtlasFTPSClient client=new AtlasFTPSClient(true);
		try {
			client.connect("ftp-us-1.noq-servers.net", 990, "SuperSaverFLM", "K65qrU8zkdH0rs3K");
			client.listFiles("/download/Test");
			String[] files=client.getFiles("/download/Test", "09--", ".txt");
			for(String file : files) {
				client.downloadFile("/download/Test/"+file, "./"+file);
				client.renameFile("/download/Test/"+file,"/download/Test/"+file+".old");
			}
			//System.out.println(files);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			client.disconnect();
		}
		System.out.println("DONE");
		
	}
}
