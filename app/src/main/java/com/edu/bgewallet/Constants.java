package com.edu.bgewallet;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.consenlabs.tokencore.wallet.model.Metadata;

public class Constants {
    public static final String WALLET_NAME = "users_wallet";
    public static final String WALLET_NAME2 = "test2_wallet";

    public static boolean IS_PRODUCTION = false;

    public static final NetworkParameters NETWORK_PARAMETERS = IS_PRODUCTION ? MainNetParams.get() : TestNet3Params.get();

    public static String segwit = Metadata.P2WPKH; // 或者 P2WPKH
}
