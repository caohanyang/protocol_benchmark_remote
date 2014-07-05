package com.cao.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExperimentSynchronous extends ExperimentServer<ServerSocketChannel,SocketChannel> {
	@Override
	public void listen() {
		// create the thread pool
		ExecutorService taskExecutor = Executors.newFixedThreadPool(THREAD_NUMBER,Executors.defaultThreadFactory());  
		//open and bind
		String arg[] = SERVER_ADDRESS.split(":");
		InetSocketAddress address = new InetSocketAddress(arg[0].trim(),Integer.parseInt(arg[1].trim()));
		try {			
			try {
				server=ServerSocketChannel.open();
			} catch (Exception e) {
				System.out.println("Cannot open the server");
				System.exit(0);
			}
			//set some options
			//server.configureBlocking(true); 			//set the blocking mode
			try {
				server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			} catch (Exception e) {
				System.out.println("Cannot set the reuse address");
				System.exit(0);
			}
			try {
				server.setOption(StandardSocketOptions.SO_RCVBUF, BUFFER_SIZE*MESSAGE_NUMBER);
			} catch (Exception e) {
				System.out.println("Cannot set the reveive buff");
				System.exit(0);
			}
			try {
				server.bind(address,THREAD_NUMBER);    //TODO //backlog=1000
			} catch (Exception e) {
				System.out.println("Cannot bind the address");
				System.exit(0);
			}
			//System.out.println("SynchronousServer:waiting for connection..."); 

			// wait to accept the socket
			while(true){
				SocketChannel socket=server.accept();
				//System.out.println("SynchronousServer:connect one client");				
				if(socket==null){
					break;
				}else{
					ServerWorker listener=new ServerWorker(socket);
					taskExecutor.execute(listener);
				}
			}
			  
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}


	@Override
	public void sendText(ByteBuffer writebuff, SocketChannel socket,int messageNow) {

		try {
			int i=socket.write(writebuff);
			System.out.println(this+"SynchronousServer:already send: "+i);
			socket.close();
			if(messageNow==MESSAGE_NUMBER){				
				//socket.close();           //close the channel //TODO
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	

	@Override
	public String receiveText(ByteBuffer readbuff,SocketChannel socket,int messageNow) {
		//receive the text
		String receivedString=null;
		try {
			readbuff.clear();
			int num = 0,sum = 0;
			while ((num=socket.read(readbuff)) >= 0 || readbuff.position() != 0) {
				//System.out.println("2:"+readbuff.position());
				sum += num;
				if (sum >= MESSAGE_SIZE + HTTP_HEAD) {
					//get the final string
					receivedString = receivedString+byteBufferToString(readbuff);
					break;
				} else {
					if (readbuff.position() == readbuff.limit()){
						//add the string which received this time to the final string
						receivedString=receivedString+byteBufferToString(readbuff);
						readbuff.clear();
					}
				}
				 
			 }
		} catch (IOException e) {		
			e.printStackTrace();
		}	
		return receivedString;
	}

}
