package com.mian.fabric_java_sdk_integration.service;

import com.mian.fabric_java_sdk_integration.util.FabricUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
* @Description: service层
* @Param:
* @return:
* @Author: 继鹏
* @Date: 2019/12/13
*/
@Service
public class TestService {

    FabricUtils fabricUtils = new FabricUtils();

    public TestService() throws Exception {
    }


    public String query(String[] querys) throws Exception {
        return fabricUtils.query(querys);
    }


    //构造通道
    public boolean gouzao() throws Exception {
        //FabricUtils fabricUtils = new FabricUtils(false, false);
        try {
            FabricUtils fabricUtils = new FabricUtils(false, false);
            fabricUtils.constructChannel();
            System.out.println("*********构造的通道Service");
            return true;
        }catch (Exception e){

            e.printStackTrace();
            return false;
        }


    }


    //安装链码
    public boolean lianzhuanglianma()throws Exception {
        try {
            fabricUtils.installProposal();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }


    //初始化链码
    public boolean cshlianma(String[] values)throws Exception {
        try {
            fabricUtils.instantiedProposal(values);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    //初始化状态
    public String cshzt()throws Exception {
       return fabricUtils.queryInstantiateStatus();
    }


    //调用链码
    public boolean dylm(String functionName,String[] values) throws Exception {
        try {
            fabricUtils.invoke(functionName,values);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
