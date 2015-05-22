package com.ubhave.datastore.file.clean;

import java.io.File;
import java.io.IOException;

import com.ubhave.datahandler.except.DataHandlerException;

public interface DirectoryCleaner
{
	public int moveDirectoryContentsForUpload(final File directory) throws DataHandlerException, IOException;
}
