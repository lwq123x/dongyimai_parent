app.service('uploadService', function ($http) {
    this.uploadFile = function () {
        //1.获取表单上传的数据
        var formData = new FormData();
        //参数一:上传控件的id
        formData.append("file", file.files[0])//第一个file相当于往后端传的参数的名字,第二个file要与前端file控件的id相对应
        //2.对上传请求数据进行配置
        return $http({
            method: 'POST',
            url: '../upload.do',
            data: formData,
            //anjularjs对于post和get请求默认的Content-Type header 是application/json。
            // 通过设置‘Content-Type’: undefined，这样浏览器会帮我们把Content-Type 设置为 multipart/form-data.
            headers: {'Content-Type': undefined},  //默认上传请求数据格式都是JSON,采用数据流的方式
            //通过设置 transformRequest: angular.identity ，anjularjs transformRequest function 将序列化我们的formdata object.
            transformRequest: angular.identity    //使用angularJS对上传的数据进行序列化
        })
    }
})