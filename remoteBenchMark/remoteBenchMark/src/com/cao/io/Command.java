package com.cao.io;

public interface Command {
	public String date = "0616";
	public String basePath = "/home/server/software/BenchMark";
	
	String getName();
    boolean startCommand();
    boolean stopCommand();
    boolean needStopCommand();
    String getReply();
    String getResult();
}
