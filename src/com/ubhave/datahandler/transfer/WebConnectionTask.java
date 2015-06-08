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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;

import android.os.AsyncTask;

import com.ubhave.datahandler.config.DataHandlerConfig;
import com.ubhave.datahandler.config.DataTransferConfig;

public class WebConnectionTask extends AsyncTask<Void, Void, String>
{
	private final String serverUrl;
	private final HashMap<String, String> params;
	private final File file;
	
	public WebConnectionTask(final String serverUrl, final File file, final HashMap<String, String> params)
	{
		this.serverUrl = serverUrl;
		this.file = file;
		this.params = params;
	}
	
	public WebConnectionTask(final String serverUrl, final HashMap<String, String> params)
	{
		this(serverUrl, null, params);
	}
	
	@Override
	protected String doInBackground(Void... ps)
	{
		String response = "";
		try
		{
			HttpClient httpclient = new DefaultHttpClient();
			httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60 * 1000);

			HttpPost httppost = new HttpPost(serverUrl);
			MultipartEntity multipartEntity = new MultipartEntity();

			if (file != null)
			{
				FileBody fileBody = new FileBody(file);
				if (fileBody != null)
				{
					DataHandlerConfig config = DataHandlerConfig.getInstance();
					String postKey = (String) config.get(DataTransferConfig.POST_KEY);
					multipartEntity.addPart(postKey, fileBody);
				}
			}

			if (params != null)
			{
				for (String key : params.keySet())
				{
					String value = params.get(key);
					multipartEntity.addPart(key, new StringBody(value));
				}
			}

			httppost.setEntity(multipartEntity);
			HttpResponse httpResponse = httpclient.execute(httppost);
			//int status = response.getStatusLine().getStatusCode(); // TODO check response code

			response = convertStreamToString(httpResponse.getEntity().getContent());
			httpclient.getConnectionManager().shutdown();
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
		}
		return response;
	}
	
	private String convertStreamToString(InputStream is)
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try
		{
			while ((line = reader.readLine()) != null)
			{
				sb.append((line));
			}
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}
}
