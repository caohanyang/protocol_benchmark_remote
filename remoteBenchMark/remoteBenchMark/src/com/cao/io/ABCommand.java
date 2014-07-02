package com.cao.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

public class ABCommand implements Command {
    long startTime, stopTime;
    public String Throughput;
    private Properties properties ;
    public ABCommand(Properties p) {
    	properties = p;
	}

	@Override
    public boolean startCommand() {
        int r;
        try {
        	String resultPath = basePath+"/"+date+"/"+date+properties.getProperty("messageSize")+"R"+".txt";
        	String throughputPath = basePath+"/"+date+"/"+date+properties.getProperty("messageSize")+"T"+".txt";
        	int messageSum = Integer.parseInt(properties.getProperty("threadNumber")) * 1; //TODO
        	String command = " ab -n "+messageSum+" -c "+properties.getProperty("threadNumber")+" -p "+properties.getProperty("messageSize")+".html -T text/plain -k -r http://"
        	+properties.getProperty("serverAddress")+"/ "+"> "+resultPath;
        	
            // generate a script file containing the command
        	final File scriptFile = new File(basePath+"/"+date+"/abCommand.sh");
        	PrintWriter w = new PrintWriter(scriptFile);
        	w.println("#!/bin/sh");
        	w.println("sleep 2");
        	w.println("cd "+basePath+"/"+date);
        	w.println(command);
        	w.close();
        	
        	startTime = System.currentTimeMillis();
        	
        	r = executeStript(scriptFile);
            
            //get the throughput
            String throughput = "awk '/total/ {print $1}' "+resultPath+"> "+throughputPath;
            
            final File throughputScriptFile = new File(basePath+"/"+date+"/result.sh");
        	PrintWriter t = new PrintWriter(throughputScriptFile);
        	t.println("#!/bin/sh");
        	t.println(throughput);
        	t.close();
        	//make the script executable
        	r = executeStript(throughputScriptFile);
            
            //put the property
            File file = new File(throughputPath);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            Throughput = reader.readLine();            
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        stopTime = System.currentTimeMillis();
        return r == 0;
    }

    @Override
    public boolean stopCommand() {
        return false;
    }

    public int executeStript(File scriptFile) {
		int r = -1;
		try {			   	
			//make the script executable
			Process p = Runtime.getRuntime().exec("chmod +x "+scriptFile.getAbsolutePath());
			r = p.waitFor();
			p = Runtime.getRuntime().exec(scriptFile.getAbsolutePath());
			r = p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
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
