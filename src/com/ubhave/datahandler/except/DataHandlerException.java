package com.ubhave.datahandler.except;

public class DataHandlerException extends Exception
{
	private static final long serialVersionUID = 8240175615135197888L;
	
	public final static int UNKNOWN_CONFIG 					= 10;
	public final static int NO_URL_TARGET 					= 11;
	public final static int IO_EXCEPTION 					= 12;
	public final static int UNIMPLEMENTED 					= 13;
	public final static int WRITING_TO_DEFAULT_DIRECTORY 	= 14;
	public final static int CONFIG_CONFLICT 				= 15;
	public final static int POST_FAILED 					= 16;
	public final static int NO_DATA							= 17;
	
	private final static String MESSAGE_UNKNOWN_CONFIG		= "Unknown config key.";
	private final static String MESSAGE_NO_URL				= "Missing URL target.";
	private final static String MESSAGE_IO_EXCEPT			= "I/O Error!";
	private final static String MESSAGE_UNIMPLEMENTED		= "This feature is unimplemented";
	private final static String MESSAGE_DEFAULT				= "Error: attempting to write to default directory.";
	private final static String MESSAGE_CONFIG_CONFLICT		= "Conflict in config values!";
	private final static String MESSAGE_POST_FAIL			= "Failure posting data to server.";
	private final static String MESSAGE_NO_DATA				= "No data.";
	
	private final int errorCode;
	
	public DataHandlerException(int code)
	{
		errorCode = code;
	}
	
	public int getErrorCode()
	{
		return errorCode;
	}

	@Override
	public String getMessage()
	{
		switch (errorCode)
		{
		case UNKNOWN_CONFIG:
			return MESSAGE_UNKNOWN_CONFIG;
		case NO_URL_TARGET:
			return MESSAGE_NO_URL;
		case IO_EXCEPTION:
			return MESSAGE_IO_EXCEPT;
		case UNIMPLEMENTED:
			return MESSAGE_UNIMPLEMENTED;
		case WRITING_TO_DEFAULT_DIRECTORY:
			return MESSAGE_DEFAULT;
		case CONFIG_CONFLICT:
			return MESSAGE_CONFIG_CONFLICT;
		case POST_FAILED:
			return MESSAGE_POST_FAIL;
		case NO_DATA:
			return MESSAGE_NO_DATA;
		}
		return super.getMessage();
	}
}
