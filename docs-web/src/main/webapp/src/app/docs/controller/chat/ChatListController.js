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
                if (response && response.users) {
                    $scope.users = response.users;
                } else {
                    $scope.users = [];
                }
            })
            .catch(function(error) {
                console.error('Error loading user list:', error);
                $scope.error = '加载用户列表失败，请稍后重试';
            })
            .finally(function() {
                $scope.loading = false;
            });
    }

    // 开始聊天
    $scope.startChat = function(username) {
        if (!username) {
            $scope.error = '无效的用户名';
            return;
        }

        console.log('Starting chat with user:', username);
        $state.go('chat.with', { username: username });
    };

    // 刷新用户列表
    $scope.refreshUsers = function() {
        loadUsers();
    };

    // 初始化加载
    loadUsers();
}); 