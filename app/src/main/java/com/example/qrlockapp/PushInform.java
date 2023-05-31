package com.example.qrlockapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class PushInform extends AppCompatActivity {
    FirebaseDatabase database;
    Button push;
    ImageButton back;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.addinform_dialog);
        View view = getLayoutInflater().inflate(R.layout.addinform_dialog, null);
        push=(Button) view.findViewById(R.id.informsubmit);
        back= (ImageButton) view.findViewById(R.id.backButton);
        EditText massage;
        Spinner spinner = findViewById(R.id.spinner);
        massage=view.findViewById(R.id.massage);

        // 創建數據源，這是一個包含要顯示的選項數據的列表
        List<String> options = new ArrayList<>();
        options.add("選項1");
        options.add("選項2");
        options.add("選項3");

        // 創建 ArrayAdapter 並設置下拉式選單的樣式
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);

        // 設置下拉式選單的樣式（可選）
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 將適配器設置給 Spinner
                spinner.setAdapter(adapter);
        push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("BUTTON", "onClick: ");
                DatabaseReference ref=database.getReference("/Hint"+spinner);
                ref.setValue(massage.getText().toString());
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });



    }


}
