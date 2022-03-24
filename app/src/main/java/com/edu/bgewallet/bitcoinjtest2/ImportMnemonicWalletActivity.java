package com.edu.bgewallet.bitcoinjtest2;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.edu.bgewallet.R;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.uri.BitcoinURI;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.Wallet;

public class ImportMnemonicWalletActivity extends AppCompatActivity {

    private EditText nameEt;
    private EditText mnemonicEt;
    private EditText pwdEt;
    private EditText pwdHintEt;
    private Button recover_btn;
    private Button transaction_sign_btn;
    private TextView mAddressText;
    private TextView mBalanceText;
    private ImageView mQrImageView;

    private Wallet wallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_mnemonic);

        nameEt = findViewById(R.id.name_et);
        mnemonicEt = findViewById(R.id.mnemonic_et);
        pwdEt = findViewById(R.id.pwd_et);
        pwdHintEt = findViewById(R.id.pwd_hint_et);
        recover_btn = findViewById(R.id.recover_btn);
        transaction_sign_btn = findViewById(R.id.transaction_sign_btn);
        mAddressText = findViewById(R.id.signed_result_tv);
        mQrImageView = findViewById(R.id.qr_code);
        mBalanceText = findViewById(R.id.balance);

        //设置默认值
        nameEt.setText("123");
        pwdEt.setText("123");
        pwdHintEt.setText("123");
        mnemonicEt.setText("cook forum notable panda caught toward tennis desk bright grunt whale paddle");
        Log.d("test123", "UseWalletActivity");

        recover_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mnemonic = mnemonicEt.getText().toString().trim();
                String name = nameEt.getText().toString().trim();
                String pwd = pwdEt.getText().toString().trim();
                String pwdHint = pwdHintEt.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(ImportMnemonicWalletActivity.this, "请输入钱包名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(mnemonic)) {
                    Toast.makeText(ImportMnemonicWalletActivity.this, "请输入助记词", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(ImportMnemonicWalletActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pwdHint)) {
                    Toast.makeText(ImportMnemonicWalletActivity.this, "请输入密码提示", Toast.LENGTH_SHORT).show();
                    return;
                }

                wallet = BitcoinJHelper.createWallet(mnemonic, pwd);
                updateUI(wallet);

                BitcoinJHelper.getWalletKit(ImportMnemonicWalletActivity.this, mnemonic);
            }
        });

        transaction_sign_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEt.getText().toString().trim();
                String pwd = pwdEt.getText().toString().trim();
                String pwdHint = pwdHintEt.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(ImportMnemonicWalletActivity.this, "请输入钱包名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(ImportMnemonicWalletActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pwdHint)) {
                    Toast.makeText(ImportMnemonicWalletActivity.this, "请输入密码提示", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    private void updateUI(final Wallet wallet) {
        final Address address = wallet.currentAddress(KeyChain.KeyPurpose.RECEIVE_FUNDS);
        String s = BitcoinURI.convertToBitcoinURI(address, null, null, null);
        final Bitmap bitmap = Qr.bitmap(s);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Coin balance = wallet.getBalance(Wallet.BalanceType.ESTIMATED);
                mAddressText.setText(address.toString());
//                String balanceString = String.valueOf(balance.value / 100000) + " mBTC";
                String balanceString = String.valueOf(balance.value) + " BTC";
                mBalanceText.setText(balanceString);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                bitmapDrawable.setFilterBitmap(false);
                mQrImageView.setImageDrawable(bitmapDrawable);
            }
        });
    }


    private View.OnClickListener copyListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            TextView tv = (TextView) v;
            copyText(tv.getText().toString().trim());
            Toast.makeText(ImportMnemonicWalletActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 复制内容
     *
     * @param text
     */
    private void copyText(String text) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", text);
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
    }
}