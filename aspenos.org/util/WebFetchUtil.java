
package org.aspenos.util;

import java.io.*;
import java.net.*;
import java.util.*;
import sun.misc.*;
import com.sun.*;



public class WebFetchUtil
{
	public static boolean printHeaders = false;
	public static boolean useStdHeaders = false;

	static final String stdReqHdr[] = {
//		"Connection", "Keep-Alive",
		"User-Agent", "Mozilla/4.5 [en] (X11; U; SunOS 5.6 sun4u)",
		"Accept", "image/gif, image/x-xbitmap, image/jpeg," +
			"image/pjpeg, image/png, */*",
		"Accept-Encoding", "gzip",
		"Accept-Language", "en",
		"Accept-Charset", "iso-8859-1,*,utf-8"
	};

	public static String doAuthGet(String user, String pwd,
		String theURL, Map nameValuePairs)
		throws Exception
	{
		String request = validateURL(theURL);

		if (nameValuePairs != null)
		{
			if (!request.endsWith("?"))  request += "?";
			request += encodeParams(nameValuePairs);
		}

		String auth = user + ":" + pwd;

		URL url = new URL(request);
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("Authorization", 
			"Basic " + Base64.encode(auth));

		getRequest(conn);
		return readResponse(conn);
	}

	public static String doGet(String theURL, Map nameValuePairs)
		throws Exception
	{
		String request = validateURL(theURL);

		if (nameValuePairs != null && nameValuePairs.size() > 0)
		{
			if (!request.endsWith("?"))
				request += "?";
			request += encodeParams(nameValuePairs);
		}

		URL url = new URL(request);
		URLConnection conn = url.openConnection();

		getRequest(conn);
		return readResponse(conn);
	}


	public static String doAuthPost(String user, String pwd,
		String theURL, Map nameValuePairs)
		throws Exception
	{
		String auth = user + ":" + pwd;
		URL url = new URL(validateURL(theURL));
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestProperty("Authorization", 
			"Basic " + Base64.encode(auth));

		postRequest(conn, nameValuePairs);
		return readResponse(conn);
	}

	public static String doPost(String theURL, Map nameValuePairs)
		throws Exception
	{
		URL url = new URL(validateURL(theURL));
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);

		postRequest(conn, nameValuePairs);
		return readResponse(conn);
	}

//=================================================================



	public static String readResponse(URLConnection conn)
		throws Exception
	{
		BufferedReader in;
		StringBuffer response = new StringBuffer();
		String line;

		try
		{ 
			in = new BufferedReader(new
				InputStreamReader(conn.getInputStream())); 
		}
		catch (Exception ex)
		{
			InputStream err = 
				((HttpURLConnection)conn).getErrorStream();
			if (err == null)  
			{
				System.err.println("Headers: " + getResponseHeaders(conn));
				throw ex;
			}
			in = new BufferedReader(new InputStreamReader(err));
		}

		if (printHeaders)
			response.append(getResponseHeaders(conn));

		while ((line = in.readLine()) != null)
			response.append(line + "\n");

		in.close();

		return response.toString();
	}

	public static String encodeParams(Map nameValuePairs)
	{
		StringBuffer params = new StringBuffer();
		Iterator nvp = nameValuePairs.keySet().iterator();
		String name, value;
		char ch;

		while (nvp.hasNext())
		{
			name = (String)nvp.next();
			value = (String)nameValuePairs.get(name);
			if (value == null) 
				value = "null";
			if (nvp.hasNext()) ch = '&'; else ch = '\n';
			params.append(name + "=" +
				URLEncoder.encode(value) + ch);
		}

		return params.toString();
	}

	public static String validateURL(String url)
	{
		if (!url.startsWith("http://"))
			return "http://" + url;
		else
			return url;
	}

	public static String getResponseHeaders(URLConnection conn)
	{
		StringBuffer headers = new StringBuffer();
		String key;
		int n=1;

		headers.append("\n----------\n");
		while ((key = conn.getHeaderFieldKey(n)) != null)
		{
			String value = conn.getHeaderField(n);
			headers.append(key + ": " + value + "\n");
			n++;
		}

		headers.append("----------\n");
		return headers.toString();
	}

	public static String getRequestHeaders(URLConnection conn)
	{
		StringBuffer headers = new StringBuffer();
		String key;
		int n=1;

		headers.append("\n----------\n");
		while ((key = conn.getHeaderFieldKey(n)) != null)
		{
			String value = conn.getHeaderField(n);
			headers.append(key + ": " + value + "\n");
			n++;
		}

		headers.append("----------\n");
		return headers.toString();
	}

	private static void postRequest(URLConnection conn, 
		Map nameValuePairs) throws Exception
	{
		if (nameValuePairs != null)
		{
			if (useStdHeaders)
			for (int i=0; i<stdReqHdr.length-1; i++)
				conn.setRequestProperty(stdReqHdr[i], 
					stdReqHdr[i+1]);

			PrintWriter out = 
				new PrintWriter(conn.getOutputStream());

			out.print(encodeParams(nameValuePairs));
			out.close();
		}
	}

	private static void getRequest(URLConnection conn)
		throws Exception
	{
		if (useStdHeaders)
		for (int i=0; i<stdReqHdr.length-1; i++)
			conn.setRequestProperty(stdReqHdr[i], 
				stdReqHdr[i+1]);

		conn.setDoOutput(true);
		conn.connect();
	}

//=================================================================

	public static void main(String args[])
	{
		String USER = null;
		String PWD = null;
		String URL = "www.yahoo.com";

		String response, name, value;
		Map hash = null;

		System.out.println("\nUSAGE:\nWebFetch [\"get\"|\"post\"] " +
				"url [name=value] [...]\n\n");

		try
		{
			WebFetchUtil.printHeaders = true;

			if (args.length > 0)
			{
				String method;
				int i, j;
				if (args[0].equalsIgnoreCase("post")) {
					method = "post";
					i=2;
				} else if (args[0].equalsIgnoreCase("get")) {
					method = "get";
					i=2;
				} else {
					method = "get";
					i=1;
				}

				j = i-1;

				hash = new Hashtable();
				while (i < args.length)
				{
					StringTokenizer st = new StringTokenizer(args[i++], "=");
					name = st.nextToken();
					value = st.nextToken();
					hash.put(name, value);
				}
				if (method.equals("post")) 
					response = WebFetchUtil.doPost(args[j], hash);
				else
					response = WebFetchUtil.doGet(args[j], hash);
			}
			else
				response = WebFetchUtil.doAuthGet(USER, PWD, URL, null);

			System.out.println(response);
		}
		catch (Exception ex)
		{ 
			System.err.println(ex.toString());
		}
	}

}





