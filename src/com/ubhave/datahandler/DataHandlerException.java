package com.ubhave.datahandler;

public class DataHandlerException extends Exception
{
	private static final long serialVersionUID = 8240175615135197888L;
	
	public final static int UNKNOWN_CONFIG = 0;
	public final static int STORAGE_CREATE_ERROR = 1;
	public final static int STORAGE_OVER_QUOTA = 2;
	public final static int NO_URL_TARGET = 3;
	public final static int IO_EXCEPTION = 4;
	public final static int UNIMPLEMENTED = 5;
	public final static int KEY_ALLOCATION_CONFLICT = 6;
	public final static int KEY_NOT_FOUND = 7;
	
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
