package com.example.ver3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificationUtils
{
	public static final String CHANNEL_ID = "rotation_channel";
	private static final String CHANNEL_NAME = "Rotation Channel";
	private static final int NOTIFICATION_ID = 1;
	
	public static void showNotification ( Context context , String message )
	{
		NotificationManager notificationManager = ( NotificationManager )
			context.getSystemService ( Context.NOTIFICATION_SERVICE );
		
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O )
		{
			NotificationChannel channel = new NotificationChannel ( CHANNEL_ID , CHANNEL_NAME ,
				NotificationManager.IMPORTANCE_DEFAULT );
			notificationManager.createNotificationChannel ( channel );
		}
		
		Notification notification = new Notification.Builder ( context , CHANNEL_ID )
			.setContentTitle ( "Rotation Action" )
			.setContentText ( message )
			.setSmallIcon ( R.drawable.ic_notification_icon )
			.build ( );
		
		notificationManager.notify ( NOTIFICATION_ID , notification );
	}
}
