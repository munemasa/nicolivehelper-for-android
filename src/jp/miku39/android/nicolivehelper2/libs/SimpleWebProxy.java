package jp.miku39.android.nicolivehelper2.libs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class SimpleWebProxy {
	final static String TAG = "SimpleWebProxy";

	private static ServerSocket sServerSocket;

	public static void terminate() {
		Log.d(TAG, "terminating...");
		try {
			sServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * runs a single-threaded proxy server on the specified local port. It never
	 * returns.
	 * 
	 * @throws IOException
	 */
	public static void runServer(int localport) {
		// Create a ServerSocket to listen for connections with
		Log.d(TAG, "Simple Web Proxy Start at " + localport);
		try {
			sServerSocket = new ServerSocket(localport);
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(TAG, "failed to create ServerSocket(" + localport + ")");
			return;
		}

		while (true) {
			Socket clientsocket = null;
			// Wait for a connection on the local port
			Log.d(TAG, "waiting for connection...");
			try {
				clientsocket = sServerSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}

			InputStream streamFromClient;
			OutputStream streamToClient = null;
			BufferedReader reader = null;
			try {
				streamFromClient = clientsocket.getInputStream();
				streamToClient = clientsocket.getOutputStream();
				reader = new BufferedReader(new InputStreamReader(
						streamFromClient));

				String path = null;
				while (true) {
					String str = reader.readLine();
					if (str == null)
						break;
					if (str.length() <= 0) {
						Log.d(TAG, "End of the request header.");
						break;
					}
					Log.d(TAG, str);
					if (str.indexOf("GET") == 0) {
						String regex = "^GET\\s+(.*)\\s+";
						Pattern p = Pattern.compile(regex);
						Matcher m = p.matcher(str);
						if (m.find()) {
							path = "http:/" + m.group(1);
							Log.d(TAG, path);
							// break;
						}
					}
				}

				HttpGet get = new HttpGet(path);
				get.addHeader("Cookie", Http.sCookie);
				get.setHeader(
						"User-Agent",
						"Mozilla/5.0 (Linux; Android 4.2.1; Nexus 7 Build/JOP40D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Safari/535.19");
				DefaultHttpClient client = new DefaultHttpClient();
				HttpResponse response = client.execute(get);
				int status = response.getStatusLine().getStatusCode();
				if (status != HttpStatus.SC_OK) {
					Log.d(TAG, "HTTP GET Status=" + status);
					String str = "HTTP/1.0 "
							+ response.getStatusLine().getStatusCode() + " "
							+ response.getStatusLine().getReasonPhrase();
					str += "\r\n\r\n";
					streamToClient.write(str.getBytes());
				} else {
					Header[] allheader = response.getAllHeaders();
					for (Header h : allheader) {
						Log.d(TAG, h.getName() + ": " + h.getValue());
					}

					Header[] headers = response.getHeaders("Content-Length");
					long length = 0;
					for (Header h : headers) {
						length = Long.parseLong(h.getValue());
					}
					String str;
					str = "HTTP/1.0 200 OK\r\nContent-Type: video/mp4\r\nContent-Length: "
							+ length + "\r\nConnection: close\r\n\r\n";
					streamToClient.write(str.getBytes());

					InputStream is = response.getEntity().getContent();
					int sum = 0;
					try {
						byte[] buffer = new byte[16 * 1024];
						while (true) {
							int readed = is.read(buffer);
							if (readed <= 0)
								break;
							sum += readed;
							streamToClient.write(buffer, 0, readed);
						}
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Log.d(TAG, sum + " bytes readed.");
				}
				Log.d(TAG, "done.");

			} catch (Exception e) {
				e.printStackTrace();

			} finally {
				try {
					if (streamToClient != null)
						streamToClient.close();
					if (reader != null)
						reader.close();
					clientsocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Log.d(TAG, "SimpleWebProxy server done.");
	}

}
