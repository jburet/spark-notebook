'use strict';

// Declare app level module which depends on views, and components
angular.module('sparkNotebook', [
  'ngRoute',
  'sparkNotebook.notebook',
  'sparkNotebook.repository',
  'ui.ace'
]).
config(['$routeProvider', function($routeProvider) {
  $routeProvider.otherwise({redirectTo: '/repository'});
}]);
