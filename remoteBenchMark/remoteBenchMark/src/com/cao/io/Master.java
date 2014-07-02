package com.cao.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Master extends BenchmarkUnit {
	public final InetSocketAddress address_1;
	public final InetSocketAddress address_2;
	public SocketChannel slave1 = null;
	public SocketChannel slave2 = null;
	private List<String> resultList = new ArrayList<String>();
	public String basePath = "/home/server/software/BenchMark";

	Master(String add1, String add2) {
		String arg1[] = add1.split(":");
		address_1 = new InetSocketAddress(arg1[0].trim(), Integer.parseInt(arg1[1].trim()));
		String arg2[] = add2.split(":");
		address_2 = new InetSocketAddress(arg2[0].trim(), Integer.parseInt(arg2[1].trim()));
	}

	public static void main(String[] args) {
		Master master = new Master(System.getProperty("slaveAddress1").trim(),
				System.getProperty("slaveAddress2").trim());
		try {
			master.start(null);
		} catch (Exception e) {
			System.err.println("Can't start master on ... ");
		}
	}

	@Override
	public void start(Properties p) throws Exception {

		String propertiesString = p.toString().substring(1, p.toString().length() - 1).replaceAll(" ", "");

		String startCommand = "START " + "type=" + p.getProperty("slaveType1")+ "," + propertiesString + ",sendTime="+ System.currentTimeMillis() + "\n";
		System.out.println(startCommand);
		slave1 = connectSlave(address_1, startCommand);

		String abCommand = "START " + "type=" + p.getProperty("slaveType2")+ "," + propertiesString + ",sendTime="+ System.currentTimeMillis() + "\n";
		System.out.println(abCommand);
		slave2 = connectSlave(address_2, abCommand);
		
		String stopCommand = "STOP " + "type=" + p.getProperty("slaveType1")+ "," + propertiesString + ",sendTime="+ System.currentTimeMillis() + "\n";
		System.out.println(stopCommand);
		handleCommand(slave1, stopCommand);
		
		closeChannel();
	}

	public SocketChannel connectSlave(InetSocketAddress address, String command)
			throws Exception {

		SocketChannel slave = connect(address);
		handleCommand(slave, command);
		return slave;
	}

	public SocketChannel connect(InetSocketAddress address) throws Exception {

		SocketChannel socket = null;

		socket = SocketChannel.open();
		try {
			socket.setOption(StandardSocketOptions.SO_RCVBUF, BUFFER);
		} catch (Exception e) {
			System.err.println("Warning, Can't change the size of the receive buffer for "+ address);
		}
		try {
			socket.setOption(StandardSocketOptions.SO_SNDBUF, BUFFER);
		} catch (Exception e) {
			System.err.println("Warning, Can't change the size of the send buffer for "+ address);
		}
		try {
			socket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		} catch (Exception e) {
			System.err.println("Warning, reuse address is unavialable for "+ address);
		}

		socket.connect(address);
//		System.out.println("Conection ok");
		return socket;
	}

	public void handleCommand(SocketChannel socket, String command)
			throws Exception {

		ByteBuffer commandBuffer = sendCommand(socket, command);
		readCommand(socket);
	}

	@Override
	boolean commandComplete(SocketChannel socket, String receivedString)
			throws Exception {
		if (receivedString.endsWith("\n")) {
			// handleResponse(receivedString);
		    System.out.println(receivedString);
			recordResult(receivedString);
			return true;
		}
		return false;
	}

	void recordResult(String receivedString) {
		resultList.add(receivedString);
	}

	@Override
	public List<String> getResultList() {
		return resultList;
	}

	@Override
	boolean stopReceiveUnit() {
		return true;
	}

	@Override
	boolean closeChannel() {
		try {
			slave1.close();
			slave2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
