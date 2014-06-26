package com.cao.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server_A1 extends Server1<AsynchronousServerSocketChannel,AsynchronousSocketChannel>{
	@Override
	public void listen() {
		    	
	 try {		
	     //use ChannelGroup
	     //AsynchronousChannelGroup group = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), CLIENT_NUMBER);
	     AsynchronousChannelGroup group = AsynchronousChannelGroup.withFixedThreadPool(THREAD_NUMBER, Executors.defaultThreadFactory());
	     //open and bind
	     String arg[] = SERVER_ADDRESS.split(":");
			InetSocketAddress address = new InetSocketAddress(arg[0].trim(),Integer.parseInt(arg[1].trim()));
		 try {
			server = AsynchronousServerSocketChannel.open(group);
		} catch (Exception e) {
			System.out.println("Cannot open the server");
			System.exit(0);
		}
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
			server.bind(address,THREAD_NUMBER);  //backlog=1000
		} catch (Exception e) {
			System.out.println("Cannot bind the address");
			System.exit(0);
		}
	 	
	 	 //System.out.println("AsynchronousServer:waiting for connection...");
//	 	 if(server.isOpen()){
//	        new Thread(new Runnable() {
//	            @Override
//	            public void run() {
//	                Runner.initClientPool();
//	            }
//	        }).start();
//	        //Runner.initClientPool();
//	 	 }
         //accept the socket
		 server.accept(null, new ServerAccept());
	 	
		 // Awaits termination of the group.
		 group.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS); 
	    }catch (IOException | InterruptedException e) {
			e.printStackTrace();
	  }	
	}

	@Override
	public void sendText(ByteBuffer writebuff, final AsynchronousSocketChannel socket,final int messageNow) {
		//write
		socket.write(writebuff, writebuff, new CompletionHandler<Integer, ByteBuffer>() {

			@Override
			public void completed(Integer result, ByteBuffer attachment) {
				//System.out.println("AsynchronousServer:already send: "+result);
				try {
					if(messageNow==MESSAGE_NUMBER){
					   socket.close();
					}else{
						receiveMessage(socket,messageNow+1);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				System.out.println("server fail to send text to the client");
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
	}

	
	@Override
	public String receiveText(final ByteBuffer readbuff,final AsynchronousSocketChannel socket,final int messageNow) {
		
		    String receivedString=null;
	    	socket.read(readbuff, readbuff, new CompletionHandler<Integer, ByteBuffer>() {

			@Override
			public void completed(Integer result, ByteBuffer attachment) {
				//System.out.println("AsynchronousServer:"+this+" already receive: "+result);
				
				if(attachment.hasRemaining()){			
					//if readbuff has remaining, then read it again
					socket.read(readbuff,readbuff,this);				
				}else{
					//AtomicInteger messageNow=new AtomicInteger(0);
					//sendMessage(readbuff,socket,messageNow);
					readbuff.flip();   //TODO	
					sendMessage(byteBufferToString(readbuff),socket,messageNow);
				}	
				
//				readbuff.flip();   //TODO				
//				//AtomicInteger messageNow=new AtomicInteger(0);
//				sendMessage(byteBufferToString(readbuff),socket,messageNow);	
			}

			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				System.out.println("server fail to receive text the client");	
				
			}
		});
	    
		return receivedString;
	}
	
	public class ServerAccept implements CompletionHandler<AsynchronousSocketChannel,Object>{

		@Override
		public void completed(AsynchronousSocketChannel result, Object attachment) {
			// get the reslut and invoke next thread.
			//System.out.println("AsynchronousServer:connect one client");
			int messageNow=0;
			server.accept(null, this);
			//do read and write	
			receiveMessage(result,++messageNow);
		}

		@Override
		public void failed(Throwable exc, Object attachment) {
			System.out.println("server fail to connect the client");		
		}	
	}
	
}
