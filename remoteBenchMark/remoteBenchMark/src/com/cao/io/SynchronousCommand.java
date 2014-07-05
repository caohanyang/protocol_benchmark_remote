package com.cao.io;

import java.util.Properties;

public class SynchronousCommand extends ServerCommand {
	public SynchronousCommand(Properties p) {
		properties = p;
	}

	@Override
	public String getName() {
		return "Synchronous";
	}

}
