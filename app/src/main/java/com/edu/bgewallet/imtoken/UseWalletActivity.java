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
import org.consenlabs.tokencore.wallet.model.ChainId;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.MnemonicAndPath;
import org.consenlabs.tokencore.wallet.model.Network;
import org.consenlabs.tokencore.wallet.transaction.BitcoinTransaction;
import org.consenlabs.tokencore.wallet.transaction.TxSignResult;

import java.io.File;
import java.util.ArrayList;

public class UseWalletActivity extends AppCompatActivity implements KeystoreStorage {

    private EditText nameEt;
    private EditText mnemonicEt;
    private EditText pwdEt;
    private EditText pwdHintEt;
    private Button recover_btn;
    private Button transaction_sign_btn;
    private TextView signed_result_tv;


    private Identity identity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use);
        //初始化存储以存储密钥库文件
        WalletManager.storage = this;
        WalletManager.scanWallets();

        nameEt = findViewById(R.id.name_et);
        mnemonicEt = findViewById(R.id.mnemonic_et);
        pwdEt = findViewById(R.id.pwd_et);
        pwdHintEt = findViewById(R.id.pwd_hint_et);
        recover_btn = findViewById(R.id.recover_btn);
        transaction_sign_btn = findViewById(R.id.transaction_sign_btn);
        signed_result_tv = findViewById(R.id.signed_result_tv);

        Log.d("test123", "UseWalletActivity");
        mnemonicEt.setText("cook forum notable panda caught toward tennis desk bright grunt whale paddle");

        recover_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mnemonic = mnemonicEt.getText().toString().trim();
                String name = nameEt.getText().toString().trim();
                String pwd = pwdEt.getText().toString().trim();
                String pwdHint = pwdHintEt.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(UseWalletActivity.this, "请输入钱包名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(mnemonic)) {
                    Toast.makeText(UseWalletActivity.this, "请输入助记词", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(UseWalletActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pwdHint)) {
                    Toast.makeText(UseWalletActivity.this, "请输入密码提示", Toast.LENGTH_SHORT).show();
                    return;
                }

                recoverIdentity(mnemonic, name, pwd, pwdHint);

                exportWallet(pwd);
            }
        });

        transaction_sign_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEt.getText().toString().trim();
                String pwd = pwdEt.getText().toString().trim();
                String pwdHint = pwdHintEt.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(UseWalletActivity.this, "请输入钱包名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(UseWalletActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pwdHint)) {
                    Toast.makeText(UseWalletActivity.this, "请输入密码提示", Toast.LENGTH_SHORT).show();
                    return;
                }
                Wallet bitcoinWallet = identity.getWallets().get(1);
                String mnemonic = WalletManager.exportMnemonic(bitcoinWallet.getId(), pwd).getMnemonic();
                if (TextUtils.isEmpty(mnemonic)) {
                    Toast.makeText(UseWalletActivity.this, "请输入助记词", Toast.LENGTH_SHORT).show();
                    return;
                }
