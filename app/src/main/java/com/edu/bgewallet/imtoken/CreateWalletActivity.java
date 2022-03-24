package com.edu.bgewallet.imtoken;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.edu.bgewallet.Constants;
import com.edu.bgewallet.R;

import org.consenlabs.tokencore.wallet.Identity;
import org.consenlabs.tokencore.wallet.KeystoreStorage;
import org.consenlabs.tokencore.wallet.Wallet;
import org.consenlabs.tokencore.wallet.WalletManager;
import org.consenlabs.tokencore.wallet.model.BIP44Util;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.MnemonicAndPath;
import org.consenlabs.tokencore.wallet.model.Network;

import java.io.File;

public class CreateWalletActivity extends AppCompatActivity implements KeystoreStorage {

    private EditText nameEt;
    private EditText pwdEt;
    private EditText pwdHintEt;
    private Button createMnemonicBtn;
    private Button createWalletBtn;
    private Button exportBtn;
    private TextView mnemonicTv;
    private TextView addressTv;
    private TextView encxpubTv;
    private TextView privatekeyTv;
    private TextView keystoreTv;

    private Identity identity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        //初始化存储以存储密钥库文件
        WalletManager.storage = this;
        WalletManager.scanWallets();

        nameEt = findViewById(R.id.name_et);
        pwdEt = findViewById(R.id.pwd_et);
        pwdHintEt = findViewById(R.id.pwd_hint_et);
        createMnemonicBtn = findViewById(R.id.create_mnemonic_btn);
        createWalletBtn = findViewById(R.id.create_wallet_btn);
        exportBtn = findViewById(R.id.export_btn);
        mnemonicTv = findViewById(R.id.mnemonic_tv);
        addressTv = findViewById(R.id.address_tv);
        encxpubTv = findViewById(R.id.encxpub_tv);
        privatekeyTv = findViewById(R.id.privatekey_tv);
        keystoreTv = findViewById(R.id.Keystore_tv);

        Log.d("test123", "CreateWalletActivity");

        createMnemonicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEt.getText().toString().trim();
                String pwd = pwdEt.getText().toString().trim();
                String pwdHint = pwdHintEt.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(CreateWalletActivity.this, "请输入钱包名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(CreateWalletActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pwdHint)) {
                    Toast.makeText(CreateWalletActivity.this, "请输入密码提示", Toast.LENGTH_SHORT).show();
                    return;
                }

                createIdentity(name, pwd, pwdHint);
            }
        });

        createWalletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = pwdEt.getText().toString().trim();
                if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(CreateWalletActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                String mnemonic = mnemonicTv.getText().toString().split(":")[1].trim();
                createBTCHDWalletExample(mnemonic, pwd);
            }
        });

        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = pwdEt.getText().toString().trim();
                if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(CreateWalletActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                exportWallet(pwd);
            }
        });

        mnemonicTv.setOnClickListener(copyListener);
        addressTv.setOnClickListener(copyListener);
        encxpubTv.setOnClickListener(copyListener);
        encxpubTv.setOnClickListener(copyListener);
        privatekeyTv.setOnClickListener(copyListener);
        keystoreTv.setOnClickListener(copyListener);
    }

    @Override
    public File getKeystoreDir() {
        return this.getFilesDir();
    }

    /**
     * 创造新身份，衍生eth、btc钱包
     */
    @SuppressLint("SetTextI18n")
    private void createIdentity(String name, String password, String passwordHint) {
        //在创建其他钱包之前，应该先创建或恢复身份
        //最后一个参数是元数据。P2WPKH表示衍生btc钱包是SegWit钱包
        identity = Identity.createIdentity(name, password, passwordHint, Network.TESTNET, Constants.segwit);

//        Wallet ethereumWallet = identity.getWallets().get(0);
        Wallet bitcoinWallet = identity.getWallets().get(1);
        Log.i("test123", "id = " + bitcoinWallet.getId());
        Log.i("test123", "Address = " + bitcoinWallet.getAddress());
        Log.i("test123", "EncXPub = " + bitcoinWallet.getEncXPub());
        Log.i("test123", "Network = " + bitcoinWallet.getMetadata().getNetwork());
        MnemonicAndPath mnemonicAndPath = WalletManager.exportMnemonic(bitcoinWallet.getId(), password);
        Log.i("test123", "mnemonic = " + mnemonicAndPath.getMnemonic());
        Log.i("test123", "path = " + mnemonicAndPath.getPath());
        //助记词
        mnemonicTv.setText("助记词:" + mnemonicAndPath.getMnemonic());
        //地址
        addressTv.setText("地址：" + bitcoinWallet.getAddress());
        encxpubTv.setText("EncXPub：" + bitcoinWallet.getEncXPub());
    }

    /**
     * 导出钱包
     */
    @SuppressLint("SetTextI18n")
    private void exportWallet(String password) {
        Wallet bitcoinWallet = identity.getWallets().get(0);
        String prvKey = WalletManager.exportPrivateKey(bitcoinWallet.getId(), password);
        Log.i("test123", String.format("PrivateKey: %s", prvKey));
        String mnemonic = WalletManager.exportMnemonic(bitcoinWallet.getId(), password).getMnemonic();
        Log.i("test123", String.format("Mnemonic: %s", mnemonic));
        String json = WalletManager.exportKeystore(bitcoinWallet.getId(), password);
        Log.i("test123", String.format("Keystore: %s", json));

        privatekeyTv.setText("privatekey:" + prvKey);
        keystoreTv.setText("keystore:" + json);
    }

    /**
     * 交易签名
     */
    private void signTransaction() {
//        EthereumTransaction tran = new EthereumTransaction()
//        TxSignResult result = tran.signTransaction();
//        String signedTx = result.getSignedTx(); // This is the signature result which you need to broadcast.
//        String txHash = result.getTxHash(); // This is txHash which you can use for locating your transaction record
    }

    public void createBTCHDWalletExample(String mnemonic, String password) {
        Log.i("test123", "-------- Create BTC Wallet Example: ");
        Metadata metadata = new Metadata();
        metadata.setSource(Metadata.FROM_MNEMONIC);
        metadata.setWalletType(Metadata.HD);
        metadata.setChainType(ChainType.BITCOIN);
        Wallet wallet = WalletManager.importWalletFromMnemonic(metadata, mnemonic, BIP44Util.BITCOIN_SEGWIT_TESTNET_PATH, password, true);
        Log.i("test123", "m/44'/0'/0'/0/0 address: " + wallet.getAddress());
        Log.i("test123", "m/44'/0'/0'/1/0 address: " + wallet.newReceiveAddress(0));
        Log.i("test123", "Enc XPub(Encrypted with 'aes-cbc-128'): " + wallet.getEncXPub());
    }

    private View.OnClickListener copyListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            TextView tv = (TextView) v;
            copyText(tv.getText().toString().trim());
            Toast.makeText(CreateWalletActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
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