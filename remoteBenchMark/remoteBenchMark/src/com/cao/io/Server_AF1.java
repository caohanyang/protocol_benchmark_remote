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

public class Server_AF1 extends Server1<AsynchronousServerSocketChannel,AsynchronousSocketChannel>{
	@Override
	public void listen() {
		ExecutorService taskExecutor = Executors.newFixedThreadPool(THREAD_NUMBER,Executors.defaultThreadFactory());  
		//ExecutorService taskExecutor = Executors.newCachedThreadPool(Executors.defaultThreadFactory()); 
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
//			if(server.isOpen()){
//		        new Thread(new Runnable() {
//		            @Override
//		            public void run() {
//		                Runner.initClientPool();
//		            }
//		        }).start();
//		        //Runner.initClientPool();
//			}
			while(true){			
				try {
					Future<AsynchronousSocketChannel> asynchronousSocketChannelFuture=server.accept();
					final AsynchronousSocketChannel asynchronousSocketChannel = asynchronousSocketChannelFuture.get();
					//System.out.println("FutrueServer:connect one client");
					if(asynchronousSocketChannel==null){
						break;
					}else{
						ServerWorkerF listener=new ServerWorkerF(asynchronousSocketChannel);
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
			int i=socket.write(writebuff).get();
			//System.out.println("AsynchronousServerFuture:already send: "+i);
			
			socket.close(); 
            if(messageNow==MESSAGE_NUMBER){
				//socket.close();           //close the channel //TODO
			}
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}	
	}
	
	@Override
	public String receiveText(ByteBuffer readbuff,AsynchronousSocketChannel socket,int messageNow) {
		String receivedString=null;
		try {
			//int i=socket.read(readbuff).get();
			//System.out.println("AsynchronousServerFuture:already receive: "+i);
			readbuff.clear();
			int num=0,sum=0;
			while((num=socket.read(readbuff).get()) >= 0 || readbuff.position() != 0) {
				//System.out.println("2:"+readbuff.position());
				sum+=num;
				//System.out.println("num="+num);
				//System.out.println("sum="+sum);
				if(sum>=MESSAGE_SIZE+HTTP_HEAD) {
					receivedString=receivedString+byteBufferToString(readbuff);
					break;
				}else{
					if(readbuff.position()==readbuff.limit()){
						receivedString=receivedString+byteBufferToString(readbuff);
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
