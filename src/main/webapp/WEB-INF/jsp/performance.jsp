<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Dubbo接口性能压测</title>
</head>
<body>
Dubbo接口性能压测工具. 现在时间是  ${now}
<hr>
<!--测试发现更改jsp文件,也需要重启启动类(清缓存也不行!) -->
<form action="${pageContext.request.contextPath}/performance/generalTest" method="post" id="performanceForm">
<table align="center" width="80%" style="table-layout:fixed">
    <tbody>
    <tr>
        <td width="50"><span style="color: red" >*</span>任务名称</td>
        <td width="400"><input size="120" type="text" name="taskName" placeholder="任务名称" /></td>
    </tr>
    <tr>
        <td width="50"><span style="color: red">*</span>测试接口</td>
        <td><input type="text" size="120" size="50" name="interfaceName" placeholder="测试接口名，例如: com.xxx.service.api.QueryServiceApi" width="400px"/><a href="/uploadJar" target="_blank">找不到类?</a></td>
    </tr>
    <tr>
        <td width="50"><span style="color: red">*</span>方法</td>
        <td><input type="text" size="120" name="method" placeholder="方法名称，例如:placeOrder" width="400px"/></td>
    </tr>
    <tr>
        <td width="50"><span>&nbsp;</span>注册中心地址</td>
        <td><input type="text" size="120" name="registryAddress" placeholder="例如：nacos://192.168.1.10:1188, 指定IP端口时可不填" width="400px"/></td>
    </tr>
    <tr>
        <td width="50"><span>&nbsp;</span>指定ip端口</td>
        <td><input type="text" size="120" name="referenceUrl" placeholder="例如：192.168.1.131:32101, 可不填，默认从nacos订阅" width="400px"/></td>
    </tr>
    <tr>
        <td width="50"><span>&nbsp;</span>参数类型</td>
        <td><input type="text" size="120" name="paramType" width="400px" placeholder='接口入参的类型json，例如:一个参数 java.lang.Long 或者多个入参 ["java.lang.Long", "com.xxx.service.dto.req.OrderReqDTO"]'/></td>
    </tr>
    <tr>
        <td width="50"><span>&nbsp;</span>参数</td>
        <td>
            <textarea name="paramValue" rows="15" cols="100" placeholder='接口入参的json，例如: {"test":"12345678"} (多个入参时为json数组),支持动态变量, __RANDOM_INT/__RANDOM_STRING'></textarea>
        </td>
    </tr>
    <tr>
        <td><span>&nbsp;</span>动态参数</td>
        <td>

        </td>
    </tr>
    <tr>
        <td></td>
        <td>
            <table id="dynamicTable">
                <tr >
                    <td>变量名</td><td>json数组(每次发请求循环获取)</td><td>操作</td>
                </tr>
                <tr id="first">
                    <td><input name="dynamicRow[0].name" type="text" placeholder="定义变量可替换\${variable}"/></td>
                    <td valign="middle" style="vertical-align:middle">
                            <textarea name="dynamicRow[0].value" rows="1" cols="60" placeholder="json数组，例如：[1,2,3,4,5,6,7,8]"></textarea>
                    </td>
                    <td>
                        <button type="button" class="addRow" >添加</button>
                        <button type="button" class="delRow">删除</button>
                    </td>
                </tr>
            </table>
        </td>
    </tr>

    <tr>
        <td width="50"><span>&nbsp;</span>结果断言(SpEL表达式)</td>
        <td><input type="text" size="120" name="spEL" width="400px" placeholder="例如：#result.data.name == 'hello' and #result.data.valueList[0].id != null"/></td>
    </tr>

    <tr>
        <td width="50"><span style="color: red">*</span>请求超时时间(ms)</td>
        <td><input type="text" size="30" name="timeout" placeholder="请求超时时间(ms)" value="5000"/></td>
    </tr>
    <tr>
        <td width="50"><span style="color: red">*</span>线程数</td>
        <td><input type="text" size="30" name="threads" placeholder="并发线程数" value="1"/></td>
    </tr>
    <tr>
        <td width="50"><span style="color: red">*</span>预热次数</td>
        <td><input type="text" size="30" name="warmupTimes" placeholder="启动预热的次数，不算入实际测量结果" value="0"/></td>
    </tr>
    <tr>
        <td width="50"><span style="color: red">*</span>实际测试次数</td>
        <td><input type="text" size="30" name="times" placeholder="实际测试次数" value="1"/></td>
    </tr>
    <tr>
        <td width="50"><span style="color: red">*</span>Fork</td>
        <td><input type="text" size="30" name="fork" value="1"/></td>
    </tr>
    <tr>
        <td colspan=2 width="100" align="center">
            <button type="button" id="submit">提交</button>
        </td>
    </tr>
    </tbody>
</table>
    <button type="reset">重置</button>
</form>
</body>

<script src="https://code.jquery.com/jquery-3.1.1.min.js"></script>
<script src="//cdn.bootcss.com/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>
<script>
    $(function() {
        $("#submit").on("click", function() {
            saveToLocalStorage("performanceForm", JSON.stringify($("#performanceForm").serializeArray()));
            $.ajax({
                type: "post",
                url: "${pageContext.request.contextPath}/performance/generalTest",
                data: $("#performanceForm").serialize(), // 序列化form表单里面的数据传到后台
                //dataType: "json", // 指定后台传过来的数据是json格式
                success: function(data){
                    alert(data)
                },
                error: function(err){
                    alert("error:" + err);
                }
            })
        });

        refreshBind();
        loadFromLocalStorage("performanceForm");
    })

    function refreshBind() {
        $(".addRow").unbind('click');
        $(".delRow").unbind('click');

        $(".addRow").on("click", function() {
            $(this).parent().parent().parent().append(
                $(this).parent().parent().clone()
            );
            refreshRowNum();
            refreshBind();
        });

        $(".delRow").on("click", function() {
            if($("#dynamicTable").find('tr').length > 2) {
                $(this).parent().parent().remove();
            }
            refreshRowNum();
        });
    }

    function refreshRowNum() {
        //第一行是表头
        var index = -1;
        $("#dynamicTable tr").each(function(){
            $(this).find("input").attr("name","dynamicRow["+index+"].name");
            $(this).find("textarea").attr("name","dynamicRow["+index+"].value");
            index++;
        });
    }


    function saveToLocalStorage(name, data) {
        if (window.localStorage) {
            console.log('This browser supports localStorage:' + data);
            window.localStorage.setItem(name, data);
        } else {
            console.log('This browser does NOT support');
        }
    }

    function loadFromLocalStorage(name) {
        if (window.localStorage) {
            var data = window.localStorage.getItem(name);
            console.log('This browser supports localStorage,data=' + data);
            if(data != null) {
                var dataObj = JSON.parse(data);　　//字符反序列化成对象
                for(var i=0;i<dataObj.length;i ++) {
                    var row = dataObj[i];
                    if(row.name.indexOf("dynamicRow") > -1 && row.name.indexOf(".name") > -1 && $('[name="' + row.name + '"]').length == 0) {
                        $("#dynamicTable").append($("#first").clone());
                        refreshRowNum();
                        refreshBind();
                        console.log("add success " + row.name)
                    }
                    if(row.value != null) {
                        $('[name="' + row.name + '"]').val(row.value);
                    }
                }
            }
        } else {
            console.log('This browser does NOT support');
        }
    }

</script>
</html>