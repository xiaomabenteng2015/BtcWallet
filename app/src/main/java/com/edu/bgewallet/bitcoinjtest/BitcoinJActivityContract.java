package com.edu.bgewallet.bitcoinjtest;


import android.content.Context;

import com.edu.bgewallet.bitcoinjtest.base.BasePresenter;
import com.edu.bgewallet.bitcoinjtest.base.BaseView;


public interface BitcoinJActivityContract {
    interface BitcoinJActivityView extends BaseView<BitcoinJActivityPresenter> {
        Context getContext();
        void displayDownloadContent(boolean isShown);
        void displayProgress(int percent);
        void displayPercentage(int percent);

        void displayMyBalance(String myBalance);
        void displayWalletPath(String walletPath);

        void displayMyAddress(String myAddress);
        void displayRecipientAddress(String recipientAddress);

        void showToastMessage(String message);
        String getRecipient();
        String getAmount();
        void clearAmount();

        void startScanQR();
        void displayInfoDialog(String myAddress);
    }
    interface BitcoinJActivityPresenter extends BasePresenter {
        void refresh();
        void pickRecipient();
        void send();

        void getInfoDialog();
    }
    interface MainActivityModel {

    }
}
