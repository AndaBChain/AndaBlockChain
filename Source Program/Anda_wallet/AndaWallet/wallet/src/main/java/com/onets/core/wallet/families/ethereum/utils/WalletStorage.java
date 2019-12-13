package com.onets.core.wallet.families.ethereum.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.onets.core.wallet.families.ethereum.data.FullWallet;
import com.onets.core.wallet.families.ethereum.data.WatchWallet;
import com.onets.core.wallet.families.ethereum.interfaces.StorableWallet;
import com.onets.wallet.BuildConfig;

import org.json.JSONException;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Map;

/**
 * 以太坊钱包存储
 */
public class WalletStorage {

    private ArrayList<StorableWallet> mapdb;
    private static WalletStorage instance;
    private String walletToExport; // Used as temp if users wants to export but still needs to grant write permission

    /**
     * 获取实例
     * @param context
     * @return
     */
    public static WalletStorage getInstance(Context context) {
        if (instance == null)
            instance = new WalletStorage(context);
        return instance;
    }


    private WalletStorage(Context context) {
        try {
            load(context);
        } catch (Exception e) {
            e.printStackTrace();
            mapdb = new ArrayList<StorableWallet>();
        }
        if (mapdb.size() == 0) // Try to find local wallets
            checkForWallets(context);
    }

    /**
     * 添加本地存储的钱包地址
     * @param addresse
     * @param context
     * @return
     */
    public synchronized boolean add(StorableWallet addresse, Context context) {
        for (int i = 0; i < mapdb.size(); i++)
            if (mapdb.get(i).getPubKey().equalsIgnoreCase(addresse.getPubKey())) return false;
        mapdb.add(addresse);
        save(context);
        return true;
    }

    /**
     * 获取存储的以太坊钱包列表
     * @return
     */
    public synchronized ArrayList<StorableWallet> get() {
        return mapdb;
    }

    /**
     * 只获得完整钱包
     * @return
     */
    public synchronized ArrayList<String> getFullOnly() {
        ArrayList<String> erg = new ArrayList<String>();
        if (mapdb.size() == 0) return erg;
        for (int i = 0; i < mapdb.size(); i++) {
            StorableWallet cur = mapdb.get(i);
            if (cur instanceof FullWallet)
                erg.add(cur.getPubKey());
        }
        return erg;
    }

    /**
     * 判断是否是完整钱包
     * @param addr
     * @return
     */
    public synchronized boolean isFullWallet(String addr) {
        if (mapdb.size() == 0) return false;
        for (int i = 0; i < mapdb.size(); i++) {
            StorableWallet cur = mapdb.get(i);
            if (cur instanceof FullWallet && cur.getPubKey().equalsIgnoreCase(addr))
                return true;
        }
        return false;
    }

