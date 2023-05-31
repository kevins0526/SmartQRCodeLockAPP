package com.example.qrlockapp;

import static com.example.qrlockapp.GlobalVariable.lockName;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class fragment2 extends Fragment {
    View view;
    private FirebaseAuth mAuth;
    TextView member;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private List<Object> mData;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_fragment2, container, false);
        mData = new ArrayList<>();
        mAdapter = new MyAdapter(mData);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();
        String displayName = user.getDisplayName();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("/Time/"+lockName);

        // 使用orderByKey()來依時間排序，並使用limitToLast()來取得最後十筆
        Query query = ref.orderByKey().limitToLast(15);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;
                List<String> latestEntries = new ArrayList<String>();

                // 迭代資料並取出
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    String time = childSnapshot.getKey();
                    String name = childSnapshot.getValue(String.class);
                    if(name.equals("unknown")){
                        latestEntries.add("\n未知用戶嘗試開鎖!!" + "\n開鎖時間: " + time+"\n");

                    }else {
                        latestEntries.add("\n用戶暱稱: " + name + "\n開鎖時間: " + time+"\n");
                    }
                    count++;
                }

                // 刪除超過十筆的最舊資料
                if (count > 15) {
                    DatabaseReference oldestEntryRef = ref.child(latestEntries.get(0).split(":")[0]);
                    oldestEntryRef.removeValue();
                }

                // 這裡是取得最新的十筆資料
                // 資料已經按照時間從晚到早排序了，所以反轉一下
                Collections.reverse(latestEntries);
                mData.addAll(latestEntries);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });

        return view;
    }
}