package com.ubhave.datahandler.store;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataStorage
{
	private BufferedWriter writer;
	
	public boolean createDirectory(final String dir)
	{
		File directory = new File(dir);
		if (!directory.exists())
		{
			return directory.mkdirs();
		}
		return true;
	}
	
	public void openFile(final String fn) throws IOException
	{
		writer = new BufferedWriter(new FileWriter(fn, true));
	}
	
	public void writeLine(final String fn, final String data) throws IOException
	{
		writer.write(data);
		writer.newLine();
	}
	
	public void closeFile() throws IOException
	{
		writer.flush();
		writer.close();
	}
}
