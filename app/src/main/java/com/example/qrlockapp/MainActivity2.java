package com.example.qrlockapp;

import static com.example.qrlockapp.GlobalVariable.lockName;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity2 extends AppCompatActivity {
    NavigationBarView navigationBarView;
    changeIsSingin changeIsSingin;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeIsSingin = new changeIsSingin(getApplicationContext());
        changeIsSingin.putBoolean(IsSingin.KEY_IS_SINGIN,true);
        setContentView(R.layout.activity_main2);
        mAuth = FirebaseAuth.getInstance();
        //Toast.makeText(this , lockName, Toast.LENGTH_SHORT).show();
            navigationBarView = findViewById(R.id.navigation);
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.FrameLayout, new fragment1()).commit(); // 切換主畫面
            }
            navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment Fragment = null;
                    if (item.getItemId() == R.id.navigation_item1) {
                        Fragment = new fragment1();
                    } else if (item.getItemId() == R.id.navigation_item2) {
                        Fragment = new fragment2();
                    } else if (item.getItemId() == R.id.navigation_item3) {
                        Fragment = new fragment3();
                    }else if(item.getItemId() == R.id.navigation_item4) {
                        Fragment =new fragment4();
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.FrameLayout, Fragment).commit();
                    return true;
                }
            });

    }
    }

