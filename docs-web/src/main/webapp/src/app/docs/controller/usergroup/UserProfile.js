'use strict';

/**
 * User profile controller.
 */
angular.module('docs').controller('UserProfile', function($stateParams, Restangular, $scope, $state) {
  // Load user
  Restangular.one('user', $stateParams.username).get().then(function(data) {
    $scope.user = data;
  }, function(error) {
    console.error('Error loading user:', error);
  });

  // 跳转到聊天页面
  $scope.startChat = function() {
    if ($scope.user && $scope.user.username) {
      $state.go('chat.with', { userId: $scope.user.username });
    }
  };
});