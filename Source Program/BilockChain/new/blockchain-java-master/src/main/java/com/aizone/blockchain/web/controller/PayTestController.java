//package com.aizone.blockchain.web.controller;
//
//import com.aizone.blockchain.dao.PaymentRecordDao;
//import com.aizone.blockchain.utils.JsonVo;
//import java.util.HashMap;
//import java.util.Map;
//import javax.servlet.http.HttpServletRequest;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//
///**
// * 测试用
// * @author Kelly
// *
// */
//@RestController
//@RequestMapping({"/payTest"})
//public class PayTestController
//{
//  @PostMapping({"/commitPayTestRecord"})
//  public JsonVo commitPayTestRecord(HttpServletRequest request)
//  {
//    PaymentRecordDao payDao = new PaymentRecordDao();
//    payDao.logSaveTest("PayTestController1", "commitPayTestRecord");
//    String id = request.getParameter("id");
//    String result = request.getParameter("result");
//    String time = request.getParameter("time");
//    payDao.logSaveTest2(id, result, time);
//    payDao.logSaveTest("PayTestController2", "commitPayTestRecord");
//    Map<String, String> map = new HashMap();
//    map.put("payTest", "commitPayTestRecord is ok");
//    JsonVo vo = JsonVo.success();
//    vo.setItem(map);
//    return vo;
//  }
//}
//
