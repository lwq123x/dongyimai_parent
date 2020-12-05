app.service('itemSearchService',function ($http) {
    this.search=function (searchMap) {
        return $http.post('../itemSearch/search.do',searchMap);
    }
})