'use strict';

/**
 * Register request controller.
 */
angular.module('docs').controller('RegisterRequest', function($scope, $state, $dialog, UserRequest, $translate) {
  $scope.userRequest = {};
  
  // Submit the request
  $scope.submit = function() {
    // Validate password length
    if ($scope.userRequest.password.length < 8) {
      var title = $translate.instant('registerrequest.error_title');
      var msg = $translate.instant('registerrequest.error_password_length');
      var btns = [{result: 'ok', label: 'OK', cssClass: 'btn-primary'}];
      $dialog.messageBox(title, msg, btns);
      return;
    }

    // Validate passwords match
    if ($scope.userRequest.password !== $scope.userRequest.passwordConfirm) {
      var title = $translate.instant('registerrequest.error_title');
      var msg = $translate.instant('registerrequest.error_password_mismatch');
      var btns = [{result: 'ok', label: 'OK', cssClass: 'btn-primary'}];
      $dialog.messageBox(title, msg, btns);
      return;
    }

    UserRequest.create($scope.userRequest).then(function() {
      var title = $translate.instant('registerrequest.success_title');
      var msg = $translate.instant('registerrequest.success_message');
      var btns = [{result: 'ok', label: 'OK', cssClass: 'btn-primary'}];
      
      $dialog.messageBox(title, msg, btns).open().then(function() {
        $state.go('login');
      });
    }, function(data) {
      if (data.data.type === 'AlreadyExistingUsername') {
        var title = $translate.instant('registerrequest.error_title');
        var msg = $translate.instant('registerrequest.error_username_exists');
        var btns = [{result: 'ok', label: 'OK', cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      } else {
        var title = $translate.instant('registerrequest.error_title');
        var msg = $translate.instant('registerrequest.error_message');
        var btns = [{result: 'ok', label: 'OK', cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      }
    });
  };
}); 