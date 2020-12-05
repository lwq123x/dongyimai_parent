var app = angular.module('dongyimai',[]);

//angularJs 的过滤器
app.filter('trustHtml',['$sce',function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
}])