'use strict';

angular.module('sparkNotebook.notebook', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/notebook', {
    templateUrl: 'notebook/notebook.html',
    controller: 'NotebookCtrl'
  });
}])

.controller('NotebookCtrl', ['$scope', '$http', '$location', function($scope, $http, $location) {

  $scope.id = ''

  $scope.init = function() {
    $scope.id = $location.search().id
  	$http.get('http://localhost:8080/notebook/'+$scope.id).
		success(function(data) {
			$scope.content = data.content
      $scope.result = data.result
		});
  }

	$scope.save = function() {
		$http.put('http://localhost:8080/notebook/'+$scope.id, $scope.content).
			success(function(data) {

			});
	};

	$scope.play = function(content) {
		var data = {content: $scope.content, result: ""}
		$http.put('http://localhost:8080/notebook/'+$scope.id+'', data)
			.success(function() {$http.post('http://localhost:8080/notebook/'+$scope.id+'/job').
				success(function(data) {

				})
			})
	}

  $scope.refresh = function() {
	$http.get('http://localhost:8080/notebook/'+$scope.id).
		success(function(data) {
			$scope.result = data.result
		});
  };
}]);
