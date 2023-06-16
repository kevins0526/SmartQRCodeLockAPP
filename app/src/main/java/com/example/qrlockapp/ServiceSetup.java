package com.example.qrlockapp;

import static com.example.qrlockapp.GlobalVariable.lockName;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class ServiceSetup extends Service {
    public boolean skip;
    private Context context;
    NotificationChannel channel;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    ValueEventListener valueEventListener;
    NotificationManager notificationManager;
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("/Time/"+lockName);
    private static final String TAG="ServiceSetup";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";

    public void onCreate() {
        super.onCreate();
        skip=true;
        context = this;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Log.d(TAG, "Service created");

        // Initialize listeners or other necessary settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Foreground Service", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Set the service to run in the foreground
        Intent notificationIntent = new Intent(this, fragment3.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("門鎖管家正在為您管理門鎖中")
                .setContentText("若有人開鎖會提醒您!")
                .setSmallIcon(R.drawable.qrcode_icon)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        // Start background monitoring task
        Log.d(TAG, "Service started");
        skip=true;

        // Register Firebase Realtime Database listener
        valueEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count=0;
                long childrenCount = snapshot.getChildrenCount();
                if(skip) {
                    if(skip==true) skip=false;
                    return;
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String time = dataSnapshot.getKey();
                    String name = dataSnapshot.getValue(String.class);

                    if (count + 1 == childrenCount) {
                        if (name.equals("unknown")) {
                            sendNotice();
                        }
                        NotificationSend(name, time);
                    }
                    count++;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        databaseRef.addValueEventListener(valueEventListener);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(valueEventListener!= null){
            databaseRef.removeEventListener(valueEventListener);
        }

        // Stop the service and remove the notification
        stopForeground(true);
        stopSelf();

        Log.d(TAG, "Service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void NotificationSend(String person,String time) {
        if(person.equals("unknown")){
            person = "未知住戶";
        }
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("ID", "notification_text_a", notificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            Intent intent = new Intent(context, MainActivity2.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent PI = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.mipmap.ic_launcher)
                    .setChannelId("ID")
                    .setContentTitle(person+"嘗試開啟門鎖")
                    .setContentText(time)
                    .setContentIntent(PI);

            Notification notification = builder.build();
            notificationManager.notify(0, notification);
        }
    }

    public void sendNotice(){
        DatabaseReference noticeMsgRef = database.getReference("/manageMsg/"+(lockName+"警訊")+"/msg/");
        DatabaseReference noticeTimeRef=database.getReference("/manageMsg/"+(lockName+"警訊")+"/time/");
        noticeMsgRef.setValue("非住戶嘗試開啟門鎖，麻煩前往查看");
        String Time = getDateTime();
        noticeTimeRef.setValue(Time);
    }

    private String getDateTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 10);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        String newTime = sdf.format(calendar.getTime());
        return newTime;
    }

    public void setSkip(){
        this.skip=true;
    }
}
