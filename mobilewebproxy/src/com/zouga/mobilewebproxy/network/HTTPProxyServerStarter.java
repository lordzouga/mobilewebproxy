
package com.zouga.mobilewebproxy.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import android.os.AsyncTask;
import android.os.Looper;
import android.text.Editable;
import com.example.mobilewebproxy.MainActivity;
import com.example.mobilewebproxy.SettingsActivity;



/**
 * Start HTTP Local Proxy server..
 * 
 * @author http://arunava.in
 * @author lordzouga http://twitter.com/lordzouga
 */
public class HTTPProxyServerStarter extends Thread {
	public int started = 0;
	
	private ServerSocket _serverSocket = null;
	private HTTPProxyStarterHelper _starterHelper = null;
	
	public volatile boolean stopThread = false;
	public Vector<Socket> acceptedConnections = new Vector<Socket>();
	
	public static void main(final String[] args) {
		new HTTPProxyServerStarter().run();
	}

	public void run() {
		MainActivity.writeLog(new Date() + " Encryption Enabled +");
		try {
			try{
			 _serverSocket = new ServerSocket();
			 _serverSocket.setReuseAddress(true);
			 
			 _serverSocket.bind(new InetSocketAddress(SettingsActivity.getHttpProxyPort()));
			 
			}catch(Exception e){
				MainActivity.writeLog("already exists. moving on");
			}
			while (!stopThread) {
				started = 1;
				
				Socket incoming = null;
				
				try{
					incoming = _serverSocket.accept();
					acceptedConnections.add(incoming);
				}catch(Exception e){
					MainActivity.writeLog("server socket exception " + e.toString());
				}
				
				if(incoming != null){
					incoming.setTcpNoDelay(true);
					incoming.setSoTimeout(10000);
					incoming.setSoLinger(true, 0);
					_starterHelper = new HTTPProxyStarterHelper(incoming);
					_starterHelper.start();
				}
			
			}
			
		} catch (final Exception e) {
			started = 2;
			MainActivity.writeLog(new Date() + " HTTPProxyServerStarter Error::"
					+ e.toString());
		}
		started = 2;
		MainActivity.writeLog("Thread has finished");
	}
	
	public synchronized boolean stopServer(){
		if(_serverSocket != null){
			try{
				MainActivity.writeLog("Shutting down server..");
				_serverSocket.close();
				stopThread = true;
				
				for(int i = 0; i < acceptedConnections.size(); i++){
					Socket temp = acceptedConnections.get(i);
					if(!temp.isClosed()){
						temp.shutdownInput();
						temp.shutdownOutput();
						temp.close();
					}
				}
				started = 2;
			}catch(IOException e){
				MainActivity.writeLog("Socket not Closed " + e.toString());
			}
			
			MainActivity.writeLog("Server Successfully Shutdown");
			return true;
		}else{
			return false;
		}
	}
	
}
/**
 * HTTP Proxy starter.
 * 
 */
class HTTPProxyStarterHelper extends Thread {
	Socket incoming;
	private HTTPProxyThreadBrowser _thread2 = null;
	private HTTPProxyThreadServer _thread1 = null;

	public HTTPProxyStarterHelper(final Socket incoming) {
		this.incoming = incoming;
	}

	public void run() {

		try {
			Socket outgoing = null;
			// If organization http present !!!!
			if (SettingsActivity.getOrganizationProxyHost().equals("")) {
				
				outgoing = new Socket(SettingsActivity.getWebServerPath(),
						SettingsActivity.getWebServerPort());
				outgoing.setSoTimeout(30000);
			} else {
				MainActivity.writeLog("webserver port is " + SettingsActivity.getOrganizationProxyPort());
				outgoing = new Socket(
						SettingsActivity.getOrganizationProxyHost(),
						SettingsActivity.getOrganizationProxyPort());
			}
			
			if(outgoing != null){
				outgoing.setTcpNoDelay(true);
			}
			
			// -->
			_thread2 = new HTTPProxyThreadBrowser(
					outgoing, incoming);
			_thread2.start();
			// <----
			_thread1 = new HTTPProxyThreadServer(
					outgoing, incoming);
			_thread1.start();
		} catch (final Exception e) {
				MainActivity.writeLog(new Date()
						+ " HTTPProxyServerStarterHelper Error :: " + e.toString());
				
		}
	}
}
