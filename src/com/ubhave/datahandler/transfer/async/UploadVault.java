package com.ubhave.datahandler.transfer.async;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONObject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import com.alutam.ziputils.ZipEncryptOutputStream;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataStorageConstants;
import com.ubhave.datahandler.except.DataHandlerException;

public class UploadVault implements UploadVaultInterface
{
	private final static String TAG = "UploadVault";
	private final Context context;
	private final DataHandlerConfig config;
	
	public UploadVault(final Context context)
	{
		this.context = context;
		this.config = DataHandlerConfig.getInstance();
	}
	
	@Override
	public boolean isUploadDirectory(final File directory) throws DataHandlerException
	{
		return directory.getAbsolutePath().equals(getUploadDirectory().getAbsolutePath());
	}
	
	private boolean canWriteToExternalStorage()
	{
		String writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
		String writeableState = Environment.getExternalStorageState();
		return context.checkCallingOrSelfPermission(writePermission) == PackageManager.PERMISSION_GRANTED
				&& Environment.MEDIA_MOUNTED.equals(writeableState);
	}
	
	@Override
	public File getUploadDirectory() throws DataHandlerException
	{
		final String uploadName = (String) config.get(DataStorageConfig.LOCAL_STORAGE_UPLOAD_DIRECTORY_NAME);
		File root;
		if (canWriteToExternalStorage())
		{
			final String rootName = (String) config.get(DataStorageConfig.LOCAL_STORAGE_ROOT_NAME);
			root = new File(Environment.getExternalStorageDirectory(), rootName);
		}
		else
		{
			root = context.getFilesDir();
		}
		
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
	
	private String createFileName(final String dataName) throws DataHandlerException
	{
		return config.getIdentifier() + "_"
				+ dataName + "_"
				+ System.currentTimeMillis() + "."
				+ DataStorageConstants.JSON_FILE_SUFFIX;
	}

	@Override
	public void writeData(final String dataName, final List<JSONObject> data) throws Exception
	{
		final String fileName = createFileName(dataName);
		final File zipFile = new File(getUploadDirectory(), fileName + DataStorageConstants.ZIP_FILE_SUFFIX);
		final OutputStream out;
		final String pw = (String) config.get(DataStorageConfig.ENCRYPTION_PASSWORD, null);
		if (pw != null)
		{
			out = new ZipEncryptOutputStream(new FileOutputStream(zipFile), pw);
		}
		else
		{
			out = new FileOutputStream(zipFile);
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
}
