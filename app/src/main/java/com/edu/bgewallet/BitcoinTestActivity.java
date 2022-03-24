package com.edu.bgewallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.edu.bgewallet.bitcoinjtest.BitcoinJActivity;
import com.edu.bgewallet.bitcoinjtest2.BitcoinWalletActivity;
import com.edu.bgewallet.bitcoinjtest2.EthereumWalletActivity;

/**
 * 入口
 */
public class BitcoinTestActivity extends AppCompatActivity {


    private TextView test1_tv;
    private TextView test2_tv;
    private TextView test3_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitcoin_test_main);

        test1_tv = findViewById(R.id.test1_tv);
        test2_tv = findViewById(R.id.test2_tv);
        test3_tv = findViewById(R.id.test3_tv);

        test1_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BitcoinTestActivity.this, BitcoinJActivity.class));
            }
        });

        test2_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BitcoinTestActivity.this, BitcoinWalletActivity.class));
            }
        });

        test3_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BitcoinTestActivity.this, EthereumWalletActivity.class));
            }
        });
    }


}