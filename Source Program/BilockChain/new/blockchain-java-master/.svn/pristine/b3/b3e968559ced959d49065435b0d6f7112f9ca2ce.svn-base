package com.aizone.blockchain.bit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * JSON解析
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

        //原始json字符串转为JSONObject
        JSONObject jsonObject = new JSONObject(jsonString);
        //打印  安卓 的方法
//        Log.d("AssessJSON", "parseJSON: jsonString " + jsonObject);//

        //data转为JSONObject
        String data = jsonObject.getString("data");
        JSONObject jsonObjectData = new JSONObject(data);
//        Log.d("AssessJSON", "parseJSON: data " + jsonObjectData);

        //获取inputs
        JSONArray inputsArray = jsonObjectData.getJSONArray("inputs");
//        Log.d("AssessJSON", "parseJSON: inputArray " + inputsArray);
        //获取inputs的第一项
        JSONObject inputs1 = inputsArray.getJSONObject(0);
//        Log.d("AssessJSON", "parseJSON: inputs1 " + inputs1);

        JSONArray preAddressArray = inputs1.getJSONArray("prev_addresses");
//        Log.d("AssessJSON", "parseJSON: preAddressArray " + preAddressArray);
        for (int i = 0; i < preAddressArray.length(); i++) {
//            Log.d("AssessJSON", "parseJSON: i " + i);
            Coinaddress = preAddressArray.getString(i);
        }
//        Log.d("AssessJSON", "parseJSON: Coinaddress " + Coinaddress);

        return Coinaddress;
    }

}
