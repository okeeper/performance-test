<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Http接口性能压测</title>
</head>
<body>
Http接口性能压测工具. 现在时间是  ${now}
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
        <td width="50"><span style="color: red">*</span>测试url</td>
        <td><input type="text" size="120" size="50" name="url" placeholder="测试接口名，例如: http://www.baidu.com,http://www.google.com 多个url可以用,分割开" width="400px"/></td>
    </tr>
    <tr>
        <td width="50"><span style="color: red">*</span>请求类型</td>
        <td><select name="method"><option value="POST" selected>POST</option><option value="GET">GET</option></select>
            </td>
    </tr>
    <tr>
        <td width="50"><span>&nbsp;</span>headers</td>
        <td>
            <textarea name="headerStr" rows="8" cols="100" placeholder='POST请求的headers'>{
"X-user-id": 18153030,
"language-id": 100,
"country-id": 1,
"Content-Type": "application/json"
}</textarea>
           </td>
    </tr>
    <tr>
        <td width="50"><span>&nbsp;</span>GET请求参数map</td>
        <td>
            <textarea name="paramsStr" rows="8" cols="100" placeholder='POST请求的headers'></textarea>
        </td>
    </tr>
    <tr>
        <td width="50"><span>&nbsp;</span>POST请求Body</td>
        <td>
            <textarea name="requestBody" rows="15" cols="100" placeholder='POST请求Body,支持动态变量, __RANDOM_INT/__RANDOM_STRING'></textarea>
        </td>
    </tr>
    <tr>
        <td width="50"><span>&nbsp;</span>结果Class</td>
        <td><input type="text" size="120" size="50" name="resultClass" value="" placeholder="返回结果解析的class类" width="400px"/>
        </td>
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
            saveToLocalStorage("performanceFormHttp", JSON.stringify($("#performanceForm").serializeArray()));
            $.ajax({
                type: "post",
                url: "${pageContext.request.contextPath}/performance/generalHttp",
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
        loadFromLocalStorage("performanceFormHttp");
    })

    function saveToLocalStorage(name, data) {
        if (window.localStorage) {
            console.log('This browser supports localStorage');
            window.localStorage.setItem(name, data);
        } else {
            console.log('This browser does NOT support');
        }
    }

    function loadFromLocalStorage(name) {
        if (window.localStorage) {
            console.log('This browser supports localStorage');
            var data = window.localStorage.getItem(name);
            if(data != null) {
                var dataObj = JSON.parse(data);　　//字符反序列化成对象
                for(var i=0;i<dataObj.length;i ++) {
                    var row = dataObj[i];
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