package com.onets.wallet.util;

import android.os.StrictMode;
import android.util.Log;

import com.onets.wallet.data.BitTranBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON解析
 * 比特币解析交易json字符串，放在服务器中
 */
public class AssessJSON {

    /**
     * 解析比特币交易数据，获取交易的第一个源地址
     * @param jsonString
     * @return
     * @throws JSONException
     */
    public String parseJSON(String jsonString) throws JSONException {
        String Coinaddress = "";
        BitTranBean bitTranBean = new BitTranBean();
        BitTranBean.DataBean dataBean = new BitTranBean.DataBean();
        BitTranBean.DataBean.InputsBean inputsBean = new BitTranBean.DataBean.InputsBean();
        List<BitTranBean.DataBean.InputsBean> inputsBeanList = new ArrayList<>();

        //原始json字符串转为JSONObject
        JSONObject jsonObject = new JSONObject(jsonString);
        Log.d("AssessJSON", "parseJSON: jsonString " + jsonObject);

        //data转为JSONObject
        String data = jsonObject.getString("data");
        JSONObject jsonObjectData = new JSONObject(data);
        Log.d("AssessJSON", "parseJSON: data " + jsonObjectData);

        //获取inputs
        JSONArray inputsArray = jsonObjectData.getJSONArray("inputs");
        Log.d("AssessJSON", "parseJSON: inputArray " + inputsArray);
        //获取inputs的第一项
        JSONObject inputs1 = inputsArray.getJSONObject(0);
        Log.d("AssessJSON", "parseJSON: inputs1 " + inputs1);

        JSONArray preAddressArray = inputs1.getJSONArray("prev_addresses");
        Log.d("AssessJSON", "parseJSON: preAddressArray " + preAddressArray);
        for (int i = 0; i < preAddressArray.length(); i++) {
            Log.d("AssessJSON", "parseJSON: i " + i);
            Coinaddress = preAddressArray.getString(i);
        }
        Log.d("AssessJSON", "parseJSON: Coinaddress " + Coinaddress);

        return Coinaddress;
    }

}
