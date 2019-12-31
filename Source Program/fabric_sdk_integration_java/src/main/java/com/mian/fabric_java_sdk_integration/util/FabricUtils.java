package com.mian.fabric_java_sdk_integration.util;

import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.ChaincodeResponse;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionInfo;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.UpgradeProposalRequest;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;


import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.mian.fabric_java_sdk_integration.util.Print.assertEquals;
import static com.mian.fabric_java_sdk_integration.util.Print.fail;
import static com.mian.fabric_java_sdk_integration.util.Print.out;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hyperledger.fabric.sdk.Channel.PeerOptions.createPeerOptions;
/**
* @Description: 最重要的方法都在这里
* @Param:
* @return:
* @Author: 继鹏
* @Date: 2019/12/13
*/
public class FabricUtils {
    private static final String CHAIN_CODE_NAME = "TwoChaincode_java";
    private static final String CHAIN_CODE_PATH = null;
    private static final String CHAIN_CODE_VERSION = "1.0.1";
    private static final String CHAIN_CODE_FILEPATH = "sdkintegration/javacc/sample1";
    private static final TransactionRequest.Type CHAIN_CODE_LANG = TransactionRequest.Type.JAVA;



    private static final String TEST_ADMIN_NAME = "admin";

    // 二次封装好难啊

    /**
     * 分割线
     */
    private final HFClient client;
    private final Channel channel;
    private final SampleOrg sampleOrg;
    private final ChaincodeID chaincodeID;
    private final SampleStore sampleStore;

    private static final String ORG_NAME = FabConfig.getOrgName();
    private static final String USERNAME = FabConfig.getUserName();
    private static final String SECRET = FabConfig.getSecret();
    private static final String channelName = FabConfig.getChannelName();

    private static final FabConfig FAB_CONFIG = FabConfig.getConfig();

    private static final String TEST_FIXTURES_PATH = FabConfig.getTEST_FIXTURES_PATH();
    private static final File chaincodeMetaInfLocation = new File(TEST_FIXTURES_PATH+"/meta-infs/end2endit");
    private static final File chaincodeSourceLocation = Paths.get(TEST_FIXTURES_PATH, CHAIN_CODE_FILEPATH).toFile();
    private static final File chaincodeendorsementpolicy = new File(TEST_FIXTURES_PATH + "/sdkintegration/chaincodeendorsementpolicy.yaml");
    private static final File channelConfigurationFile = new File(TEST_FIXTURES_PATH + "/sdkintegration/e2e-2Orgs/" + FAB_CONFIG.getFabricConfigGenVers() + "/" + FabConfig.getChannelName() + ".tx");
    private static final File chaincodeEndorsementPolicyYaml = new File(TEST_FIXTURES_PATH + "/sdkintegration/chaincodeendorsementpolicy.yaml");

    public FabricUtils() throws Exception {
        this(true, true);
    }
    public FabricUtils(boolean enrollUser, boolean setupChannel) throws Exception {
        sampleStore = new SampleStore();
        sampleOrg = FAB_CONFIG.getIntegrationTestsSampleOrg(ORG_NAME);
        sampleOrg.setPeerAdmin(setupPeerAdmin(sampleOrg));

        if (enrollUser) {

            SampleUser sampleUser = enroll(USERNAME, SECRET, sampleOrg.getMSPID(), sampleOrg.getName(), sampleOrg.getCAName(), sampleOrg.getCALocation(), sampleOrg.getCAProperties());

            sampleOrg.addUser(sampleUser);

        }

        client = HFClient.createNewInstance();
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        client.setUserContext(sampleOrg.getPeerAdmin());


        if (setupChannel) {
            channel = client.newChannel(channelName);
            Collection<Orderer> orderers = new LinkedList<>();
            Set<String> ordererNames = sampleOrg.getOrdererNames();
            Map<String, String> orderNameAndLocations = sampleOrg.getOrderNameAndLocations();
            Collection<Peer> peers = new LinkedList<>();
            Set<String> peerNames = sampleOrg.getPeerNames();
            Map<String, String> peerNameAndLocations = sampleOrg.getPeerNameAndLocations();
            // orderers
            for (String ordererName : ordererNames) {
                Properties ordererProperties = FAB_CONFIG.getOrdererProperties(ordererName);
                //example of setting keepAlive to avoid timeouts on inactive http2 connections.
                // Under 5 minutes would require changes to server side to accept faster ping rates.
                ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit.MINUTES});
                ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{8L, TimeUnit.SECONDS});
                ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveWithoutCalls", new Object[]{true});
                orderers.add(client.newOrderer(ordererName, orderNameAndLocations.get(ordererName), ordererProperties));
            }
            for (Orderer orderer: orderers) {
                channel.addOrderer(orderer);
            }
            for (String peerName : peerNames) {
                String peerLocation = peerNameAndLocations.get(peerName);
                Properties peerProperties = FAB_CONFIG.getPeerProperties(peerName); //test properties for peer.. if any.
                if (peerProperties == null) {
                    peerProperties = new Properties();
                }
                //Example of setting specific options on grpc's NettyChannelBuilder
                peerProperties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);
                // 新节点
                Peer peer = client.newPeer(peerName, peerLocation, peerProperties);
                peers.add(peer);
            }
            for (Peer peer : peers) {
                // 给通道加入节点
                channel.addPeer(peer,
                        createPeerOptions()
                                .setPeerRoles(EnumSet.of(Peer.PeerRole.ENDORSING_PEER, Peer.PeerRole.LEDGER_QUERY, Peer.PeerRole.CHAINCODE_QUERY, Peer.PeerRole.EVENT_SOURCE)));
            }
            channel.initialize();
        } else {
            channel = null;
        }
        chaincodeID = getChaincodeID();
    }

