

    // $(function(){
    //     alert("页面加载弹窗")
    //     $.ajax({
    //         type: 'POST',
    //         url: "/getUserList",
    //         success: function (data) {
    //
    //             swal("查询所有用户成功",data,"success")
    //         },
    //         error: function (data) {
    //             swal("失败");
    //         }
    //     });
    // });
    function queryfun() {
        var queryname = $("#queryname").val();
        $.ajax({
            data : {
                queryname : queryname
            },
            type: 'GET',
            url: "/query",
            success: function (data) {
                if(data == "空"){
                    swal("错误","请检查账户是否存在","error");
                }else{
                    swal("查询用户数据成功",data,"success");
                }
            }
        });
    }
function construct() {

    $.ajax({
        type: 'POST',
        url: "/construct",
        success: function (data) {
            if (data == true){
                swal("成功","构建通道成功","success");
            }else {
                swal("错误","构造通道失败","error");
            }
        }
    });
}
function installCheckin() {

    $.ajax({
        type: 'GET',
        url: "/installCheckin",
        success: function (data) {
            if (data == true){
                swal("成功","安装链码成功","success");
            }else {
                swal("错误","安装链码失败","error");
            }
        }
    });
}
function instantiedCheckin() {

    var name1 = $("#name1").val();
    var money1 = $("#money1").val();
    var name2 = $("#name2").val();
    var money2 = $("#money2").val();
    $.ajax({
        data : {
            name1 : name1,
            money1 : money1,
            name2 : name2,
            money2 : money2
        },
        type: 'GET',
        url: "/instantiedCheckin",
        success: function (data) {
            if (data == true){
                swal("成功","初始化链码成功","success");
            }else {
                swal("错误","初始化链码失败","error");
            }
        },
    });
}

function move() {
    var user1 = $("#user1").val();
    var user2 = $("#user2").val();
    var money = $("#money").val();
    $.ajax({
        data : {
            user1 : user1,
            user2 : user2,
            money : money
        },
        type: 'GET',
        url: "/move",
        success: function (data) {
            if (data == true){
                swal("成功","转账成功","success");
            }else {
                swal("错误","转账失败","error");
            }
        },
    });
}


function cshzt() {
    $.ajax({
        type: 'GET',
        url: "/cshzt",
        success: function (data) {
            if ("没找到"==(data)) {
                swal("错误", "暂未发现已经实例化的链码,可以通过下方功能完成链码的实例化", "error");
            } else {
                swal("查询链码初始化状态成功", data, "success");
            }
        },
    });
}
    function deluser() {
        var delusername = $("#delusername").val();
        $.ajax({
            data : {
                delusername : delusername
            },
            type: 'GET',
            url: "/deluser",
            success: function (data) {
                if (data == true){
                    swal("完成","删除 " + delusername + "成功","success");
                }else {
                    swal("错误","删除失败","error");
                }
            },
        });
    }
    function multigroup() {
        var Key01 = $("#Key01").val();
        var Value01 = $("#Value01").val();
        var Key02 = $("#Key02").val();
        var Value02 = $("#Value02").val();
        var Key03 = $("#Key03").val();
        var Value03 = $("#Value03").val();
        $.ajax({
            data : {
                Key01 : Key01,
                Value01 : Value01,
                Key02 : Key02,
                Value02 : Value02,
                Key03 : Key03,
                Value03 : Value03
            },
            type: 'GET',
            url: "/multigroup",
            success: function (data) {
                if (data == true){
                    swal("成功","批量导入数据成功","success");
                }else {
                    swal("错误","批量导入数据失败","error");
                }
            },
        });
    }


