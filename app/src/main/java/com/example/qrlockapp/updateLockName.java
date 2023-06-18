package com.example.qrlockapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class updateLockName extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    EditText eLockName;
    TextView tWrongCode,tNowLockName;
    Button updateLockName_Btn;
    SharedPreferences pref;
    @SuppressLint("MissingInflatedId")
    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_lock_name);
        GlobalVariable gv = (GlobalVariable)getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        pref = getSharedPreferences("PREF",MODE_PRIVATE);
        eLockName = findViewById(R.id.eLockName);
        tWrongCode = findViewById(R.id.tWrongCode);
        tNowLockName = findViewById(R.id.tNowLockName);
        FirebaseUser user = mAuth.getCurrentUser();
        updateLockName_Btn = findViewById(R.id.updateLockName_Btn);
//        if(!gv.getLockName().equals("")){
//            tNowLockName.setText("目前綁定門鎖為:"+gv.getLockName());
//        }
        updateLockName_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lockNameCode = eLockName.getText().toString();
                if (lockNameCode.equals("")) {
                    tWrongCode.setText("不得為空");
                } else {
                    DatabaseReference lockNameRef = database.getReference("/lockName/" + lockNameCode);
                    lockNameRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String lockName = snapshot.getValue(String.class);
                                gv.saveLock(lockName);
                                gv.lockName = gv.readLock();
                                tNowLockName.setText("目前綁定門鎖為:" + lockName);
                                Toast.makeText(updateLockName.this, "已更改綁定門鎖為:" + lockName, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                intent.setClass(updateLockName.this, MainActivity2.class);
                                startActivity(intent);
                            } else {
                                tWrongCode.setText("代碼錯誤，請輸入正確代碼!");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
            });
    }
}