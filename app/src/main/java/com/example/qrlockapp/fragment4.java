package com.example.qrlockapp;


import static com.example.qrlockapp.GlobalVariable.lockName;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class fragment4 extends Fragment {
    View view;
    FirebaseAuth mAuth;
    AlertDialog add_dialog,delete_dialog,manage_dialog;
    RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    Button Hint,resident,add,delete,managedAdd;
    private List<Object> mData;
    String selectedOption,deleteSelectedOption;
    String nodeInfo="";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_fragment4, container, false);
        mData = new ArrayList<>();
        mAdapter = new MyAdapter(mData);
        mRecyclerView = view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("留言提醒");
        View addInformView = getLayoutInflater().inflate(R.layout.addinform_dialog, null);
        builder.setView(addInformView);
        add_dialog=builder.create();

        Button push=addInformView.findViewById(R.id.informsubmit);
        Spinner spinner = addInformView.findViewById(R.id.spinner);
        EditText massage= addInformView.findViewById(R.id.massage);
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setTitle("刪除留言");
        View deleteInformView = getLayoutInflater().inflate(R.layout.deleteinform_dialog, null);
        builder1.setView(deleteInformView);
        delete_dialog=builder1.create();

        Button deletePush=deleteInformView.findViewById(R.id.deleteSubmit);
        Spinner deleteSpinner = deleteInformView.findViewById(R.id.deleteSpinner);

        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
        builder2.setTitle("傳送消息給管理室");
        View manageInformView = getLayoutInflater().inflate(R.layout.manageaddinform_dialog, null);
        builder2.setView(manageInformView);
        manage_dialog=builder2.create();
        Button manageBtn=manageInformView.findViewById(R.id.manageInformSubmit);
        EditText manageMassage= manageInformView.findViewById(R.id.manageMassage);
        List<String> HintEntries = new ArrayList<>();
        List<String> residentEntries = new ArrayList<>();

        managedAdd = view.findViewById(R.id.manageAdd);
        Hint=view.findViewById(R.id.Hint);
        resident=view.findViewById(R.id.resident);
        add= view.findViewById(R.id.add);
        delete = view.findViewById(R.id.delete);
        Hint.setSelected(true);
        resident.setSelected(false);
        delete.setVisibility(View.GONE);
        add.setVisibility(View.GONE);
        managedAdd.setVisibility(View.VISIBLE);
        Hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Hint.setSelected(true);
                resident.setSelected(false);
                mData.clear();
                mData.addAll(HintEntries);
                managedAdd.setVisibility(View.VISIBLE);
                delete.setVisibility(View.GONE);
                add.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
            }
        });

        resident.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                Hint.setSelected(false);
                resident.setSelected(true);
                mData.clear();
                mData.addAll(residentEntries);
                managedAdd.setVisibility(View.GONE);
                delete.setVisibility(View.VISIBLE);
                add.setVisibility(View.VISIBLE);
                mAdapter.notifyDataSetChanged();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_dialog.show();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_dialog.show();
            }
        });
        managedAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manage_dialog.show();
            }
        });

        manageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference manageMsgRef=database.getReference("/manageMsg/"+lockName+"/msg/");
                DatabaseReference manageTimeRef=database.getReference("/manageMsg/"+lockName+"/time/");
                manageMsgRef.setValue(manageMassage.getText().toString());
                manageTimeRef.setValue(getDateTime());
                Toast.makeText(getActivity(), "已傳送給管理室", Toast.LENGTH_SHORT).show();
                manage_dialog.dismiss();
            }
        });
        DatabaseReference hintRef = database.getReference("/Resident/");
        hintRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> options = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String nodeName = snapshot.getKey();
                    options.add(nodeName);
                }
                // 創建 ArrayAdapter 並設置下拉式選單的樣式
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, options);
                // 設置下拉式選單的樣式（可選）
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // 將適配器設置給 Spinner
                spinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 處理數據讀取被取消的情況
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 獲取選中的項目
                selectedOption = (String) parent.getItemAtPosition(position);
                // 在這裡處理選中項目的資料
                // 你可以將資料傳遞給其他地方處理，或執行相應的操作
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 如果沒有項目被選擇時的處理（可選）
            }
        });

        DatabaseReference deleteRef = database.getReference("/Hint/"+lockName);
        deleteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> deleteOptions = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String nodeName = snapshot.getKey();
                    deleteOptions.add(nodeName);
                }
                // 創建 ArrayAdapter 並設置下拉式選單的樣式
                ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, deleteOptions);
                // 設置下拉式選單的樣式（可選）
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // 將適配器設置給 Spinner
                deleteSpinner.setAdapter(adapter1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 處理數據讀取被取消的情況
            }
        });

        deleteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 獲取選中的項目
                deleteSelectedOption = (String) parent.getItemAtPosition(position);
                // 在這裡處理選中項目的資料
                // 你可以將資料傳遞給其他地方處理，或執行相應的操作
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 如果沒有項目被選擇時的處理（可選）
            }
        });

        push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref=database.getReference("/Hint/"+selectedOption+"/"+lockName);
                ref.setValue(massage.getText().toString()+"\n\n留言時間:"+getDateTime());
                Toast.makeText(getActivity(), "留言成功", Toast.LENGTH_SHORT).show();
                mAdapter.notifyDataSetChanged();
                add_dialog.dismiss();
            }
        });
        deletePush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref=database.getReference("/Hint/"+lockName+"/"+deleteSelectedOption);
                Toast.makeText(getActivity(), "刪除成功", Toast.LENGTH_SHORT).show();
                ref.removeValue();
            }
        });

        DatabaseReference allHintRef = database.getReference("/Hint/allHint/all");
        allHintRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String informVal = dataSnapshot.getValue(String.class);
                if (informVal != null) {
                    String inform = "\n社區公告:\n\n" + informVal+"\n";
                    HintEntries.add(inform);
                }
                DatabaseReference residentHint = database.getReference("/Hint/allHint/"+lockName);
                residentHint.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String informVal = snapshot.getValue(String.class);
                        if (informVal != null) {
                            String inform = "\n管理室提醒您:\n\n" + informVal+"\n";
                            HintEntries.add(inform);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                DatabaseReference reference = database.getReference("/Resident/" + lockName);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            String nodeName = childSnapshot.getKey();
                            String nodeValue = childSnapshot.getValue(String.class);
                            if (nodeValue.equals("✓")) {
                                nodeInfo += "\n"+nodeName + ": " + "未繳交\n";
                            }
                        }
                        if(!nodeInfo.equals("")) {
                            HintEntries.add("\n管理室提醒您:\n"+nodeInfo);
//                            nodeInfo="\n管理室提醒您:\n";
                            mData.addAll(HintEntries);
                            mAdapter.notifyDataSetChanged();
                        }else{
                            mData.addAll(HintEntries);
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("讀取數據時出錯拉!：" + databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("讀取數據時出錯拉!" + databaseError.getMessage());
            }
        });

        DatabaseReference ResidentHintRef = database.getReference("/Hint/"+lockName);

        ResidentHintRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                residentEntries.clear(); // 清空 residentEntries 列表，只保留最新的留言
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String nodeName = childSnapshot.getKey();
                    String nodeValue = childSnapshot.getValue(String.class);
                    String nodeInfo = "\n"+nodeName+"住戶的留言:\n\n"+nodeValue+"\n";
                    residentEntries.add(nodeInfo);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("讀取數據時出錯拉!：" + databaseError.getMessage());
            }
        });
        return view;
    }
    private String getDateTime() {
        // 獲取當前時間
        Calendar calendar = Calendar.getInstance();
        // 添加30分鐘
        calendar.add(Calendar.SECOND, 10);
        // 將時間格式化為你需要的格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        String newTime = sdf.format(calendar.getTime());
        return newTime;
    }
}