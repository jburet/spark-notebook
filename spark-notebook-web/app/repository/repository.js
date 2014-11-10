'use strict';

angular.module('sparkNotebook.repository', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/repository', {
    templateUrl: 'repository/repository.html',
    controller: 'RepositoryCtrl'
  });
}])

.controller('RepositoryCtrl', ['$scope', '$http', '$location', function($scope, $http, $location) {
  $scope.content = []

  $scope.init = function() {
    $http.get('http://localhost:8080/notebooks').
    success(function(data) {
      console.log(data.names)
      $scope.content = data.names
    });
  }

  $scope.loadNotebook = function(id){
    $http.get('http://localhost:8080/notebook/'+id).
    success(function(data) {
      $location.path('/notebook').search("id", id);
    });
  }

  $scope.newNotebook = function() {
    $http.post('http://localhost:8080/notebook').
    success(function(data) {
      console.log("notebook created: "+data)
      $location.path('/notebook').search("id", data);
    });
  }
}]);
