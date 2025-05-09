'use strict';

/**
 * Chat list controller.
 */
angular.module('docs').controller('ChatListController', function($scope, $state, Restangular) {
    console.log('ChatListController initialized');
    
    $scope.users = [];
    $scope.loading = true;
    $scope.error = null;
    $scope.searchUser = '';

    // 加载用户列表
    function loadUsers() {
        console.log('Loading user list');
        $scope.loading = true;
        $scope.error = null;

        Restangular.one('user/list').get({ sort_column: 1, asc: true })
            .then(function(response) {
                console.log('User list loaded:', response);
                $scope.users = response.users;
                $scope.loading = false;
            })
            .catch(function(error) {
                console.error('Error loading user list:', error);
                $scope.error = '加载用户列表失败';
                $scope.loading = false;
            });
    }

    // 开始聊天
    $scope.startChat = function(username) {
        console.log('Starting chat with user:', username);
        $state.go('chat.with', { userId: username });
    };

    // 初始化加载
    loadUsers();
}); 