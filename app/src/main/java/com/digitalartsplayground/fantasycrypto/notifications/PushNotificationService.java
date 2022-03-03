package com.digitalartsplayground.fantasycrypto.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.digitalartsplayground.fantasycrypto.MainActivity;
import com.digitalartsplayground.fantasycrypto.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.jetbrains.annotations.NotNull;

public class PushNotificationService extends FirebaseMessagingService {

    @Override
    @SuppressWarnings("deprecation")
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {

        final String CHANNEL_ID = "HEADS_UP_NOTIFICATION";
        String title = remoteMessage.getNotification().getTitle();
        String text = remoteMessage.getNotification().getBody();


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Heads Up Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setAutoCancel(true);
            NotificationManagerCompat.from(this).notify(1, notification.build());

        } else {

            final Intent emptyIntent = new Intent(getApplicationContext(), MainActivity.class);

            PendingIntent pendingIntent = PendingIntent
                    .getActivity(this, 0, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(2, mBuilder.build());

        }

        super.onMessageReceived(remoteMessage);
    }
}

