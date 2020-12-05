app.service('payLogService',function ($http) {
    this.findAllPayLog=function(){
       return $http.get('../paylog/findAllPayLog.do');
    }

    //按分页查询
    this.findByPage=function (page,rows) {
        return $http.get('../paylog/findPage.do?page='+page+'&rows='+rows);
    }


})