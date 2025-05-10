'use strict';

angular.module('docs').controller('ChatController', function($scope, $stateParams, Restangular, $timeout, $interval, $state) {
    console.log('ChatController initialized with userId:', $stateParams.userId);
    
    $scope.currentUser = null;
    $scope.targetUser = null;
    $scope.messages = [];
    $scope.newMessage = '';
    $scope.error = null;
    let messagePollingInterval = null;

    // 检查认证状态
    function checkAuth() {
        var cookies = document.cookie.split(';');
        for (var i = 0; i < cookies.length; i++) {
            var cookie = cookies[i].trim();
            if (cookie.startsWith('auth_token=')) {
                return true;
            }
        }
        return false;
    }

    // 加载用户信息
    function loadCurrentUser() {
        if (!checkAuth()) {
            console.log('No auth token found, redirecting to login');
            $state.go('login', {
                redirectState: 'chat.with',
                redirectParams: JSON.stringify({ userId: $stateParams.userId })
            });
            return;
        }

        console.log('Attempting to load current user...');
        // 使用正确的API路径获取当前用户
        Restangular.one('user/list').get({ sort_column: 1, asc: true }).then(
            function(response) {
                console.log('User list loaded:', response);
                // 假设第一个用户是当前用户
                if (response.users && response.users.length > 0) {
                    $scope.currentUser = response.users[0];
                    console.log('Current user loaded successfully:', $scope.currentUser);
                    loadTargetUser();
                } else {
                    $scope.error = '无法获取当前用户信息';
                }
            },
            function(error) {
                console.error('Error loading current user:', error);
                console.log('Error status:', error.status);
                console.log('Error data:', error.data);
                
                if (error.status === 401 || error.status === 403) {
                    console.log('Authentication error, redirecting to login');
                    $state.go('login', {
                        redirectState: 'chat.with',
                        redirectParams: JSON.stringify({ userId: $stateParams.userId })
                    });
                } else {
                    $scope.error = '加载用户信息失败，请刷新页面重试';
                }
            }
        );
    }

    // 加载目标用户信息
    function loadTargetUser() {
        console.log('Loading target user:', $stateParams.userId);
        Restangular.one('user/list').get({ sort_column: 1, asc: true }).then(
            function(response) {
                console.log('User list loaded:', response);
                // 在用户列表中查找目标用户
                const targetUser = response.users.find(user => user.username === $stateParams.userId);
                if (targetUser) {
                    console.log('Target user loaded successfully:', targetUser);
                    $scope.targetUser = targetUser;
                    startMessagePolling();
                } else {
                    $scope.error = '目标用户不存在';
                }
            },
            function(error) {
                console.error('Error loading target user:', error);
                console.log('Error status:', error.status);
                console.log('Error data:', error.data);
                $scope.error = '加载目标用户信息失败';
            }
        );
    }

    // 开始轮询消息
    function startMessagePolling() {
        console.log('Starting message polling...');
        // 立即加载一次消息
        loadMessages();
        
        // 每3秒轮询一次新消息
        messagePollingInterval = $interval(loadMessages, 3000);
        
        // 当控制器销毁时停止轮询
        $scope.$on('$destroy', function() {
            if (messagePollingInterval) {
                $interval.cancel(messagePollingInterval);
            }
        });
    }

    // 加载消息
    function loadMessages() {
        if (!$scope.currentUser || !$scope.targetUser) {
            console.log('Cannot load messages: user information not complete');
            return;
        }
        
        console.log('Loading messages for user:', $scope.currentUser.username);
        Restangular.one('chat').one('messages', $scope.currentUser.username).get().then(
            function(messages) {
                console.log('Messages loaded successfully:', messages);
                $scope.messages = messages;
                $scope.error = null;
            },
            function(error) {
                console.error('Error loading messages:', error);
                console.log('Error status:', error.status);
                console.log('Error data:', error.data);
                $scope.error = '加载消息失败，请刷新页面重试';
            }
        );
    }

    // 发送消息
    $scope.sendMessage = function() {
        if (!$scope.newMessage.trim()) return;
        if (!$scope.currentUser || !$scope.targetUser) {
            $scope.error = '用户信息未加载完成，请稍后重试';
            return;
        }
        
        console.log('Sending message to:', $scope.targetUser.username);
        const messageData = {
            senderId: $scope.currentUser.username,
            receiverId: $scope.targetUser.username,
            content: $scope.newMessage
        };
        
        Restangular.all('chat/send').post(messageData).then(
            function(response) {
                console.log('Message sent successfully:', response);
                $scope.newMessage = '';
                $scope.error = null;
                loadMessages(); // 立即刷新消息列表
            },
            function(error) {
                console.error('Error sending message:', error);
                console.log('Error status:', error.status);
                console.log('Error data:', error.data);
                $scope.error = '发送消息失败，请重试';
            }
        );
    };

    // 处理回车键发送
    $scope.onKeyPress = function(event) {
        if (event.keyCode === 13 && !event.shiftKey) {
            event.preventDefault();
            $scope.sendMessage();
        }
    };

    // 初始化时加载当前用户
    loadCurrentUser();
}); 