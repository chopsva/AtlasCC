package com.atlas.SCAM.Controller;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class SCAMControllerServer {

	private static SCAMControllerServer instance=null;
	private int watchPort=8899;
	private HttpServer theServer=null;
	
	public static SCAMControllerServer getInstance() {
		if (instance==null) {
			instance=new SCAMControllerServer();
		}
		return instance;
	}
	
	private SCAMControllerServer() {
		if (theServer==null) {
			initServer();
		}
	}
	
	private void initServer() {
		try  {
			this.theServer=HttpServer.create(new InetSocketAddress(this.watchPort),0);
		} catch (IOException e) {
			System.out.println("Can not create server:"+e);
			e.printStackTrace();
			return;
		}
		if (this.theServer==null) {
			System.out.println("server is still null!!?!?!?!?");
			return;
		}
		System.out.println("file root:"+System.getProperty("user.dir")); //add "public"?
		this.theServer.createContext("/",new StaticFileHandler(System.getProperty("user.dir")));
		this.theServer.setExecutor(null);
		this.theServer.start();
	}
}
