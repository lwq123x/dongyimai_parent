app.controller('baseController', function ($scope) {
    //定义分页组件的属性
    $scope.paginationConf = {
        'totalItems': 10,    //总记录数
        'currentPage': 1,    //当前页码
        'itemsPerPage': 10,  //每页显示记录数
        'perPageOptions': [10, 20, 30, 40, 50],  //设置分页显示条数控件
        onChange: function () {
            //执行分页查询
            //$scope.findPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
            $scope.reloadList();
        }
    }

    //优化查询方法
    $scope.reloadList = function () {
        //$scope.findPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    //初始化ID的数组
    $scope.selectIds = [];
    //选中.反选
    $scope.updateSelection = function ($event, id) {
        //判断复选框是否选中还是反选
        if ($event.target.checked) {
            $scope.selectIds.push(id); //选中将元素放入数组
        } else {
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index, 1); //参数一:元素的索引位置 参数二:删除个数
        }
    }


    //将json字符串转换为可读形式
    $scope.jsonToString = function (jsonStr, key) {
        //将json结构的字符串转换为json对象
        var json = JSON.parse(jsonStr);
        var value = '';
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                value += ",";
            }
            value += json[i][key]
        }
        return value;
    }

    //判断对象在集合中是否存在 通过key值判断
    $scope.searchObjectByKey = function (list, key, keyValue) {
        for (var i = 0; i < list.length; i++) {
            //判断key值在集合对象中存在
            if (list[i][key] == keyValue) {
                return list[i];  //返回该对象
            }
        }
        return null;
    }
})