package com.cao.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public abstract class ServerCommand implements Command {
	public static int portNum = 0;
	
	long startTime, stopTime;
	String cpu,memory;
    public Properties properties ;
    
    @Override
    public boolean startCommand() {
    	startTime = System.currentTimeMillis();
        // start sar
    	launchSar(properties);
    	
    	String exeCommand = "java -DserverAddress="+properties.getProperty("serverAddress")+" -Dtype="+this.getName()+
    			" -DclientNumber="+properties.getProperty("clientNumber")+" -DthreadNumber="+properties.getProperty("threadNumber")+
    			" -DmessageNumber="+properties.getProperty("messageNumber")+" -DmessageSize="+properties.getProperty("messageSize")+
    			" -DbufferSize="+properties.getProperty("bufferSize")+" com.cao.io.ExperimentServer & ";
    	
    	// launch the Experiment server
        return launchServer(exeCommand);
    }

    @Override
    public boolean stopCommand() {
        // gather sar results => fill properties
    	gatherSar(properties);
        // kill server
    	return killServer(properties); // if that is false we should shutdown sar
    }


    @Override
    public String getReply() {
    	String reply = "type="+this.getName()+","+properties.toString().substring(1, properties.toString().length()-1).replaceAll(" ", "")
    			+",replyTime="+System.currentTimeMillis()+"\n";		
		return reply;
	}
    
    @Override
    public String getResult() {
    	String reply = "type="+this.getName()+",cpu="+cpu
    			+","+properties.toString().substring(1, properties.toString().length()-1).replaceAll(" ", "")
    			+",replyTime="+System.currentTimeMillis()+"\n";	
		return reply;
	}
    
    
    @Override
    public boolean needStopCommand() {
    	return true;
    }
    
    boolean launchSar(Properties props) {
    	new Thread( new Runnable() {
			@Override
			public void run() {
				String sarCommand = "sar -o test.sar -p 1 ";
				try {
					Process p = Runtime.getRuntime().exec(sarCommand);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
    	}).start();
        return true;
	}
    
    public BufferedReader getOutput(Process p) {
		return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }
    
    boolean gatherSar(Properties props) {
    	// gather the result of the sar
		int r;
        try {    
        	String[] killSar = {"/bin/sh","-c", "ps -ef|grep sar|awk '{print $2}'|xargs -n 1 -r kill -9"};
        	String[] cpuResult = {"/bin/sh","-c", "sar -p -f test.sar "};
        	
        	Process p = Runtime.getRuntime().exec(cpuResult);
        	
        	//get the result(Cpu)
        	BufferedReader output = getOutput(p);
        	String line = "";
        	while((line = output.readLine()) != null){
        		if (line.contains("Average")) {
        			System.out.println(line);
        			cpu = line.trim().substring(line.indexOf("all")+8).trim().split(" ")[0];
        		}
        	}
        	
        	// kill the sar
        	p = Runtime.getRuntime().exec("rm test.sar");
        	p = Runtime.getRuntime().exec(killSar);
        	r = p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return r == 0;
	}
    boolean launchServer(String command) {
		int r;
        try {
        	Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c",command});
            r = p.waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        }
		 
        // mark the stop time
        stopTime = System.currentTimeMillis();
        return r == 0;
	}
    
    boolean killServer(Properties props) {
    	// kill the Experiment Server
		int r;
        try {
        	String exeCommand = "ps -ef|grep java|grep com.cao.io.ExperimentServer|awk '{print $2}'|xargs -n 1 -r kill -9 ";
            
            Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c",exeCommand});
            r = p.waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return r == 137;   //kill: exit value==137
	}
    
}
