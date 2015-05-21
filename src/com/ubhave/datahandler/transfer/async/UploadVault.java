package com.ubhave.datahandler.transfer.async;

import java.io.File;
import java.util.List;

import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.json.JSONObject;

import android.content.Context;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.except.DataHandlerException;

public class UploadVault implements UploadVaultInterface
{
	private final static String TAG = "UploadVault";
	private final Context context;
	private final ZipParameters parameters;
	private final DataHandlerConfig config;
	
//	final File uploadDirectory = new File((String) config.get(DataStorageConfig.LOCAL_STORAGE_UPLOAD_DIRECTORY_PATH));
//	final String uploadDirectoryName = (String) config.get(DataStorageConfig.LOCAL_STORAGE_UPLOAD_DIRECTORY_NAME);
	
	public UploadVault(final Context context)
	{
		this.context = context;
		this.parameters = getZipParameters();
		this.config = DataHandlerConfig.getInstance();
	}
	
	private ZipParameters getZipParameters()
	{
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
		
		final String pw = getEncryptionPassword();
		if (pw != null)
		{
			parameters.setEncryptFiles(true);
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
			parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
			parameters.setPassword(pw);
		}
		return parameters;
	}
	
	private String getEncryptionPassword()
	{
		try
		{
			return (String) config.get(DataStorageConfig.FILE_STORAGE_ENCRYPTION_PASSWORD);
		}
		catch (DataHandlerException e)
		{
			return null;
		}
	}
	
	private File getUploadDirectory()
	{
		File directory;
		String uploadDir = config.getLocalUploadDirectoryPath();
		directory = new File(uploadDir);
		if (!directory.exists())
		{
			directory.mkdirs();
		}
//		directory = context.getCacheDir();
//		if (!directory.exists())
//		{
//			directory.mkdirs();
//		}
//		else
//		{
//			for (File d : directory.listFiles())
//			{
//				if (d.getName().contains(DataStorageConstants.LOG_FILE_SUFFIX))
//				{
//					if (DataHandlerConfig.shouldLog())
//					{
//						Log.d(TAG, "Delete temp file: " + d.getName());
//					}
//					d.delete();
//				}
//			}
//		}
		return directory;
	}
	
	

	@Override
	public void writeData(final String dataName, final List<JSONObject> data) throws DataHandlerException
	{
		// TODO Auto-generated method stub
	}

}
