app.controller('itemPageController', function ($scope,$http) {

    //数量操作 点击+ - 修改购买数量的方法
    $scope.addNum = function (num) {
        $scope.num = $scope.num + num;
        //格式校验
        if ($scope.num < 1) {
            $scope.num = 1;
        }
    }

    //初始化规格列表的数据结构  记录用户选择的规格
    $scope.specificationItems = {};
    //用户选择规格
    $scope.selectSpecification = function (key, value) {
        $scope.specificationItems[key] = value;
        searchSku();//读取sku
    }

    //判断某规格选项是否被用户选中
    $scope.isSelected = function (key, value) {
        if ($scope.specificationItems[key] == value) {
            return true;
        } else {
            return false;
        }
    }

    //加载默认SKU
    $scope.loadSku = function () {
        $scope.sku = skuList[0];
        $scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec));//深克隆
    }

    //匹配两个对象  比较两个Map集合是否相等
    matchObject = function (map1, map2) {
        for (var k in map1) {
            if (map1[k] != map2[k]) {
                return false;
            }
        }

        for (var k in map2) {
            if (map2[k] != map1[k]) {
                return false;
            }
        }
        return true;
    }

    //查询SKU
    searchSku = function () {
        //遍历SKU集合
        for (var i = 0; i < skuList.length; i++) {
            //根据选中的规格和SKU的spec对象进行比对,如果相等,则将该SKU的数据取出
            if (matchObject($scope.specificationItems, skuList[i].spec)) {
                $scope.sku = skuList[i];
                return;
            }

        }
        //skuList 一个满足条件的数据都没有,默认赋空值
        $scope.sku={'id':0,'title':'---','price':0,'spec':{}};
    }

    //添加商品到购物车
    $scope.addToCart=function () {
        //alert('skuid:'+$scope.sku.id)
        $http.get('http://localhost:9108/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+'&num='+ $scope.num,{'withCredentials':true}).success(
            function (response) {
            if (response.success){
                location.href='http://localhost:9108/cart.html'; //跳转到购物车页面
            }else {
                alert(response.message);
            }
        })
    }

})