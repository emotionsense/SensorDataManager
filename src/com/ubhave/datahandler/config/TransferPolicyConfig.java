package com.ubhave.datahandler.config;


public abstract class TransferPolicyConfig
{
	/*
	 * Config Values
	 */
	public final static int NO_TRANSFER = -1; // No transfer (store only)
	public final static int TRANSFER_IMMEDIATE = 0; // Post immediately (error on no connection)
	public final static int TRANSFER_INTERVAL = 1;
	
	public final static int ALL_CONNECTIONS = 0;
	public final static int WIFI_ONLY = 1;
}
