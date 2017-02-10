package nikhil.com.smsapplication.Receivers;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import nikhil.com.smsapplication.Activites.MainActivity;
import nikhil.com.smsapplication.R;

/**
 * Created by Nikil on 2/10/2017.
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String str = "no message received";
        if (bundle != null) {

            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {

                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                str += "SMS from Phone No: " + msgs[i].getOriginatingAddress();
                str += "\n" + "Message is: ";
                str += msgs[i].getMessageBody().toString();
                str += "\n";



            }
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("New Message")
                            .setContentText(str);


           Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        }
    }
}
