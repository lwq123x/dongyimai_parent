//商品类目控制层
app.controller('itemCatController', function ($scope, $controller, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        itemCatService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        itemCatService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        itemCatService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }

    //保存
    /* $scope.save = function () {
         var serviceObject;//服务层对象
         if ($scope.entity.id != null) {//如果有ID
             serviceObject = itemCatService.update($scope.entity); //修改
         } else {
             serviceObject = itemCatService.add($scope.entity);//增加
         }
         serviceObject.success(
             function (response) {
                 if (response.success) {
                     //重新查询
                     $scope.reloadList();//重新加载
                 } else {
                     alert(response.message);
                 }
             }
         );
     }*/


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        itemCatService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    //$scope.reloadList();//刷新列表
                    $scope.findByParentId($scope.parentId);//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        itemCatService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //根据父级id查询下级列表
    $scope.findByParentId = function (parentId) {
        itemCatService.findByParentId(parentId).success(function (response) {
            $scope.list = response;
        })
    }

    $scope.grade = 1;//默认为一级
    //设置级别
    $scope.setGrade = function (value) {
        $scope.grade = value;
    }

    //读取列表
    $scope.selectList = function (p_entity) {
        if ($scope.grade == 1) {//如果为一级
            $scope.entity_1 = null;
            $scope.entity_2 = null;
        }

        if ($scope.grade == 2) {//如果为二级
            $scope.entity_1 = p_entity;
            $scope.entity_2 = null;
        }
        if ($scope.grade == 3) { //如果为三级
            $scope.entity_2 = p_entity;
        }
        $scope.findByParentId(p_entity.id);//查询此级下级列表
    }

    //定义上级ID
    $scope.parentId = 0;
    //根据上级ID查询下级列表
    $scope.findByParentID = function (parentId) {
        $scope.parentId = parentId;//记住上级Id
        itemCatService.findByParentId(parentId).success(function (response) {
            $scope.list = response;
        })
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//有ID,修改
            serviceObject = itemCatService.update($scope.entity);
        } else {
            $scope.entity.parentId = $scope.parentId;//没有Id,赋予上级Id,增加
            serviceObject = itemCatService.add($scope.entity);
        }
        serviceObject.success(function (response) {
            if (response.success) {
                //重新查询
                $scope.findByParentId($scope.parentId);
            } else {
                alert(response, message);
            }
        })
    }


    //获取下拉菜单数据
    $scope.typeTemplateList = {data: []}; //模板列表
    //读取模板列表
    $scope.findtypeTemplateList = function () {
        typeTemplateService.selectOptionList().success(function (response) {
            $scope.typeTemplateList = {data: response};
        })
    }


});	