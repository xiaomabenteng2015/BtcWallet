package com.edu.bgewallet.imtoken;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.edu.bgewallet.R;

/**
 * Imtoken 库 com.github.consenlabs:token-core-android:v0.1
 * 测试类
 */
public class ImtokenTest1Activity extends AppCompatActivity {


    private TextView createWalletTv;
    private TextView useWalletTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imtoken_test);

        createWalletTv = findViewById(R.id.create_tv);
        useWalletTv = findViewById(R.id.use_tv);

        createWalletTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ImtokenTest1Activity.this, CreateWalletActivity.class));
            }
        });

        useWalletTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ImtokenTest1Activity.this, UseWalletActivity.class));
            }
        });
    }


}