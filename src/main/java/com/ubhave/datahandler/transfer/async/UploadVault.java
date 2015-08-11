package com.ubhave.datahandler.transfer.async;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.alutam.ziputils.ZipEncryptOutputStream;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataStorageConstants;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datastore.file.FileVault;

public class UploadVault extends FileVault implements UploadVaultInterface
{
	private final static String TAG = "UploadVault";
	private static UploadVault instance;
	
	public static UploadVault getInstance(final Context context, final String dataPassword)
	{
		if (instance == null)
		{
			instance = new UploadVault(context, dataPassword);
		}
		return instance;
	}
	
	protected UploadVault(final Context context, final String dataPassword)
	{
		super(context, dataPassword);
	}
	
	@Override
	public boolean isUploadDirectory(final File directory) throws DataHandlerException
	{
		return directory.getAbsolutePath().equals(getUploadDirectory().getAbsolutePath());
	}
	
	@Override
	public File getUploadDirectory() throws DataHandlerException
	{
		final String uploadName = (String) config.get(DataStorageConfig.LOCAL_STORAGE_UPLOAD_DIRECTORY_NAME);
		if (canWriteToExternalStorage())
		{
			return getDirectory(getLocalDirectory(), uploadName);
		}
		else
		{
			File root = context.getFilesDir();
			File uploadDir = new File(root, uploadName);
			if (!uploadDir.exists())
			{
				if (DataHandlerConfig.shouldLog())
				{
					Log.d(TAG, "Creating upload dir: "+uploadDir.getAbsolutePath());
				}
				uploadDir.mkdirs();
			}
			return uploadDir;
		}
	}
	
	private String createFileName(final String dataName) throws DataHandlerException
	{
		return config.getIdentifier() + "_"
				+ dataName + "_"
				+ System.currentTimeMillis();
	}

	@Override
	public void writeData(final String dataName, final List<JSONObject> data) throws Exception
	{
		if (!data.isEmpty())
		{
			final String fileName = createFileName(dataName);
			final File zipFile = new File(getUploadDirectory(), fileName + DataStorageConstants.ZIP_FILE_SUFFIX);
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "Creating upload file: "+zipFile.getAbsolutePath());
			}
			final OutputStream out;
			final String dataPassword = getPassword();
			if (dataPassword != null)
			{
				out = new ZipEncryptOutputStream(new FileOutputStream(zipFile), dataPassword);
			}
			else
			{
				out = new FileOutputStream(zipFile);
			}
			
			ZipOutputStream zos = new ZipOutputStream(out);
			writeCompressed(fileName, data, zos);
			zos.close();
			out.close();
		}
	}
	
	private void writeCompressed(final String fileName, final List<JSONObject> entries, final ZipOutputStream zos) throws Exception
	{
		ZipEntry ze = new ZipEntry(fileName + DataStorageConstants.JSON_FILE_SUFFIX);
		zos.putNextEntry(ze);
		if (DataHandlerConfig.shouldLog())
		{
			Log.d(TAG, "Write "+entries.size()+" entries.");
		}
		for (JSONObject entry : entries)
		{
			String line = entry.toString() + "\n";
			zos.write(line.getBytes());
		}
        zos.closeEntry();
	}
}
