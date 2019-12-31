package com.aizone.blockchain.utils;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class ObjectToJson {

    /**
     * javabean to json
     * 
     * @param person
     * @return
     */
    public static String javabeanToJson(Object person) {
        Gson gson = new Gson();
        String json = gson.toJson(person.toString());
        return json;
    }

    /**
     * list to json
     * 
     * @param list
     * @return
     */
    public static String listToJson(List<Object> list) {

        Gson gson = new Gson();
        String json = gson.toJson(list.toArray());
        return json.toString();
    }

    /**
     * map to json
     * 
     * @param map
     * @return
     */
    public static String mapToJson(Map<String, Object> map) {

        Gson gson = new Gson();
        String json = gson.toJson(map.toString());
        return json.toString();
    }
}