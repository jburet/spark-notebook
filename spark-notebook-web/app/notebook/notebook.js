'use strict';

angular.module('sparkNotebook.notebook', ['ngRoute', 'angular.atmosphere'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/notebook', {
    templateUrl: 'notebook/notebook.html',
    controller: 'NotebookCtrl'
  });
}])

.controller('NotebookCtrl', ['$scope', '$http', '$location', 'atmosphereService', function($scope, $http, $location, atmosphereService) {

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

  $scope.aceLoaded = function(_editor) {
    // Options
    _editor.setAutoScrollEditorIntoView(true);
    _editor.setOption("maxLines", 20);
    _editor.setOption("minLines", 4);
  };

  // Async
  $scope.model = {
    transport: 'websocket',
    messages: []
  };

  var socket;

  var request = {
    url: 'ws://localhost:8080/async/notebook-status',
    contentType: 'application/json',
    logLevel: 'debug',
    transport: 'websocket',
    trackMessageLength: false,
    reconnectInterval: 5000,
    enableXDR: true,
    timeout: 24 * 3600 * 60000
  };

  request.onClientTimeout = function(response){
    console.log("onClientTimeout, "+response)
    setTimeout(function(){
      socket = atmosphereService.subscribe(request);
    }, request.reconnectInterval);
  };

  request.onReopen = function(response){
    console.log("onReopen, "+response)
  };

  request.onMessage = function(response){
    switch (JSON.parse(response.responseBody).type) {
      case "job_complete":
        $scope.refresh()
        break;
    }
  };

  request.onClose = function(response){
    console.log('Closing socket connection for client ' + $rootScope.clientId);
  };

  request.onMessagePublished = function(response) {
    console.log("onMessagePublished, "+response)
  }

  request.onOpen = function(response){
    $scope.model.transport = response.transport;
    $scope.model.connected = true;
    $scope.model.content = 'Atmosphere connected using ' + response.transport;

    // Register for notebook event
    socket.push(JSON.stringify({type: "register_notebook", id: $scope.id}))
  };

  socket = atmosphereService.subscribe(request)

}]);
