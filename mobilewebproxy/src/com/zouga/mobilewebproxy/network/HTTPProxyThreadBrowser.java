
package com.zouga.mobilewebproxy.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

import com.example.mobilewebproxy.MainActivity;
import com.example.mobilewebproxy.SettingsActivity;

import android.text.Editable;
import android.util.Base64;

/**
 * Reading from browser and write to server
 * 
 * @author http://arunava.in
 * 
 */
class HTTPProxyThreadBrowser extends Thread {

	private Socket incoming;

	private Socket outgoing;

	HTTPProxyThreadBrowser(final Socket outgoing, final Socket incoming) {
		this.incoming = incoming;
		this.outgoing = outgoing;
		
	}

	public void run() {

		ByteArrayOutputStream mainBuffer = new ByteArrayOutputStream();

		
		long count1 = 0;
		long count2 = 0;

			MainActivity.writeLog(new Date()
					+ " HTTPProxyThreadBrowser ::Started  " + currentThread());

		final byte[] buffer = new byte[SettingsActivity.getMaxBuffer()];
		int numberRead = 0;
		BufferedOutputStream server;
		BufferedInputStream client;

		try {

			client = new BufferedInputStream(incoming.getInputStream());
			server = new BufferedOutputStream(outgoing.getOutputStream());
			
			String proxyAuth = "";
			// If Organization proxy required Authentication
			if (!SettingsActivity.getOrganizationProxyUsername().equals("")) {
				final String authString = SettingsActivity.getOrganizationProxyUsername()
						+ ":"
						+ SettingsActivity.getOrganizationProxyPassword();
				proxyAuth = "Basic "
						+ Base64.encodeToString(authString
								.getBytes(), Base64.DEFAULT);
			}

			int rdL = 0;
			final StringBuilder header = new StringBuilder(9999);
			while (true) {
				try{
				rdL = client.read();
				}catch(Exception e){
					
				}
				if (rdL == -1) {
					break;
				}
				header.append((char) rdL);
				if (header.indexOf("\n\n") != -1) {
					break;
				}
			}

				MainActivity.writeLog(new Date()
						+ " HTTPProxyThreadBrowser :: request data = "
						+ currentThread() + " \n" + header);

			final String allInpRequest = header.toString();
			String host = "";
			String port = "";
			String tmpHost = "";
			final int indexOf = allInpRequest.toLowerCase().indexOf("host:");
			if (indexOf != -1) {
				final int immediateNeLineChar = allInpRequest.toLowerCase()
						.indexOf("\n",
								allInpRequest.toLowerCase().indexOf("host:"));
				tmpHost = allInpRequest.substring(
						allInpRequest.toLowerCase().indexOf("host:") + 5,
						immediateNeLineChar).trim();
				final int isPortThere = tmpHost.indexOf(":");
				if (isPortThere != -1) {
					host = tmpHost.substring(0, tmpHost.indexOf(":"));
					port = tmpHost.substring(tmpHost.indexOf(":") + 1);

				} else {
					port = "80";
					host = tmpHost;
				}
			}

			// ////////////////// Added since rapidshare not opening
			// Making it relative request.

			String modifyGet = header.toString().toLowerCase();

			
                        if (modifyGet.startsWith("get http://")) {
				int i2 = modifyGet.indexOf("/", 11);
				header.replace(4, i2, "");
			}
			if (modifyGet.startsWith("post http://")) {
				int i2 = modifyGet.indexOf("/", 12);
				header.replace(5, i2, "");
			} 
                        
                        

			// ///////////////////////////////////////////////

			final String proxyServerURL = SettingsActivity.getWebServerUrl();
			
			String isSecure = "";
			final String HeaderHost = SettingsActivity.getWebServerPath();

			if (header.indexOf("X-IS-SSL-RECURSIVE:") == -1) {
				isSecure = "N";
			} else {
				isSecure = "Y";
				// Now detect which Port 443 or 8443 ?
				// Like : abcd X-IS-SSL-RECURSIVE: 8443
				final int p1 = header.indexOf("X-IS-SSL-RECURSIVE: ");
				port = header.substring(p1 + 20, p1 + 20 + 4);
				port = "" + Integer.valueOf(port).intValue();
			} 

				MainActivity.writeLog(new Date()
						+ " HTTPProxyThreadBrowser ::Started  "
						+ currentThread() + "URL Information :\n" + "Host="
						+ host + " Port=" + port + " ProxyServerURL="
						+ proxyServerURL + " HeaderHost=" + HeaderHost);

			// Get Content length
			String contentLenght = "";
			final int contIndx = header.toString().toLowerCase().indexOf(
					"content-length: ");
			if (contIndx != -1) {
				final int endI = header.indexOf("\n", contIndx + 17);
				contentLenght = header.substring(contIndx + 16, endI);
			}

			MainActivity.writeLog("header is: " + header);
			String data = header.toString();
			
			/*data = data.replaceFirst("\n\n",
					"Connection: Close\n\n");*/

			// Replace culprit KeepAlive
			// Should have used Regex
			data = data.replaceFirst("Keep-Alive: ", "X-Dummy-1: ");
			data = data.replaceFirst("keep-alive: ", "X-Dummy-1: ");
			data = data.replaceFirst("Keep-alive: ", "X-Dummy-1: ");
			data = data.replaceFirst("keep-Alive: ", "X-Dummy-1: ");

			data = data.replaceFirst("keep-alive", "Close");
			data = data.replaceFirst("Keep-Alive", "Close");
			data = data.replaceFirst("keep-Alive", "Close");
			data = data.replaceFirst("Keep-alive", "Close");

			int totallength = 0;
			if (!contentLenght.equals("")) {
				totallength = Integer.parseInt(contentLenght.trim())
						+ (data.length() + 61 + 1);
			} else {
				totallength = (data.length() + 61 + 1);
			}

			String header1 = "";
			header1 = header1 + "POST " + proxyServerURL + " HTTP/1.1\n";
			header1 = header1 + "Host: " + HeaderHost + "\n";
			header1 = header1 + "Connection: Close\n";
			header1 = header1 + "Content-Length: " + totallength + "\n";
			header1 = header1 + "Cache-Control: no-cache\n";

			MainActivity.writeLog("sent header data is " + header1);
			if (!SettingsActivity.getOrganizationProxyUsername().equals("")) {
				header1 = header1 + "Proxy-Authorization: " + proxyAuth
						+ "\n";
			}

			count1 = totallength;

			header1 = header1 + "\n";
			server.write(header1.getBytes());

			MainActivity.writeUploadCount(header.length());
			
			int temp = 0;
			if (SettingsActivity.getEncryptionMode()) {
				// Let know PHP waht are we using
				server.write(("Y".getBytes()));

				
				server.write(SimpleEncryptDecrypt.enc(host.getBytes()));
				
				// Padding with space
				for (int i = 0; i < 50 - host.length(); i++) {
					server.write(SimpleEncryptDecrypt.enc(" ".getBytes()));
				}
				
				server.write(SimpleEncryptDecrypt.enc(port.getBytes()));
				
				// Padding with space
				for (int i = 0; i < 10 - port.length(); i++) {
					server.write(SimpleEncryptDecrypt.enc(" ".getBytes()));
				}
				
				// Write fsockopen info
				server.write(SimpleEncryptDecrypt.enc(isSecure.getBytes()));

				// It is destination header
				server.write(SimpleEncryptDecrypt.enc(data.getBytes()));

			} else {
				// Let know PHP what we are using
				server.write(("N".getBytes()));

				server.write(host.getBytes());
				// Padding with space
				for (int i = 0; i < 50 - host.length(); i++) {
					server.write(" ".getBytes());
				}
				server.write(port.getBytes());
				// Padding with space
				for (int i = 0; i < 10 - port.length(); i++) {
					server.write(" ".getBytes());
				}
				// Write fsockopen info
				server.write(isSecure.getBytes());

				// It is destination header
				server.write(data.getBytes());

			}
			//calculate uploaded bytes
			temp++;
			temp += host.length();
			temp += (50 - host.length());
			temp += port.length();
			temp += (10 - port.length());
			temp += isSecure.length();
			temp += data.length();
			
			server.flush();

			MainActivity.writeUploadCount(temp);
			
				MainActivity.writeLog(new Date()
						+ " HTTPProxyThreadBrowser :: destination header   = "
						+ currentThread() + " \n" + data);

			while (true) {
				numberRead = client.read(buffer);
				if (numberRead == -1) {
					MainActivity.writeLog("closing outgoing socket");
					//outgoing.close();
					
					MainActivity.writeLog("closing incoming socket");
					//incoming.close();
					break;
				}
				
				count2 = count2 + numberRead;
				
				if (SettingsActivity.getEncryptionMode()) {
					server.write(SimpleEncryptDecrypt.enc(buffer, numberRead),
							0, numberRead);
				} else {
					server.write(buffer, 0, numberRead);
				}
				
				server.flush();
				MainActivity.writeUploadCount(numberRead);
			}

				MainActivity.writeLog(new Date()
						+ " HTTPProxyThreadBrowser :: Finish "
						+ currentThread());
				
		} catch (final Exception e) {
			MainActivity.writeLog("HTTPProxyThreadBrowser error " + e.toString());
		}
		synchronized (new Object()) {
			SettingsActivity.TOTAL_TRANSFER = SettingsActivity.TOTAL_TRANSFER
					+ count1 + count2;
		}
	}

}
