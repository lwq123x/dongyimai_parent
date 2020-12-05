app.controller("cartController", function ($scope, cartService) {
    $scope.findCartList = function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;
            //购物车列表更新则总数量也更新
            $scope.totalValue = cartService.sum($scope.cartList);
        })
    }

    //添加商品到购物车
    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(function (response) {
            if (response.success) {
                $scope.findCartList();//刷新列表
            } else {
                alert(response.message); //弹出错误提示
            }
        })
    }

    //获取当前登录人的地址列表
    $scope.findAddressList = function () {
        cartService.findAddressList().success(function (response) {
            $scope.addressList = response;
            //设置默认地址
            for (var i = 0; i < $scope.addressList.length; i++) {
                if ($scope.addressList[i].isDefault == '1') {
                    $scope.address = $scope.addressList[i];
                    break;
                }
            }
        })
    }

    //选择地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
    }

    //判断是否选中当前的地址
    $scope.isSelectedAddress = function (address) {
        if (address == $scope.address) {
            return true;
        } else {
            return false;
        }
    }

    $scope.order = {paymentType: '1'}; //初始化订单的数据结构
    //选择支付方式
    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type;
    }
    
    //保存订单
    $scope.submitOrder=function () {
        $scope.order.receiverAreaName = $scope.address.address;        //收货地址
        $scope.order.receiverMobile = $scope.address.mobile;           //收货人手机
        $scope.order.receiver = $scope.address.contact;                //收货人

        cartService.submitOrder($scope.order).success(function (response) {
            if (response.success){
                if ($scope.order.paymentType=='1'){  //如果是线上支付,跳转到支付页面
                    location.href="pay.html";
                }else {
                    alert(response.message);
                }
            }
        })
    }
    
    


})