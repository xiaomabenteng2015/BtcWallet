package com.edu.bgewallet.bitcoinjtest2;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.edu.bgewallet.Constants;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.subgraph.orchid.encoders.Hex;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.consenlabs.tokencore.foundation.crypto.Crypto;
import org.consenlabs.tokencore.foundation.utils.DateUtil;
import org.consenlabs.tokencore.foundation.utils.MnemonicUtil;
import org.consenlabs.tokencore.wallet.address.SegWitBitcoinAddressCreator;
import org.consenlabs.tokencore.wallet.keystore.HDMnemonicKeystore;
import org.consenlabs.tokencore.wallet.model.BIP44Util;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.spongycastle.crypto.params.KeyParameter;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * BitcoinJ 测试二
 */
public class BitcoinJHelper {
    /**
     * 密码
     */
    public static final String passphrase = "";

    /**
     * 通过Wallet 获取 助记词
     *
     * @param wallet
     * @return
     */
    public static List<String> getSeedWordsFromWallet(Wallet wallet) {
        DeterministicSeed seed = wallet.getKeyChainSeed();
        return seed.getMnemonicCode();
    }

    /**
     * 通过私钥获取ECKey
     *
     * @param priKey
     * @return
     */
    public static ECKey getECKeyFromPriKey(String priKey) {
        ECKey ecKey = ECKey.fromPrivate(Numeric.toBigInt(priKey));
        return ecKey;
    }

    public static String getPubKeyFrom(ECKey ecKey) {
        NetworkParameters params = getParams();
        return ecKey.toAddress(params).toBase58().toString();
    }


    //通过seed 获取钱包
    public static Wallet getFromSpeed(String seedCode) {
        NetworkParameters params = getParams();
        DeterministicSeed seed;
        try {
            seed = new DeterministicSeed(seedCode, null, passphrase, Utils.currentTimeSeconds());

            Wallet restoredWallet = Wallet.fromSeed(params, seed);
            return restoredWallet;
        } catch (UnreadableWalletException e) {
            e.printStackTrace();
        }
        return null;
    }


    //通过本地文件获取Wallet
    public static Wallet getWalletFromFile(String filePath) {
        try {
            return Wallet.loadFromFile(new File(filePath));
        } catch (UnreadableWalletException e) {
            e.printStackTrace();
        }
        return null;
    }


