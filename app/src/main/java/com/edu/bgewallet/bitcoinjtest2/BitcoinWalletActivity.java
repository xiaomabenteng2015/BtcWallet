package com.edu.bgewallet.bitcoinjtest2;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.edu.bgewallet.Constants;
import com.edu.bgewallet.R;
import com.edu.bgewallet.imtoken.CreateWalletActivity;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.uri.BitcoinURI;
import org.bitcoinj.utils.MonetaryFormat;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.bitcoinj.wallet.listeners.WalletReorganizeEventListener;

/**
 * 比特币钱包测试
 */
public class BitcoinWalletActivity extends AppCompatActivity {

    private static final String TAG = "BitcoinWalletActivity";
    protected Toolbar toolbar_AT;
    private TextView mAddressText;
    private TextView mBalanceText;
    private ImageView mQrImageView;
    private ImageView copy_iv;
    private Wallet wallet;


    private EditText mToAddressEdit;
    private EditText mAmountEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitcoin_wallet);
        initView();
        BitcoinWalletManager.getInstance().loadWallet(this, new BitcoinWalletManager.OnWalletLoadedListener() {
            @Override
            public void onWalletLoaded(final Wallet w) {
                wallet = w;
                addWalletListeners();
                updateUI(wallet);
                startService(new Intent(BitcoinWalletActivity.this, BlockChainService.class));

            }

        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.btc_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export_wallet_file:
                exportKeyStore();
                break;
            case R.id.export_private_key:
                exportPrivateKey();
                break;
            case R.id.import_mnemonics:
                Intent intent1 = new Intent(this, ImportMnemonicWalletActivity.class);
                startActivity(intent1);
                break;
            case R.id.mnemonics:
                Intent intent = new Intent(this, MnemonicActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    private void initView() {
        toolbar_AT = findViewById(R.id.toolbar_AT);
        mAddressText = findViewById(R.id.address);
        mBalanceText = findViewById(R.id.balance);
        mQrImageView = findViewById(R.id.qr_code);
        mToAddressEdit = findViewById(R.id.to);
        mAmountEdit = findViewById(R.id.amount);
        copy_iv = findViewById(R.id.copy_iv);

        //复制
        copy_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = mAddressText.getText().toString().trim();
                copyText(address);
                Toast.makeText(BitcoinWalletActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
            }
        });

        initToolbar();
    }

    private void initToolbar() {
        setSupportActionBar(toolbar_AT);
        if (getSupportActionBar() != null) {
            String title = getResources().getString(R.string.btc_wallet);
            getSupportActionBar().setTitle(title);
        }
    }

    private void addWalletListeners() {
        wallet.addChangeEventListener(mWalletListener);
        wallet.addCoinsReceivedEventListener(mWalletListener);
        wallet.addCoinsSentEventListener(mWalletListener);
        wallet.addReorganizeEventListener(mWalletListener);
    }

    private void removeWalletListener() {
        wallet.removeChangeEventListener(mWalletListener);
        wallet.removeCoinsReceivedEventListener(mWalletListener);
        wallet.removeCoinsSentEventListener(mWalletListener);
        wallet.removeReorganizeEventListener(mWalletListener);
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
                String balanceString = String.valueOf(balance.value / 100000) + " mBTC";
                mBalanceText.setText(balanceString);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                bitmapDrawable.setFilterBitmap(false);
                mQrImageView.setImageDrawable(bitmapDrawable);
            }
        });
    }

    private WalletListener mWalletListener = new WalletListener();

    public void onSendBitcoin(View view) {
        String to = mToAddressEdit.getText().toString();
        String amount = mAmountEdit.getText().toString();
        if (TextUtils.isEmpty(to) || TextUtils.isEmpty(amount)) {
            return;
        }
        Address address = Address.fromBase58(Constants.NETWORK_PARAMETERS, to);
        Coin coin = MonetaryFormat.MBTC.parse(amount);
        SendRequest sendRequest = SendRequest.to(address, coin);
        try {
            Transaction transaction = wallet.sendCoinsOffline(sendRequest);
            BlockChainService.broadcastTransaction(BitcoinWalletActivity.this, transaction);
        } catch (InsufficientMoneyException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private class WalletListener implements WalletChangeEventListener,
            WalletCoinsSentEventListener, WalletReorganizeEventListener, WalletCoinsReceivedEventListener {

        @Override
        public void onWalletChanged(Wallet wallet) {
            Log.d(TAG, "onWalletChanged: " + wallet.currentAddress(KeyChain.KeyPurpose.RECEIVE_FUNDS));
            updateUI(wallet);
        }

        @Override
        public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
            Log.d(TAG, "onCoinsSent: " + tx.getHashAsString() + "preBalance: "
                    + prevBalance.getValue() + "newBalance: " + newBalance.getValue());
            updateUI(wallet);
        }

        @Override
        public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
            Log.d(TAG, "onCoinsReceived: " + tx.getHashAsString() + "prevBalance" + prevBalance.getValue()
                    + "newBalance " + newBalance.getValue());
            updateUI(wallet);
        }

        @Override
        public void onReorganize(Wallet wallet) {
            updateUI(wallet);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeWalletListener();
    }

    private void exportKeyStore() {
        if (wallet == null) {
            return;
        }
        Intent intent = new Intent(this, KeyStoreActivity.class);
//        intent.putExtra("keystore", EthWalletManager.getInstance().exportKeyStore(wallet));
//        startActivity(intent);
    }

    private void exportPrivateKey() {
        if (wallet == null) {
            return;
        }

//        Intent intent = new Intent(this, PrivateKeyActivity.class);
//        intent.putExtra("pk", EthWalletManager.getInstance().exportPrivateKey(wallet));
//        startActivity(intent);

    }

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
