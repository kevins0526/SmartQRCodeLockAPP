package com.example.qrlockapp;


import static com.example.qrlockapp.GlobalVariable.aesPassword;
import static com.example.qrlockapp.GlobalVariable.lockName;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class fragment3 extends Fragment {
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_fragment3, container, false);
        GlobalVariable gv = (GlobalVariable) getActivity().getApplication();
        Button profileBtn,signOutBtn,clearLockBtn,protectBtn;
        profileBtn = view.findViewById(R.id.profileBtn);
        signOutBtn = view.findViewById(R.id.signOutBtn);
        clearLockBtn = view.findViewById(R.id.clearLockRecord);
        protectBtn = view.findViewById(R.id.protectBtn);
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
                deleteAesPassword(aesPassword);
                gv.clearAesPassword();
                lockName = null;
                if (isServiceRunning()) {
                    // 关闭前台服务
                    requireActivity().stopService(new Intent(requireContext(), ServiceSetup.class));
                    Toast.makeText(getActivity(), "門鎖管家已關閉", Toast.LENGTH_SHORT).show();
                }
            }
        });
        clearLockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog();
            }
        });
        protectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isServiceRunning()) {
                    // 关闭前台服务
                    requireActivity().stopService(new Intent(requireContext(), ServiceSetup.class));
                    Toast.makeText(getActivity(), "門鎖管家已關閉", Toast.LENGTH_SHORT).show();
                } else {
                    // 开启前台服务
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        requireActivity().startForegroundService(new Intent(requireContext(), ServiceSetup.class));
                    } else {
                        requireActivity().startService(new Intent(requireContext(), ServiceSetup.class));
                    }
                    Toast.makeText(getActivity(), "門鎖管家已開啟", Toast.LENGTH_SHORT).show();
                }
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
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) requireContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);

        // 遍历运行中的服务列表
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (ServiceSetup.class.getName().equals(service.service.getClassName())) {
                // 找到了前台服务，返回 true
                return true;
            }
        }
        // 没有找到前台服务，返回 false
        return false;
    }
    public void deleteAesPassword(String aesPassword){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userPassword =database.getReference("/aesPassword/"+lockName+"/"+aesPassword);
        userPassword.removeValue();
    }
}
