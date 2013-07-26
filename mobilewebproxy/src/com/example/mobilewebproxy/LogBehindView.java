package com.example.mobilewebproxy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import android.widget.ScrollView;

public class LogBehindView extends ScrollView{
	
	public LogBehindView(Context context, AttributeSet attrs){
		super(context, attrs);
		
		LayoutInflater inflater = (LayoutInflater)context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.log_behind_view, this, true);
	}
}
