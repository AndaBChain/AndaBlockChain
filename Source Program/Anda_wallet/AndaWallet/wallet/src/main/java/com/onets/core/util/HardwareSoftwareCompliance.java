package com.onets.core.util;

import org.bitcoinj.core.ECKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hardware Software Compliance(软件硬件合规)
 * @author Yu K.Q.
 */
public class HardwareSoftwareCompliance {

    private static final Logger log = LoggerFactory.getLogger(HardwareSoftwareCompliance.class);

    /**
     * 椭圆曲线加密兼容
     * return:true or false
     * Some devices have software or hardware bugs that causes the EC crypto to malfunction.
     * 有些设备有软件或硬件缺陷，导致EC密码故障
     * Will return false in case the device is NOT compliant.
     * 如果设备不兼容，将返回false
     */
    public static boolean isEllipticCurveCryptographyCompliant() {
        boolean isDeviceCompliant;
        try {
            new ECKey().getPubKey();
            isDeviceCompliant = true;
        } catch (Throwable e) {
            log.error("This device failed the EC compliance test", e);
            isDeviceCompliant = false;
        }
        return isDeviceCompliant;
    }
}
