package com.example.qrlockapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class forgotPassword extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ImageButton backButton = findViewById(R.id.backButton);
        Button forgot_submit = findViewById(R.id.btn_forgot_submit);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        forgot_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText forgot_email = findViewById(R.id.forgot_email);
                String userMail = forgot_email.getText().toString();
                if (TextUtils.isEmpty(userMail)) {
                    Toast.makeText(forgotPassword.this, "請填寫有效的電子郵件", Toast.LENGTH_SHORT).show();
                }
                else {
                    mAuth.sendPasswordResetEmail(userMail).addOnCompleteListener(new OnCompleteListener() {

                        @Override
                        public void onComplete(@NonNull Task task) {

                            if (task.isSuccessful()) {

                                Toast.makeText(forgotPassword.this, "請確認您的電子郵件", Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(forgotPassword.this, MainActivity.class));

                            } else {

                                Toast.makeText(forgotPassword.this, "未找到電子信箱，請在試一次", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            }
        });

    }
}