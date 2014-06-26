 package com.cao.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

abstract public class Server1<S extends NetworkChannel,C extends NetworkChannel> {
	//public static int SERVER_PORT=Integer.parseInt(System.getProperty("serverPort").trim());
	public int BUFFER_SIZE = Integer.parseInt(System.getProperty("bufferSize").trim());
	public int MESSAGE_SIZE = Integer.parseInt(System.getProperty("messageSize").trim());
	public int MESSAGE_NUMBER = Integer.parseInt(System.getProperty("messageNumber").trim());
	public int CLIENT_NUMBER = Integer.parseInt(System.getProperty("clientNumber").trim());
	public int THREAD_NUMBER = Integer.parseInt(System.getProperty("threadNumber").trim());
	public String SERVER_ADDRESS = System.getProperty("serverAddress").trim();
	public int HTTP_HEAD = 159;
	protected S server;
	abstract public void listen();
	abstract public void sendText(ByteBuffer writebuff,C socket,int messageNow);
	abstract public String receiveText(ByteBuffer readbuff,C socket,int messageNow);
	
	public void sendMessage(String receivedString,C socket,int messageNow) {
        ByteBuffer writebuff = ByteBuffer.allocate(BUFFER_SIZE);
        //String Hello = "asdfqwer12341234123eqwerqwxqwty467vscssac";
        
        //encode the string
        String digest=encode("SHA1",receivedString);
        
        //System.out.println("digest"+digest.length());
        String sendString="HTTP/1.1 200 OK\n"+
        "Date: Wed, 01 May 2013 15:46:04 GMT\n"+
        "Server: Apache/2.2.22 (Ubuntu)\n"+
        "X-Powered-By: PHP/5.3.10-1ubuntu3.6\n"+
        "Vary: Accept-Encoding\n"+
        "Last-Modified: Wed, 01 May 2013 12:52:39 GMT\n"+
        "Transfer-Encoding: chunked\n"+
        "Content-Type: text/xml; charset=UTF-8\n"+digest;
        
        writebuff=ByteBuffer.wrap(sendString.getBytes(), 0, sendString.length());
        writebuff.limit(sendString.length());
        writebuff.rewind();
        
        sendText(writebuff,socket,messageNow);

    }
    
    public String receiveMessage(C socket,int messageNow){
		
    	ByteBuffer readbuff = ByteBuffer.allocate(BUFFER_SIZE+HTTP_HEAD);

    	String receivedString=receiveText(readbuff,socket,messageNow);
  	
    	//readbuff.flip();

    	//System.out.println("1limit: "+readbuff.limit());
       // System.out.println("1position: "+readbuff.position());
        
        return receivedString;
    }
    
    public String byteBufferToString(ByteBuffer buffer){
    	String str=new String(buffer.array(),0,buffer.limit());
		return str;

    }
    
    public String encode(String algorithm, String str) {
		if (str == null) {
			return null;
		}
		try {
			MessageDigest alga = MessageDigest.getInstance(algorithm);
			alga.update(str.getBytes()); 
			byte[] digesta = alga.digest();
	            
	        return byte2hex(digesta);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
			if (n < b.length - 1)
				hs = hs + ":";
		}
		return hs.toUpperCase();
	}
	
	public class ServerWorker implements Runnable{
		public SocketChannel socket=null;
		public ServerWorker(SocketChannel socket){
			this.socket=socket;
		}
		@Override
		public void run() {
			
			for(int i=1;i<=MESSAGE_NUMBER;i++){

				String receivedString =receiveMessage1(socket,i);
				//ByteBuffer readbuff=receiveMessage(socket);
				if(receivedString==null){				
					 break;
				}else{
					sendMessage1(receivedString,socket,i);	
				}
			}
			
		}	
		
