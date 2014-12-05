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
  $scope.newContent=''
  $scope.notebook = undefined

  $scope.init = function() {
    $scope.id = $location.search().id
  	$http.get('http://localhost:8080/notebook/'+$scope.id).
		success(function(data) {
      $scope.notebook = data
		});
  }

	$scope.save = function(callback) {
    // Check if new content
    if($scope.newContent.length > 0){
      // Add to paragraphs object an clean 'new data'
      $scope.notebook.paragraphs.push({content: $scope.newContent, result:'', data:''})
      $scope.newContent = ''
    }

    var pdata = []
    angular.forEach($scope.notebook.paragraphs, function(p){
      pdata.push(p.content)
    })
		$http.put('http://localhost:8080/notebook/'+$scope.id, pdata).
			success(function(data) {
        callback(data)
			});
	};

	$scope.play = function() {
		$scope.save(function() {$http.post('http://localhost:8080/notebook/'+$scope.id+'/job').
      success(function(data) {

      })
    })
	}

  $scope.refresh = function() {
	$http.get('http://localhost:8080/notebook/'+$scope.id).
		success(function(data) {
			$scope.notebook = data
		});
  };

  $scope.aceLoaded = function(_editor) {
    // Options
    _editor.setAutoScrollEditorIntoView(true);
    _editor.setBehavioursEnabled(false)
    _editor.setOption("maxLines", 20);
    _editor.setOption("minLines", 4);
  }

  $scope.displayRes = function(p){
    return p.result.length === 0
  }

  $scope.displayData = function(p){
    return p.data.length === 0
  }

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
