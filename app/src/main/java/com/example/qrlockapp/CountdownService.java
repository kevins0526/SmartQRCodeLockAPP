package com.example.qrlockapp;

import static com.example.qrlockapp.GlobalVariable.lockName;
import static com.example.qrlockapp.guestKey.guestName;
import static com.example.qrlockapp.guestKey.guestPassword;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class CountdownService extends Service {
    private static final long COUNTDOWN_INTERVAL = 1000; // 1秒
    private static final long TOTAL_COUNTDOWN_TIME = 3600000; // 60秒
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "CountdownChannel";
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    NotificationManager notificationManager;
    NotificationChannel channel;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final GlobalVariable app = (GlobalVariable) getApplication();

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化倒计时器
        timeLeftInMillis = TOTAL_COUNTDOWN_TIME;
        countDownTimer = new CountDownTimer(timeLeftInMillis, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                // 更新UI或执行其他必要的操作
            }

            @Override
            public void onFinish() {
                // 倒计时结束时执行的操作
                stopForeground(true); // 停止前台服务
                deleteAesPassword(guestPassword);
                NotificationSend();
                final GlobalVariable app = (GlobalVariable) getApplication();
                app.getSwitchGuest(true);
                stopSelf(); // 停止Service
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int selectedHour = intent.getIntExtra("hour", 0);
            int selectedMinute = intent.getIntExtra("min", 0);

            // 將選擇的小時和分鐘轉換為毫秒數
            long selectedTimeInMillis = (selectedHour * 60 + selectedMinute) * 60 * 1000;

            // 更新倒計時器的時間和間隔
            timeLeftInMillis = selectedTimeInMillis;
            countDownTimer = new CountDownTimer(timeLeftInMillis, COUNTDOWN_INTERVAL) {
                // ... 倒計時器的onTick和onFinish方法
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMillis = millisUntilFinished;
                    // 更新UI或执行其他必要的操作
                }

                @Override
                public void onFinish() {
                    // 倒计时结束时执行的操作
                    stopForeground(true); // 停止前台服务
                    deleteAesPassword(guestPassword);
                    NotificationSend();
                    final GlobalVariable app = (GlobalVariable) getApplication();
                    app.getSwitchGuest(true);
                    stopSelf(); // 停止Service
                }
            };

            startForeground(NOTIFICATION_ID, createNotification());
            countDownTimer.start(); // 啟動倒計時器
        }
        return START_STICKY; // 确保Service在被系统杀死后重新启动
    }

    @Override
    public void onDestroy() {
//        if (countDownTimer != null) {
//            countDownTimer.cancel(); // 取消倒计时器
//        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void NotificationSend() {
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("ID", "Countdown Channel", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            Intent intent = new Intent(this, guestKey.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent PI = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            android.app.Notification.Builder builder = new android.app.Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher)
                    .setChannelId("ID")
                    .setContentTitle("訪客鑰匙已到期")
                    .setContentText("訪客鑰匙已失效，可以重新生成囉!")
                    .setContentIntent(PI);

            android.app.Notification notification = builder.build();
            notificationManager.notify(0, notification);

        }
    }

    public void deleteAesPassword(String aesPassword){
        DatabaseReference userPassword =database.getReference("/aesPassword/"+lockName+"/"+aesPassword);
        userPassword.removeValue();
        DatabaseReference deleteUserID =database.getReference("/userID/"+guestName);
        deleteUserID.removeValue();
    }
    private Notification createNotification() {
        // 创建一个前台服务通知
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("訪客鑰匙使用者:"+guestName)
                .setContentText("訪客鑰匙使用權限倒計時中")
                .setSmallIcon(R.drawable.ic_baseline_access_time_24)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Countdown Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}

