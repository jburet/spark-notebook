'use strict';

angular.module('sparkNotebook.repository', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/repository', {
    templateUrl: 'repository/repository.html',
    controller: 'RepositoryCtrl'
  });
}])

.controller('RepositoryCtrl', ['$scope', '$http', '$location', function($scope, $http, $location) {
  var base_url = "/api-v1"

  $scope.content = []

  $scope.init = function() {
    $http.get(base_url+'/notebooks').
    success(function(data) {
      console.log(data.names)
      $scope.content = data.names
    });
  }

  $scope.loadNotebook = function(id){
    $http.get(base_url+'/notebook/'+id).
    success(function(data) {
      $location.path('/notebook').search("id", id);
    });
  }

  $scope.newNotebook = function() {
    $http.post(base_url+'/notebook').
    success(function(data) {
      console.log("notebook created: "+data)
      $location.path('/notebook').search("id", data);
    });
  }
}]);