//    注册用户
    public void enrollUsersSetup() throws Exception {
        // 创建通道 加入节点 测试链码
        ////////////////////////////
        //Set up USERS

        //SampleUser can be any implementation that implements org.hyperledger.fabric.sdk.User Interface
        // 不止是SampleUser， 这里可以是任何实现了User接口的类

        final SampleStore sampleStore = new SampleStore();
        final HFCAClient ca = sampleOrg.getCAClient();
        final String orgName = sampleOrg.getName();
        // 成员关系服务提供者
        final String mspid = sampleOrg.getMSPID();
        ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

        if (FAB_CONFIG.isRunningFabricTLS()) {
            //这显示了如何从Fabric CA获取客户端TLS证书
            //我们将为订购者对等使用一个客户端TLS证书。
            final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
            enrollmentRequestTLS.addHost("localhost");
            enrollmentRequestTLS.setProfile("tls");
            final Enrollment enroll = ca.enroll("admin", FabConfig.getAdminPass(), enrollmentRequestTLS);
            final String tlsCertPEM = enroll.getCert();
            final String tlsKeyPEM = FabConfig.getPEMStringFromPrivateKey(enroll.getKey());

            //保存在样本存储中以进行测试。
            sampleStore.storeClientPEMTLCertificate(sampleOrg, tlsCertPEM);
            sampleStore.storeClientPEMTLSKey(sampleOrg, tlsKeyPEM);
        }

        // 预注册的admin （只需要用Fabric caClient登记）
        SampleUser admin = sampleStore.getMember(TEST_ADMIN_NAME, orgName);
        if (!admin.isEnrolled()) {  //预先注册的管理员只需要使用Fabric caClient进行注册。
            admin.setEnrollment(ca.enroll(admin.getName(), FabConfig.getAdminPass()));
            admin.setMspId(mspid);
        }
        // 注册用户
        SampleUser user = sampleStore.getMember(USERNAME, orgName);
        if (!user.isRegistered()) {  // 用户需要注册
            RegistrationRequest rr = new RegistrationRequest(user.getName(), "org1.department1");
            user.setEnrollmentSecret(ca.register(rr, admin));
        }
        System.out.println("\n\n\n\n密码 " + user.getEnrollmentSecret());
        // 登记用户
        if (!user.isEnrolled()) {
            user.setEnrollment(ca.enroll(user.getName(), user.getEnrollmentSecret()));
            user.setMspId(mspid);
        }

        sampleOrg.addUser(user);
        sampleOrg.setAdmin(admin); // 该组织的管理员 --

    }


