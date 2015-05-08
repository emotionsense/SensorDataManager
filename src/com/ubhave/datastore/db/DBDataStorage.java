package com.ubhave.datastore.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataStorageConfig;
import com.ubhave.datahandler.config.DataStorageConstants;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.datastore.DataStorageInterface;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class DBDataStorage implements DataStorageInterface {
	private static final String TAG = "LogDBDataStorage";
	private static final String DEFAULT_DB_NAME = "com.ubhave.datastore";
	private static final Object fileTransferLock = new Object();

	private final Context context;
	private final DataTables dataTables;
	private static final String ARCHIVE_PW = "HGKnSl4mN0805Kft";
	private final ZipParameters parameters;

	public DBDataStorage(final Context context) {
		this.context = context;
		this.dataTables = new DataTables(context, getDBName());
		parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
		parameters.setEncryptFiles(true);
		parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
		parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
		parameters.setPassword(ARCHIVE_PW);
	}

	private String getDBName() {
		try {
			return (String) DataHandlerConfig.getInstance().get(
					DataStorageConfig.LOCAL_STORAGE_ROOT_NAME);
		} catch (Exception e) {
			e.printStackTrace();
			return DEFAULT_DB_NAME;
		}
	}

	private File getCleanCacheDir() {
		File outputDir = context.getCacheDir();
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		} else
			for (File d : outputDir.listFiles()) {
				if (d.getName().contains(DataStorageConstants.LOG_FILE_SUFFIX)) {
					if (DataHandlerConfig.shouldLog()) {
						Log.d(TAG, "Delete temp file: " + d.getName());
					}
					d.delete();
				}
			}
		return outputDir;
	}

	@Override
	public void onDataUploaded() {
		getCleanCacheDir();
		for (String tableName : dataTables.getTableNames()) {
			Log.d("DB", tableName + " set synced");
			dataTables.setSynced(tableName);
		}
	}

	private synchronized int writeMaxEntries(final File outputFile,
			List<JSONObject> entries) throws IOException, ZipException {
		int written = 0;
		final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
				outputFile));
		File tmpFile = File.createTempFile("data", null);
		try {
			List<JSONObject> entriesCopy = new ArrayList<JSONObject>();
			for (JSONObject e : entries) {
				entriesCopy.add(e);
			}

			// First, write dummy data to tmpFile to get right parameters
			OutputStream os = new FileOutputStream(tmpFile);
			for (int i = 0; i < DataStorageConstants.UPLOAD_FILE_MAX_LINES; i++) {
				if (!entriesCopy.isEmpty()) {
					JSONObject entry = entries.get(0);
					byte[] data = (entry.toString() + "\n").getBytes();
					os.write(data, 0, data.length);
					entriesCopy.remove(0);
				} else {
					break;
				}
			}
			os.close();

			out.putNextEntry(tmpFile, parameters);

			for (int i = 0; i < DataStorageConstants.UPLOAD_FILE_MAX_LINES; i++) {
				if (!entries.isEmpty()) {
					JSONObject entry = entries.get(0);
					byte[] data = (entry.toString() + "\n").getBytes();
					out.write(data, 0, data.length);
					entries.remove(0);
					written++;
				} else {
					break;
				}
			}
		} finally {
			tmpFile.delete();
			out.closeEntry();
			out.finish();
			out.close();
		}

		return written;
	}

	private void writeEntries(final File outputDir, final String id,
			final String tableName, final List<JSONObject> entries)
			throws IOException {

		while (!entries.isEmpty()) {
			synchronized (fileTransferLock) {
				String zipFileName = id + "_" + tableName + "_"
						+ System.currentTimeMillis()
						+ DataStorageConstants.LOG_FILE_SUFFIX
						+ DataStorageConstants.ZIP_FILE_SUFFIX;

				final File outputFile = new File(outputDir, zipFileName);
				if (DataHandlerConfig.shouldLog()) {
					Log.d(TAG, "Writing to: " + outputFile.getAbsolutePath());
				}
				int written;
				try {
					written = writeMaxEntries(outputFile, entries);
				} catch (ZipException e) {
					Log.e(TAG, "Failed writing " + outputFile.getAbsolutePath());
					e.printStackTrace();
					outputFile.delete();
					break;
				}
				if (written == 0) {
					outputFile.delete();
					break;
				}
			}
		}
	}

	@Override
	public String prepareDataForUpload() {
		if (DataHandlerConfig.shouldLog()) {
			Log.d(TAG, "DB prepareDataForUpload()");
		}

		try {
			DataHandlerConfig config = DataHandlerConfig.getInstance();
			String id = config.getIdentifier();

			File outputDir = getCleanCacheDir();

			int written = 0;
			for (String tableName : dataTables.getTableNames()) {
				try {
					List<JSONObject> entries = dataTables
							.getUnsyncedData(tableName);
					if (DataHandlerConfig.shouldLog()) {
						Log.d(TAG,
								"Prepare: " + tableName + " has "
										+ entries.size()
										+ " entries for upload.");
					}
					if (!entries.isEmpty()) {
						writeEntries(outputDir, id, tableName, entries);
						written++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (written == 0) {
				Log.d(TAG, "DB prepareDataForUpload(): no data to upload.");
				return null;
			}
			return outputDir.getAbsolutePath();
		} catch (DataHandlerException e) {
			if (DataHandlerConfig.shouldLog()) {
				e.printStackTrace();
			}

			return null;
		}
	}

	@Override
	public List<SensorData> getRecentSensorData(int sensorId,
			long startTimestamp) throws ESException, IOException {
		String tableName = SensorUtils.getSensorName(sensorId);
		JSONFormatter formatter = DataFormatter.getJSONFormatter(context,
				sensorId);
		return dataTables.getRecentSensorData(tableName, formatter,
				startTimestamp);
	}

	@Override
	public void logSensorData(final SensorData data,
			final DataFormatter formatter) throws DataHandlerException {
		String sensorName;
		try {
			sensorName = SensorUtils.getSensorName(data.getSensorType());
		} catch (ESException e) {
			sensorName = DataStorageConstants.UNKNOWN_SENSOR;
			e.printStackTrace();
		}
		dataTables.writeData(sensorName, formatter.toString(data));
	}

	@Override
	public void logExtra(final String tag, final String data)
			throws DataHandlerException {
		dataTables.writeData(tag, data);
	}
}
