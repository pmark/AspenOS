
package org.aspenos.util;

import java.io.*;
import java.net.*;
import java.util.*;
import sun.misc.*;
import com.sun.*;

//import org.aspenos.app.aoscontentserver.util.ServerInit;


public class WebFetch {

	private boolean _printHeaders = false;
	private boolean _useStdHeaders = false;
	
	static final String stdReqHdr[] = {
		"Connection", "Keep-Alive",
		"User-Agent", "Mozilla/4.5 [en] (X11; U; SunOS 5.6 sun4u)",
		"Accept", "image/gif, image/x-xbitmap, image/jpeg," +
			"image/pjpeg, image/png, */*",
		"Accept-Encoding", "gzip",
		"Accept-Language", "en",
		"Accept-Charset", "iso-8859-1,*,utf-8"
	};


	/////////////////////////////////////////////////
	///// RETURN THE PAGE ///////////////////////////
	/////////////////////////////////////////////////

	public String doAuthGet(String user, String pwd,
		String theURL, Map nameValuePairs)
		throws Exception {
		String request = validateURL(theURL);

		if (nameValuePairs != null) {
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

	public String doGet(String theURL, Map nameValuePairs)
		throws Exception {
		String request = validateURL(theURL);

		if (nameValuePairs != null && nameValuePairs.size() > 0) {
			if (!request.endsWith("?"))
				request += "?";
			request += encodeParams(nameValuePairs);
		}

		//System.out.println("req: " + request);
		URL url = new URL(request);
		//System.out.println("url: " + url.toString());
		URLConnection conn = url.openConnection();

		getRequest(conn);
		return readResponse(conn);
	}


	public String doAuthPost(String user, String pwd,
		String theURL, Map nameValuePairs)
		throws Exception {
		String auth = user + ":" + pwd;
		URL url = new URL(validateURL(theURL));
		URLConnection conn = url.openConnection();
		//conn.setDoOutput(true);
		conn.setRequestProperty("Authorization", 
			"Basic " + Base64.encode(auth));

		postRequest(conn, nameValuePairs);
		return readResponse(conn);
	}

	public String doPost(String theURL, Map nameValuePairs)
		throws Exception {
		URL url = new URL(validateURL(theURL));
		URLConnection conn = url.openConnection();
		//conn.setDoOutput(true);

		postRequest(conn, nameValuePairs);
		return readResponse(conn);
	}

	/////////////////////////////////////////////////
	///// RETURN THE CONNECTION STREAM //////////////
	/////////////////////////////////////////////////

	public InputStream doAuthGetStream(String user, String pwd,
		String theURL, Map nameValuePairs)
		throws Exception {
		String request = validateURL(theURL);

		if (nameValuePairs != null) {
			if (!request.endsWith("?"))  request += "?";
			request += encodeParams(nameValuePairs);
		}

		String auth = user + ":" + pwd;

		URL url = new URL(request);
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("Authorization", 
			"Basic " + Base64.encode(auth));

		getRequest(conn);
		return conn.getInputStream();
	}

	public InputStream doGetStream(String theURL, Map nameValuePairs)
		throws Exception {

		String request = validateURL(theURL);

		if (nameValuePairs != null && nameValuePairs.size() > 0) {
			if (!request.endsWith("?"))
				request += "?";
			request += encodeParams(nameValuePairs);
		}

		URL url = new URL(request);
		URLConnection conn = url.openConnection();


		getRequest(conn);
		return conn.getInputStream();
	}


	public InputStream doAuthPostStream(String user, String pwd,
		String theURL, Map nameValuePairs)
		throws Exception {
		String auth = user + ":" + pwd;
		URL url = new URL(validateURL(theURL));
		URLConnection conn = url.openConnection();
		//conn.setDoOutput(true);
		conn.setRequestProperty("Authorization", 
			"Basic " + Base64.encode(auth));

		postRequest(conn, nameValuePairs);
		return conn.getInputStream();
	}

	public InputStream doPostStream(String theURL, Map nameValuePairs)
		throws Exception {
		URL url = new URL(validateURL(theURL));
		URLConnection conn = url.openConnection();
		//conn.setDoOutput(true);

		postRequest(conn, nameValuePairs);
		return conn.getInputStream();
	}

//=================================================================



	public String readResponse(URLConnection conn)
		throws Exception {
		BufferedReader in;
		StringBuffer response = new StringBuffer();
		String line;

		try { 
			in = new BufferedReader(new
				InputStreamReader(conn.getInputStream())); 
		} catch (Exception ex) {
			InputStream err = 
				((HttpURLConnection)conn).getErrorStream();
			if (err == null)  {
				//ServerInit.log("ERROR: Headers: " + getResponseHeaders(conn));
				throw ex;
			}
			in = new BufferedReader(new InputStreamReader(err));
		}

		if (_printHeaders)
			response.append(getResponseHeaders(conn));

		while ((line = in.readLine()) != null)
			response.append(line + "\n");

		in.close();

		return response.toString();
	}

	public String encodeParams(Map nameValuePairs) {
		if (nameValuePairs == null)
			return "";
		StringBuffer params = new StringBuffer();
		Iterator nvp = nameValuePairs.keySet().iterator();
		String name, value;
		char ch;

		while (nvp.hasNext()) {
			name = (String)nvp.next();
			value = (String)nameValuePairs.get(name);
			if (value == null) value = "null";
			if (nvp.hasNext()) ch = '&'; else ch = '\n';
			params.append(name + "=" +
				URLEncoder.encode(value) + ch);
		}

		return params.toString();
	}

	public String validateURL(String url) {
		if (!url.startsWith("http://") && 
			!url.startsWith("https://")) {
			return "http://" + url;
		} else {
			return url;
		}
	}

	public String getResponseHeaders(URLConnection conn) {
		StringBuffer headers = new StringBuffer();
		String key;
		int n=1;

		headers.append("\n----------\n");
		while ((key = conn.getHeaderFieldKey(n)) != null) {
			String value = conn.getHeaderField(n);
			headers.append(key + ": " + value + "\n");
			n++;
		}

		headers.append("----------\n");
		return headers.toString();
	}

	public String getRequestHeaders(URLConnection conn) {
		StringBuffer headers = new StringBuffer();
		String key;
		int n=1;

		headers.append("\n----------\n");
		while ((key = conn.getHeaderFieldKey(n)) != null) {
			String value = conn.getHeaderField(n);
			headers.append(key + ": " + value + "\n");
			n++;
		}

		headers.append("----------\n");
		return headers.toString();
	}

	private void postRequest(URLConnection conn, 
			Map nameValuePairs) throws Exception {
		if (nameValuePairs != null) {
			if (_useStdHeaders)
			for (int i=0; i<stdReqHdr.length-1; i++)
				conn.setRequestProperty(stdReqHdr[i], 
					stdReqHdr[i+1]);

			// added from iSaSiLk example on 2/15/01
			conn.setDoInput(true);

			conn.setDoOutput(true);
			PrintWriter out = 
				new PrintWriter(conn.getOutputStream());

			//System.out.println("WF: encoded params: " + encodeParams(nameValuePairs));

			out.print(encodeParams(nameValuePairs));
			out.close();
		}
	}

	private void getRequest(URLConnection conn)
		throws Exception {
		if (_useStdHeaders)
		for (int i=0; i<stdReqHdr.length-1; i++)
			conn.setRequestProperty(stdReqHdr[i], 
				stdReqHdr[i+1]);

		conn.setDoOutput(true);
		conn.connect();
	}

//=================================================================

	public static void main(String args[]) {
		String USER = null;
		String PWD = null;
		String URL = "https://www.telski.com";

		String response, name, value;
		HashMap hash = null;


		// set the HTTPS URL handler to Sun's JSSE impl
		System.setProperty("java.protocol.handler.pkgs", 
			"com.sun.net.ssl.internal.www.protocol");

		System.out.println("\nUSAGE:\nWebFetch [\"get\"|\"post\"] " +
				"url [name=value] [...]\n\n");

		try {
			WebFetch fetcher = new WebFetch();
			//_printHeaders = true;

			if (args.length > 0) {
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

				hash = new HashMap();
				while (i < args.length) {
					StringTokenizer st = new StringTokenizer(args[i++], "=");
					name = st.nextToken();
					value = st.nextToken();
					hash.put(name, value);
				}
				if (method.equals("post")) 
					response = fetcher.doPost(args[j], hash);
				else
					response = fetcher.doGet(args[j], hash);
			} else {
				System.out.println("Getting: " + URL);
				response = fetcher.doGet(URL, null);
				//response = fetcher.doAuthGet(USER, PWD, URL, null);
			}

			System.out.println(response);
		} catch (Exception ex) { 
			System.err.println(ex.toString());
		}
	}

}