//构建通道
    public void constructChannel() throws Exception {

        out("构造通道 %s", channelName);

        // 唯一的对等管理员组织
        // eroll的用户

        Collection<Orderer> orderers = new LinkedList<>();

        // orderers
        for (String orderName : sampleOrg.getOrdererNames()) {

            Properties ordererProperties = FAB_CONFIG.getOrdererProperties(orderName);

            //设置keepAlive以避免在无效的http2连接上超时的示例。
            //在5分钟内，需要更改服务器端以接受更快的ping速率。
            ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[] {5L, TimeUnit.MINUTES});
            ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[] {8L, TimeUnit.SECONDS});
            ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveWithoutCalls", new Object[] {true});

            orderers.add(client.newOrderer(orderName, sampleOrg.getOrdererLocation(orderName),
                    ordererProperties));
        }

        //只需选择列表中的第一个订购者即可创建频道。
        // 一个orderer节点 7050
        Orderer anOrderer = orderers.iterator().next();
        orderers.remove(anOrderer);


        ChannelConfiguration channelConfiguration = new ChannelConfiguration(channelConfigurationFile);

        //创建只有一个签名者的频道，该签名者是此组织的对等管理员。如果频道创建策略需要更多签名，则也需要添加它们。
        // 只有orgs peer admin才可以创建通道
        SampleUser peerAdmin = sampleOrg.getPeerAdmin();
        Channel newChannel = client.newChannel(channelName, anOrderer, channelConfiguration, client.getChannelConfigurationSignature(channelConfiguration, peerAdmin));

        out("创建通道： %s", channelName);

        boolean everyother = true; //在进行对等事件时测试两种情况。
        for (String peerName : sampleOrg.getPeerNames()) {
            String peerLocation = sampleOrg.getPeerLocation(peerName);

            Properties peerProperties = FAB_CONFIG.getPeerProperties(peerName); //test properties for peer.. if any.
            if (peerProperties == null) {
                peerProperties = new Properties();
            }

            //在grpc的NettyChannelBuilder上设置特定选项的示例
            peerProperties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);

            // 新节点
            Peer peer = client.newPeer(peerName, peerLocation, peerProperties);
            // 给通道加入节点
            newChannel.joinPeer(peer, createPeerOptions().setPeerRoles(EnumSet.of(Peer.PeerRole.ENDORSING_PEER, Peer.PeerRole.LEDGER_QUERY, Peer.PeerRole.CHAINCODE_QUERY, Peer.PeerRole.EVENT_SOURCE))); //Default is all roles.

            out("节点 %s 加入 通道 %s", peerName, channelName);
            everyother = !everyother;
        }

        // 将剩余命令加入通道
        for (Orderer orderer : orderers) { //添加剩余的订购者（如果有）。
            newChannel.addOrderer(orderer);
        }

        out("完成构造通道 %s", channelName);
    }

//    安装链码
    public void installProposal() {
        try {
            out("安装 %s", channelName);

            Collection<ProposalResponse> responses;
            Collection<ProposalResponse> successful = new LinkedList<>();
            Collection<ProposalResponse> failed = new LinkedList<>();


            ////////////////////////////
            // 安装提案请求
            //
            client.setUserContext(sampleOrg.getPeerAdmin());

            out("**********创建安装建议");

            InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
            installProposalRequest.setChaincodeID(chaincodeID);

            // 在foo链上，从目录安装。

            ////对于GO语言并仅服务于单个用户，chaincodeSource很可能是用户GOPATH
            // 链码地址
            installProposalRequest.setChaincodeSourceLocation(chaincodeSourceLocation);
            installProposalRequest.setChaincodeMetaInfLocation(chaincodeMetaInfLocation);
            installProposalRequest.setChaincodeVersion(CHAIN_CODE_VERSION);
            installProposalRequest.setChaincodeLanguage(CHAIN_CODE_LANG);


            out("发送安装建议");

            ////////////////////////////
            // 客户端只能向自己组织的节点发送安装请求
            // 只有与对等方来自同一组织的客户端才能发出安装请求
            int numInstallProposal = 0;
            //    Set<String> orgs = orgPeers.keySet();
            //   for (SampleOrg org : testSampleOrgs) {

            Collection<Peer> peers = channel.getPeers();
            numInstallProposal = numInstallProposal + peers.size();
            /**
             *
             * 发送安装提案
             *
             */
            responses = client.sendInstallProposal(installProposalRequest, peers);

            FabConfig.resultVerify(responses, successful, failed, numInstallProposal);
        } catch (Exception e) {
            out("抓到异常运行通道 %s", channel.getName());
            e.printStackTrace();
            fail("测试失败，出现错误 : " + e.getMessage());
        }
    }

    // 实例化chaincode。
    public void instantiedProposal(String[] args) {
        try {
            out("运行通道 %s", channelName);

            Collection<ProposalResponse> responses;
            Collection<ProposalResponse> successful = new LinkedList<>();
            Collection<ProposalResponse> failed = new LinkedList<>();

            ///////////////
            //
            InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
            instantiateProposalRequest.setProposalWaitTime(FAB_CONFIG.getDeployWaitTime());
            instantiateProposalRequest.setChaincodeID(chaincodeID);
            instantiateProposalRequest.setChaincodeLanguage(CHAIN_CODE_LANG);
            instantiateProposalRequest.setFcn("init");
            instantiateProposalRequest.setArgs(args);
            Map<String, byte[]> tm = new HashMap<>();
            tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
            tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
            instantiateProposalRequest.setTransientMap(tm);


            ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
            chaincodeEndorsementPolicy.fromYamlFile(chaincodeendorsementpolicy);
            instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

            successful.clear();
            failed.clear();

            responses = channel.sendInstantiationProposal(instantiateProposalRequest, channel.getPeers());
//            responses = channel.sendInstantiationProposal(instantiateProposalRequest);
            FabConfig.resultVerify(responses, successful, failed, 0);
            ///////////////

            channel.sendTransaction(successful, client.getUserContext());
        } catch (Exception e) {
            out("抓到异常运行通道 %s", channel.getName());
            e.printStackTrace();
            fail("测试失败，出现错误: " + e.getMessage());
        }
    }


