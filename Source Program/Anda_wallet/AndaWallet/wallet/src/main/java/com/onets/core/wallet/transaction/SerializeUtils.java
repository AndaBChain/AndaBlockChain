package com.onets.core.wallet.transaction;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * 序列化工具类
 */
public class SerializeUtils {

    /**
     * 反序列化
     * @return
     */
    public static Object unSerialize(byte[] bytes){
        Input input = new Input(bytes);
        Object obj = new Kryo().readClassAndObject(input);
        input.close();
        return obj;
    }

    /**
     * 序列化
     * @param object 原始公钥，使用toHex转换
     * @return
     */
    public static byte[] serialize(Object object){
        Output output = new Output(4096, -1);
        new Kryo().writeClassAndObject(output, object);
        byte[] bytes = output.toBytes();
        output.close();
        return bytes;
    }
}
