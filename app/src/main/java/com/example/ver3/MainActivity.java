package com.example.ver3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity
{
	
	@Override
	protected void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate ( savedInstanceState );
		setContentView ( R.layout.activity_main );
		
		// Start the background service
		BackgroundService.startService ( this );
	}
	
	@Override
	protected void onDestroy ( )
	{
		super.onDestroy ( );
		
		// Stop the background service when the app is destroyed
		Intent intent = new Intent ( this , BackgroundService.class );
		stopService ( intent );
	}
}
