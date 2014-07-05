package com.cao.io;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Properties;

public class Slave extends BenchmarkUnit{

	public final InetSocketAddress address;
	public Command currentCommand = null;
	public ServerSocketChannel server;
	public SocketChannel socket;

	public static void main(String[] args) {
		Slave slave = new Slave(System.getProperty("slaveAddress").trim());
		// start he slave
		try {
			slave.start(null);
		} catch (Exception e) {
			System.err.println("Can't start slave on ... ");
		}
	}

	Slave(String address) {
		String arg[] = address.split(":");
		this.address = new InetSocketAddress(arg[0].trim(),Integer.parseInt(arg[1].trim()));
	}

	@Override
	public void start(Properties p) throws Exception {
		// open and bind
		server = ServerSocketChannel.open();
		try {
			server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		} catch (Exception e) {
			System.err.println("Warning, reuse address is unavialable for "+ address);
		}
		try {
			server.setOption(StandardSocketOptions.SO_RCVBUF, BUFFER);
		} catch (Exception e) {
			System.err.println("Warning, Can't change the size of the receive buffer for "+ address);
		}
		server.bind(address);
		
		//accept socket and read buffer
		handleSocket();
	}

	@Override
	public void handleSocket() throws Exception {
		while (true) {
			try {
				//accept the socket
				socket = server.accept();
			} catch (Exception e1) {
			}
			try {
				//read command from the socket
				readCommand(socket);
			} catch (Exception e) {
				System.err.println("Unable to handle request for " + socket+ ": " + e.getMessage());
			}
		}
	}

	public String byteBufferToString(ByteBuffer buffer) {
		//transform the data from byte to String
		String str = new String(buffer.array(), 0, buffer.position());
		return str;
	}
	
	@Override
	boolean commandComplete(SocketChannel socket, String receivedString)
			throws Exception {
		if (receivedString.endsWith("\n")) {
			// The command is end, execute the command
			String response = executeCommand(receivedString);

//			System.out.println("response="+response);
			//send the response back
			sendCommand(socket, response);	
			return true;
		}
		return false;
	}
	
	String statusFromBoolean(boolean status) {
		return status ? "OK " : "KO";
	}

	private String startCommand(Command command) {
		return statusFromBoolean(command.startCommand()) + command.getReply();
	}

	private String stopCommand(Command command) {
		return statusFromBoolean(command.stopCommand())+command.getResult();
	}

	
	public String executeCommand(String commandLine) throws Exception {
		String response = null;
//		System.out.println(commandLine);
		switch (parseAction(commandLine)) {
		case "START":
			// action==START
			if (currentCommand != null)
				throw new Exception("Unable to start the command, there is existing a command.");

			currentCommand = parseCommand(commandLine);
			//System.out.println("name="+currentCommand.getName());
			if (currentCommand == null)
				throw new Exception("There is no command to execute");

			response = startCommand(currentCommand);
			if (!currentCommand.needStopCommand())
				currentCommand = null;
			break;
		case "STOP":
			// action==STOP
			if (currentCommand == null)
				throw new Exception("There is no command to stop");
			response = stopCommand(currentCommand);
			currentCommand = null;
			break;
		default:
			break;
		}
		return response;
	}

	public Command parseCommand(String commandLine) {
		Properties p = createProperties(commandLine);
		String type = p.getProperty("type", "Unknown");
		try {
			Class<?> clazz = Class.forName(type);
			if(!Command.class.isAssignableFrom(clazz))
				return null;
			@SuppressWarnings("unchecked")
			Constructor<? extends Command> ctor = (Constructor<? extends Command>) clazz.getConstructor(Properties.class);
			return ctor.newInstance(p);
		} catch (ClassNotFoundException | NoSuchMethodException
				| SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			System.err.println(e);
			return null;
		}
	}

	public String parseAction(String commandLine) {
		//System.out.println(commandLine);
		int idx = commandLine.indexOf(' ');
		if (idx < 0)
			return "ERROR";
		return commandLine.substring(0, idx).toUpperCase();
	}

	public Properties createProperties(String commandLine) {
		Properties p = new Properties();
		String arg[] = commandLine.split(" ");
		String parameters = arg[1].trim();
		String para[] = parameters.split(",");
		for (int i = 0; i < para.length; i++) {
			String value[] = para[i].split("=");
			p.put(value[0], value[1].trim());
		}
		return p;
	}
	
	@Override
	boolean stopReceiveUnit() {
		return false;
	}

	@Override
	boolean closeChannel() {
		try {
			//socket.close();
			server.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	List<String> getResultList() {
		return null;
	}
}
