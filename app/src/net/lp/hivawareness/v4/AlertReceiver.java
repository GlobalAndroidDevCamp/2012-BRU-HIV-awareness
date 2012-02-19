package net.lp.hivawareness.v4;

import net.lp.hivawareness.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlertReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("AlertReceiver", "Received Alert");

		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		CharSequence title = context.getString(R.string.app_name);
		CharSequence message = intent.getStringExtra("message");
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(), 0);
		
		Notification notif = new Notification(R.drawable.android_love,
				message, System.currentTimeMillis());
		notif.setLatestEventInfo(context, title, message, contentIntent);
		nm.notify(1, notif);

	}

}
