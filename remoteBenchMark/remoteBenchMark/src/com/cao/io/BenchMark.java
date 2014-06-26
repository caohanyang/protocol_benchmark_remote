package com.cao.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class BenchMark {
    public boolean firstTime=true;
    public Properties properties=null;
    public List<String> typeList=null;
    public Map<String,int[]> variableMap=null;
    
	public static void main(String[] args) {
		BenchMark benchMark=new BenchMark();
		benchMark.prepareVariable();
		benchMark.experiment("messageSize");  
		benchMark.experiment("threadNumber"); 
	}
    

	public void prepareVariable() {
		
		typeList=new ArrayList<String>();
		typeList.add("com.cao.io.ExperimentSynchronous");
		typeList.add("com.cao.io.ExperimentAsynchronous");
		typeList.add("com.cao.io.ExperimentFuture");
		
		int[] messageSize=new int[]{64,256,1024,4096,8192};
		int[] threadNumber=new int[]{1,10,100};
		int[] clientNumber=new int[]{1,10,100};
		int[] messageNumber=new int[]{1,10,100};
		
		variableMap=new HashMap<String,int[]>();
		variableMap.put("messageSize", messageSize);
		variableMap.put("threadNumber", threadNumber);
		variableMap.put("clientNumber", clientNumber);
		variableMap.put("messageNumber", messageNumber);
		
		properties=createProperties();
	}


	public void experiment(String testVariable){
		final BenchmarkUnit master=new Master(System.getProperty("slaveAddress1").trim(),System.getProperty("slaveAddress2").trim());
        BenchmarkUnit slave1=new Slave(System.getProperty("slaveAddress1").trim());
        BenchmarkUnit slave2=new Slave(System.getProperty("slaveAddress2").trim());
       
		try {
			startSlave(slave1, slave2);
			new Thread().sleep(2000);
			startMaster(master,slave1,slave2,testVariable);
			closeChannel(master,slave1,slave2);
  		} catch (Exception e) {
  			System.err.println("Can't start the thread ... ");
  		}
	}
	
	private void startSlave(BenchmarkUnit slave1, BenchmarkUnit slave2) {
		firstTime=true;
		createThread(slave1,firstTime).start();
		createThread(slave2,firstTime).start();
		firstTime=false;
	}
	
	private boolean closeChannel(BenchmarkUnit master, BenchmarkUnit slave1,
			BenchmarkUnit slave2) {
		master.closeChannel();
		if(slave1.closeChannel()){
			if(slave2.closeChannel()){
				//System.out.println("Close OK");
				return true;
			}
		}
		return false;
	}

	public void startMaster(BenchmarkUnit master, final BenchmarkUnit slave1,final BenchmarkUnit slave2, String testVariable)
			throws Exception {
		int[] variable = variableMap.get(testVariable);
		for(int j=0;j<variable.length;j++){
			switch (testVariable){
			case "messageSize":
				properties.put(testVariable,variable[j]);
			    properties.put("bufferSize",variable[j]);
			    break;
			default: 
				properties.put(testVariable,variable[j]);
				break;
			}				
			
			for(int t=1; t<=2; t++){
				properties.put("times", t);
				System.out.println("times="+t);
				for(int i=0;i<typeList.size();i++){
					properties.put("slaveType1",typeList.get(i));
					if(!firstTime){
						createThread(slave1,firstTime).start();
						createThread(slave2,firstTime).start();					
					}
					System.out.println(properties);
					master.start(properties);
				}
			}
		}
	}

	public Properties createProperties(){
		Properties p=new Properties();
		p.put("serverAddress", System.getProperty("serverAddress").trim());
		p.put("clientNumber",1);
		p.put("threadNumber",100);
		p.put("messageNumber",1);
		p.put("slaveType1",System.getProperty("slaveType1").trim());
		p.put("slaveType2",System.getProperty("slaveType2").trim());
		p.put("messageSize",64);
		p.put("bufferSize",64);	
		return p;
		
	}
	public Thread createThread(final BenchmarkUnit unit,final boolean firstTime){
		Thread thread=new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					if(firstTime){
						unit.start(null);
					}else{
						unit.handleSocket();
					}
				} catch (Exception e) {				
					e.printStackTrace();
				} 			
			}			
		});
		return thread;
	}	
}