//                bitcoinTransactionSignExample1(mnemonic, name, pwd, pwdHint);

            }
        });
    }

    @Override
    public File getKeystoreDir() {
        return this.getFilesDir();
    }

    /**
     * 创造新身份，衍生eth、btc钱包
     */
    @SuppressLint("SetTextI18n")
    private void recoverIdentity(String mnemonic, String name, String password, String passwordHit) {
        //在创建其他钱包之前，应该恢复身份
        //最后一个参数是元数据。P2WPKH表示衍生btc钱包是SegWit钱包
        identity = Identity.recoverIdentity(mnemonic, name, password, passwordHit, Network.TESTNET, Constants.segwit);

//        Wallet ethereumWallet = identity.getWallets().get(0);
        Wallet bitcoinWallet = identity.getWallets().get(1);
        Log.i("test123", "id = " + bitcoinWallet.getId());
        Log.i("test123", "Address = " + bitcoinWallet.getAddress());
        Log.i("test123", "EncXPub = " + bitcoinWallet.getEncXPub());
        Log.i("test123", "Network = " + bitcoinWallet.getMetadata().getNetwork());
        MnemonicAndPath mnemonicAndPath = WalletManager.exportMnemonic(bitcoinWallet.getId(), password);
        Log.i("test123", "mnemonic = " + mnemonicAndPath.getMnemonic());
        Log.i("test123", "path = " + mnemonicAndPath.getPath());
    }

    /**
     * 导出钱包
     */
    @SuppressLint("SetTextI18n")
    private void exportWallet(String password) {
        Wallet bitcoinWallet = identity.getWallets().get(1);
        String prvKey = WalletManager.exportPrivateKey(bitcoinWallet.getId(), password);
        Log.i("test123", String.format("PrivateKey: %s", prvKey));
        String mnemonic = WalletManager.exportMnemonic(bitcoinWallet.getId(), password).getMnemonic();
        Log.i("test123", String.format("Mnemonic: %s", mnemonic));
        String json = WalletManager.exportKeystore(bitcoinWallet.getId(), password);
        Log.i("test123", String.format("Keystore: %s", json));
    }

    /**
     * 交易签名
     */
    private void signTransaction() {
//        EthereumTransaction tran = new EthereumTransaction();
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

    public void bitcoinTransactionSignExample1(String mnemonic, String name, String password, String passwordHit) {
        Log.i("test123", "");
        Log.i("test123", "-------- Bitcoin transaction sign example: ");
        Metadata metadata = new Metadata();
        metadata.setSource(Metadata.FROM_MNEMONIC);
        metadata.setWalletType(Metadata.HD);
        metadata.setChainType(ChainType.BITCOIN);
        metadata.setNetwork(Network.TESTNET);
        Wallet wallet = WalletManager.importWalletFromMnemonic(metadata, mnemonic, BIP44Util.BITCOIN_SEGWIT_TESTNET_PATH, password, true);

        TxSignResult signedResult = createMultiUXTOOnTestnet().signSegWitTransaction(Integer.toString(ChainId.BITCOIN_TESTNET), password, wallet);
        Log.i("test123", "Sign Result: " + signedResult.getSignedTx());
    }

    public void bitcoinTransactionSignExample(String mnemonic, String name, String password, String passwordHit) {
        Log.i("test123", "");
        Log.i("test123", "-------- Bitcoin transaction sign example: " + mnemonic);
        Metadata walletMetadata = new Metadata(ChainType.BITCOIN, Network.TESTNET, name, passwordHit);
        walletMetadata.setSource(Metadata.FROM_MNEMONIC);
        walletMetadata.setNetwork(Network.TESTNET);
        Wallet wallet = WalletManager.importWalletFromMnemonic(walletMetadata, mnemonic, BIP44Util.BITCOIN_SEGWIT_TESTNET_PATH,
                password, true);

        TxSignResult signedResult = createMultiUXTOOnTestnet().signSegWitTransaction(Integer.toString(ChainId.BITCOIN_TESTNET), password, wallet);
        Log.i("test123", "Sign Result: " + signedResult.getSignedTx());
    }


    private static BitcoinTransaction createMultiUXTOOnTestnet() {
        ArrayList<BitcoinTransaction.UTXO> utxo = new ArrayList<>();

        utxo.add(new BitcoinTransaction.UTXO(
                "983adf9d813a2b8057454cc6f36c6081948af849966f9b9a33e5b653b02f227a", 0,
                200000000, "mh7jj2ELSQUvRQELbn9qyA4q5nADhmJmUC",
                "76a914118c3123196e030a8a607c22bafc1577af61497d88ac",
                "0/22"));
//        utxo.add(new BitcoinTransaction.UTXO(
//                "45ef8ac7f78b3d7d5ce71ae7934aea02f4ece1af458773f12af8ca4d79a9b531", 1,
//                200000000, "mkeNU5nVnozJiaACDELLCsVUc8Wxoh1rQN",
//                "76a914383fb81cb0a3fc724b5e08cf8bbd404336d711f688ac",
//                "0/0"));
//        utxo.add(new BitcoinTransaction.UTXO(
//                "14c67e92611dc33df31887bbc468fbbb6df4b77f551071d888a195d1df402ca9", 0,
//                200000000, "mkeNU5nVnozJiaACDELLCsVUc8Wxoh1rQN",
//                "76a914383fb81cb0a3fc724b5e08cf8bbd404336d711f688ac",
//                "0/0"));
//        utxo.add(new BitcoinTransaction.UTXO(
//                "117fb6b85ded92e87ee3b599fb0468f13aa0c24b4a442a0d334fb184883e9ab9", 1,
//                200000000, "mkeNU5nVnozJiaACDELLCsVUc8Wxoh1rQN",
//                "76a914383fb81cb0a3fc724b5e08cf8bbd404336d711f688ac",
//                "0/0"));

        BitcoinTransaction tran = new BitcoinTransaction("moLK3tBG86ifpDDTqAQzs4a9cUoNjVLRE3", 53,
                750000000, 502130, utxo);

        return tran;
    }

    private View.OnClickListener copyListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            TextView tv = (TextView) v;
            copyText(tv.getText().toString().trim());
            Toast.makeText(UseWalletActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
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