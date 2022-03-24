package com.edu.bgewallet.bitcoinjtest2;

import android.content.ContextWrapper;
import android.os.AsyncTask;

import com.edu.bgewallet.Constants;

import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletFiles;
import org.bitcoinj.wallet.WalletProtobufSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class BitcoinWalletManager {

    private static BitcoinWalletManager sManager;

    private Wallet wallet;

    private BitcoinWalletManager() {
    }

    public static BitcoinWalletManager getInstance() {
        if (sManager == null) {
            synchronized (BitcoinWalletManager.class) {
                if (sManager == null) {
                    sManager = new BitcoinWalletManager();
                }
            }
        }
        return sManager;
    }

    public void loadWallet(final ContextWrapper activity, final OnWalletLoadedListener listener) {
        if (wallet != null && listener != null) {
            listener.onWalletLoaded(wallet);
            return;
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //本地钱包文件
                    File walletFile = activity.getFileStreamPath("wallet-protobuf");
                    if (walletFile.exists()) {
                        //如果存在，则读取文件
                        wallet = readWallet(walletFile);
                    } else {
                        //如果不存在，则创建
                        wallet = createWallet(walletFile);
                    }
                    if (listener != null) {
                        listener.onWalletLoaded(wallet);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnreadableWalletException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 创建钱包
     * @param walletFile
     * @return
     * @throws IOException
     */
    private Wallet createWallet(File walletFile) throws IOException {
        Wallet wallet = new Wallet(Constants.NETWORK_PARAMETERS);
        WalletFiles walletFiles = wallet.autosaveToFile(walletFile, 3 * 1000, TimeUnit.MILLISECONDS, null);
        walletFiles.saveNow();
        return wallet;
    }

    /**
     * 读取钱包
     * @param walletFile
     * @return
     * @throws FileNotFoundException
     * @throws UnreadableWalletException
     */
    private Wallet readWallet(File walletFile) throws FileNotFoundException, UnreadableWalletException {
        InputStream inputStream = new FileInputStream(walletFile);
        Wallet wallet  = new WalletProtobufSerializer().readWallet(inputStream);
        wallet.autosaveToFile(walletFile, 3 * 1000, TimeUnit.MILLISECONDS, null);
        wallet.cleanup();
        return wallet;
    }

    public Wallet getWallet() {
        return wallet;
    }


    public static interface OnWalletLoadedListener {
        void onWalletLoaded(Wallet wallet);
    }
}
