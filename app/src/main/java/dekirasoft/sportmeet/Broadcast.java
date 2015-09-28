package dekirasoft.sportmeet;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by Kota on 1/13/2015.
 */
public class Broadcast extends ParsePushBroadcastReceiver {


    private static final String TAG = "MyCustomReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
            try {
        if (intent == null)
        {
            Log.d(TAG, "Receiver intent null");
        }
        else
        {
            String action = intent.getAction();
            Log.d(TAG, "got action " + action );
            if (action.equals("com.parse.push.intent.RECEIVE"))
            {
                String channel = intent.getExtras().getString("com.parse.Channel");
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
                Iterator itr = json.keys();
                String title = json.getString("title");
                String text = json.getString("alert");
                String pushId = json.getString("push_hash");
                while (itr.hasNext()) {
                    String key = (String) itr.next();

                    if (key.equals("customdata"))
                    {
                       CreateNotication(context, title, pushId, text);
                    }
                    Log.d(TAG, "..." + key + " => " + json.getString(key));
                }
            }
        }
    } catch (JSONException e) {
        Log.d(TAG, "JSONException: " + e.getMessage());
    }
    }

   public void CreateNotication(Context context, String title, String pushId, String text) {
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, UserInvites.class), 0);

        Notification.Builder mBuilder = new Notification.Builder(context)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle(title)
                .setContentText(text)
                 .setAutoCancel(true)
                .setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(pushId, 1, mBuilder.build());
    }



}
