package com.edu.bgewallet.bitcoinjtest;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.edu.bgewallet.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import net.glxn.qrgen.android.QRCode;

/**
 * BitcoinJ 测试一
 */
public class BitcoinJActivity extends AppCompatActivity implements BitcoinJActivityContract.BitcoinJActivityView {

    private BitcoinJActivityContract.BitcoinJActivityPresenter presenter;


    protected FrameLayout flDownloadContent_LDP;

    protected ProgressBar pbProgress_LDP;

    protected TextView tvPercentage_LDP;


    protected Toolbar toolbar_AT;

    protected SwipeRefreshLayout srlContent_AM;

    protected TextView tvMyBalance_AM;

    protected TextView tvMyAddress_AM;

    protected ImageView ivMyQRAddress_AM;

    protected TextView tvWalletFilePath_AM;

    protected EditText tvRecipientAddress_AM;

    protected ImageView iv_scan;

    protected TextView etAmount_AM;

    protected Button btnSend_AM;

    protected ImageView ivCopy_AM;


    protected ClipboardManager clipboardManager;


    protected String strScanRecipientQRCode;

    protected String strAbout;

    protected int colorGreenDark;

    protected int colorGreyDark;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitcionj_test1);

        initData();

        initUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuScanQR_MM:
                clickMenuGetRecipientQR();
                break;
            case R.id.menuInfo_MM:
                clickMenuInfo();
                break;
            default:
                break;
        }
        return true;
    }

    protected void initData() {
        new BitcoinJActivityPresenter(this, getCacheDir());

        strScanRecipientQRCode = getString(R.string.scan_recipient_qr);
        strAbout = getString(R.string.about);

        colorGreenDark = getResources().getColor(android.R.color.holo_green_dark);
        colorGreyDark = getResources().getColor(android.R.color.darker_gray);
    }


    protected void initUI() {
        initView();

        initToolbar();

        setListeners();

        presenter.subscribe();
    }

    private void initView() {
        flDownloadContent_LDP = findViewById(R.id.flDownloadContent_LDP);
        pbProgress_LDP = findViewById(R.id.pbProgress_LDP);
        tvPercentage_LDP = findViewById(R.id.tvPercentage_LDP);
        toolbar_AT = findViewById(R.id.toolbar_AT);
        srlContent_AM = findViewById(R.id.srlContent_AM);
        tvMyBalance_AM = findViewById(R.id.tvMyBalance_AM);
        tvMyAddress_AM = findViewById(R.id.tvMyAddress_AM);
        ivMyQRAddress_AM = findViewById(R.id.ivMyQRAddress_AM);
        tvWalletFilePath_AM = findViewById(R.id.tvWalletFilePath_AM);
        tvRecipientAddress_AM = findViewById(R.id.tvRecipientAddress_AM);
        iv_scan = findViewById(R.id.iv_scan);
        etAmount_AM = findViewById(R.id.etAmount_AM);
        btnSend_AM = findViewById(R.id.btnSend_AM);
        ivCopy_AM = findViewById(R.id.ivCopy_AM);

        //获取剪贴板管理器：
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    }


    protected void clickMenuGetRecipientQR() {
        presenter.pickRecipient();
    }


    protected void clickMenuInfo() {
        presenter.getInfoDialog();
    }

    private void initToolbar() {
        setSupportActionBar(toolbar_AT);
        if (getSupportActionBar() != null) {
            String title = getResources().getString(R.string.btc_wallet);
            getSupportActionBar().setTitle(title);
        }
    }


    @Override
    public void setPresenter(BitcoinJActivityContract.BitcoinJActivityPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void displayDownloadContent(boolean isShown) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                flDownloadContent_LDP.setVisibility(isShown ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void displayProgress(int percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (pbProgress_LDP.isIndeterminate()) pbProgress_LDP.setIndeterminate(false);
                pbProgress_LDP.setProgress(percent);
            }
        });
    }

    @Override
    public void displayPercentage(int percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvPercentage_LDP.setText(String.valueOf(percent) + " %");
            }
        });
    }

    @Override
    public void displayMyBalance(String myBalance) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvMyBalance_AM.setText(myBalance);
            }
        });
    }

    @Override
    public void displayWalletPath(String walletPath) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvWalletFilePath_AM.setText(walletPath);
            }
        });
    }

    @Override
    public void displayMyAddress(String myAddress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvMyAddress_AM.setText(myAddress);
                Log.i("test123","test123 我的地址 myAddress = " + myAddress);
                Bitmap bitmapMyQR = QRCode.from(myAddress).bitmap();   //base58 address
                ivMyQRAddress_AM.setImageBitmap(bitmapMyQR);
                if(srlContent_AM.isRefreshing()) srlContent_AM.setRefreshing(false);
            }
        });
    }

    @Override
    public void displayRecipientAddress(String recipientAddress) {
        tvRecipientAddress_AM.setText(TextUtils.isEmpty(recipientAddress) ? strScanRecipientQRCode : recipientAddress);
        tvRecipientAddress_AM.setTextColor(TextUtils.isEmpty(recipientAddress) ? colorGreyDark : colorGreenDark);
    }


    @Override
    public void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getRecipient() {
        return tvRecipientAddress_AM.getText().toString().trim();
    }

    @Override
    public String getAmount() {
        return etAmount_AM.getText().toString();
    }

    @Override
    public void clearAmount() {
        etAmount_AM.setText(null);
    }

    @Override
    public void startScanQR() {
        new IntentIntegrator(this).initiateScan();
    }

    @Override
    public void displayInfoDialog(String myAddress) {
        String title = getResources().getString(R.string.about_title);
        String btnText = getResources().getString(R.string.got_it);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(Html.fromHtml(strAbout));
        builder.setCancelable(true);
        builder.setPositiveButton(btnText, (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        TextView msgTxt = (TextView) alertDialog.findViewById(android.R.id.message);
        msgTxt.setMovementMethod(LinkMovementMethod.getInstance());
        Log.i("test123","test123 TestNet3 比特币网络1 = http://faucet.xeno-genesis.com/" );
        Log.i("test123","test123 TestNet3 比特币网络2 = https://testnet.coinfaucet.eu/en/" );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            Log.i("test123","test123 scanResult.getContents() = " + scanResult.getContents());
            displayRecipientAddress(scanResult.getContents());
        }
    }

    private void setListeners() {
        srlContent_AM.setOnRefreshListener(() -> presenter.refresh());
        iv_scan.setOnClickListener(v -> presenter.pickRecipient());
        btnSend_AM.setOnClickListener(v -> presenter.send());
        ivCopy_AM.setOnClickListener(v -> {
            String preText = getResources().getString(R.string.my_wallet_address);
            String copiedText = getResources().getString(R.string.copied);
            ClipData clip = ClipData.newPlainText(preText, tvMyAddress_AM.getText().toString());
            clipboardManager.setPrimaryClip(clip);
            Toast.makeText(BitcoinJActivity.this, copiedText, Toast.LENGTH_SHORT).show();
        });
    }
}