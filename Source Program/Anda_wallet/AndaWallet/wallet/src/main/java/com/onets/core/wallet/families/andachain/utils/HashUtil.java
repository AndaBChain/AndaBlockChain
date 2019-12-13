/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package com.onets.core.wallet.families.andachain.utils;

import com.onets.core.wallet.families.andachain.sm.SM3Digest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.copyOfRange;
import static org.spongycastle.util.encoders.Hex.encode;

public class HashUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HashUtil.class);

    public static byte[] sm3_Hash(byte[] input) {
        return sm3_Hash(input, 0, input.length);
    }

    public static byte[] sm3_Hash(byte[] input1, byte[] input2) {

        SM3Digest sm3 = new SM3Digest();
        sm3.update(input1, 0, input1.length);
        sm3.update(input2, 0, input1.length);

        byte[] md = new byte[sm3.getDigestSize()];

        sm3.doFinal(md, 0);

        return md;
    }


    public static byte[] sm3_Hash(byte[] input, int start, int length) {


        SM3Digest sm3 = new SM3Digest();
        sm3.update(input, start, length);

        byte[] md = new byte[sm3.getDigestSize()];

        sm3.doFinal(md, 0);

        return md;

    }


    /**
     * Calculates RIGTMOST160(SHA3(input)). This is used in address
     * calculations. *
     *
     * @param input - data
     * @return - 20 right bytes of the hash keccak of the data
     */
    public static byte[] sha3omit12(byte[] input) {
        byte[] hash = sm3_Hash(input, 0, input.length);
        return copyOfRange(hash, 12, hash.length);
    }

    public static String toHexString(
            byte[] data) {
        return toHexString(data, 0, data.length);
    }

    public static String toHexString(byte[] data, int off, int length) {
        byte[] encoded = encode(data, off, length);
        return fromByteArray(encoded);
    }

    public static String fromByteArray(byte[] bytes) {
        return new String(asCharArray(bytes));
    }

    public static char[] asCharArray(byte[] bytes) {
        char[] chars = new char[bytes.length];

        for (int i = 0; i != chars.length; i++) {
            chars[i] = (char) (bytes[i] & 0xff);
        }

        return chars;
    }
}
