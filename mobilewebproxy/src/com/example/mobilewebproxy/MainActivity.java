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

public class MainActivity extends SlidingActivity{

	private LogBehindView _behindView = null;
	public static TextView logView = null;
	private Animation _indicatorAnimation = null;
	private ImageView _indicatorView = null;
	private static TextView _downloadedTextView = null;
	private static TextView _uploadedTextView = null;
	private ServerController _serverController = null;
	private static Editable _editable = null;
	public static Context _context;
	private Button _startButton = null;
	private boolean _serverStarted = false;
	private NotificationManager _notificationManager = null;
	
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
		
		SlidingMenu slideMenu = getSlidingMenu();
		slideMenu.setShadowWidth(R.dimen.shadow_width);
		slideMenu.setShadowDrawable(R.drawable.shadow);
		slideMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		slideMenu.setFadeDegree(0.35f);
		slideMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		setSlidingActionBarEnabled(true);
	}
	
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
	
	public static void writeLog(final String msg){
		
		Runnable runnable = new Runnable(){
			@Override
			public void run(){
				_editable.append(msg);
				_editable.append("\n");
			}
		};
		
		logView.post(runnable);
	}
	
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
