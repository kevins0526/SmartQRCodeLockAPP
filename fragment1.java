package com.example.qrlockapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class fragment1 extends Fragment{
    private FirebaseAuth mAuth;
    Button CreateBtn,GuestBtn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  //fragment 視圖
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_fragment1, container, false);
        CreateBtn = (Button) myView.findViewById(R.id.create_btn);
        GuestBtn = (Button) myView.findViewById(R.id.guest_btn);
        //CreateBtn.setOnClickListener(this);
        getFirebaseValue();
        CreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCode();
            }
        });
        GuestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpToGuest();
            }
        });
        return  myView;
    }
    public void getCode() {
        ImageView ivCode = (ImageView)getView().findViewById(R.id.imageView4);
        TextView password = (TextView)getView().findViewById(R.id.seepassword);
        long IV=Randomize.IV();
        String AesPassword = mixKey(password.getText().toString(),IV);
        Toast.makeText(getActivity(), String.valueOf(IV), Toast.LENGTH_SHORT).show();
        updateFirebaseValue(AesPassword,IV); //傳加密後密碼到firebase
        BarcodeEncoder encoder = new BarcodeEncoder();
        try {
            Bitmap bit = encoder.encodeBitmap(AesPassword, BarcodeFormat.QR_CODE, 1000, 1000);
            ivCode.setImageBitmap(bit);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
    public String mixKey(String aes ,long IV){ //混合原始資料
        //String str=AES.encrypt(aes);
        //Log.i("-=-=解密",AES.decrypt(str));
        String str=AES.cbcEncrypt(aes, String.valueOf(IV));
        return str;
    }
//    public void onClick(View v) {
//        getCode();
//    }

    public void getFirebaseValue(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference password = database.getReference("password"); //讀取的根結點
        password.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                TextView password = (TextView)getView().findViewById(R.id.seepassword);
                password.setText(value);
                getCode(); //目前不確定firebase資料庫更新會不會造成閃退 但不更改不報錯
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException());
            }
        });
    }
    public void updateFirebaseValue(String AesPas,long IV){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();
        String uid = user.getUid();
        DatabaseReference AesPassword = database.getReference(uid+"/AesPassword"); //讀取的根結點
        DatabaseReference ivKey=database.getReference(uid+"/ivKey");
        ivKey.setValue(IV);
        AesPassword.setValue(AesPas);

    }
    public void jumpToGuest(){
        Intent intent = new Intent(getActivity(),guestKey.class);
        startActivity(intent);
    }
}
