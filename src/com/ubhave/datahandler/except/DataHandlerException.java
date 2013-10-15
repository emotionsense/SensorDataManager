package com.ubhave.datahandler.except;

public class DataHandlerException extends Exception
{
	private static final long serialVersionUID = 8240175615135197888L;
	
	public final static int UNKNOWN_CONFIG = 10;
	public final static int NO_URL_TARGET = 11;
	public final static int IO_EXCEPTION = 12;
	public final static int UNIMPLEMENTED = 13;
	public final static int WRITING_TO_DEFAULT_DIRECTORY = 14;
	public final static int CONFIG_CONFLICT = 15;
	public final static int POST_FAILED = 16;
	
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
