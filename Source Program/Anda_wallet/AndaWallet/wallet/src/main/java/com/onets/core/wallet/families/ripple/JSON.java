package com.onets.core.wallet.families.ripple;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * JSON工具类
 */
public class JSON {

    public static JSONObject parseJSON(String s) {
        try {
            return new JSONObject(s);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static String prettyJSON(JSONObject jsonObject) {
        try {
            return jsonObject.toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

//    private void test(){
//        Account account = client.accountFromSeed(blob.getString("master_seed"));
//        final TransactionManager tm = account.transactionManager();
//
//        final ManagedTxn tx = new ManagedTxn(TransactionType.OfferCreate, tm.transactionID++);
//        tx.put(Amount.Amount, "1");
//        tx.put(Field.TakerGets, Amount.fromString("1.0/BTC/rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"));
//        tx.put(Field.TakerPays, Amount.fromString("1.0/XRP/"));
//    }
}
