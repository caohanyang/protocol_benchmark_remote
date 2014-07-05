package com.cao.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExperimentFuture extends ExperimentServer <AsynchronousServerSocketChannel,AsynchronousSocketChannel>{
	@Override
	public void listen() {
		// create the thread pool
		ExecutorService taskExecutor = Executors.newFixedThreadPool(THREAD_NUMBER,Executors.defaultThreadFactory());  
		//open and bind
		String arg[] = SERVER_ADDRESS.split(":");
		InetSocketAddress address = new InetSocketAddress(arg[0].trim(),Integer.parseInt(arg[1].trim()));
		try {			
			
			try {
				server=AsynchronousServerSocketChannel.open();
			} catch (Exception e) {
				System.out.println("Cannot open the server");
				System.exit(0);
			}
			//set some options
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
				server.bind(address,THREAD_NUMBER); //backlog=100
			} catch (Exception e) {
				System.out.println("Cannot bind the address");
				System.exit(0);
			}
			
			//System.out.println("AsynchronousServer:waiting for connection...");  
			while(true){			
				try {
					//get the socket
					Future<AsynchronousSocketChannel> asynchronousSocketChannelFuture=server.accept();
					final AsynchronousSocketChannel asynchronousSocketChannel = asynchronousSocketChannelFuture.get();
					//System.out.println("FutrueServer:connect one client");
					if(asynchronousSocketChannel==null){
						break;
					}else{
						ServerWorker listener=new ServerWorker(asynchronousSocketChannel);
						taskExecutor.execute(listener);
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				} 				
			}		  
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void sendText(ByteBuffer writebuff, AsynchronousSocketChannel socket,int messageNow) {
		try {
			socket.write(writebuff).get();
			//System.out.println("AsynchronousServerFuture:already send: "+i);
			socket.close(); 
            if (messageNow == MESSAGE_NUMBER) {
				//socket.close();           //close the channel //TODO
			}
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}	
	}
	
	@Override
	public String receiveText(ByteBuffer readbuff,AsynchronousSocketChannel socket,int messageNow) {
		//receive the message
		String receivedString=null;
		try {
			readbuff.clear();
			int num = 0,sum = 0;
			while ((num = socket.read(readbuff).get()) >= 0|| readbuff.position() != 0) {
				sum += num;
				if (sum >= MESSAGE_SIZE + HTTP_HEAD) {
					// get the final string
					receivedString = receivedString
							+ byteBufferToString(readbuff);
					break;
				} else {
					if (readbuff.position() == readbuff.limit()) {
						// add the string which received this time to the final
						// string
						receivedString = receivedString
								+ byteBufferToString(readbuff);
						readbuff.clear();
					}
				}
			 }
		} catch (InterruptedException | ExecutionException e) {		
			e.printStackTrace();
		}	
		return receivedString;
	}
	
}
