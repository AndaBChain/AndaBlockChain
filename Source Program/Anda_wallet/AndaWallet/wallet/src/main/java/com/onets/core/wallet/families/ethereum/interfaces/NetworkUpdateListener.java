package com.onets.core.wallet.families.ethereum.interfaces;


import okhttp3.Response;

public interface NetworkUpdateListener {

    public void onUpdate(Response s);
}
