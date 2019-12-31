<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<script type="text/javascript" src="js/jquery-1.5.1.min.js"></script>
<script type="text/javascript" src="js/sweetalert-dev.js"></script>
<script type="text/javascript" src="js/jipeng.js"></script>
<link rel="stylesheet" type="text/css" href="css/sweetalert.css">
<link rel="stylesheet" type="text/css" href="css/currency.css">
<head>
    <title>fabric-sdk功能测试页面</title>
</head>
<body>
<table cellspacing="0" cellpadding="0" style="margin-top: 100px" align="center">
    <tr>
        <td style="padding-left: 50px">
            <p style="color: yellowgreen">安装链码</p>
            <form>
                安装链码：
                <input type="button" value="确认" onclick="installCheckin()" />
            </form>
        </td>
    </tr>
    <tr>
        <td style="padding-left: 50px">
            <p style="color: yellowgreen">查询链码初始化状态</p>
            <form>
                查询链码初始化状态：<input type="button" value="查询" onclick="cshzt()" />
            </form>
        </td>
    </tr>
    <tr>
        <td style="padding-left: 50px">
            <p style="color: yellowgreen">初始化链码
                <span style="color: #FF0000; ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                链码初始化需要等待一段时间，且链码只能初始化一次</span> </p>
            <form>
                用户名1：<input type="text" id="name1" placeholder="用户1，如：张三" autocomplete="on">
                初始化金额：<input type="text" id="money1" placeholder="金额，如：100" autocomplete="on">
                <br/>
                用户名2：<input type="text" id="name2" placeholder="用户2，如：李四" autocomplete="on">
                初始化金额：<input type="text" id="money2" placeholder="金额，如：100" autocomplete="on">
                <br/>
                <input type="button" value="确认" onclick="instantiedCheckin()" />
            </form>
        </td>
    </tr>

    <tr>
        <td style="padding-left: 50px">
            <p style="color: yellowgreen">查询功能</p>
            <form>
                输入查询账户名：
                <input type="text" id="queryname" placeholder="用户名，如：张三" autocomplete="on"/>
                <input type="button" value="查询" onclick="queryfun()" />
            </form>
        </td>
    </tr>

    <tr>
        <td style="padding-left: 50px">
            <p style="color: yellowgreen">转账功能</p>
            <form>
                转账账户：<input type="text" id="user1" placeholder="支出账号，如：张三" autocomplete="on">
                收款账户：<input type="text" id="user2" placeholder="收款账号，如：李四" autocomplete="on">
                转账金额：<input type="text" id="money" placeholder="金额，如：10" autocomplete="on">
                <input type="button" value="确认" onclick="move()" />
            </form>
        </td>
    </tr>
    <tr>
        <td style="padding-left: 50px">
            <p style="color: yellowgreen">删除功能</p>
            <form>
                请输入要删除的账户：<input type="text" id="delusername" placeholder="请输入账号，如：张三" autocomplete="on">
                <input type="button" value="确认" onclick="deluser()" />
            </form>
        </td>
    </tr>
    <tr>
        <td style="padding-left: 50px">
            <p style="color: yellowgreen">导入多组数据（Key-Value键值对的形式）</p>
            <form>
                Key01：<input type="text" id="Key01"autocomplete="on">
                Value01：<input type="text" id="Value01"autocomplete="on">
            <br/>
                Key02：<input type="text" id="Key02"autocomplete="on">
                Value02：<input type="text" id="Value02"autocomplete="on">
            <br/>
                Key03：<input type="text" id="Key03"autocomplete="on">
                Value03：<input type="text" id="Value03"autocomplete="on">
            <br/>
                <input type="button" value="确认" onclick="multigroup()" />
            </form>
        </td>
    </tr>
    <tr>
        <td style="padding-left: 50px">
            <p style="color: red">创建通道功能</p>
            <form>
                通道名称：<input type="text" id="channelname" placeholder="输入通道名称，如：通道一" autocomplete="on">
                <input type="button" value="确认" onclick="construct()" />
            </form>
        </td>
    </tr>
</table>
</body>
</html>

