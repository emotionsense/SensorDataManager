package com.ubhave.datahandler.logdata;

public class UserInteraction extends AbstractLogData
{
	public final static String TAG = "Interaction";
	private final String tag;
	
	public UserInteraction(final String tag, final String action, final String detail)
	{
		super(action, detail);
		this.tag = tag;
	}
	
	@Override
	public String getDataType()
	{
		return tag;
	}
}
