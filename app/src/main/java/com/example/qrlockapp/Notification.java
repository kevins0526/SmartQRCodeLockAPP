package com.example.qrlockapp;

import static com.example.qrlockapp.GlobalVariable.lockName;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Notification extends AppCompatActivity {

    NotificationManager notificationManager;
    Context context;
    NotificationChannel channel;
    Button BNotification;

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/Time/"+lockName);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private void NotificationSend(String preson,String time) {
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("ID", "notification_text_a", notificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            Intent intent = new Intent(context, MainActivity2.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent PI = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            android.app.Notification.Builder builder = new android.app.Notification.Builder(context);
            builder.setSmallIcon(R.mipmap.ic_launcher)
                    .setChannelId("ID")
                    .setContentTitle(preson+"嘗試開啟門鎖")
                    .setContentText(time)
                    .setContentIntent(PI);

            android.app.Notification notificaion = builder.build();
            notificationManager.notify(0, notificaion);
        }
    }

}
