package com.ubhave.datastore.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataStorageConstants;

public class FileVault
{
	private static final String TAG = "FileVault";
	private static final String CIPHER_ALGORITHM = "AES";
	private static final String PASSWORD_HASH_ALGORITHM = "SHA-256";
	private static final String UTF8 = "UTF-8";

	private static final HashMap<String, Object> lockMap = new HashMap<String, Object>();
	
	public static Object getLock(final String key)
	{
		Object lock;
		synchronized (lockMap)
		{
			if (lockMap.containsKey(key))
			{
				lock = lockMap.get(key);
			}
			else
			{
				lock = new Object();
				lockMap.put(key, lock);
			}
		}
		return lock;
	}
	
	private final String dataPassword;
	private final Key key;
	
	public FileVault(final String dataPassword)
	{
		this.dataPassword = dataPassword;
		this.key = buildKey();
	}
	
	public String getPassword()
	{
		return dataPassword;
	}
	
	public Key buildKey()
	{
		if (dataPassword != null)
		{
			try
			{
				MessageDigest digester = MessageDigest.getInstance(PASSWORD_HASH_ALGORITHM);
				digester.update(String.valueOf(dataPassword).getBytes(UTF8));
				SecretKeySpec spec = new SecretKeySpec(digester.digest(), CIPHER_ALGORITHM);
				return spec;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		else
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "Warning: No encryption password set. Data will be stored in clear text.");
			}
			return null;
		}
	}

	private Cipher buildCipher(int mode) throws Exception
	{
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		cipher.init(mode, key);
		return cipher;
	}
	
	public boolean isEncrypted()
	{
		return key != null;
	}
	
	public OutputStream openForWriting(final File dataFile) throws Exception
	{
		if (key == null)
		{
			return new FileOutputStream(dataFile, true);
		}
		else
		{
			Cipher cipher = buildCipher(Cipher.ENCRYPT_MODE);
			return new CipherOutputStream(new FileOutputStream(dataFile), cipher);
		}
	}
	
	public InputStream openForReading(final File dataFile) throws Exception
	{
		if (key == null)
		{
			return new FileInputStream(dataFile);
		}
		else
		{
			Cipher cipher = buildCipher(Cipher.DECRYPT_MODE);
			return new CipherInputStream(new FileInputStream(dataFile), cipher);
		}
	}
	
	public boolean isDueForUpload(final File file)
	{
		if (file != null)
		{
			String fileName = file.getName();
			if (fileName.contains(DataStorageConstants.ZIP_FILE_SUFFIX))
			{
				String timeStr = fileName.substring(0, fileName.indexOf(DataStorageConstants.ZIP_FILE_SUFFIX));
				long fileTimestamp = Long.parseLong(timeStr);
				long currTime = System.currentTimeMillis();
				
				DataHandlerConfig config = DataHandlerConfig.getInstance();
				long durationLimit = (Long) config.get(DataStorageConfig.DATA_LIFE_MILLIS, DataStorageConfig.DEFAULT_FILE_LIFE_MILLIS);
				if ((currTime - fileTimestamp) > durationLimit)
				{
					return true;
				}
			}
		}
		return false;
	}
}
