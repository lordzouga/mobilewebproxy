package com.example.mobilewebproxy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class SettingsActivity extends SherlockPreferenceActivity{
	
	private static Context _context = null;
	
	public static long TOTAL_TRANSFER = 0;
	public static int ENCRYPTION_KEY = 20;

	private static String PREFERENCES_NAME = null;
	
	
	public static final String KEY_PREF_HTTP_PROXY_PORT = "http_proxy_port";
	public static final String KEY_PREF_SSL_PROXY_PORT = "ssl_proxy_port";
	public static final String KEY_PREF_ORGANIZATION_PROXY_HOST = "organization_proxy_host";
	public static final String KEY_PREF_ORGANIZATION_PROXY_PORT = "organization_proxy_port";
	public static final String KEY_PREF_ORGANIZATION_PROXY_USERNAME = "organizatioin_proxy_username";
	public static final String KEY_PREF_ORGANIZATION_PROXY_PASSWORD = "organization_proxy_password";
	public static final String KEY_PREF_TOGGLE_ENCRYPTION = "toggle_encryption";
	public static final String KEY_PREF_WEBSERVER_HOST = "webserver_host";
	public static final String KEY_PREF_WEBSERVER_PORT = "webserver_port";
	public static final String KEY_PREF_WEBSERVER_URL = "webserver_url";
	public static final String KEY_PREF_MAX_BUFFER = "max_buffer";
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);

	}

	public static void initPrefs(Context context){
		_context = context;
		PREFERENCES_NAME = "com.example.mobilewebproxy_preferences";
	}
	
	public static SharedPreferences refreshPrefs(){
		return _context.getSharedPreferences(PREFERENCES_NAME, MODE_WORLD_READABLE);
	}
	
	public static int getHttpProxyPort(){
		String temp = refreshPrefs().getString(KEY_PREF_HTTP_PROXY_PORT, "");
		
		return Integer.valueOf(temp);
	}
	
	public static int getSslProxyPort(){
		String temp = refreshPrefs().getString(KEY_PREF_SSL_PROXY_PORT, "");
		
		return Integer.valueOf(temp);
	}
	
	public static String getOrganizationProxyHost(){
		
		SharedPreferences mine = refreshPrefs();
		String damn = mine.getString(KEY_PREF_ORGANIZATION_PROXY_HOST, "");
		
		return damn;
	}
	
	public static int getOrganizationProxyPort(){
		String temp = refreshPrefs().getString(KEY_PREF_ORGANIZATION_PROXY_PORT, "");
		
		return (int)Integer.parseInt(temp);
	}
	
	public static String getOrganizationProxyUsername(){
		return refreshPrefs().getString(KEY_PREF_ORGANIZATION_PROXY_USERNAME, "");
	}
	
	public static String getOrganizationProxyPassword(){
		return refreshPrefs().getString(KEY_PREF_ORGANIZATION_PROXY_PASSWORD, "");
	}
	
	public static boolean getEncryptionMode(){
		return refreshPrefs().getBoolean(KEY_PREF_TOGGLE_ENCRYPTION, false);
	}
	
	public static String getWebServerPath(){
		return refreshPrefs().getString(KEY_PREF_WEBSERVER_HOST, "");
	}
	
	public static int getWebServerPort(){
		String temp = refreshPrefs().getString(KEY_PREF_WEBSERVER_PORT, "");
		
		return Integer.valueOf(temp);
	}
	
	public static int getMaxBuffer(){
		String temp = refreshPrefs().getString(KEY_PREF_MAX_BUFFER, "");
		
		return Integer.valueOf(temp);
	}
	
	public static String getWebServerUrl(){
		return refreshPrefs().getString(KEY_PREF_WEBSERVER_URL, "");
	}

	
}
