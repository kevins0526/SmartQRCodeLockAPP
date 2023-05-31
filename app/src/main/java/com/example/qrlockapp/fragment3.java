package com.example.qrlockapp;


import static com.example.qrlockapp.GlobalVariable.lockName;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class fragment3 extends Fragment {
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_fragment3, container, false);
        Button profileBtn,signOutBtn,changeLockBtn,clearLockBtn;
        profileBtn = (Button)view.findViewById(R.id.profileBtn);
        signOutBtn = (Button)view.findViewById(R.id.signOutBtn);
        //changeLockBtn = (Button)view.findViewById(R.id.changeLockName);
        clearLockBtn = (Button)view.findViewById(R.id.clearLockRecord);
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),profile.class);
                startActivity(intent);
            }
        });
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(),MainActivity.class);
                startActivity(intent);

                ServiceSetup serviceSetup=new ServiceSetup();
                serviceSetup.setSkip();

                changeIsSingin changeIsSingin = new changeIsSingin(getActivity());
                changeIsSingin.putBoolean(IsSingin.KEY_IS_SINGIN,false);
            }
        });
//        changeLockBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getActivity(),updateLockName.class);
//                startActivity(intent);
//            }
//        });
        clearLockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog();
            }
        });
        return view;
    }
    private void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("注意!");  //設置標題
        builder.setIcon(R.mipmap.ic_launcher_round); //標題前面那個小圖示
        builder.setMessage("點選確定將會清空綁定門鎖的開啟紀錄"); //提示訊息

        //確定 取消 一般 這三種按鈕就看你怎麼發揮你想置入的功能囉
        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference clearRef =database.getReference("/Time/"+lockName);
                clearRef.removeValue();
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }
}