		public void sendMessage1(String receivedString,SocketChannel socket,int messageNow) {
	        ByteBuffer writebuff = ByteBuffer.allocate(BUFFER_SIZE);
	        //String Hello = "asdfqwer12341234123eqwerqwxqwty467vscssac";
	        
	        //encode the string
	        String digest=encode("SHA1",receivedString);
	        
	        //System.out.println("1111receString"+receivedString.length());
	        //System.out.println("digest"+digest.length());
	        String sendString="HTTP/1.1 200 OK\n"+
	        "Date: Wed, 01 May 2013 15:46:04 GMT\n"+
	        "Server: Apache/2.2.22 (Ubuntu)\n"+
	        "X-Powered-By: PHP/5.3.10-1ubuntu3.6\n"+
	        "Vary: Accept-Encoding\n"+
	        "Last-Modified: Wed, 01 May 2013 12:52:39 GMT\n"+
	        "Transfer-Encoding: chunked\n"+
	        "Content-Type: text/xml; charset=UTF-8\n"+digest;
	        
	        writebuff=ByteBuffer.wrap(sendString.getBytes(), 0, sendString.length());
	        writebuff.limit(sendString.length());
	        writebuff.rewind();
	        
	        sendText1(writebuff,socket,messageNow);

	    }
	    
	    public String receiveMessage1(SocketChannel socket,int messageNow){
			
	    	ByteBuffer readbuff = ByteBuffer.allocate(BUFFER_SIZE+HTTP_HEAD);

	    	String receivedString=receiveText1(readbuff,socket,messageNow);
	  	
	    	//readbuff.flip();

	    	//System.out.println("1limit: "+readbuff.limit());
	       // System.out.println("1position: "+readbuff.position());
	        
	        return receivedString;
	    }
	    
