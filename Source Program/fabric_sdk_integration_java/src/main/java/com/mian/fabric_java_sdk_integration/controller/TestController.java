package com.mian.fabric_java_sdk_integration.controller;

import com.mian.fabric_java_sdk_integration.service.TestService;
import com.mian.fabric_java_sdk_integration.util.FabricUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.swing.*;
import java.util.ArrayList;

@Controller
public class TestController {
    @Autowired
    TestService testService;

    @RequestMapping("/")
    public String test(){
        return "test";
    }


    //查询
    @RequestMapping("/query")
    @ResponseBody
    public String query(String queryname) throws Exception {
        System.out.println("*********输入的参数是 = " + queryname);
        //转换
        String[] strs = new String[]{queryname};
        String m = testService.query(strs);
        System.out.println("*********查询出的金额是 = " + m);
        if (m == ""||m == null||m.isEmpty()||m.length() ==0){
            return "空";

        }

        return "账户名： " + queryname + "\r\n余额为： "+m;

    }


    //构造通道
    @RequestMapping("/construct")
    @ResponseBody
    public boolean constructChannelTest() throws Exception {
        System.out.println("*********构造的通道 = "+ testService.gouzao());
        return testService.gouzao();
    }


    //安装链码
    @RequestMapping("/installCheckin")
    @ResponseBody
    public boolean installCheckinTest() throws Exception {
        System.out.println("*********安装链码 = "+ testService.lianzhuanglianma());
        return testService.lianzhuanglianma();
    }


    //初始化链码
    @RequestMapping("/instantiedCheckin")
    @ResponseBody
    public boolean instantiedCheckinProposalTest(String name1,String money1,String name2,String money2) throws Exception {
        System.out.println("*********用户名01 = "+ name1);
        System.out.println("*********初始化金额 01= "+ money1);
        System.out.println("*********用户名02 = "+ name2);
        System.out.println("*********初始化金额 02= "+ money2);
        //转换
        String[] values = new String[]{name1,money1,name2,money2};

        return testService.cshlianma(values);

    }


    //查询初始化状态
    @RequestMapping("/cshzt")
    @ResponseBody
    public String queryInstantiedCheckinProposalTest() throws Exception {
        return testService.cshzt();
    }


    //转账
    @RequestMapping("/move")
    @ResponseBody
    public boolean move(String user1,String user2,String money) throws Exception {
        //转换
        String[] values = new String[]{user1,user2,money};
        return testService.dylm("move",values);
    }

    //删除账户
    @RequestMapping("/deluser")
    @ResponseBody
    public boolean deluser(String delusername) throws Exception {
        //转换
        String[] values = new String[]{delusername};
        return testService.dylm("delete",values);
    }

    //添加多个数据
    @RequestMapping("/multigroup")
    @ResponseBody
    public boolean multigroup(String Key01,String Value01,String Key02,String Value02,String Key03,String Value03) throws Exception {
        //转换
        String[] values = new String[]{Key01,Value01,Key02,Value02,Key03,Value03};
        return testService.dylm("add",values);
    }


}