//  调用链码
    public CompletableFuture<BlockEvent.TransactionEvent> invoke(String functionName, String[] args) throws Exception {
        final SampleUser user = sampleOrg.getPeerAdmin();
        final Collection<ProposalResponse> successful = new LinkedList<>();
        final Collection<ProposalResponse> failed = new LinkedList<>();

        ///////////////
        /// 向所有对等方发送事务建议
        final TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        transactionProposalRequest.setFcn(functionName);
        transactionProposalRequest.setArgs(args);
        transactionProposalRequest.setProposalWaitTime(FAB_CONFIG.getProposalWaitTime());
        if (user != null) { // 特定用户使用
            transactionProposalRequest.setUserContext(user);
        }
        out("向所有具有参数的对等方发送事务建议");

        final Collection<ProposalResponse> invokePropResp = channel.sendTransactionProposal(transactionProposalRequest);
        for (ProposalResponse response : invokePropResp) {

            if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                out("成功的交易建议响应: %s 从 peer %s", response.getTransactionID(), response.getPeer().getName());
                successful.add(response);
            } else {
                failed.add(response);
            }
        }

        out("收到 %d 交易建议回复。成功+验证: %d . 失败: %d",
                invokePropResp.size(), successful.size(), failed.size());
        if (failed.size() > 0) {
            ProposalResponse firstTransactionProposalResponse = failed.iterator().next();

            throw new ProposalException(format("没有足够的背书人( %s):%d 背书人 错误:%s. 已验证:%b",
                    "0", firstTransactionProposalResponse.getStatus().getStatus(), firstTransactionProposalResponse.getMessage(), firstTransactionProposalResponse.isVerified()));

        }
        out("已成功收到交易建议响应.");

        ////////////////////////////
        // 将事务发送给订购方
        out("将链码事务发送到 orderer.");
        if (user != null) {
            return channel.sendTransaction(successful, user);
        }
        return channel.sendTransaction(successful);
    }

//  查询初始化状态
    public String queryInstantiateStatus() throws Exception {
        try {
            Peer peer = channel.getPeers().iterator().next();
            out("检查实例化的66链码： %s, 版本： %s, peer节点: %s", CHAIN_CODE_NAME, CHAIN_CODE_VERSION, peer.getName());
            List<Query.ChaincodeInfo> ccinfoList = channel.queryInstantiatedChaincodes(peer);
            checkChaincodeStatus(ccinfoList);
            //需要遍历list
            String ccname = null;
            String ccversion = null;
            for(Query.ChaincodeInfo info:ccinfoList){
                ccname=info.getName();
                ccversion = info.getVersion();
            }
            if (ccname==null||ccversion==null){
                return "没找到";
            }
            return "从" + peer.getName()+"节点返回数据，\r\n链码名称："+ccname + "\r\n链码版本：" + ccversion +"\r\n可以跳过\"初始化链码\"的步骤" ;
        }catch (Exception e) {
            e.printStackTrace();
            return "没找到";
        }
    }

