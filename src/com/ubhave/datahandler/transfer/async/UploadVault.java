package com.ubhave.datahandler.transfer.async;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONObject;

import com.alutam.ziputils.ZipEncryptOutputStream;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataStorageConstants;

public class UploadVault implements UploadVaultInterface
{
//	private final static String TAG = "UploadVault";
//	private final Context context;
	private final DataHandlerConfig config;
	
//	final File uploadDirectory = new File((String) config.get(DataStorageConfig.LOCAL_STORAGE_UPLOAD_DIRECTORY_PATH));
//	final String uploadDirectoryName = (String) config.get(DataStorageConfig.LOCAL_STORAGE_UPLOAD_DIRECTORY_NAME);
	
	public UploadVault()
	{
//		this.context = context;
		this.config = DataHandlerConfig.getInstance();
	}
	
	@Override
	public boolean isUploadDirectory(final File directory)
	{
//		String absoluteDir = (String) config.get(DataStorageConfig.LOCAL_STORAGE_ROOT_NAME);
//		return absoluteDir +"/"+ config.get(DataStorageConfig.LOCAL_STORAGE_UPLOAD_DIRECTORY_NAME);
//		File directory;
//		String uploadDir = config.getLocalUploadDirectoryPath();
//		directory = new File(uploadDir);
//		if (!directory.exists())
//		{
//			directory.mkdirs();
//		}
////		directory = context.getCacheDir();
////		if (!directory.exists())
////		{
////			directory.mkdirs();
////		}
////		else
////		{
////			for (File d : directory.listFiles())
////			{
////				if (d.getName().contains(DataStorageConstants.LOG_FILE_SUFFIX))
////				{
////					if (DataHandlerConfig.shouldLog())
////					{
////						Log.d(TAG, "Delete temp file: " + d.getName());
////					}
////					d.delete();
////				}
////			}
////		}
//		return directory;
		return false; // TODO implement
	}
	
	private String getEncryptionPassword()
	{
		return (String) config.get(DataStorageConfig.FILE_STORAGE_ENCRYPTION_PASSWORD, null);
	}	

	@Override
	public void writeData(final String dataName, final List<JSONObject> data) throws Exception
	{
		final String pw = getEncryptionPassword();
		final String fileName = config.getIdentifier() + "_"
								+ dataName + "_"
								+ System.currentTimeMillis() + "."
								+ DataStorageConstants.JSON_FILE_SUFFIX;
		final String zipName = fileName + DataStorageConstants.ZIP_FILE_SUFFIX;
		final OutputStream out;
		if (pw != null)
		{
			out = new ZipEncryptOutputStream(new FileOutputStream(zipName), pw);
		}
		else
		{
			out = new FileOutputStream(zipName);
		}
		
		writeCompressed(fileName, data, out);
	}
	
	private void writeCompressed(final String fileName, final List<JSONObject> entries, final OutputStream out) throws Exception
	{
		ZipOutputStream zos = new ZipOutputStream(out);
		ZipEntry ze = new ZipEntry(fileName);
        
		zos.putNextEntry(ze);
		for (JSONObject entry : entries)
		{
			String line = entry.toString() + "\n";
			out.write(line.getBytes());
		}

        zos.closeEntry();
		zos.close();
	}

	@Override
	public void writeData(final String dataName, final String data)
	{
		// TODO Auto-generated method stub	
	}
	
//	@Override
//	public void writeData(final String dataName, final File dataFile)
//	{
//		// TODO implement
//	}
}
