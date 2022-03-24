package com.edu.bgewallet.bitcoinjtest2;

import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.edu.bgewallet.R;

public class PrivateKeyActivity extends AppCompatActivity {

    private EditText mPrivateKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_key);

        String pk = getIntent().getStringExtra("pk");

        mPrivateKey = findViewById(R.id.private_key);
        mPrivateKey.setText(pk);

    }
}