//  查询
    public String query(String[] args) throws Exception {
        QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
        queryByChaincodeRequest.setArgs(args);
        queryByChaincodeRequest.setFcn("query");
        queryByChaincodeRequest.setChaincodeID(chaincodeID);

        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
        queryByChaincodeRequest.setTransientMap(tm2);

        Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest, channel.getPeers());
        System.out.println("queryProposals = " + queryProposals);
        for (ProposalResponse proposalResponse : queryProposals) {
            if (!proposalResponse.isVerified() || proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
                fail("Failed query proposal from peer " + proposalResponse.getPeer().getName() + " status: " + proposalResponse.getStatus() +
                        ". Messages: " + proposalResponse.getMessage()
                        + ". Was verified : " + proposalResponse.isVerified());
                throw new IllegalArgumentException("Failed query proposal from peer " + proposalResponse.getPeer().getName() + " status: " + proposalResponse.getStatus() +
                        ". Messages: " + proposalResponse.getMessage()
                        + ". Was verified : " + proposalResponse.isVerified());
            } else {
                String payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
                out("从对等体 %s 查询的结果为： %s", proposalResponse.getPeer().getName(), payload);
                return payload;
            }
        }
        return null;
    }
    public SampleUser enroll(String userName, String enrollmentSecret, String mspid, String orgName, String caName, String caLocation, Properties caProperties) throws Exception {
        SampleUser user = sampleStore.getMember(userName, orgName);
        HFCAClient ca = getCAClient(caName, caLocation, caProperties); // 成员关系服务提供者

        user.setEnrollmentSecret(enrollmentSecret);
        // 登记用户
        if (!user.isEnrolled()) {

            user.setEnrollment(ca.enroll(userName, enrollmentSecret));
            user.setMspId(mspid);
        }
        return user;
    }

    /**********************************************************************************************************
     *
     *
     *
     **********************************************************************************************************/

    private ChaincodeID getChaincodeID() {
        final ChaincodeID chaincodeID;
        ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME)
                .setVersion(CHAIN_CODE_VERSION);
        if (null != CHAIN_CODE_PATH) {
            chaincodeIDBuilder.setPath(CHAIN_CODE_PATH);
        }
        chaincodeID = chaincodeIDBuilder.build();
        return chaincodeID;
    }

    private boolean checkChaincodeStatus(List<Query.ChaincodeInfo> ccinfoList) {
        boolean found = false;

        for (Query.ChaincodeInfo ccifo : ccinfoList) {

            if (CHAIN_CODE_PATH != null) {
                found = CHAIN_CODE_NAME.equals(ccifo.getName()) && CHAIN_CODE_PATH.equals(ccifo.getPath()) && CHAIN_CODE_VERSION.equals(ccifo.getVersion());
                if (found) {
                    break;
                }
            }
            found = CHAIN_CODE_NAME.equals(ccifo.getName()) && CHAIN_CODE_VERSION.equals(ccifo.getVersion());
            if (found) {
                break;
            }
        }
        return found;
    }

    private SampleUser setupPeerAdmin(SampleOrg sampleOrg) throws Exception {
        // peerAdmin
        String domainName = sampleOrg.getDomainName();
        String orgName = sampleOrg.getName();
        File adminKeystore = Paths.get(FAB_CONFIG.getChannelPath(), "crypto-config/peerOrganizations/",
                domainName, format("/users/Admin@%s/msp/keystore", domainName)).toFile();
        File adminCert = Paths.get(FAB_CONFIG.getChannelPath(), "crypto-config/peerOrganizations/", domainName,
                format("/users/Admin@%s/msp/signcerts/Admin@%s-cert.pem", domainName, domainName)).toFile();
        SampleUser peerOrgAdmin = sampleStore.getMember(orgName + "Admin", orgName, sampleOrg.getMSPID(),
                Util.findFileSk(adminKeystore),
                adminCert);
        return peerOrgAdmin;
    }

    private HFCAClient getCAClient(String caName, String caLocation, Properties caProperties) {
        HFCAClient caclient = null;
        try {
            caclient = HFCAClient.createNewInstance(caName, caLocation, caProperties);
            caclient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite()); // 成员关系服务提供者
        } catch (Exception e) {
            e.printStackTrace();
        }
        return caclient;
    }

    private Channel setupChannel(String path, HFClient client, String name, SampleUser peerAdmin, Orderer orderer) throws Exception {
        ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(path));

        //Create channel that has only one signer that is this orgs peer admin. If channel creation policy needed more signature they would need to be added too.
        // 只有orgs peer admin才可以创建通道
        //        Channel newChannel = client.newChannel(name);
        return client.newChannel(name, orderer, channelConfiguration, client.getChannelConfigurationSignature(channelConfiguration, peerAdmin));
    }

    private void joinPeerToChannel(Channel channel, Collection<Peer> peers) throws ProposalException {
        for (Peer peer : peers) {
            // 给通道加入节点
            channel.joinPeer(peer,
                createPeerOptions()
                    .setPeerRoles(EnumSet.of(Peer.PeerRole.ENDORSING_PEER, Peer.PeerRole.LEDGER_QUERY, Peer.PeerRole.CHAINCODE_QUERY, Peer.PeerRole.EVENT_SOURCE)));
        }
    }



    public void upgradeRequest(String[] initArgs) throws Exception {
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        UpgradeProposalRequest upgradeProposalRequest = client.newUpgradeProposalRequest();
        upgradeProposalRequest.setChaincodeID(chaincodeID);
        upgradeProposalRequest.setProposalWaitTime(FAB_CONFIG.getDeployWaitTime());
        upgradeProposalRequest.setFcn("init");
        upgradeProposalRequest.setArgs(initArgs);    // 没有参数不会更改分类帐请参阅链码。
        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy;

        chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
        chaincodeEndorsementPolicy.fromYamlFile(chaincodeEndorsementPolicyYaml);

        upgradeProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

        out("发送升级建议");

        Collection<ProposalResponse> responses2;

        responses2 = channel.sendUpgradeProposal(upgradeProposalRequest);

        successful.clear();
        failed.clear();
        for (ProposalResponse response : responses2) {
            if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                out("成功的升级建议响应Txid: %s 来自peer %s", response.getTransactionID(), response.getPeer().getName());
                successful.add(response);
            } else {
                failed.add(response);
            }
        }

        out("被认为标准的 %d 升级提案回复。成功+验证： %d . 失败: %d", channel.getPeers().size(), successful.size(), failed.size());

        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            throw new AssertionError("没有足够的支持升级 :"
                    + successful.size() + ".  " + first.getMessage());
        }

        channel.sendTransaction(successful).get(FAB_CONFIG.getTransactionWaitTime(), TimeUnit.SECONDS);
    }

