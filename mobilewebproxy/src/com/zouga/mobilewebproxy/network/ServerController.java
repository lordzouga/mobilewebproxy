package com.zouga.mobilewebproxy.network;

import com.example.mobilewebproxy.MainActivity;

import android.os.Handler;

public class ServerController extends Handler{

	HTTPProxyServerStarter _httpServerStarter = null;
	
	public void startServer(){
		try{
				_httpServerStarter = new HTTPProxyServerStarter();
				_httpServerStarter.start();
		}catch(Exception e){
			MainActivity.writeLog("ServerControllerError:: " + e.toString());
		}
	}
	
	public boolean stopServer(){
		
		/*try{
			_httpServerStarter.interrupt();
		}catch(Exception e){
			_httpServerStarter.interrupt();
		}finally{
			return true;
		}*/
		_httpServerStarter.stopThread = true;
		return _httpServerStarter.stopServer();
	}
	
	public boolean hasStarted(){
		return _httpServerStarter.started == 1;
	}
	
}
