app.controller('payLogController',function ($scope, payLogService,$controller) {

    $controller('baseController',{$scope:$scope});//继承

   $scope.findAllPayLog=function () {
        payLogService.findAllPayLog().success(
            function (response) {
            $scope.payLogList=response;
        })
    }

    $scope.findAllPayLog=function () {
        payLogService.findAllPayLog().success(function (response) {
            $scope.payLogList=response.rows;
            $scope.paginationConf.totalItems=response.total;//更新总记录数
        })
    }


})