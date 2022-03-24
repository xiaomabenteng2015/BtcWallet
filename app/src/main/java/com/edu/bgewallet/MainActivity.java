package com.edu.bgewallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.edu.bgewallet.bitcoinjtest.BitcoinJActivity;
import com.edu.bgewallet.imtoken.ImtokenTest1Activity;

/**
 * 入口
 */
public class MainActivity extends AppCompatActivity {


    private TextView imtoken1Tv;
    private TextView bitcionjTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imtoken1Tv = findViewById(R.id.imtoken1_tv);
        bitcionjTv = findViewById(R.id.bitcionj_tv);

        imtoken1Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ImtokenTest1Activity.class));
            }
        });

        bitcionjTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BitcoinTestActivity.class));
            }
        });
    }


}