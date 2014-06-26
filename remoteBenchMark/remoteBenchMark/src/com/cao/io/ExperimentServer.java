package com.cao.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public abstract class ExperimentServer implements Command {

	long startTime, stopTime;
	String cpu,memory;
    public Properties properties ;

    @Override
    public boolean startCommand() {
    	startTime = System.currentTimeMillis();
        // start sar
    	Boolean isStart=launchSar(properties);
    	String exeCommand="java -DserverAddress="+properties.getProperty("serverAddress")+" -Dtype="+this.getName()+
    			" -DclientNumber="+properties.getProperty("clientNumber")+" -DthreadNumber="+properties.getProperty("threadNumber")+
    			" -DmessageNumber="+properties.getProperty("messageNumber")+" -DmessageSize="+properties.getProperty("messageSize")+
    			" -DbufferSize="+properties.getProperty("bufferSize")+" com.cao.io.StartServer1 & ";
    	if(isStart){
    		return launchServer(exeCommand); // if that is false we should shutdown sar
    	}else{
    		//kill the sar
    	}
        return false;
    }

    @Override
    public boolean stopCommand() {
        // gather sar results => fill properties
    	getherSar(properties);
        // kill server
    	return killServer(properties); // if that is false we should shutdown sar
    }
    
    abstract boolean launchServer(String command);
    abstract boolean killServer(Properties props);
    abstract boolean launchSar(Properties props);
    abstract boolean getherSar(Properties props);

    @Override
    public String getReply() {
    	String reply="type="+this.getName()+",time="+System.currentTimeMillis()+"\n";		
		return reply;
	}
    
    @Override
    public String getResult() {
    	String reply="type="+this.getName()+",cpu="+cpu+",costTime="+Long.toString(stopTime - startTime)+",time="+System.currentTimeMillis()+"\n";	
		return reply;
	}
    
    public void readTextFile(String filePath){
		
		try {
			File file=new File(filePath);
			if(file.isFile()&&file.exists()){
				BufferedReader reader=new BufferedReader(new FileReader(file));
				String row=null;
				while((row=reader.readLine())!=null){
					cpu=row;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
    
    public int executeStript(File scriptFile) {
		int r=-1;
		try {			   	
			//make the script executable
			Process p=Runtime.getRuntime().exec("chmod +x "+scriptFile.getAbsolutePath());
			r = p.waitFor();
			p = Runtime.getRuntime().exec(scriptFile.getAbsolutePath());
			r = p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}
    
    @Override
    public boolean needStopCommand() {
    	return true;
    }
}
