package com.example.mobilewebproxy;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.Editable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
import com.zouga.mobilewebproxy.network.ServerController;

/*
 * MainActvity is the entry point of this application.
 * 
 * it is the main UI class that displays the core functionalities of the program
 * This class manages the Log view behind the UI and also updates the download and
 * upload counter.
 * 
 * it is created from the SlidingMenu library project http://twitter.com/slidingmenu
 * and also from the Sherlock Action Bar Library http://actionbarsherlock.com/ to allow
 * action bar support for android versions earlier than 3.0
 * 
 * License
-------

    Copyright 2013 Ozojie Gerald ozojiechikelu@yahoo.com
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

public class MainActivity extends SlidingActivity{

	private LogBehindView _behindView = null;//holds the log view
	public static TextView logView = null;
	
	/*_indicatorAnimation is the animation used for rotating the outer ring.
	 * it is used with _indicatorView to provide a way to monitor the network 
	 * download speed.
	 * 
	 * I felt that using the usual textual byte update is too mainstream*/
	private Animation _indicatorAnimation = null;
	private ImageView _indicatorView = null;
	
	/*_downloadedTextView and _uploadedTextView are used to monitor the size of downloded 
	 * and uploaded data respectively*/
	private static TextView _downloadedTextView = null;
	private static TextView _uploadedTextView = null;
	
	/*
	 * an instance of the ServerController Class used to monitor start or stop the server.
	 */
	private ServerController _serverController = null;
	
	/* 
	 * a handle for the Editable class instance available in the Log view. 
	 * used to update the textView with relevant runtime messages for the user
	 */
	private static Editable _editable = null;
	
	
	public static Context _context;//context
	
	private Button _startButton = null;//controls the start and stop of the server

	private boolean _serverStarted = false;//is true if server has started
	
	private NotificationManager _notificationManager = null;//used to manage app wide notification feature
	
	/*
	 * (non-Javadoc)
	 * @see com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity#onCreate(android.os.Bundle)
	 * 
	 * initializes all useful classes
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_behindView = new LogBehindView(this, null);
		_serverController = new ServerController();
		
		setBehindContentView(_behindView);
		
		setContentView(R.layout.activity_main);
		
		_notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		logView = (TextView)_behindView.findViewById(R.id.log_view);
		_startButton = (Button)findViewById(R.id.start_button);
		_downloadedTextView = (TextView)findViewById(R.id.download_counter);
		_uploadedTextView = (TextView)findViewById(R.id.upload_counter);
		logView.setText(" ", TextView.BufferType.EDITABLE);
		logView.setEditableFactory(Editable.Factory.getInstance());
		_editable = (Editable)logView.getText();
		setTitle("Main");
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
		
		SettingsActivity.initPrefs(getApplicationContext());
		
		_indicatorView = (ImageView)findViewById(R.id.indicator);
		_indicatorAnimation = AnimationUtils.loadAnimation(this, R.anim.indicator_rotate);
		
		/* see the SlidingMenu documentation for details*/
		SlidingMenu slideMenu = getSlidingMenu();
		slideMenu.setShadowWidth(R.dimen.shadow_width);
		slideMenu.setShadowDrawable(R.drawable.shadow);
		slideMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		slideMenu.setFadeDegree(0.35f);
		slideMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		///////////////////////////////////////////////////////////////
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		setSlidingActionBarEnabled(true);
	}
	
	/*createNotification() creates a new notification and alerts the user*/
	private void createNotification(){
		NotificationCompat.Builder builder = 
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle("Mobile web proxy")
		.setContentText("Proxy server started");
		
		Intent resultIntent = new Intent(this, MainActivity.class);
		resultIntent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
		
		PendingIntent resultPendingIntent = PendingIntent.
				getActivity(this, 0, resultIntent, 0);
		
		builder.setContentIntent(resultPendingIntent);
		
		_notificationManager.notify(2, builder.getNotification());
	}
	
	/*
	 * onStartButtonclick() is called whenever the button is clicked
	 * 
	 * it checks if the server has already started by checking the value of _serverStarted
	 * if the server has already started, it stops it else it starts it.
	 * 
	 * @param: View v
	 * 		view to be clicked
	 */
	public void onStartButtonClick(View v){
		
		if(!_serverStarted){
			_serverController.startServer();
				writeLog("server has started\n");
				_serverStarted = true;
				_indicatorView.startAnimation(_indicatorAnimation);
				_startButton.setText("Stop");
				//createNotification();
		}else{
			if(_serverController.stopServer()){
				_serverStarted = false;
				_startButton.setText("Start");
				_indicatorView.clearAnimation();
				//_notificationManager.cancelAll();
			}
		}
	}
	
	/*
	 * writeLog() manages write operations to the log View.
	 * the method uses the View's post() (see android's View documentation for details)
	 * since the function will be often times called from another thread.
	 * it uses a handle of the view's editable to write to the view
	 * 
	 * @param: String msg
	 * 		message to be posted to in the log view
	 */
	public static void writeLog(final String msg){
		
		//runnable instance to be executed in the view's thread
		Runnable runnable = new Runnable(){
			@Override
			public void run(){
				_editable.append(msg);
				_editable.append("\n");
			}
		};
		
		logView.post(runnable);
	}
	
	/*
	 * writeDownloadCount() updates the UI's download count.
	 * it uses the View's post() (see android's View documentation for more details)
	 * since the method will be often called from another thread.
	 * 
	 * it updates the download count by calling getBytesTextValue() with downloadCount as argument
	 * and sets the returned value as updated text
	 * 
	 * @param: long downloadCount
	 * 		
	 */
	public static void writeDownloadCount(final long downloadCount){
		Runnable runnable = new Runnable(){
			@Override
			public void run(){
				String prevCount = (String)_downloadedTextView.getText();
				_downloadedTextView.setText(getBytesTextValue(prevCount, downloadCount));
			}
		};
		
		_downloadedTextView.post(runnable);
	}
	
	/*
	 * writeDow
	 */
	public static void writeUploadCount(final long uploadCount){
		Runnable runnable = new Runnable(){
			@Override
			public void run(){
				String prevCount = (String)_uploadedTextView.getText();
				_uploadedTextView.setText(getBytesTextValue(prevCount, uploadCount));
			}
		};
		
		_uploadedTextView.post(runnable);
	}
	
	private static String getBytesTextValue(String prevCount, long countRaise){

		long totalBytes = countRaise;
		Double prevNum;
		Double convertedValue;
		StringBuilder builder = new StringBuilder(prevCount);
		
		if(!prevCount.endsWith("kb") && !prevCount.endsWith("mb")){
			builder.deleteCharAt(prevCount.length() - 1);
			
			Integer temp = Integer.parseInt(builder.toString());
			totalBytes += temp.longValue();
		}
		else if(prevCount.endsWith("kb")){
			builder.delete(prevCount.length() - 2, prevCount.length());
			
			prevNum = Double.parseDouble(builder.toString());
			convertedValue = prevNum * 1024.0;
			
			totalBytes += convertedValue.longValue();
		}
		
		else if(prevCount.endsWith("mb")){
			builder.delete(prevCount.length() - 2, prevCount.length());
			
			prevNum = Double.parseDouble(builder.toString());
			convertedValue = prevNum * 1024.0 * 1024.0;
			
			totalBytes += convertedValue.longValue();
		}
		
		return bytesToReadableString(totalBytes);
	}
	
	private static String bytesToReadableString(long rawBytes){
		
		StringBuilder retVal = new StringBuilder();
		
		if(rawBytes <= 1024){
			retVal.append(String.valueOf(rawBytes) + "b");
		}
		else if(rawBytes <= (1024 * 1024)){
			Double temp = (double)(rawBytes / (1024.0));
			
			retVal.append(getApproxValue(temp).toString() + "kb");
			
		}
		else{
			Double temp = (double)(rawBytes / (1024.0 * 1024.0));
			
			retVal.append(getApproxValue(temp).toString() + "mb");
		}
		
		return retVal.toString();
	}
	
	private static Double getApproxValue(Double val){
		int index;
		final String delim = ".";
		final String stringValue = val.toString();
		StringBuilder builder = new StringBuilder(stringValue);
		
		index = stringValue.indexOf(delim);
		
		if(index != -1)
			builder.delete(index + 2, builder.length());
		
		return Double.parseDouble(builder.toString());
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case android.R.id.home:
			toggle();
			break;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		}
		return true;
	}
}
