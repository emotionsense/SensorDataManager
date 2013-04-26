package com.ubhave.datahandler.except;

public class FileSyncException extends DataHandlerException
{
	private static final long serialVersionUID = 9024298856203245145L;
	
	public final static int KEY_ALLOCATION_CONFLICT = 20;
	public final static int KEY_NOT_FOUND = 21;
	public final static int REQUEST_ALREADY_EXISTS = 22;
	public final static int NO_LISTENER = 23;
	
	public FileSyncException(int code)
	{
		super(code);
	}
}
