package com.cao.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Properties;

public abstract class BenchmarkUnit {
	public static final int BUFFER = Integer.getInteger("buffersize", 1024);
	
	abstract boolean stopReceiveUnit();
	abstract boolean closeChannel();
	abstract void start(Properties properties) throws Exception;
	abstract boolean commandComplete(SocketChannel socket, String receivedString) throws Exception ;
	abstract List<String> getResultList();
	
	public static void main(String[] args) {
		BenchmarkUnit unit = new Slave(System.getProperty("slaveAddress").trim());
		try {
			unit.start(null);
		} catch (Exception e) {
			System.err.println("Can't start unit on ... ");
		}
	}
	
	public ByteBuffer sendCommand(SocketChannel socket, String command){
		if (socket != null) {
			try {
			  ByteBuffer commandBuffer = ByteBuffer.wrap(command.getBytes());
			  socket.write(commandBuffer);
//			  System.out.println("writeNum"+j);
			  commandBuffer.clear();		  
			  return commandBuffer;	
			} catch (IOException e) {				
				e.printStackTrace();
			}		
		} else {
			    System.err.println("Unable to open the socket channel!");
		}
		return null;
	}
	
	
  public void readCommand(SocketChannel socket) throws Exception {
	  ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER);
		String receivedString = null;	
		int num = 0;
		try {					
			receiveBuffer.clear();	
			while ((num = socket.read(receiveBuffer)) >= 0) {
				//receive the command
				if (receivedString == null) {
					receivedString = byteBufferToString(receiveBuffer);
				} else {
					receivedString = receivedString+byteBufferToString(receiveBuffer);
				}
				if (commandComplete(socket, receivedString)) {
					num = 0;
					receivedString = null;
					if (stopReceiveUnit()) break;
				}
				
				receiveBuffer.clear();
			 }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
  }
	
  public String byteBufferToString(ByteBuffer buffer) {
		String str = new String(buffer.array(), 0, buffer.position());
		return str;
   }
  	
  public void handleSocket() throws Exception {
   }
}
