'use strict';

/**
 * Notifications controller.
 */
angular.module('docs').controller('Notifications', function($scope, UserRequest, $translate, $dialog) {
  // Load pending requests
  $scope.loadRequests = function() {
    UserRequest.list().then(function(data) {
      $scope.requests = data.requests;
    });
  };
  
  // Process a request (accept/reject)
  $scope.processRequest = function(id, action) {
    var title = action === 'accept' ? 
      $translate.instant('userrequest.accept_title') : 
      $translate.instant('userrequest.reject_title');
    var msg = action === 'accept' ? 
      $translate.instant('userrequest.accept_message') : 
      $translate.instant('userrequest.reject_message');
    var btns = [
      {result: 'cancel', label: $translate.instant('cancel')},
      {result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}
    ];
    
    $dialog.messageBox(title, msg, btns, function(result) {
      if (result === 'ok') {
        UserRequest.process(id, action).then(function() {
          $scope.loadRequests();
        });
      }
    });
  };
  
  // Initial load
  $scope.loadRequests();
}); 