    /**
     * 删除钱包
     * @param address
     * @param context
     */
    public void removeWallet(String address, Context context) {
        int position = -1;
        for (int i = 0; i < mapdb.size(); i++) {
            if (mapdb.get(i).getPubKey().equalsIgnoreCase(address)) {
                position = i;
                break;
            }
        }
        if (position >= 0) {
            if (mapdb.get(position) instanceof FullWallet) // IF full wallet delete private key too
                new File(context.getFilesDir(), address.substring(2, address.length())).delete();
            mapdb.remove(position);
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(address);
        editor.apply();
        save(context);
    }

    /**
     * 检查钱包
     * @param c
     */
    public void checkForWallets(Context c) {
        // Full wallets
        File[] wallets = c.getFilesDir().listFiles();
        if (wallets == null) {
            return;
        }
        for (int i = 0; i < wallets.length; i++) {
            if (wallets[i].isFile()) {
                if (wallets[i].getName().length() == 40) {
                    add(new FullWallet("0x" + wallets[i].getName(), wallets[i].getName()), c);
                }
            }
        }

        // Watch only
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        Map<String, ?> allEntries = preferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().length() == 42 && !mapdb.contains(entry.getKey()))
                add(new WatchWallet(entry.getKey()), c);
        }
        if (mapdb.size() > 0)
            save(c);
    }

    public void setWalletForExport(String wallet) {
        walletToExport = wallet;
    }

    public boolean exportWallet(Activity c) {
        return exportWallet(c, false);
    }

    public void importWallets(Context c, ArrayList<File> toImport) throws Exception {
        for (int i = 0; i < toImport.size(); i++) {

            String address = stripWalletName(toImport.get(i).getName());
            if (address.length() == 40) {
                copyFile(toImport.get(i), new File(c.getFilesDir(), address));
                if(! BuildConfig.DEBUG)
                    toImport.get(i).delete();
                WalletStorage.getInstance(c).add(new FullWallet("0x" + address, address), c);
                AddressNameConverter.getInstance(c).put("0x" + address, "Wallet " + ("0x" + address).substring(0, 6), c);

                Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri fileContentUri = Uri.fromFile(toImport.get(i)); // With 'permFile' being the File object
                mediaScannerIntent.setData(fileContentUri);
                c.sendBroadcast(mediaScannerIntent); // With 'this' being the context, e.g. the activity

            }
        }
    }

    public static String stripWalletName(String s) {
        if (s.lastIndexOf("--") > 0)
            s = s.substring(s.lastIndexOf("--") + 2);
        if (s.endsWith(".json"))
            s = s.substring(0, s.indexOf(".json"));
        return s;
    }

    private boolean exportWallet(Activity c, boolean already) {
        if (walletToExport == null) return false;
        if (walletToExport.startsWith("0x"))
            walletToExport = walletToExport.substring(2);

        if (ExternalStorageHandler.hasPermission(c)) {
            File folder = new File(Environment.getExternalStorageDirectory(), "Lunary");
            if (!folder.exists()) folder.mkdirs();

            File storeFile = new File(folder, walletToExport + ".json");
            try {
                copyFile(new File(c.getFilesDir(), walletToExport), storeFile);
            } catch (IOException e) {
                return false;
            }

            // fix, otherwise won't show up via USB
            Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri fileContentUri = Uri.fromFile(storeFile); // With 'permFile' being the File object
            mediaScannerIntent.setData(fileContentUri);
            c.sendBroadcast(mediaScannerIntent); // With 'this' being the context, e.g. the activity
            return true;
        } else if (!already) {
            ExternalStorageHandler.askForPermission(c);
            return exportWallet(c, true);
        } else {
            return false;
        }
    }


    private void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    public Credentials getFullWallet(Context context, String password, String wallet) throws IOException, JSONException, CipherException {
        if (wallet.startsWith("0x"))
            wallet = wallet.substring(2, wallet.length());
        return WalletUtils.loadCredentials(password, new File(context.getFilesDir(), wallet));
    }


    public synchronized void save(Context context) {
        FileOutputStream fout;
        try {
            fout = new FileOutputStream(new File(context.getFilesDir(), "wallets.dat"));
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(mapdb);
            oos.close();
            fout.close();
        } catch (Exception e) {
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void load(Context context) throws IOException, ClassNotFoundException {

        File file = new File(context.getFilesDir().getAbsolutePath(), "wallets.dat");
        File fileParent = file.getParentFile();
        if (!fileParent.exists()){
            fileParent.mkdirs();
        }
        file.createNewFile();

        Log.d("WalletStorage", "max memory = " + Runtime.getRuntime().maxMemory());
        Log.d("WalletStorage", "free memory = " + Runtime.getRuntime().freeMemory());
        Log.d("WalletStorage", "total memory = " + Runtime.getRuntime().totalMemory());

        FileInputStream fout = new FileInputStream(file);
        Log.d("WalletStorage", "load: " + file.length());
        ObjectInputStream oos = new ObjectInputStream(new BufferedInputStream(fout));
        mapdb = (ArrayList<StorableWallet>) oos.readObject();
        oos.close();
        fout.close();
    }

}