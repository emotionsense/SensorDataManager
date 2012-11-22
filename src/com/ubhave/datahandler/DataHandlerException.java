package com.ubhave.datahandler;

public class DataHandlerException extends Exception
{
	private static final long serialVersionUID = 8240175615135197888L;
	
	public final static int UNKNOWN_CONFIG = 0;
	
	private final int errorCode;
	
	public DataHandlerException(int code)
	{
		errorCode = code;
	}
	
	public int getErrorCode()
	{
		return errorCode;
	}
	
}
