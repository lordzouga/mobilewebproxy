
package com.zouga.mobilewebproxy.network;

import com.example.mobilewebproxy.SettingsActivity;

/**
 * @author Arunava Bhowmick SIMPLE Enc. dec module. Keeping the output size
 *         same.
 * 
 */
public class SimpleEncryptDecrypt {

	public static byte[] enc(byte[] tmp) {
		for (int y = 0; y < tmp.length; y++) {
			tmp[y] = (byte) (tmp[y] + SettingsActivity.ENCRYPTION_KEY);
		}
		return tmp;
	}

	public static byte[] enc(byte[] tmp, int size) {
		for (int y = 0; y < size; y++) {
			tmp[y] = (byte) (tmp[y] + SettingsActivity.ENCRYPTION_KEY);
		}
		return tmp;
	}

	public static byte[] dec(byte[] tmp) {
		for (int y = 0; y < tmp.length; y++) {
			tmp[y] = (byte) (tmp[y] - SettingsActivity.ENCRYPTION_KEY);
		}
		return tmp;
	}

	public static byte[] dec(byte[] tmp, int size) {
		for (int y = 0; y < size; y++) {
			tmp[y] = (byte) (tmp[y] - SettingsActivity.ENCRYPTION_KEY);
		}
		return tmp;
	}

}
