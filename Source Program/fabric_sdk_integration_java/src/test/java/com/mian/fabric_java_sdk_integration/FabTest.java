package com.mian.fabric_java_sdk_integration;

import com.mian.fabric_java_sdk_integration.util.FabricUtils;
import org.junit.Test;
/**
* @Description: 集中测试类
* @Param:
* @return:
* @Author: 继鹏
* @Date: 2019/12/13
*/
public class FabTest {


    /**
    * @Description: 注册用户
    * @Param: []
    * @return: void
    * @Author: 继鹏
    * @Date: 2019/12/13
    */
    @Test
    public void enrollTest() throws Exception {
        FabricUtils fabricUtils = new FabricUtils(false, false);
        fabricUtils.enrollUsersSetup();
    }

    /**
    * @Description: 构造通道
    * @Param: []
    * @return: void
    * @Author: 继鹏
    * @Date: 2019/12/13
    */
    @Test
    public void constructChannelTest() throws Exception {
        FabricUtils fabricUtils = new FabricUtils(false, false);
        fabricUtils.constructChannel();
    }

    /**
    * @Description: 安装链码
    * @Param: []
    * @return: void
    * @Author: 继鹏
    * @Date: 2019/12/13
    */
    @Test
    public void installCheckinTest() throws Exception {
        FabricUtils fabricUtils = new FabricUtils();
        fabricUtils.installProposal();
    }

    /**
    * @Description: 初始化链码
    * @Param: []
    * @return: void
    * @Author: 继鹏
    * @Date: 2019/12/13
    */
    @Test
    public void instantiedCheckinProposalTest() throws Exception {

        String[] values = {"A","555","B","444","C","333","D","222"};
        FabricUtils fabricUtils = new FabricUtils();
        fabricUtils.instantiedProposal(values);
    }

    /**
    * @Description: 查询初始化状态
    * @Param: []
    * @return: void
    * @Author: 继鹏
    * @Date: 2019/12/13
    */
    @Test
    public void queryInstantiedCheckinProposalTest() throws Exception {
        FabricUtils fabricUtils = new FabricUtils();
        fabricUtils.queryInstantiateStatus();
    }

    /**
    * @Description: 查询
    * @Param: []
    * @return: void
    * @Author: 继鹏
    * @Date: 2019/12/13
    */
    @Test
    public void query() throws Exception {
        String[] values = {"张三"};
        FabricUtils fabricUtils = new FabricUtils();
        String query = fabricUtils.query(values);
        System.out.println("query = " + query);
    }



    /**
    * @Description: 调用链码//delete,add,move
    * @Param: []
    * @return: void
    * @Author: 继鹏
    * @Date: 2019/12/13
    */
    @Test
    public void invoke() throws Exception {
        String[] values = {"李四","张三","6"};
//        String[] values = {"老李","1","老王","2","老张","3"};
//        String[] values = new String[]{"张三"};
        FabricUtils fabricUtils = new FabricUtils();
        fabricUtils.invoke("move",values);
    }

    /**
    * @Description: 安装升级请求
    * @Param: []
    * @return: void
    * @Author: 继鹏
    * @Date: 2019/12/13
    */
    @Test
    public void installUpgradeTest() throws Exception {
        FabricUtils fabricUtils = new FabricUtils();
        fabricUtils.installProposal();
    }

    /**
    * @Description: 升级链码
    * @Param: []
    * @return: void
    * @Author: 继鹏
    * @Date: 2019/12/13
    */
    @Test
    public void upgrade() throws Exception {
        FabricUtils fabricUtils = new FabricUtils();
        String[] args = {"继鹏","666"};
        fabricUtils.upgradeRequest(args);
    }

    /**
    * @Description: 查询链上数据
    * @Param: []
    * @return: void
    * @Author: 继鹏
    * @Date: 2019/12/13
    */
    @Test
    public void queryBlockchainInfo() throws Exception {
        FabricUtils fabricUtils = new FabricUtils();
        fabricUtils.queryBlockchainInfo("bcb5370c2a58cfdc007c34a3d25366db440a177b8ae58d269c509fc480e21f55");
    }



}
