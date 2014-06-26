package com.cao.io;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StartServer1 {
	public static boolean bolOpen=false;
	public static int CLIENT_NUMBER = Integer.parseInt(System.getProperty("clientNumber").trim());
	public static void main(String[] args) {
		new Thread(new Runnable() {
            @Override
            public void run() {
                newServer().listen();
            }
        }).start();

	}
	
	static Server1<?,?> newServer() {
		String serverType=System.getProperty("type").trim();
        if ("Synchronous".equalsIgnoreCase(serverType))    	
            return new Server_S1();
        
        else if ("Asynchronous".equalsIgnoreCase(serverType))
        	return new Server_A1();
        
        else if ("Future".equalsIgnoreCase(serverType))
        	return new Server_AF1();
        
        return null;
    
    }
	
}
