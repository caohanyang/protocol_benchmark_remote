 package com.cao.io;

import java.nio.ByteBuffer;
import java.nio.channels.NetworkChannel;
import java.security.MessageDigest;

abstract public class ExperimentServer<S extends NetworkChannel,C extends NetworkChannel> {
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
	
	public static void main(String[] args) {
		new Thread(new Runnable() {
            @Override
            public void run() {
                newServer().listen();
            }
        }).start();

	}
	
	static ExperimentServer<?,?> newServer() {
		String serverType=System.getProperty("type").trim();
        if ("Synchronous".equalsIgnoreCase(serverType))    	
            return new ExperimentSynchronous();
        
        else if ("Asynchronous".equalsIgnoreCase(serverType))
        	return new ExperimentAsynchronous();
        
        else if ("Future".equalsIgnoreCase(serverType))
        	return new ExperimentFuture();
        
        return null;
    
    }
	
	public void sendMessage(String receivedString,C socket,int messageNow) {
        ByteBuffer writebuff = ByteBuffer.allocate(BUFFER_SIZE);
        
        //encode the string
        String digest=encode("SHA1",receivedString);
        
        //System.out.println("digest"+digest.length());
        String sendString="HTTP/1.1 200 OK\n" + "Date: Wed, 01 May 2013 15:46:04 GMT\n" + "Server: Apache/2.2.22 (Ubuntu)\n"+
        "X-Powered-By: PHP/5.3.10-1ubuntu3.6\n" + "Vary: Accept-Encoding\n" + "Last-Modified: Wed, 01 May 2013 12:52:39 GMT\n"+
        "Transfer-Encoding: chunked\n" + "Content-Type: text/xml; charset=UTF-8\n" + digest;
        
        writebuff=ByteBuffer.wrap(sendString.getBytes(), 0, sendString.length());
        writebuff.limit(sendString.length());
        writebuff.rewind();
        
        //send the string
        sendText(writebuff,socket,messageNow);

    }
    
    public String receiveMessage(C socket,int messageNow){
		
    	//receive the message
    	ByteBuffer readbuff = ByteBuffer.allocate(BUFFER_SIZE+HTTP_HEAD);

    	String receivedString=receiveText(readbuff,socket,messageNow);
        
        return receivedString;
    }
    
    public String byteBufferToString(ByteBuffer buffer){
    	String str=new String(buffer.array(),0,buffer.limit());
		return str;

    }
    
    public String encode(String algorithm, String str) {
    	// encode the string, use the SHA1
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
		public C socket=null;
		public ServerWorker(C socket){
			this.socket=socket;
		}
		@Override
		public void run() {
			
			for(int i=1;i<=MESSAGE_NUMBER;i++){
				
				//receive message
				String receivedString =receiveMessage(socket,i);
				//send message
				if(receivedString==null){				
					 break;
				}else{
					sendMessage(receivedString,socket,i);	
				}
			}
		}	
	}
	
}
