
package com.zouga.mobilewebproxy.network;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import com.example.mobilewebproxy.SettingsActivity;

/**
 * Read Chunk HTTP Response Data. Does not read any header after chunk data.
 * 
 * @author http://arunava.in
 * 
 */
public class HTTPChunkResponseReader {

	public static long readFullyANDWriteFullyHelper(final OutputStream client,
			final InputStream server, final int size) {
		long count3 = 0;
		final int bufSize = SettingsActivity.getMaxBuffer();
		int read;
		int totalRead = 0;
		int toRead = bufSize;

		if (toRead > size) {
			toRead = size;
		}
		final byte[] by = new byte[toRead];

		try {
			while (true) {
				read = server.read(by, 0, toRead);
				count3 = count3 + read;
				if (SettingsActivity.getEncryptionMode()) {
					client.write(SimpleEncryptDecrypt.dec(by, read), 0, read);
				} else {
					client.write(by, 0, read);
				}
				
					System.out
							.println(new Date()
									+ " HTTPChunkResponseReader :: Read chunked data size =  "
									+ read);

					final ByteArrayOutputStream bo = new ByteArrayOutputStream();
					bo.write(by, 0, read);
					System.out.println("::Chunked read::" + bo);

				totalRead = totalRead + read;
				if (size - totalRead < toRead) {
					toRead = size - totalRead;
				}
				if (totalRead == size) {
					break;
				}
			}
			client.flush();
		} catch (final Exception e) {
				e.printStackTrace();
		}
		return count3;
	}

	public static long readFullyANDWriteFully(final OutputStream client,
			final InputStream server) {
		long count3 = 0;
		try {
			int chunkSize = Integer.parseInt(readChunkSize(server), 16);

			while (chunkSize != 0) {
				count3 = count3
						+ readFullyANDWriteFullyHelper(client, server,
								chunkSize);
				String sizeStr = readChunkSize(server);
				if ("".equals(sizeStr)) {
					sizeStr = readChunkSize(server);
				}
				if ("".equals(sizeStr)) {
					break;
				}
				chunkSize = Integer.parseInt(sizeStr, 16);
			}

		} catch (final Exception e) {
				e.printStackTrace();
		}
		return count3;
	}

	private static String readChunkSize(final InputStream server) {
		int rdL;
		String format = "";
		final StringBuffer line = new StringBuffer(1111);
		try {
			while (true) {
				rdL = server.read();
				if (rdL == -1) {
					break;
				}
				line.append((char) rdL);
				if (line.indexOf("\n") != -1) {
					break;
				}
			}
		} catch (final Exception e) {
				e.printStackTrace();
		}
		format = line.toString().trim();
		if (!format.equals("")) {
			// Generally some chunk data size comes with extra data Like :
			// 222;ignore this
			final int separator = format.indexOf(";");
			if (separator != -1) {
				format = format.substring(0, separator - 1);
			}
		}
		return format;
	}

}
