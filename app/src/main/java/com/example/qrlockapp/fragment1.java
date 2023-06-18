package com.example.qrlockapp;


import static android.content.Context.MODE_PRIVATE;

import static com.example.qrlockapp.GlobalVariable.aesPassword;
import static com.example.qrlockapp.GlobalVariable.lockName;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;



import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.EnumMap;
import java.util.Map;

public class fragment1 extends Fragment{
    private FirebaseAuth mAuth;
    Button CreateBtn,GuestBtn;
    SharedPreferences pref;
    String displayName;
    ImageView ivCode;
    long IV;
    private ValueEventListener loginTimeListener; //紀錄監聽器 防止多監聽器出現
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  //fragment 視圖
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_fragment1, container, false);
        pref = getActivity().getSharedPreferences("PREF",MODE_PRIVATE);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();
        displayName = user.getDisplayName();
        ivCode = (ImageView)myView.findViewById(R.id.imageView4);
        if(!displayName.equals(readDisplayName())){
            aesPassword = "";
            getCode();
        }else{
            aesPassword = readKey();
            BarcodeEncoder encoder = new BarcodeEncoder();
            try {
                Bitmap bit = encoder.encodeBitmap(aesPassword, BarcodeFormat.QR_CODE, 1000, 1000);
                ivCode.setImageBitmap(bit);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }

        CreateBtn = (Button) myView.findViewById(R.id.create_btn);
        GuestBtn = (Button) myView.findViewById(R.id.guest_btn);

        CreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                getCode();
            }
        });
        GuestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpToGuest();
            }
        });
        displayName = user.getDisplayName();
        DatabaseReference loginTime = database.getReference("/userID/"+displayName+"/loginTime");
    // 检查监听器是否为 null，避免重复添加
        if (loginTimeListener == null) {
            loginTimeListener = new ValueEventListener() {
                int times = 0;
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (times > 0) {
                        getCode();
                    }
                    times++;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };

            // 添加监听器
            loginTime.addValueEventListener(loginTimeListener);
        }

        return  myView;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // 在退出 Fragment 时移除监听器
        if (loginTimeListener != null) {
            DatabaseReference loginTime = database.getReference("/userID/" + displayName + "/loginTime");
            loginTime.removeEventListener(loginTimeListener);
            loginTimeListener = null;
        }
    }
    public void getCode() {
        deleteAesPassword(aesPassword);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();
        displayName = user.getDisplayName();
        IV=Randomize.IV();
        aesPassword=AEScbc.encrypt(displayName,String.valueOf(IV));
        updateAesPassword(aesPassword,String.valueOf(IV));
        saveKey();
        BarcodeEncoder encoder = new BarcodeEncoder();
        Map<EncodeHintType, ErrorCorrectionLevel> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q); // 設定容錯能力為 25%
        try {
            Bitmap bit = encoder.encodeBitmap(aesPassword, BarcodeFormat.QR_CODE, 1000, 1000);
            ivCode.setImageBitmap(bit);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
    public void jumpToGuest(){
        Intent intent = new Intent(getActivity(),guestKey.class);
        startActivity(intent);
    }
    public void saveKey(){
        pref.edit()
                .putString("KEY",aesPassword)
                .putString("DisplayName",displayName)
                .apply();                   //或commit()
    }
    public void updateAesPassword(String aesPassword,String IV){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userPassword =database.getReference("/aesPassword/"+lockName+"/"+aesPassword);
        userPassword.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    getCode();
                }
                else {
                    userPassword.setValue(IV);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public void deleteAesPassword(String aesPassword){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userPassword =database.getReference("/aesPassword/"+lockName+"/"+aesPassword);
        userPassword.removeValue();
    }
    public String readKey(){
        return pref.getString("KEY","");
    }
    public String readDisplayName(){
        return pref.getString("DisplayName","");
    }

}
