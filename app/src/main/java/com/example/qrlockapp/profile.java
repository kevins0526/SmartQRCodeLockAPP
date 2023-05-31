package com.example.qrlockapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class profile extends AppCompatActivity {
    TextView name,number,email;
    Button backBtn,submit,update;
    AlertDialog dialog;
    FirebaseAuth mAuth;
    String nameValue;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getProfile();
        update = findViewById(R.id.updateProfie);
        backBtn = findViewById(R.id.backBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("更新資料");
        View view = getLayoutInflater().inflate(R.layout.updateprofile_dialog, null);
        EditText eNumber,eEmail;
        eNumber = view.findViewById(R.id.ePhoneNumber);
        eEmail = view.findViewById(R.id.eEmail);
        submit = view.findViewById(R.id.submit);
        builder.setView(view);
        dialog=builder.create();
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                mAuth = FirebaseAuth.getInstance();
                DatabaseReference userNumber =database.getReference("/profile/"+nameValue+"/電話號碼");
                DatabaseReference userEmail =database.getReference("/profile/"+nameValue+"/電子信箱");
                userNumber.setValue(eNumber.getText().toString());
                userEmail.setValue(eEmail.getText().toString());
                getProfile();
                dialog.dismiss();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    public void getProfile(){
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();
        String displayName = user.getDisplayName();
        name = findViewById(R.id.fullName);
        number = findViewById(R.id.phoneNumber);
        email = findViewById(R.id.Email);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userProfile =database.getReference("/userID/"+displayName+"/姓名");
        userProfile.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                nameValue = dataSnapshot.getValue(String.class);
                name.setText("姓名 :\n"+nameValue);
                DatabaseReference userNumber =database.getReference("/profile/"+nameValue+"/電話號碼");
                DatabaseReference userEmail =database.getReference("/profile/"+nameValue+"/電子信箱");

                userNumber.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String value = dataSnapshot.getValue(String.class);
                        number.setText("電話號碼 :\n"+value);
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("TAG", "Failed to read value.", error.toException());
                    }
                });

                userEmail.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String value = dataSnapshot.getValue(String.class);
                        email.setText("電子信箱 :\n"+value);
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("TAG", "Failed to read value.", error.toException());
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException());
            }
        });

    }
}