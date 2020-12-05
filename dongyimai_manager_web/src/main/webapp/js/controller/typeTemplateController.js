 //控制层 
app.controller('typeTemplateController' ,function($scope,$controller   ,typeTemplateService,brandService,specificationService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		typeTemplateService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		typeTemplateService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		typeTemplateService.findOne(id).success(
			function(response){
				$scope.entity= response;

				//将数据库中的json结构字符串转换成json对象
				$scope.entity.brandIds = JSON.parse($scope.entity.brandIds);
				$scope.entity.specIds = JSON.parse($scope.entity.specIds);
				$scope.entity.customAttributeItems = JSON.parse($scope.entity.customAttributeItems);
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=typeTemplateService.update( $scope.entity ); //修改  
		}else{
			serviceObject=typeTemplateService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		typeTemplateService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		typeTemplateService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}


	//初始化下拉品牌的数据
	//$scope.brandList = {data:[{"id": 1,"text": "小米"},{"id": 2,"text": "华为"},{"id": 3,"text": "苹果"},{"id": 4,"text": "VIVO"}]};

	//查询下拉品牌数据
	$scope.findBrandList = function () {
		brandService.selectOptions().success(function (response) {
			$scope.brandList = {'data':response};
		})
	}

	//查询下拉规格的数据
	$scope.findSpecList = function () {
		specificationService.selectOptions().success(function (response) {
			$scope.specList = {'data':response};
		})
	}

	$scope.initSelect = function () {
		$scope.findBrandList();
		$scope.findSpecList();
	}


	//初始化扩展属性数组的数据结构
	$scope.entity = {'customAttributeItems':[]}
	//新增行
	$scope.addTableRow = function (){
		$scope.entity.customAttributeItems.push({});
	}
	//删除行
	$scope.deleteTableRow = function (index){
		$scope.entity.customAttributeItems.splice(index,1);
	}

});	