    //发送交易
    public static void send(Wallet wallet, String recipientAddress, String amount) {
        NetworkParameters params = getParams();
        Address targetAddress = Address.fromBase58(params, recipientAddress);
        // Do the send of 1 BTC in the background. This could throw InsufficientMoneyException.
        SPVBlockStore blockStore = null;
        try {
            blockStore = new SPVBlockStore(params, getBLockFile());
        } catch (BlockStoreException e) {
            e.printStackTrace();
        }
        BlockChain chain = null;
        try {
            chain = new BlockChain(params, wallet, blockStore);
            PeerGroup peerGroup = new PeerGroup(params, chain);
            try {
                Wallet.SendResult result = wallet.sendCoins(peerGroup, targetAddress, Coin.parseCoin(amount));
                // Save the wallet to disk, optional if using auto saving (see below).
                //wallet.saveToFile(....);
                // Wait for the transaction to propagate across the P2P network, indicating acceptance.
                try {
                    Transaction transaction = result.broadcastComplete.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return;
            } catch (InsufficientMoneyException e) {
                e.printStackTrace();
            }
        } catch (BlockStoreException e) {
            e.printStackTrace();
        }

    }

    public static String send(WalletAppKit walletAppKit, String recipientAddress, String amount) {
        NetworkParameters params = getParams();
        String err = "";
        if (TextUtils.isEmpty(recipientAddress) || recipientAddress.equals("Scan recipient QR")) {
            err = "Select recipient";
            return err;
        }
        if (TextUtils.isEmpty(amount) | Double.parseDouble(amount) <= 0) {
            err = "Select valid amount";
            return err;

        }
        if (walletAppKit.wallet().getBalance().isLessThan(Coin.parseCoin(amount))) {
            err = "You got not enough coins";
            return err;
        }
        SendRequest request = SendRequest.to(Address.fromBase58(params, recipientAddress), Coin.parseCoin(amount));
        try {
            walletAppKit.wallet().completeTx(request);
            walletAppKit.wallet().commitTx(request.tx);
            walletAppKit.peerGroup().broadcastTransaction(request.tx).broadcast();
            return "";
        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    public static File getBLockFile() {
        File file = new File("/tmp/bitcoin-blocks");
        if (!file.exists()) {
            try {
                boolean newFile = file.createNewFile();
                if (newFile) {
                    return file;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static void getWallet() {
        NetworkParameters params = getParams();
        Wallet wallet = new Wallet(params);

        List<ECKey> keys = new ArrayList<>();
        ECKey ecKey = new ECKey();
        //加密eckey
        ecKey.encrypt(wallet.getKeyCrypter(), wallet.getKeyCrypter().deriveKey("123456"));
        keys.add(ecKey);


        wallet.importKeysAndEncrypt(keys, "123456");
        try {
            SPVBlockStore blockStore = new SPVBlockStore(params, getBLockFile());
            BlockChain chain = new BlockChain(params, wallet, blockStore);
            PeerGroup peerGroup = new PeerGroup(params, chain);
            peerGroup.addWallet(wallet);
            peerGroup.startAsync();
            peerGroup.downloadBlockChain();
            //startAndWait()
        } catch (BlockStoreException e) {
            e.printStackTrace();
        }
    }


    public static void testAddress(Wallet wallet) {
        Address a = wallet.currentReceiveAddress();
        ECKey b = wallet.currentReceiveKey();
        Address c = wallet.freshReceiveAddress();
    }


    public static void userSPeed(Wallet wallet) {

        NetworkParameters params = getParams();
        DeterministicSeed seed = wallet.getKeyChainSeed();
        Log.i("test123", "test123 Seed words are: " + Joiner.on(" ").join(seed.getMnemonicCode()));
        Log.i("test123", "test123 Seed birthday is: " + seed.getCreationTimeSeconds());


        //通过speed 获取Wallet
        String seedCode = "yard impulse luxury drive today throw farm pepper survey wreck glass federal";
        String seedCode2 = "liberty identify erase shuffle dignity armed produce mention actual you top vendor";
        long creationtime = 1409478661L;
        DeterministicSeed seed2;
        try {
            seed2 = new DeterministicSeed(seedCode, null, "", creationtime);
            Wallet restoredWallet = Wallet.fromSeed(params, seed2);
        } catch (UnreadableWalletException e) {
            e.printStackTrace();
        }

    }

    public static NetworkParameters getParams() {
        return Constants.IS_PRODUCTION ? MainNetParams.get() : TestNet3Params.get();
    }


    public static void t(Wallet wallet, String recipientAddress, String password, String mount) {
        Address a = Address.fromBase58(getParams(), recipientAddress);
        SendRequest req = SendRequest.to(a, Coin.parseCoin(mount));
        req.aesKey = wallet.getKeyCrypter().deriveKey(password);
        try {
            wallet.sendCoins(req);
        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
        }
    }

    public static void watchAddress() {
        Wallet toWatch = null;
        DeterministicKey watchingKey = toWatch.getWatchingKey();
        String s = watchingKey.serializePubB58(getParams());
        long creationTimeSeconds = watchingKey.getCreationTimeSeconds();


        DeterministicKey key = DeterministicKey.deserializeB58(null, "key data goes here", getParams());

        Wallet wallet = Wallet.fromWatchingKey(getParams(), key);


        NetworkParameters params = TestNet3Params.get();

        DeterministicSeed seed = new DeterministicSeed(new SecureRandom(), 128, "password", Utils.currentTimeSeconds());
        wallet = Wallet.fromSeed(params, seed);

        //tobytes
        byte[] bytes = MnemonicCode.toSeed(new ArrayList<>(), passphrase);

    }

    public static void test() {
        NetworkParameters params = TestNet3Params.get();
        DeterministicSeed seed = new DeterministicSeed(new SecureRandom(), 128, "123456", Utils.currentTimeSeconds());
        List<String> mnemonicCode = seed.getMnemonicCode();
        Log.i("test123", "test123 mnemonicCode" + mnemonicCode);
//       byte[] bytes = MnemonicCode.toSeed(mnemonicCode, "123456");
        Wallet wallet = Wallet.fromSeed(params, seed);
    }

    public static void test2(ECKey ceKey) {
        NetworkParameters params = getParams();
        String s = ceKey.toAddress(params).toBase58().toString();
        String privateKeyAsWiF = ceKey.getPrivateKeyAsWiF(params);// 私钥， WIF(Wallet Import Format)
        Log.i("test123", "test123 privateKeyAsWiF = " + privateKeyAsWiF + "==========" + s);
    }


    //通过私钥拿到eckey
    public static ECKey getECkey(String prikey) {
        ECKey key = DumpedPrivateKey.fromBase58(getParams(), prikey).getKey();
        return key;
    }


    //通过助记词导入新钱包
    public static Wallet createWallet(String mnemonicCode, String password) {
        KeyChainGroup kcg;
        DeterministicSeed deterministicSeed = null;
        try {
            deterministicSeed = new DeterministicSeed(mnemonicCode, null, passphrase, Utils.currentTimeSeconds());
        } catch (UnreadableWalletException e) {
            e.printStackTrace();
        }
        kcg = new KeyChainGroup(getParams(), deterministicSeed);
        Wallet wallet = new Wallet(getParams(), kcg);
        ImmutableList<ChildNumber> path = wallet.getWatchingKey().getPath();
        for (ChildNumber childNumber : path) {
            Log.i("test123", "test123 wallet path = " + childNumber.toString());
        }
        return wallet;
    }

    //创建新钱包。
    public static Wallet createWallet2() {
        KeyChainGroup kcg = new KeyChainGroup(getParams());
        Wallet wallet = new Wallet(getParams(), kcg);
        wallet.getParams().getId();
        return wallet;

    }

    //通过助记词
    public static WalletAppKit getWalletKit(Context context, String mnemonicCode) {
        WalletAppKit walletAppKit = new WalletAppKit(getParams(), context.getCacheDir(), Constants.WALLET_NAME2) {
            @Override
            protected void onSetupCompleted() {
                if (wallet().getImportedKeys().size() < 1) wallet().importKey(new ECKey());
                wallet().allowSpendingUnconfirmedTransactions();
                setupWalletListeners(wallet());
                ECKey ecKey = wallet().getImportedKeys().get(0);
                Log.i("test123", "test123 isCompressed = " + ecKey.isCompressed());
                Log.i("test123", "test123 " + getPubKeyFrom(ecKey));
//                test2(ecKey);
//                //打印助记词
                List<String> seedWordsFromWallet = getSeedWordsFromWallet(wallet());
                Log.i("test123", "test123 " + seedWordsFromWallet.toString());
                //当前地址
                String s1 = wallet().currentReceiveAddress().toBase58();

                // String s2 = wallet().currentChangeAddress().toBase58();
                String privateKeyAsWiF = wallet().currentReceiveKey().getPrivateKeyAsWiF(getParams());
                Log.i("test123", "test123 currentReceiveAddress = " + s1 + " ========= privateKeyAsWiF = " + privateKeyAsWiF);
//                Log.i("test123",  "freshReceiveAddress="+s+"==="+"currentReceiveAddress="+s1);
                Coin balance = wallet().getBalance();
                Log.i("test123", "test123 balance = " + balance.toFriendlyString());

                HDMnemonicKeystore(true,mnemonicCode);
            }
        };


        walletAppKit.setAutoSave(true);
        walletAppKit.setBlockingStartup(false);

        setDownListener(walletAppKit);
        if (getParams() == RegTestParams.get()) {
            walletAppKit.connectToLocalHost();
        }
        if (!TextUtils.isEmpty(mnemonicCode)) {
            try {
                DeterministicSeed seed = new DeterministicSeed(mnemonicCode, null, passphrase, Utils.currentTimeSeconds());
                walletAppKit.restoreWalletFromSeed(seed);
            } catch (UnreadableWalletException e) {
                e.printStackTrace();
            }

        }
        walletAppKit.startAsync();
        walletAppKit.awaitRunning();
        return walletAppKit;
    }

    //加载本地文件
    public static WalletAppKit getWalletKit(Context context) {
        return getWalletKit(context, "");
    }


    public static void setDownListener(WalletAppKit walletAppKit) {
        walletAppKit.setDownloadListener(new DownloadProgressTracker() {
            @Override
            protected void progress(double pct, int blocksSoFar, Date date) {
                super.progress(pct, blocksSoFar, date);
                int percentage = (int) pct;
                Log.i("test123", "test123 " + percentage + "% percentage");
            }

            @Override
            protected void doneDownload() {
                super.doneDownload();
                String myAddress = walletAppKit.wallet().currentReceiveAddress().toBase58();
                String s = walletAppKit.wallet().getBalance().toFriendlyString();

                Log.i("test123", "test123 doneDownload myAddress = " + myAddress + "== 余额" + s);
            }
        });
    }

    public static void setupWalletListeners(Wallet wallet) {
        wallet.addCoinsReceivedEventListener((wallet1, tx, prevBalance, newBalance) -> {
            String s = wallet.getBalance().toFriendlyString();
            String s1 = "";
            if (tx.getPurpose() == Transaction.Purpose.UNKNOWN) {
                s1 = newBalance.minus(prevBalance).toFriendlyString();
            }
            Log.i("test123", "test123" + s + "===" + s1);
        });
        wallet.addCoinsSentEventListener((wallet12, tx, prevBalance, newBalance) -> {
            String s = wallet.getBalance().toFriendlyString();
            String s1 = "Sent " + prevBalance.minus(newBalance).minus(tx.getFee()).toFriendlyString();
            Log.i("test123", "test123" + s + "===" + s1);
        });


    }

    public void Test() {

        ECKey k1 = new ECKey(); // some random key

        // encrypting a key
        KeyCrypter crypter1 = new KeyCrypterScrypt();

        KeyParameter aesKey1 = crypter1.deriveKey("some arbitrary passphrase");
        ECKey k2 = k1.encrypt(crypter1, aesKey1);
        //System.out.println(k2.isEncrypted()); // true

        // decrypting a key
        KeyCrypter crypter2 = k2.getKeyCrypter();
        KeyParameter aesKey2 = crypter2.deriveKey("some arbitrary passphrase");
        ECKey k3 = k2.decrypt(aesKey2);

        //System.out.println(k1.equals(k3));  // true
    }


    public static void closedWallet(WalletAppKit walletAppKit) {
        walletAppKit.stopAsync();
        walletAppKit.awaitTerminated();
    }

    public static void test2(WalletAppKit walletAppKit) {
        DeterministicKey deterministicKey = walletAppKit.wallet().getWatchingKey().dropPrivateBytes();
        deterministicKey = HDKeyDerivation.createMasterPubKeyFromBytes(deterministicKey.getPubKey(), deterministicKey.getChainCode());
        String xPublicKey = deterministicKey.serializePubB58(getParams());
        String privateKey = walletAppKit.wallet().getKeyByPath(DeterministicKeyChain.ACCOUNT_ZERO_PATH).getPrivateKeyAsWiF(getParams());
        Log.e("key", xPublicKey.toString());
        Log.e("privatekey", privateKey.toString());
        if (getParams() == RegTestParams.get()) {
            walletAppKit.connectToLocalHost();
        }
    }

    public static void si(String privateKey, String recipientAddress, String amount) {
        SendRequest request = SendRequest.to(Address.fromBase58(getParams(),
                recipientAddress), Coin.parseCoin(amount));
        Signingtrasaction(privateKey, request.tx.getHashAsString());
    }

    public static void Signingtrasaction(String wif, String msg) {
        try {
            // creating a key object from WiF
            DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(getParams(), wif);
            ECKey key = dpk.getKey();
            // checking our key object
            // NetworkParameters main = MainNetParams.get();
            String check = key.getPrivateKeyAsWiF(getParams());
            System.out.println(wif.equals(check));  // true
            Log.e("wif check", String.valueOf(wif.equals(check)));
            // creating Sha object from string
            Sha256Hash hash = Sha256Hash.wrap(msg);
            // creating signature
            ECKey.ECDSASignature sig = key.sign(hash);
            // encoding
            byte[] res = sig.encodeToDER();
            // converting to hex
            //String hex = DatatypeConverter.printHexBinary(res);
            // String hex = new String(res);
            String hex = android.util.Base64.encodeToString(res, 16);
            Log.e("sigendTransiction", hex.toString());
            Log.e("decrypttx", "" + Hex.decode(sig.encodeToDER()));
        } catch (Exception e) {   //signingkey = ecdsa.from_string(privateKey.decode('hex'), curve=ecdsa.SECP256k1)
            Log.e("signing exception", e.getMessage().toString());
        }
    }

    /**
     * 根据是否使用隔间见证 解析地址
     * @param isSegwit
     * @param mnemonic
     */
    public static void HDMnemonicKeystore(boolean isSegwit, String mnemonic) {
        String address = "";
        String xpub = "";

        List<String> mnemonics = Arrays.asList(mnemonic.split(" "));
        String path ="";
        if (isSegwit) {
            path =  Constants.IS_PRODUCTION ? BIP44Util.BITCOIN_SEGWIT_MAIN_PATH : BIP44Util.BITCOIN_SEGWIT_TESTNET_PATH;
        } else {
            path = Constants.IS_PRODUCTION  ? BIP44Util.BITCOIN_MAINNET_PATH : BIP44Util.BITCOIN_TESTNET_PATH;
        }

        MnemonicUtil.validateMnemonics(mnemonics);
        DeterministicSeed seed = new DeterministicSeed(mnemonics, null, "", 0L);
        DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();

        DeterministicKey parent = keyChain.getKeyByPath(BIP44Util.generatePath(path), true);
        NetworkParameters networkParameters = Constants.NETWORK_PARAMETERS;
        xpub = parent.serializePubB58(networkParameters);
        String xprv = parent.serializePrivB58(networkParameters);
        DeterministicKey mainAddressKey = keyChain.getKeyByPath(BIP44Util.generatePath(path + "/0/0"), true);
        if (isSegwit) {
            address = new SegWitBitcoinAddressCreator(networkParameters).fromPrivateKey(mainAddressKey.getPrivateKeyAsHex());
        } else {
            address = mainAddressKey.toAddress(networkParameters).toBase58();
        }

        Log.i("test123", "test123  HDMnemonicKeystore address = " + address);
    }
}