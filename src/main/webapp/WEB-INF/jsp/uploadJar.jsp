<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>上传Jar</title>
</head>
<body>
上传Jar. 现在时间是  ${now}
<hr>

<!--测试发现更改jsp文件,也需要重启启动类(清缓存也不行!) -->
<form method="post" enctype="multipart/form-data" id="uploadForm">
<table align="center" width="80%" style="table-layout:fixed">
    <tbody>
    <tr>
        <td width="50"><span style="color: red" >*</span>上传Api的jar</td>
        <td width="400">
            <input type="file" name="upload" id="fileupload" multiple="multiple" accept="/jar"/>
        </td>
    </tr>
    <tr>
        <td colspan=2>
            <button type="button" id="submit">上传</button>
        </td>
    </tr>
    </tbody>
</table>
</form>
</body>

<script src="https://code.jquery.com/jquery-3.1.1.min.js"></script>
<script src="//cdn.bootcss.com/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>
<script>
    $(function () {
        //异步上传多个文件，带表单参数
        $("#submit").click(function () {
            //将form表单转换为FormData对象
            var data = new FormData(document.querySelector("#uploadForm"));
            $.post({
                url: '${pageContext.request.contextPath}/performance/uploadJar',
                contentType:false, //jQuery不要去设置Content-Type请求头
                processData:false, //jQuery不要去处理发送的数据
                cache:false, //不缓存
                dataType:'json', //返回类型json
                data:data,  //表单数据
                success:function (res) {
                    console.log(res);
                },
                error:function (error) {
                    console.log(error);
                }
            });
        });
    });

</script>
</html>