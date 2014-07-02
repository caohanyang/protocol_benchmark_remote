package com.cao.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

public class ExperimentAsynchronous extends ExperimentServer {
	public ExperimentAsynchronous(Properties p) {
		properties = p;
	}

	@Override
	boolean launchServer(String command) {
		int r;
    	final File scriptFile = new File(basePath+"/"+date+"/command.sh");
    	try {
			PrintWriter w = new PrintWriter(scriptFile);
        	w.println("#!/bin/sh");
        	w.println("cd "+basePath+"/bin");
        	w.println(command);
        	w.close();
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	
    	r = executeStript(scriptFile);
    		  
        stopTime = System.currentTimeMillis();
        return r == 0;
	}

	@Override
	boolean killServer(Properties props) {
		int r;
        try {
        	String exeCommand = "ps -ef|grep java|grep com.cao.io.StartServer1|awk '{print $2}'|xargs -n 1 -r kill -9 ";
            
            Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c",exeCommand});
            r = p.waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return r == 137;   //kill: exit value==137
	}

	@Override
	boolean launchSar(Properties props) {
		int r;
        try {        	
        	String sarCommand = "sar -o test.sar -p 1 >/dev/null 2>&1 & ";
            
        	// generate a script file containing the command
        	final File scriptFile = new File(basePath+"/"+date+"/sarStart.sh");
        	PrintWriter w = new PrintWriter(scriptFile);
        	w.println("#!/bin/sh");
        	w.println("cd "+basePath+"/"+date);
        	w.println(sarCommand);
        	w.close();
        	
        	r = executeStript(scriptFile);
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return r == 0;
	}

	@Override
	boolean getherSar(Properties props) {
		int r;
        try {    
        	String killSar = "ps -ef|grep sar|awk '{print $2}'|xargs -n 1 -r kill -9";
        	String sarResult = "sar -p -f test.sar |grep Average|awk '{print $3}' >"+basePath+"/"+date+"/cpu.txt";
            
        	// generate a script file containing the command
        	final File scriptFile = new File(basePath+"/"+date+"/sarEnd.sh");
        	PrintWriter w = new PrintWriter(scriptFile);
        	w.println("#!/bin/sh");
        	w.println("cd "+basePath+"/"+date);     	
        	w.println(sarResult);
        	w.println("rm "+basePath+"/"+date+"/test.sar");
        	w.println(killSar);
        	w.close();
        	
        	//make the script executable
        	r = executeStript(scriptFile);
            
            //get the cpu result from the txt
            readTextFile(basePath+"/"+date+"/cpu.txt");
            //readTextFile(basePath+"/"+date+"/memory.txt");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return r == 0;
	}

	@Override
	public String getName() {
		return "Asynchronous";
	}
}
