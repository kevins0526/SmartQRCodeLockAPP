package com.example.qrlockapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private SharedPreferences pref;//暫時存取字串用
    private SharedPreferences.Editor editor;
    private Button login;
    private CheckBox rememberPass;
    public GlobalVariable gv; // 声明全局的 GlobalVariable 变量
    Activity context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        changeIsSingin changeIsSingin = new changeIsSingin(getApplicationContext());

        if(changeIsSingin.getBoolean(IsSingin.KEY_IS_SINGIN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity2.class);
            startActivity(intent);
            finish();
        }

        gv = (GlobalVariable) getApplication(); // 初始化 GlobalVariable 实例
//        GlobalVariable gv = (GlobalVariable)getApplicationContext();
        // 检查是否是第一次使用应用程序
        boolean isFirstTime = checkIfFirstTime();
        // 如果是第一次使用应用程序，则启动前台服务
        if (isFirstTime) {
            Intent serviceIntent = new Intent(this, ServiceSetup.class);
            startService(serviceIntent);
        }
        Button button = findViewById(R.id.button);
        TextView textView = findViewById(R.id.forgotPassword);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        EditText txtUsername = findViewById(R.id.etUsername);
        EditText txtPassword = findViewById(R.id.etPassword);
        rememberPass = findViewById(R.id.remember_account);
        login = (Button) findViewById(R.id.button);
        boolean isRemember = pref.getBoolean("remember_account",false);
        if(isRemember){
            String account = pref.getString("account","");//取pref已存帳號
            String password = pref.getString("password","");//取pref已存密碼
            txtUsername.setText(account);
            txtPassword.setText(password);
            rememberPass.setChecked(true);
        }
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v) {
                //調用
                EditText txtUsername = findViewById(R.id.etUsername);
                EditText txtPassword = findViewById(R.id.etPassword);
               //取值
                String username = txtUsername.getText().toString();
                String password = txtPassword.getText().toString();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    String msg = "帳號或密碼不得為空白!";
                    TextView wrongPassword = findViewById(R.id.wrong);
                    wrongPassword.setText(msg);
                } else {
                    editor=pref.edit();//取得pref存入功能
                    if(rememberPass.isChecked()){
                        editor.putBoolean("remember_account",true);
                        editor.putString("account",username);//存帳號
                        editor.putString("password",password);//存密碼
                    }else{
                        editor.clear();
                    }
                    editor.commit();//提交
                    mAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                FirebaseUser user=mAuth.getCurrentUser();
                                String displayName = user.getDisplayName();
                                DatabaseReference lockRef = database.getReference("/userID/"+displayName+"/lockName");
                                lockRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        gv.saveLock(snapshot.getValue(String.class));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                Intent intent = new Intent();
//                                if (gv.readLock().equals("")) {
//                                    intent.setClass(MainActivity.this, updateLockName.class);
//                                } else {
                                    intent.setClass(MainActivity.this, MainActivity2.class);
//                                }
                                startActivity(intent);
                            } else {
                                String msg = "帳號或密碼錯誤!";
                                TextView wrongPassword = findViewById(R.id.wrong);
                                wrongPassword.setText(msg);
                                txtUsername.setText(null);
                                txtPassword.setText(null);
                            }
                        }
                    });
                }
            }
        });
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, forgotPassword.class);
                startActivity(intent);
            }
        });
    }
    private boolean checkIfFirstTime() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isFirstTime = sharedPreferences.getBoolean("isFirstTime", true);

        if (isFirstTime) {
            // 标记为已使用过应用程序
            sharedPreferences.edit().putBoolean("isFirstTime", false).apply();
        }

        return isFirstTime;
    }

}