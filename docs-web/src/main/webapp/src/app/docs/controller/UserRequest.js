'use strict';

/**
 * User request controller.
 */
angular.module('docs').controller('UserRequest', function($scope, $state, $dialog, UserRequest, $translate) {
  // Load user requests
  $scope.load = function() {
    UserRequest.list().then(function(data) {
      $scope.requests = data.requests;
    });
  };
  
  // Accept a request
  $scope.accept = function(request) {
    var title = $translate.instant('userrequest.accept_title');
    var msg = $translate.instant('userrequest.accept_message');
    var btns = [
      {result: 'cancel', label: $translate.instant('cancel')},
      {result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}
    ];
    
    $dialog.messageBox(title, msg, btns).open().then(function(result) {
      if (result === 'ok') {
        UserRequest.process(request.id, 'accept').then(function() {
          $scope.load();
        });
      }
    });
  };
  
  // Reject a request
  $scope.reject = function(request) {
    var title = $translate.instant('userrequest.reject_title');
    var msg = $translate.instant('userrequest.reject_message');
    var btns = [
      {result: 'cancel', label: $translate.instant('cancel')},
      {result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}
    ];
    
    $dialog.messageBox(title, msg, btns).open().then(function(result) {
      if (result === 'ok') {
        UserRequest.process(request.id, 'reject').then(function() {
          $scope.load();
        });
      }
    });
  };
  
  // Initial load
  $scope.load();
}); 