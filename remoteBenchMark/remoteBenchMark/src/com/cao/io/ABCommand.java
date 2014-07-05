package com.cao.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class ABCommand implements Command {
    long startTime, stopTime;
    public String Throughput;
    public static int portNum = 0;
    private Properties properties ;
    public ABCommand(Properties p) {
    	properties = p;
	}

	@Override
    public boolean startCommand() {
        int r;
        try {
        	//prepare the command
        	int messageSum = Integer.parseInt(properties.getProperty("threadNumber")) * 1; //TODO
        	String address = properties.getProperty("serverAddress");
        	String command = " ab -n "+messageSum+" -c "+properties.getProperty("threadNumber")+" -p "+basePath+"/"+date+"/"+properties.getProperty("messageSize")+".html -T text/plain -k -r http://"
                	+address.split(":")[0]+":"+(Integer.parseInt(address.split(":")[1]))+"/ ";
        	
        	//sleep 2s to wait the ExperimentServer start 
		    Thread.sleep(2000);
        	
        	startTime = System.currentTimeMillis();
        	//execute the command
        	Process p = Runtime.getRuntime().exec(command);
        	r = p.waitFor();
        	
        	//get the result(Throughput)
        	BufferedReader output = getOutput(p);
        	String line = "";
        	while((line = output.readLine()) != null){
        		System.out.println(line);
        		if (line.contains("total")) {
        			Throughput = line.trim().split(" ")[0];
        		}
        	}
        	
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        
        //get the stopTime
        stopTime = System.currentTimeMillis();
        return r == 0;
    }

    @Override
    public boolean stopCommand() {
        return false;
    }

    public BufferedReader getOutput(Process p) {
		return new BufferedReader(new InputStreamReader(p.getInputStream()));
    	
    }

	@Override
	public String getName() {
		return "abCommand";
	}

	@Override
	public String getReply() {
		String reply = "type="+this.getName()+",costTime="+Long.toString(stopTime - startTime)
				+",throughput="+Throughput+","+properties.toString().substring(1, properties.toString().length()-1).replaceAll(" ", "")
				+",replyTime="+System.currentTimeMillis()+"\n";
		return reply;
	}

	@Override
	public String getResult() {
		return null;
	}

	@Override
	public boolean needStopCommand() {
		return false;
	}
}
