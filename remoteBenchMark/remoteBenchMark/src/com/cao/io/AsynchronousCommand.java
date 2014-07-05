package com.cao.io;

import java.util.Properties;

public class AsynchronousCommand extends ServerCommand {
	public AsynchronousCommand(Properties p) {
		properties = p;
	}

	@Override
	public String getName() {
		return "Asynchronous";
	}
}
