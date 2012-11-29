/* **************************************************
 Copyright (c) 2012, University of Cambridge
 Neal Lathia, neal.lathia@cl.cam.ac.uk
 Kiran Rachuri, kiran.rachuri@cl.cam.ac.uk

This application was developed as part of the EPSRC Ubhave (Ubiquitous and
Social Computing for Positive Behaviour Change) Project. For more
information, please visit http://www.emotionsense.org

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ************************************************** */

package com.ubhave.datahandler.transfer;


// OLD CODE >> REFERENCE ONLY

public class FileUploadService extends Thread
{
//	private static FileUploadService fileUploadService;
//	private static Object lock = new Object();
//	private static String LOG_TAG = "FileUploadService";
//	private boolean isRunning = true;
//
//	private FileUploadService()
//	{
//	}
//
//	public void shutdown()
//	{
//		isRunning = false;
//		this.interrupt();
//	}
//
//	public static void startFileUploadService()
//	{
//		if (fileUploadService == null)
//		{
//			synchronized (lock)
//			{
//				if (fileUploadService == null)
//				{
//					fileUploadService = new FileUploadService();
//					fileUploadService.start();
//					ESLogger.log(LOG_TAG, "startFileUploadService() file upload service initialized");
//				}
//			}
//		}
//	}
//
//	public static FileUploadService getFileUploadService()
//	{
//		if (fileUploadService == null)
//		{
//			ESLogger.log(LOG_TAG, "getFileUploadService() start service first.");
//		}
//		return fileUploadService;
//	}
//
//	// this service uploads the logs to the server. the logs are periodically
//	// moved from logs folder to to_be_uploaded folder and then uploaded.
//	// the upload is performed only when the phone is charging and is connected
//	// to a WiFi network
//	public void run()
//	{
//		// SharedPreferences preferences =
//		// SurveyApplication.getContext().getSharedPreferences(Constants.PREFS_NAME,
//		// Activity.MODE_PRIVATE);
//		// SharedPreferences.Editor editor = preferences.edit();
//		while (isRunning)
//		{
//			try
//			{
//				// 15 minutes
//				Utilities.sleep(15 * 60 * 1000);
//
//				// move logs
//				if (!UserPreferences.logsHaveMoved())
//				{
//					ESLogger.log(LOG_TAG, "preferences does not contain last logs moved time");
//					UserPreferences.setLogsMoved(System.currentTimeMillis());
//				}
//
//				long lastLogsMovedTime = UserPreferences.getLogsMoved(System.currentTimeMillis());
//				long timeElapsedLogsMoved = (System.currentTimeMillis() - lastLogsMovedTime);
//				ESLogger.log(LOG_TAG, "lastLogsMovedTime: " + lastLogsMovedTime + " timeElapsed: " + timeElapsedLogsMoved);
//
//				if (timeElapsedLogsMoved > Constants.LOG_FILE_UPLOAD_INTERVAL)
//				{
//					// move all log files to to_be_uploaded folder
//					DataLogger.getDataLogger().moveFilesForUploading(Constants.TO_BE_UPLOADED_LOGS_DIR);
//					UserPreferences.setLogsMoved(System.currentTimeMillis());
//					// updated last moved time
//				}
//
//				// upload logs
//				if (!UserPreferences.lastUploadSet())
//				{
//					ESLogger.log(LOG_TAG, "preferences does not contain last uploaded time");
//					UserPreferences.setUploadTime(System.currentTimeMillis());
//				}
//
//				long lastUploadTime = UserPreferences.getUploadTime(System.currentTimeMillis());
//				long timeElapsedLastUpload = (System.currentTimeMillis() - lastUploadTime);
//				ESLogger.log(LOG_TAG, "lastUploadTime: " + lastUploadTime + " timeElapsed: " + timeElapsedLastUpload);
//
//				if (timeElapsedLastUpload > Constants.LOG_FILE_UPLOAD_INTERVAL)
//				{
//					// if wifi connected
//					if (Utilities.isWiFiConnected())
//					{
//						ESLogger.log(LOG_TAG, "Wifi connected");
//
//						// upload log and pcm files to the server
//						String[] uploadFileTypes = new String[] { ".log", ".pcm" };
//
//						// upload files
//						boolean uploaded = uploadAllFilesToServer(Constants.TO_BE_UPLOADED_LOGS_DIR, uploadFileTypes);
//
//						if (uploaded)
//						{
//							// update last upload timestamp
//							UserPreferences.setUploadTime(System.currentTimeMillis());
//						}
//					}
//				}
//			}
//			catch (Exception exp)
//			{
//				ESLogger.error(LOG_TAG, exp);
//				Utilities.sleep(120 * 1000); // 2 minutes
//			}
//		}
//	}
//
//	public static boolean uploadAllFilesToServer(String logsDir, String[] fileTypes)
//	{
//		boolean success = true;
//		for (String afileType : fileTypes)
//		{
//			File[] allFiles = Utilities.getAllFiles(Constants.TO_BE_UPLOADED_LOGS_DIR, afileType);
//			if (allFiles.length > 0)
//			{
//				ESLogger.log(LOG_TAG, "run() uploading files to server");
//
//				int batchSize;
//				if (afileType.equals(".pcm"))
//				{
//					batchSize = 100;
//				}
//				else
//				{
//					batchSize = 10;
//				}
//
//				// log, pcm files should be uploaded in batches, otherwise if
//				// there are many files then the size of the zip file to be
//				// uploaded can become too large, and this may cause issues if
//				// the webserver has limit on the filesizes to be uploaded
//
//				// divide into batches
//				ArrayList<File[]> fileBatches = Utilities.divideIntoBatches(allFiles, batchSize);
//
//				ESLogger.log(LOG_TAG, "uploadAllFilesToServer() divided " + allFiles.length + " files to " + fileBatches.size() + " batches of size " + batchSize);
//
//				for (File[] afileBatch : fileBatches)
//				{
//					// zip and upload all log files
//					String zipFile = Utilities.zipFiles(logsDir, afileType, afileBatch);
//					HashMap<String, String> paramsMap = new HashMap<String, String>();
//					paramsMap.put("password", Constants.SERVER_UPLOAD_PASSWD);
//
//					ESLogger.log(LOG_TAG, "uploadAllFilesToServer() uploading file: " + zipFile);
//
//					String response = WebConnection.uploadFileToServer(Constants.WEB_URL_UPLOAD_LOG, zipFile, "application/zip", paramsMap);
//
//					if (response.equals(Constants.FILES_UPLOAD_SUCCESS_RESPONSE))
//					{
//						ESLogger.log(LOG_TAG, "run() files uploaded, response: " + response);
//						// remove all local files
//						Utilities.deleteFiles(Constants.TO_BE_UPLOADED_LOGS_DIR, afileBatch);
//						// TODO zip files are not deleted
//					}
//					else
//					{
//						success = false;
//						ESLogger.error(LOG_TAG, "error response received while uploading logs to server: " + response);
//					}
//				}
//			}
//			else
//			{
//				ESLogger.log(LOG_TAG, "run() no files to upload to server, filetype: " + afileType);
//			}
//		}
//		return success;
//	}
}
