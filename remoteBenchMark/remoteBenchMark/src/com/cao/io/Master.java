package com.cao.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Properties;
import java.util.Random;


public class Master extends BenchmarkUnit{
	public final InetSocketAddress address_1;
	public final InetSocketAddress address_2;
	public String basePath="/home/server/software/BenchMark";
	
	Master(String add1, String add2) {
		String arg1[] = add1.split(":");
		address_1 = new InetSocketAddress(arg1[0].trim(),Integer.parseInt(arg1[1].trim()));
		String arg2[] = add2.split(":");
		address_2 = new InetSocketAddress(arg2[0].trim(),Integer.parseInt(arg2[1].trim()));
	}
	public static void main(String[] args) {	
		Master master=new Master(System.getProperty("slaveAddress1").trim(),System.getProperty("slaveAddress2").trim());
		try {		
			master.start(null);
		} catch (Exception e) {
			System.err.println("Can't start master on ... ");
		}
	}

	@Override
	public void start(Properties p) throws Exception{
			
		String propertiesString=p.toString().substring(1, p.toString().length()-1).replaceAll(" ", "");
		
		String startCommand ="START "+"type="+p.getProperty("slaveType1")+","+propertiesString+",time="+System.currentTimeMillis()+"\n";
		String abCommand ="START "+"type="+p.getProperty("slaveType2")+","+propertiesString+",time="+System.currentTimeMillis()+"\n";		
		String stopCommand ="STOP "+"type="+p.getProperty("slaveType1")+","+propertiesString+",time="+System.currentTimeMillis()+"\n";
		
//		System.out.println(startCommand);
//		System.out.println(abCommand);
		SocketChannel slave1=connectSlave(address_1,startCommand);
		SocketChannel slave2=connectSlave(address_2,abCommand);
		handleCommand(slave1,stopCommand);
	}
	
	public SocketChannel connectSlave(InetSocketAddress address,String command) throws Exception{
		
		SocketChannel slave=connect(address);
		handleCommand(slave,command);
		return slave;
	}
	public SocketChannel connect(InetSocketAddress address) throws Exception {

		SocketChannel socket=null;
        
    	socket = SocketChannel.open();
        try {
        	socket.setOption(StandardSocketOptions.SO_RCVBUF, BUFFER);
		} catch (Exception e) {
			System.err.println("Warning, Can't change the size of the receive buffer for " + address);
		}
        try {
        	socket.setOption(StandardSocketOptions.SO_SNDBUF, BUFFER); 
		} catch (Exception e) {
			System.err.println("Warning, Can't change the size of the send buffer for " + address);
		}
        try {
			socket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		} catch (Exception e) {
			System.err.println("Warning, reuse address is unavialable for " + address);
		}
        
        socket.connect(address);  
        return socket;
    }
	

	public void handleCommand(SocketChannel socket,String command) throws Exception {
			
		ByteBuffer commandBuffer=sendCommand(socket,command);
		readCommand(socket);
	}

	private  void handleResponse(String response) {
		String arg[] = response.split(" ");
		String action = arg[0].trim();
		if("OK".equals(action)){
			String parameters = arg[1].trim();
			String para[] = parameters.split(",");
			for (int i = 0; i < para.length; i++) {
				String value[] = para[i].split("=");
				switch (value[0]) {
				case "costTime":
					String costTime = value[1].trim();
					break;
				case "throughput":
					String throughput = value[1].trim();
					//writeCSV("throughput",throughput);
					//System.out.println(throughput);
					break;	
				case "cpu":
					String cpu = value[1].trim();
					//writeCSV("cpu",cpu);
					break;
				case "time":
					break;
				default:
					break;
				}
			}
		}			
	}	
	
	private  void writeCSV(String type,String value){
		//TODO
		String filePath=basePath+"/"+type+".csv";
		try {
			File file=new File(filePath);
			if(file.isFile()&&file.exists()){
				BufferedReader reader=new BufferedReader(new FileReader(file));
				String row=null;
				while((row=reader.readLine())!=null){
					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	boolean commandComplete(SocketChannel socket, String receivedString)throws Exception {
		if(receivedString.endsWith("\n")){
			handleResponse(receivedString);
			System.out.println("Receive from slave "+socket.getLocalAddress()+": "+receivedString);
			return true;
		}
		return false;
	}
	
	@Override
	boolean stopReceiveUnit() {
		return true;
	}
	@Override
	boolean closeChannel() {
		return false;
	}
}