//    查询区块详情
    public void queryBlockchainInfo(String testTxID) throws Exception {

        // Channel queries

        // 我们只能将通道查询发送到与SDK用户上下文位于同一组织中的对等方
        // 从当前使用的组织中获取对等点，并随机选择一个将查询发送到。
        //  Set<Peer> peerSet = sampleOrg.getPeers();
        //  Peer queryPeer = peerSet.iterator().next();
        //   out("Using peer %s for channel queries", queryPeer.getName());

        BlockchainInfo channelInfo = channel.queryBlockchainInfo();
        out("通道信息 : " + channelName);
        out("通道高度: " + channelInfo.getHeight());
        String chainCurrentHash = Hex.encodeHexString(channelInfo.getCurrentBlockHash());
        String chainPreviousHash = Hex.encodeHexString(channelInfo.getPreviousBlockHash());
        out("链当前块哈希: " + chainCurrentHash);
        out("链上一个块哈希: " + chainPreviousHash);

        //按块号查询。应返回最新的块，即块号2
        BlockInfo returnedBlock = channel.queryBlockByNumber(channelInfo.getHeight() - 1);
        String previousHash = Hex.encodeHexString(returnedBlock.getPreviousHash());
        out("queryBlockByNumber返回了正确的blockNumber块 " + returnedBlock.getBlockNumber()
                + " \n 上一个哈希 " + previousHash);
        assertEquals(channelInfo.getHeight() - 1, returnedBlock.getBlockNumber());
        assertEquals(chainPreviousHash, previousHash);

        // 按块哈希查询。使用最新块的前一个哈希，因此应返回块号1
        byte[] hashQuery = returnedBlock.getPreviousHash();
        returnedBlock = channel.queryBlockByHash(hashQuery);
        out("queryBlockByHash返回blockNumber为的块 " + returnedBlock.getBlockNumber());
        assertEquals(channelInfo.getHeight() - 2, returnedBlock.getBlockNumber());

        // 按TxID查询块。因为这是最后一个TxID，应该是2号块
        returnedBlock = channel.queryBlockByTransactionID(testTxID);
        out("queryBlockByTxID返回blockNumber为的块 " + returnedBlock.getBlockNumber());
        assertEquals(channelInfo.getHeight() - 1, returnedBlock.getBlockNumber());

        //按ID查询事务
        TransactionInfo txInfo = channel.queryTransactionByID(testTxID);
        out("QueryTransactionByID返回TransactionInfo:txID ***"+txInfo.getTransactionID()
                + "\n **验证码** " + txInfo.getValidationCode().getNumber()
        );

    }



}
