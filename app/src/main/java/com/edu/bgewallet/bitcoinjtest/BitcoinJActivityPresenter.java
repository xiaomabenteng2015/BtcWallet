package com.edu.bgewallet.bitcoinjtest;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;


import com.edu.bgewallet.Constants;
import com.edu.bgewallet.R;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.util.Date;

public class BitcoinJActivityPresenter implements BitcoinJActivityContract.BitcoinJActivityPresenter {

    private BitcoinJActivityContract.BitcoinJActivityView view;
    private File walletDir; //Context.getCacheDir();

    private NetworkParameters parameters;
    private WalletAppKit walletAppKit;

    public BitcoinJActivityPresenter(BitcoinJActivityContract.BitcoinJActivityView view, File walletDir) {
        this.view = view;
        this.walletDir = walletDir;

        view.setPresenter(this);
    }

    @Override
    public void subscribe() {
        setBtcSDKThread();
        parameters = Constants.IS_PRODUCTION ? MainNetParams.get() : TestNet3Params.get();
        BriefLogFormatter.init();

        walletAppKit = new WalletAppKit(parameters, walletDir, Constants.WALLET_NAME) {
            @Override
            protected void onSetupCompleted() {
                if (wallet().getImportedKeys().size() < 1) wallet().importKey(new ECKey());
                wallet().allowSpendingUnconfirmedTransactions();
                view.displayWalletPath(vWalletFile.getAbsolutePath());
                setupWalletListeners(wallet());

                Log.d("myLogs", "My address = " + wallet().freshReceiveAddress());
            }
        };
        walletAppKit.setDownloadListener(new DownloadProgressTracker() {
            @Override
            protected void progress(double pct, int blocksSoFar, Date date) {
                super.progress(pct, blocksSoFar, date);
                int percentage = (int) pct;
                view.displayPercentage(percentage);
                view.displayProgress(percentage);
            }

            @Override
            protected void doneDownload() {
                super.doneDownload();
                view.displayDownloadContent(false);
                refresh();
            }
        });
        walletAppKit.setBlockingStartup(false);
        walletAppKit.startAsync();
    }

    @Override
    public void unsubscribe() {

    }

    @Override
    public void refresh() {
        String myAddress = walletAppKit.wallet().freshReceiveAddress().toBase58();

        view.displayMyBalance(walletAppKit.wallet().getBalance().toFriendlyString());
        view.displayMyAddress(myAddress);

    }

    @Override
    public void pickRecipient() {
        view.displayRecipientAddress(null);
        view.startScanQR();
    }

    @Override
    public void send() {
        String scan_recipient_qr_text = view.getContext().getResources().getString(R.string.scan_recipient_qr);
        String select_recipient_text = view.getContext().getResources().getString(R.string.select_recipient);
        String select_valid_amount_text = view.getContext().getResources().getString(R.string.select_valid_amount);
        String you_got_not_enough_coins_text = view.getContext().getResources().getString(R.string.you_got_not_enough_coins);

        String recipientAddress = view.getRecipient();
        String amount = view.getAmount();
        if (TextUtils.isEmpty(recipientAddress) || recipientAddress.equals(scan_recipient_qr_text)) {
            view.showToastMessage(select_recipient_text);
            return;
        }
        if (TextUtils.isEmpty(amount) | Double.parseDouble(amount) <= 0) {
            view.showToastMessage(select_valid_amount_text);
            return;
        }
        if (walletAppKit.wallet().getBalance().isLessThan(Coin.parseCoin(amount))) {
            view.showToastMessage(you_got_not_enough_coins_text);
            view.clearAmount();
            return;
        }
        SendRequest request = SendRequest.to(Address.fromBase58(parameters, recipientAddress), Coin.parseCoin(amount));
        try {
            walletAppKit.wallet().completeTx(request);
            walletAppKit.wallet().commitTx(request.tx);
            walletAppKit.peerGroup().broadcastTransaction(request.tx).broadcast();
        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
            view.showToastMessage(e.getMessage());
        }

    }

    @Override
    public void getInfoDialog() {
        view.displayInfoDialog(walletAppKit.wallet().currentReceiveAddress().toBase58());
    }

    private void setBtcSDKThread() {
        final Handler handler = new Handler();
        Threading.USER_THREAD = handler::post;
    }

    private void setupWalletListeners(Wallet wallet) {
        String send_text = view.getContext().getResources().getString(R.string.send);
        String receive_text = view.getContext().getResources().getString(R.string.receive);
        wallet.addCoinsReceivedEventListener((wallet1, tx, prevBalance, newBalance) -> {
            view.displayMyBalance(wallet.getBalance().toFriendlyString());
            if (tx.getPurpose() == Transaction.Purpose.UNKNOWN)
                view.showToastMessage(receive_text + " " + newBalance.minus(prevBalance).toFriendlyString());
        });
        wallet.addCoinsSentEventListener((wallet12, tx, prevBalance, newBalance) -> {
            view.displayMyBalance(wallet.getBalance().toFriendlyString());
            view.clearAmount();
            view.displayRecipientAddress(null);
            view.showToastMessage(send_text + " " + prevBalance.minus(newBalance).minus(tx.getFee()).toFriendlyString());
        });
    }
}
