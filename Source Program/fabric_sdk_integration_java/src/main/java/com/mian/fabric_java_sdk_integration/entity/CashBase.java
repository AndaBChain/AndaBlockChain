package com.mian.fabric_java_sdk_integration.entity;

import lombok.Data;

import java.math.BigDecimal;
/**
* @Description: 测试用实体类
* @Param:
* @return:
* @Author: 继鹏
* @Date: 2019/12/13
*/
@Data
public class CashBase {
    private String ID;
    private String BagCode;
    private String BagType;
    private String BagDes;
    private BigDecimal CashMoney;
}
