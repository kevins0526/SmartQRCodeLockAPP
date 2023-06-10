package com.example.qrlockapp;

import static com.example.qrlockapp.GlobalVariable.lockName;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;

public class guestKey extends AppCompatActivity {
    Button backBtn,requestGuestKeyBtn,shareBtn,btnSelectTime,deleteBtn;
    ImageView guestQrcodeView;
    EditText guestNameEdit;
    public static String guestName;
    public static String guestPassword;
    TextView countDownTimeTextView,tvSelectedTime;
    SharedPreferences pref;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    Bitmap bit;
    FirebaseAuth mAuth;
    long IV;
    int selectedHour=0;
    int selectedMinute=10;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);
        backBtn = findViewById(R.id.back_btn);
        btnSelectTime = findViewById(R.id.timeBtn);
        tvSelectedTime = findViewById(R.id.tHour2);
        shareBtn = findViewById(R.id.shareBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        requestGuestKeyBtn = findViewById(R.id.requestGuestKey);
        guestQrcodeView = findViewById(R.id.guestQrcode);
        guestNameEdit = findViewById(R.id.guestName);
        countDownTimeTextView = findViewById(R.id.countDownTime);
        mAuth = FirebaseAuth.getInstance();

        final GlobalVariable app = (GlobalVariable) getApplication();
        if(app.switchGuest()){
            requestGuestKeyBtn.setVisibility(View.VISIBLE);
            guestNameEdit.setVisibility(View.VISIBLE);
            btnSelectTime.setVisibility(View.VISIBLE);
            tvSelectedTime.setVisibility(View.VISIBLE);
        }else{
            shareBtn.setVisibility(View.VISIBLE);
            deleteBtn.setVisibility(View.VISIBLE);
            requestGuestKeyBtn.setVisibility(View.GONE);
            guestNameEdit.setVisibility(View.GONE);
            btnSelectTime.setVisibility(View.GONE);
            tvSelectedTime.setVisibility(View.GONE);
            String tempKey=read();
            String endTime = readTime();
            countDownTimeTextView.setText(guestName+"訪客鑰匙時效截止\n"+endTime);
            BarcodeEncoder encoder1 = new BarcodeEncoder();
            Map<EncodeHintType, ErrorCorrectionLevel> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q); // 設定容錯能力為 25%
                    try {
                        bit = encoder1.encodeBitmap(tempKey, BarcodeFormat.QR_CODE, 800, 800);
                        guestQrcodeView.setImageBitmap(bit);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
        }
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSelectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedHour = hourOfDay;
                        selectedMinute = minute;
                        // 更新TextView的文本為選擇的時間
                        String time = String.format("%02d時%02d分", selectedHour, selectedMinute);
                        tvSelectedTime.setText(time);
                    }
                };

                TimePickerDialog timePickerDialog = new TimePickerDialog(guestKey.this,
                        timeSetListener, selectedHour, selectedMinute, true);

                timePickerDialog.show();
            }
        });
        requestGuestKeyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = guestNameEdit.getText().toString();
                if(name.equals("")){
                    Toast.makeText(guestKey.this, "請輸入訪客名稱喔!", Toast.LENGTH_SHORT).show();
                }else {
                    requestGuestKeyBtn.setVisibility(View.GONE);
                    guestNameEdit.setVisibility(View.GONE);
                    btnSelectTime.setVisibility(View.GONE);
                    tvSelectedTime.setVisibility(View.GONE);
                    shareBtn.setVisibility(View.VISIBLE);
                    deleteBtn.setVisibility(View.VISIBLE);
                    app.getSwitchGuest(false);
                    guestName = "Guest_" + name;
                    IV = Randomize.IV();
                    guestPassword = AEScbc.encrypt(guestName, String.valueOf(IV));
                    saveName();
                    updateAesPassword(guestPassword, String.valueOf(IV));
                    BarcodeEncoder encoder = new BarcodeEncoder();
                    Map<EncodeHintType, ErrorCorrectionLevel> hints = new EnumMap<>(EncodeHintType.class);
                    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q); // 設定容錯能力為 25%
                    try {
                        bit = encoder.encodeBitmap(guestPassword, BarcodeFormat.QR_CODE, 800, 800);
                        guestQrcodeView.setImageBitmap(bit);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
//                  countDownTime();
                    String dateTime = getDateTime(selectedHour, selectedMinute);
                    countDownTimeTextView.setText(guestName+"訪客鑰匙時效截止\n"+dateTime);
                    saveTime();
                    Intent serviceIntent = new Intent(guestKey.this, CountdownService.class);
                    serviceIntent.putExtra("hour", selectedHour); // 將值放入Intent中，"key"是鍵名，value是要傳遞的值
                    serviceIntent.putExtra("min", selectedMinute); // 將值放入Intent中，"key"是鍵名，value是要傳遞的值
                    startService(serviceIntent);
                }
                //Toast.makeText(guestKey.this, guestName, Toast.LENGTH_SHORT).show();
            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bit == null) {
                    // 如果 bit 為空值，顯示提醒訊息
                    new AlertDialog.Builder(guestKey.this)
                            .setTitle("提醒")
                            .setMessage("沒有 QR Code 圖像可分享")
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                } else {
                    // 建立一個分享對象 Intent
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);

                    // 設定分享對象的 MIME 類型為圖像
                    shareIntent.setType("image/*");

                    // 將 Bitmap 圖像加入到分享對象中
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bit.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), bit, "QR Code", null);
                    Uri imageUri = Uri.parse(path);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

                    PopupMenu popupMenu = new PopupMenu(guestKey.this, shareBtn);
                    popupMenu.getMenuInflater().inflate(R.menu.share_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.line:
                                    // 呼叫 LINE 應用程式的分享功能
                                    shareIntent.setPackage("jp.naver.line.android");
                                    startActivity(shareIntent);
                                    break;
                                case R.id.messenger:
                                    // 執行 Messager 分享功能
                                    shareIntent.setPackage("com.facebook.orca");
                                    startActivity(shareIntent);
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 停止服务
                Intent stopIntent = new Intent(guestKey.this, CountdownService.class);
                stopService(stopIntent);
                deleteAesPassword(guestPassword);
                clear();
                countDownTimeTextView.setText("可以新增訪客鑰匙囉~");
                shareBtn.setVisibility(View.GONE);
                deleteBtn.setVisibility(View.GONE);
                requestGuestKeyBtn.setVisibility(View.VISIBLE);
                btnSelectTime.setVisibility(View.VISIBLE);
                guestNameEdit.setVisibility(View.VISIBLE);
                tvSelectedTime.setVisibility(View.VISIBLE);
                guestQrcodeView.setImageBitmap(null);
                app.getSwitchGuest(true); //時間過後再進才能重新生成
            }
        });
    }
    public void updateAesPassword(String aesPassword,String IV){
        DatabaseReference userPassword =database.getReference("/aesPassword/"+lockName+"/"+aesPassword);
        userPassword.setValue(IV);
    }
    public void deleteAesPassword(String aesPassword){
        DatabaseReference userPassword =database.getReference("/aesPassword/"+lockName+"/"+aesPassword);
        userPassword.removeValue();
        DatabaseReference deleteUserID =database.getReference("/userID/"+guestName);
        deleteUserID.removeValue();
    }

    public void saveName(){
        pref = getSharedPreferences("DATA",MODE_PRIVATE);
        pref.edit()
                .putString("NAME",guestNameEdit.getText().toString())
                .putString("KEY",guestPassword)
                .apply();                   //或commit()
    }
    public void saveTime(){
        pref = getSharedPreferences("DATA",MODE_PRIVATE);
        String dateTime = getDateTime(selectedHour, selectedMinute);
        pref.edit()
                .putString("TIME",dateTime)
                .apply();                   //或commit()
    }
    public String readTime(){
        pref = getSharedPreferences("DATA",MODE_PRIVATE);
        String time = pref.getString("TIME","");
        return time;
    }
    //讀取資料
    public String read(){
        pref = getSharedPreferences("DATA",MODE_PRIVATE);
        String name = pref.getString("NAME","");
        String key = pref.getString("KEY","");
        return key;
    }
    //清除EditTexts內容
    public void clear(){
        pref = getSharedPreferences("DATA",MODE_PRIVATE);
        //下面程式碼能清除所有資料
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
    private String getDateTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        // 添加所选的小时和分钟
        calendar.add(Calendar.HOUR_OF_DAY, hour);
        calendar.add(Calendar.MINUTE, minute);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        String newTime = sdf.format(calendar.getTime());
        return newTime;
    }

}