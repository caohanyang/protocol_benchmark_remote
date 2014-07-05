package com.cao.io;

import java.util.Properties;

public class FutureCommand extends ServerCommand {
	public FutureCommand(Properties p) {
		properties = p;
	}

	@Override
	public String getName() {
		return "Future";
	}
	
}
