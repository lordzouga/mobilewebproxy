
package com.zouga.mobilewebproxy.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.util.Date;

import com.example.mobilewebproxy.MainActivity;
import com.example.mobilewebproxy.SettingsActivity;

/**
 * Reading from server and write to browser.
 * 
 * @author http://arunava.in
 * 
 */
class HTTPProxyThreadServer extends Thread {
	private Socket incoming;

	private Socket outgoing;

	HTTPProxyThreadServer(final Socket outgoing, final Socket incoming) {
		this.incoming = incoming;
		this.outgoing = outgoing;
	}

	public void run() {
		long count1 = 0;
		long count2 = 0;
		long count3 = 0;

			MainActivity.writeLog(new Date()
					+ " HTTPProxyThreadServer ::Started  " + currentThread());
			
		final byte[] buffer = new byte[SettingsActivity.getMaxBuffer()];
		int numberRead = 0;
		BufferedOutputStream client;
		BufferedInputStream server;

		try {
			server = new BufferedInputStream(outgoing.getInputStream());
			client = new BufferedOutputStream(incoming.getOutputStream());

			// Reading response header:
			int rdL = 0;
			final StringBuilder header = new StringBuilder(9999);
			while (true) {
				count1++;
				try{
				rdL = server.read();
				} catch(Exception e){
					
				}
				if (rdL == -1) {
					break;
				}
				header.append((char) rdL);
				if (header.indexOf("\n\n") != -1) {
					MainActivity.writeLog("broke from here");
					break;
				}
			}

			client.write(header.toString().getBytes());
			client.flush();
			
			MainActivity.writeDownloadCount(count1);
				MainActivity.writeLog(new Date()
						+ " HTTPProxyThreadServer ::Response Header =  "
						+ currentThread() + " " + header.toString());

			if (header.toString().toLowerCase().indexOf(
					"Transfer-Encoding: chunked".toLowerCase()) != -1) {

					System.out
							.println(new Date()
									+ " HTTPProxyThreadServer :: It is Chunked Response   "
									+ currentThread());
					
				count3 = HTTPChunkResponseReader.readFullyANDWriteFully(client,
						server);
				MainActivity.writeDownloadCount(count3);
			} else {
					System.out
							.println(new Date()
									+ " HTTPProxyThreadServer :: It is Normal Response   "
									+ currentThread());
				while (true) {
					try{
					numberRead = server.read(buffer);
					}catch(Exception e){
						
					}
					count2 = count2 + numberRead;
					if (numberRead == -1) {
						outgoing.close();
						incoming.close();
						break;
					}

					if (SettingsActivity.getEncryptionMode()) {
						client.write(SimpleEncryptDecrypt.dec(buffer,
								numberRead), 0, numberRead);
						client.flush();
					} else {
						client.write(buffer, 0, numberRead);
						client.flush();
					}
						final ByteArrayOutputStream bo = new ByteArrayOutputStream();
						bo.write(buffer, 0, numberRead);
						System.out.println("::::::Normal Read" + bo
								+ "::::::Normal Read");
						
						MainActivity.writeDownloadCount(numberRead);
				}
			}
				MainActivity.writeLog(new Date()
						+ " HTTPProxyThreadServer :: End   " + currentThread());
		} catch (final Exception e) {
			MainActivity.writeLog("HTTPProxyThreadServer::error" + e.toString());
		}
		synchronized (new Object()) {
			SettingsActivity.TOTAL_TRANSFER = SettingsActivity.TOTAL_TRANSFER
					+ count1 + count2 + count3;
		}
	}

}
