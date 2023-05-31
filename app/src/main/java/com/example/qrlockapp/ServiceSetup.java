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
    public boolean skip,finish;

    private Context context;
    NotificationChannel channel;
    Button BNotification;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    NotificationCompat.Builder builder;
    ValueEventListener valueEventListener;
    NotificationManager notificationManager;
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("/Time/"+lockName);
    private static final String TAG="ServiceSetup";

    public void onCreate() {
        super.onCreate();
        skip=true;
        context = this;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        Log.d(TAG, "Service created");


        // 初始化監聽器或其他必要的設置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channelId", "channelName", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 在這裡執行後台監聽任務
        Log.d(TAG, "Service started");
        skip=true;


        // 註冊Firebase Realtime Database監聽器


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

        Intent serviceIntent = new Intent(this, ServiceSetup.class);
        stopService(serviceIntent);
        // 停止監聽器或其他清理操作

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

            android.app.Notification.Builder builder = new android.app.Notification.Builder(context);
            builder.setSmallIcon(R.mipmap.ic_launcher)
                    .setChannelId("ID")
                    .setContentTitle(person+"嘗試開啟門鎖")
                    .setContentText(time)
                    .setContentIntent(PI);

            android.app.Notification notificaion = builder.build();
            notificationManager.notify(0, notificaion);

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
        // 獲取當前時間
        Calendar calendar = Calendar.getInstance();
        // 添加30分鐘
        calendar.add(Calendar.SECOND, 10);
        // 將時間格式化為你需要的格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        String newTime = sdf.format(calendar.getTime());
        return newTime;
    }
    public void setSkip(){
        this.skip=true;
    }
}
