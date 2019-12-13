package com.onets.core.util;

import com.onets.core.exceptions.AddressMalformedException;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.families.andachain.AndaAddress;

import org.bitcoinj.script.Script;

import static com.onets.core.Preconditions.checkArgument;

/**
 * @author Yu K.Q.
 * 安达地址工具类
 */
public class AndaAddressUtils {
    /**
     * 检测是否P2SH地址
     * @param address
     * @return
     */
    public static boolean isP2SHAddress(AbstractAddress address) {
        checkArgument(address instanceof AndaAddress, "This address cannot be a P2SH address");
        return ((AndaAddress) address).isP2SHAddress();
    }

    /**
     * 获取HASH160
     * @param address
     * @return
     */
    public static byte[] getHash160(AbstractAddress address) {
        checkArgument(address instanceof AndaAddress, "Cannot get hash160 from this address");
        return ((AndaAddress) address).getHash160();
    }

    /**
     * 产品地址
     * @param script
     * @param address
     * @return
     */
    public static boolean producesAddress(Script script, AbstractAddress address) {
        try {
            return AndaAddress.from(address.getType(), script).equals(address);
        } catch (AddressMalformedException e) {
            return false;
        }
    }
}