		public void sendText1(ByteBuffer writebuff, SocketChannel socket,int messageNow) {

			try {
				int i=socket.write(writebuff);
				//System.out.println(this+"SynchronousServer:already send: "+i);
				
				socket.close();
				
				if(messageNow==MESSAGE_NUMBER){				
					//socket.close();           //close the channel //TODO
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		public String receiveText1(ByteBuffer readbuff,SocketChannel socket,int messageNow) {
			String receivedString=null;
			try {

				//int i=socket.read(readbuff);
				//System.out.println(this.toString()+"SynchronousServer:already receive: "+i);
				//System.out.println("1:"+readbuff.position());
				readbuff.clear();
				int num=0,sum=0;
				while((num=socket.read(readbuff)) >= 0 || readbuff.position() != 0) {
					//System.out.println("2:"+readbuff.position());
					sum+=num;
					//System.out.println("num="+num);
					//System.out.println("sum="+sum);
					if(sum>=MESSAGE_SIZE+HTTP_HEAD) {
						receivedString=receivedString+byteBufferToString(readbuff);
						//System.out.println(this.toString()+"receivedLenth:"+receivedString.length());
						break;
					}else{
						if(readbuff.position()==readbuff.limit()){
							receivedString=receivedString+byteBufferToString(readbuff);
							readbuff.clear();
						}
					}
					 
				 }
				//if(socket.read(readbuff)!=-1){
				//if readbuff has remaining, then read it again
				//int j=socket.read(readbuff);
				//System.out.println("1SynchronousServer:receive rest of the string:"+j);
			    //}
				
				//if(socket.read(readbuff)!=-1){
					//if readbuff has remaining, then read it again
					//int k=socket.read(readbuff);
					//System.out.println("2SynchronousServer:receive rest of the string:"+k);
				//}
				
//				while(readbuff.hasRemaining()){
//					//if readbuff has remaining, then read it again
//					int j=socket.read(readbuff);
//					System.out.println("SynchronousServer:receive rest of the string:"+j);
//				}
			} catch (IOException e) {		
				e.printStackTrace();
			}	
			return receivedString;
		}
		
	}

	public class ServerWorkerF implements Runnable{
		public AsynchronousSocketChannel socket=null;
		public ServerWorkerF(AsynchronousSocketChannel socket){
			this.socket=socket;
		}
		@Override
		public void run() {
			
			//System.out.println(this.toString());
//			for(int i=1;i<=MESSAGE_NUMBER;i++){
//				ByteBuffer readbuff=receiveMessage(socket,i);
//				//ByteBuffer readbuff=receiveMessage(socket);
//				if(readbuff==null){				
//					 break;
//				}else{
//					sendMessage(readbuff,socket,i);	
//				}
//			}
			
			for(int i=1;i<=MESSAGE_NUMBER;i++){

				String receivedString =receiveMessage2(socket,i);
				//ByteBuffer readbuff=receiveMessage(socket);
				if(receivedString==null){				
					 break;
				}else{
					sendMessage2(receivedString,socket,i);	
				}
			}
			
		}	
		
		public void sendMessage2(String receivedString,AsynchronousSocketChannel socket,int messageNow) {
	        ByteBuffer writebuff = ByteBuffer.allocate(BUFFER_SIZE);
	        //String Hello = "asdfqwer12341234123eqwerqwxqwty467vscssac";
	        
	        //encode the string
	        String digest=encode("SHA1",receivedString);
	        
	        //System.out.println("1111receString"+receivedString.length());
	        //System.out.println("digest"+digest.length());
	        String sendString="HTTP/1.1 200 OK\n"+
	        "Date: Wed, 01 May 2013 15:46:04 GMT\n"+
	        "Server: Apache/2.2.22 (Ubuntu)\n"+
	        "X-Powered-By: PHP/5.3.10-1ubuntu3.6\n"+
	        "Vary: Accept-Encoding\n"+
	        "Last-Modified: Wed, 01 May 2013 12:52:39 GMT\n"+
	        "Transfer-Encoding: chunked\n"+
	        "Content-Type: text/xml; charset=UTF-8\n"+digest;
	        
	        writebuff=ByteBuffer.wrap(sendString.getBytes(), 0, sendString.length());
	        writebuff.limit(sendString.length());
	        writebuff.rewind();
	        
	        sendText2(writebuff,socket,messageNow);

	    }
	    
	    public String receiveMessage2(AsynchronousSocketChannel socket,int messageNow){
			
	    	ByteBuffer readbuff = ByteBuffer.allocate(BUFFER_SIZE+HTTP_HEAD);

	    	String receivedString=receiveText2(readbuff,socket,messageNow);
	  	
	    	//readbuff.flip();

	    	//System.out.println("1limit: "+readbuff.limit());
	       // System.out.println("1position: "+readbuff.position());
	        
	        return receivedString;
	    }
	    
		public void sendText2(ByteBuffer writebuff, AsynchronousSocketChannel socket,int messageNow) {

			try {
				int i=socket.write(writebuff).get();
				//System.out.println(this+"SynchronousServer:already send: "+i);
				
				socket.close();
				
				if(messageNow==MESSAGE_NUMBER){				
					//socket.close();           //close the channel //TODO
				}
				
			} catch (IOException | InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}	
		}
		public String receiveText2(ByteBuffer readbuff,AsynchronousSocketChannel socket,int messageNow) {
			String receivedString=null;
			try {

				//int i=socket.read(readbuff);
				//System.out.println(this.toString()+"SynchronousServer:already receive: "+i);
				//System.out.println("1:"+readbuff.position());
				readbuff.clear();
				int num=0,sum=0;
				while((num=socket.read(readbuff).get()) >= 0 || readbuff.position() != 0) {
					//System.out.println("2:"+readbuff.position());
					sum+=num;
					//System.out.println("num="+num);
					//System.out.println("sum="+sum);
					if(sum>=MESSAGE_SIZE+HTTP_HEAD) {
						receivedString=receivedString+byteBufferToString(readbuff);
						//System.out.println(this.toString()+"receivedLenth:"+receivedString.length());
						break;
					}else{
						if(readbuff.position()==readbuff.limit()){
							receivedString=receivedString+byteBufferToString(readbuff);
							readbuff.clear();
						}
					}
					 
				 }
				//if(socket.read(readbuff)!=-1){
				//if readbuff has remaining, then read it again
				//int j=socket.read(readbuff);
				//System.out.println("1SynchronousServer:receive rest of the string:"+j);
			    //}
				
				//if(socket.read(readbuff)!=-1){
					//if readbuff has remaining, then read it again
					//int k=socket.read(readbuff);
					//System.out.println("2SynchronousServer:receive rest of the string:"+k);
				//}
				
//				while(readbuff.hasRemaining()){
//					//if readbuff has remaining, then read it again
//					int j=socket.read(readbuff);
//					System.out.println("SynchronousServer:receive rest of the string:"+j);
//				}
			} catch (InterruptedException | ExecutionException e) {		
				e.printStackTrace();
			}	
			return receivedString;
		}
		
	}
}
