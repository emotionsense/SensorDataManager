package com.ubhave.datastore.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.except.DataHandlerException;

public class FileVault
{
	private static final String TAG = "FileVault";
	private static final String CIPHER_ALGORITHM = "AES";
	private static final String PASSWORD_HASH_ALGORITHM = "SHA-256";
	private static final String UTF8 = "UTF-8";

	private final Key key;

	public FileVault()
	{
		this.key = buildKey();
	}
	
	public Key buildKey()
	{
		try
		{
			DataHandlerConfig config = DataHandlerConfig.getInstance();
			String password = (String) config.get(DataStorageConfig.FILE_STORAGE_ENCRYPTION_PASSWORD);
			MessageDigest digester = MessageDigest.getInstance(PASSWORD_HASH_ALGORITHM);
			digester.update(String.valueOf(password).getBytes(UTF8));
			SecretKeySpec spec = new SecretKeySpec(digester.digest(), CIPHER_ALGORITHM);
			return spec;
		}
		catch (DataHandlerException e)
		{
			if (DataHandlerConfig.shouldLog())
			{
				Log.d(TAG, "Warning: No encryption password set. Data will be stored in clear text.");
			}
			return null;
		}
		catch (Exception e)
		{
			Log.e(TAG, "Error: "+e.getMessage());
			e.printStackTrace();
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
}
