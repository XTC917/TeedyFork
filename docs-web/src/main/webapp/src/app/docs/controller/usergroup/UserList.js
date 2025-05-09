'use strict';

/**
 * User list controller.
 */
angular.module('docs').controller('UserList', function($scope, Restangular, $state) {
  $scope.users = [];
  $scope.loading = true;
  $scope.error = null;

  // Load users
  Restangular.all('user').getList().then(function(users) {
    $scope.users = users;
    $scope.loading = false;
  }, function(error) {
    console.error('Error loading users:', error);
    $scope.error = '加载用户列表失败';
    $scope.loading = false;
  });

  // 跳转到聊天页面
  $scope.startChat = function(user) {
    if (user && user.id) {
      $state.go('chat', { userId: user.id });
    }
  };
}); 