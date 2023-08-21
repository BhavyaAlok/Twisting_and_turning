package com.example.ver3;

import static com.example.ver3.NotificationUtils.CHANNEL_ID;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

public class BackgroundService extends Service implements SensorEventListener
{
	private SensorManager sensorManager;
	private Sensor gyroSensor, accSensor;
	private static final double ROTATION_THRESHOLD = Math.PI / 16; // in degrees
	private static final int NOTIFICATION_ID_FOREGROUND = 101; // Notification ID for foreground service
	private long last_update_time = System.currentTimeMillis ( );
	private static final long IDLE_TIME_THRESHOLD = 30 * 1000; // 5 minutes in milliseconds
	private long lastSensorUpdateTime = System.currentTimeMillis ( );
	private boolean isGyroSensorActive = true;
	
	public static void startService ( Context context )
	{
		Intent intent = new Intent ( context , BackgroundService.class );
		context.startService ( intent );
	}
	
	
	@Override
	public int onStartCommand ( Intent intent , int flags , int startId )
	{
		// Initialize sensors and register listeners
		sensorManager = ( SensorManager ) getSystemService ( Context.SENSOR_SERVICE );
		gyroSensor = sensorManager.getDefaultSensor ( Sensor.TYPE_GYROSCOPE );
		accSensor = sensorManager.getDefaultSensor ( Sensor.TYPE_ACCELEROMETER );
		
		sensorManager.registerListener ( this , gyroSensor , SensorManager.SENSOR_DELAY_NORMAL );
		sensorManager.registerListener ( this , accSensor , SensorManager.SENSOR_DELAY_NORMAL );
		
		Intent notificationIntent = new Intent ( this , MainActivity.class );
		PendingIntent pendingIntent =
			PendingIntent.getActivity ( this , 0 , notificationIntent , PendingIntent.FLAG_IMMUTABLE );
		
		// Build the notification
		Notification notification = new Notification.Builder ( this , CHANNEL_ID )
			.setContentTitle ( "Motion checker app" )
			.setContentText ( "Running in the background" )
			.setSmallIcon ( R.drawable.ic_notification_icon )
			.setContentIntent ( pendingIntent )
			.build ( );
		
		// Start the service in the foreground with the notification
		startForeground ( NOTIFICATION_ID_FOREGROUND , notification );
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy ( )
	{
		super.onDestroy ( );
		// Unregister listeners
		sensorManager.unregisterListener ( this );
	}
	
	@Nullable
	@Override
	public IBinder onBind ( Intent intent )
	{
		return null;
	}
	
	@Override
	public void onSensorChanged ( SensorEvent event )
	{
		
		String message;
		
		if ( event.sensor.getType ( ) == Sensor.TYPE_GYROSCOPE )
		{
			long currentTime = System.currentTimeMillis ( );
			long dt = currentTime - lastSensorUpdateTime;
			
			float[] angularVelocity = event.values;
			
			// Calculate time difference in seconds since last sensor update
			float deltaTime = ( currentTime - last_update_time ) / 1000.0f;
			
			// Calculate rotation angles using angular velocity
			float rotationX = angularVelocity[ 0 ] * deltaTime;
			float rotationY = angularVelocity[ 1 ] * deltaTime;
			float rotationZ = angularVelocity[ 2 ] * deltaTime;
			
			
			// Check if rotation or twist is significant
			if ( Math.abs ( rotationX ) > ROTATION_THRESHOLD )
			{
				if ( Math.toDegrees ( rotationX ) < 0 )
				{
					message = "You moved your phone's head away from you";
				} else
				{
					message = "You moved your phone's head towards you";
				}
				NotificationUtils.showNotification ( this , message );
				lastSensorUpdateTime = System.currentTimeMillis ( );
				
			} else if ( Math.abs ( rotationY ) > ROTATION_THRESHOLD )
			{
				message = "You moved your phone about its axis (a twist is registered). ";
				
				NotificationUtils.showNotification ( this , message );
				lastSensorUpdateTime = System.currentTimeMillis ( );
				
			} else if ( Math.abs ( rotationZ ) > ROTATION_THRESHOLD )
			{
				message = "You moved your phone sideways (a rotation/yaw is registered). ";
				
				NotificationUtils.showNotification ( this , message );
				lastSensorUpdateTime = System.currentTimeMillis ( );
				
			} else
			{
				if ( dt >= IDLE_TIME_THRESHOLD && isGyroSensorActive )
				{
					// No motion detected for the idle period, stop gyro sensor
					sensorManager.unregisterListener ( this , gyroSensor );
					isGyroSensorActive = false;
					message = " the sensor is now stopped due to inactivity, but the app is still running. ";
					NotificationUtils.showNotification ( this , message );
				}
			}
			
			// Update the last update time
			last_update_time = System.currentTimeMillis ( );
		}
	}
	
	
	@Override
	public void onTaskRemoved ( Intent rootIntent )
	{
		super.onTaskRemoved ( rootIntent );
		// Unregister listeners when the app is removed from the recent apps list
		unregisterListeners ( );
	}
	
	
	private void unregisterListeners ( )
	{
		if ( sensorManager != null )
		{
			sensorManager.unregisterListener ( this );
		}
	}
	
	@Override
	public void onAccuracyChanged ( Sensor sensor , int accuracy )
	{
	}
}
