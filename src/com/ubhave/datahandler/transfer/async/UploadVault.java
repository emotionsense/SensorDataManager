package com.ubhave.datahandler.transfer.async;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONObject;

import com.alutam.ziputils.ZipEncryptOutputStream;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;

public class UploadVault implements UploadVaultInterface
{
	private final static String TAG = "UploadVault";
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
	public File getUploadDirectory()
	{
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
		return null; // TODO implement
	}
	
	private String getEncryptionPassword()
	{
		return (String) config.get(DataStorageConfig.FILE_STORAGE_ENCRYPTION_PASSWORD, null);
	}	

	@Override
	public void writeData(final String dataName, final List<JSONObject> data) throws Exception
	{
		final String pw = getEncryptionPassword();
		if (pw != null)
		{
			writeEncrypted(dataName, data, pw);
		}
	}
	
	private void writeEncrypted(final String dataName, final List<JSONObject> entries, final String password) throws Exception
	{
		// TODO name the files correctly
		ZipEncryptOutputStream zeos = new ZipEncryptOutputStream(new FileOutputStream("NewFile.zip"), password);
		ZipOutputStream zos = new ZipOutputStream(zeos);
		ZipEntry ze = new ZipEntry("EntryFile.json");
        
		zos.putNextEntry(ze);
        write(entries, zos);

        zos.closeEntry();
		zos.close();
	}
	
	private void write(final List<JSONObject> entries, final OutputStream out) throws IOException
	{
		for (JSONObject entry : entries)
		{
			String line = entry.toString() + "\n";
			out.write(line.getBytes());
		}
	}

	@Override
	public void writeData(String dataName, String data)
	{
		// TODO Auto-generated method stub	
	}
}
