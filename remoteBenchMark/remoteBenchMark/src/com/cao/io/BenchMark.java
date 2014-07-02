package com.cao.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.csvreader.CsvWriter;

public class BenchMark {
    public Properties properties = null;
    public List<String> typeList = null;
    public Map<String, int[]> variableMap = null;
    public String basePath = "/home/server/software/BenchMark";
    public int repeatTimes = Integer.getInteger("repeatTimes", 2);
    public String date = "0617";
	public int[] variable;
    
	public static void main(String[] args) {
		BenchMark benchMark = new BenchMark();
		benchMark.prepareVariable();
		benchMark.experiment("messageSize");  
		//benchMark.experiment("threadNumber"); 
	}
    

	public void prepareVariable() {
		
		typeList = new ArrayList<String>();
		typeList.add("com.cao.io.ExperimentSynchronous");
		typeList.add("com.cao.io.ExperimentAsynchronous");
		typeList.add("com.cao.io.ExperimentFuture");
		
		int[] messageSize = new int[]{64,256,1024,4096,8192};
		int[] threadNumber = new int[]{1,10,100};
		int[] clientNumber = new int[]{1,10,100};
		int[] messageNumber = new int[]{1,10,100};
		
		variableMap = new HashMap<String, int[]>();
		variableMap.put("messageSize", messageSize);
		variableMap.put("threadNumber", threadNumber);
		variableMap.put("clientNumber", clientNumber);
		variableMap.put("messageNumber", messageNumber);
		
		properties = getDefaultProperties();
	}


	public void experiment(String testVariable){
		final BenchmarkUnit master = new Master(System.getProperty("slaveAddress1").trim(),System.getProperty("slaveAddress2").trim());
       
		try {
			startMaster(master, testVariable);
			handleResult(master);
  		} catch (Exception e) {
  			System.err.println("Can't start the thread ... ");
  		}
	}

	public void startMaster(BenchmarkUnit master, String testVariable) throws Exception {
		variable = variableMap.get(testVariable);
		for (int t = 1; t <= repeatTimes; t++) {
			
			for (int j = 0; j < variable.length; j++) {
				switch (testVariable) {
				case "messageSize":
					properties.put(testVariable, variable[j]);
					properties.put("bufferSize", variable[j]);
					break;
				default:
					properties.put(testVariable, variable[j]);
					break;
				}
				
				properties.put("times", t);
				for (int i = 0; i < typeList.size(); i++) {
					properties.put("slaveType1", typeList.get(i));
					master.start(properties);
				}
			 }
		}
	}

	public Properties getDefaultProperties(){
		Properties p = new Properties();
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
	
	private void handleResult(BenchmarkUnit master) {
		List<String> resultList = master.getResultList();
		List<String> throughputList = new ArrayList<String>();
		List<String> cpuList = new ArrayList<String>();
		List<String> costTimeList = new ArrayList<String>();	
		for (String result : resultList) {
			
			System.out.println(result);
			String action = result.split(" ")[0].trim();
			
			if ("OK".equals(action)) {
				String parameters = result.split(" ")[1].trim();
				String throughput = getValue(parameters, "throughput");
				String messageSize = getValue(parameters, "messageSize");	
				String cpu = getValue(parameters, "cpu");	
				String type = getValue(parameters, "slaveType1");
				String costTime = getValue(parameters, "costTime");
				
				if (throughput != null) throughputList.add(type+"-"+messageSize+"-"+throughput);
				if (cpu != null) cpuList.add(type+"-"+messageSize+"-"+cpu);
				if (costTime != null) costTimeList.add(type+"-"+messageSize+"-"+costTime);
			}
		}
		
		writeFile(throughputList, "Throughput");
		writeFile(cpuList, "Cpu");
		writeFile(costTimeList, "CostTime");
	}


	public String getValue(String parameters, String str) {
		int index = parameters.indexOf(str);
		if (index != -1) return parameters.substring(index).split(",")[0].split("=")[1];
		return null;
	}
	
	private void writeFile(List<String> list, String str){
		for (String type : typeList) {
			String symbol = type.charAt(21) + "-";
			String fileName = basePath + "/" + date + "/" + symbol + str + ".csv";
			List<String> resultList = new ArrayList<String>();
			String record = null;
			
			for (int i = 0; i < list.size(); i++) {
				if (type.equals(list.get(i).split("-")[0])) {
					record = handleString(list, record, i);
				}
				if ((i + 1) % (typeList.size() * variable.length) == 0) {
					resultList.add(record);
					record = null;
				}
			}
			writeCSV(fileName, resultList);
		}
	}

	public String handleString(List<String> list, String str, int i) {
		if (str == null) {
			str = list.get(i).split("-")[2];
		} else {
			str = str+","+list.get(i).split("-")[2];
		}
		return str;
	}

	public void writeCSV(String fileName, List<String> list) {
		CsvWriter wb = new CsvWriter(fileName,',',Charset.forName("SJIS")); 
		try {
			for (String str : list) {
		    	wb.writeRecord(str.split(","));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	    wb.close();
	}
}
