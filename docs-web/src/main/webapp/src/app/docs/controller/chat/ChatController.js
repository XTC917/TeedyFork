'use strict';

angular.module('docs').controller('ChatController', function($scope, $rootScope, $stateParams, Restangular, $interval, $state) {
    var messagePollingInterval;
    let prevCount = 0;
    $scope.messages = [];
    $scope.newMessage = '';
    $scope.error = null;
    $scope.loading = true;
    $scope.currentUser = $rootScope.userInfo;

    // 获取当前登录用户信息
    if (!$rootScope.userInfo) {
        console.error('User info not available');
        $scope.error = '请先登录';
        $state.go('login');
        return;
    }

    var currentUser = $rootScope.userInfo;
    var targetUsername = $stateParams.username;

    if (!targetUsername) {
        console.error('Target username is not provided in route parameters');
        $scope.error = '未指定聊天对象';
        return;
    }

    console.log('ChatController initialized with:');
    console.log('Current User:', {
        username: currentUser.username,
        email: currentUser.email
    });
    console.log('Target Username:', targetUsername);

    // 加载目标用户信息
    function loadTargetUser() {
        if (!targetUsername) {
            console.error('Cannot load target user - username is undefined');
            return;
        }

        console.log('Loading target user information for username:', targetUsername);
        return Restangular.one('user', targetUsername).get()
            .then(function(user) {
                if (!user || !user.username) {
                    throw new Error('Invalid user data received');
                }
                console.log('Target user loaded successfully:', {
                    username: user.username,
                    email: user.email
                });
                $scope.targetUser = user;
                // 在获取到目标用户信息后开始轮询消息
                startMessagePolling();
            })
            .catch(function(error) {
                console.error('Error loading target user:', error);
                $scope.error = '加载用户信息失败';
            });
    }

    // 加载消息
    function loadMessages() {
        if (!$scope.targetUser || !$scope.targetUser.username) {
            console.log('Skipping message load - target user not loaded yet');
            return;
        }

        if ($scope.targetUser.username === currentUser.username) {
            console.error('Cannot load messages - target user is the same as current user');
            $scope.error = '不能与自己聊天';
            return;
        }

        console.log('Loading messages between users:', {
            username1: currentUser.username,
            username2: $scope.targetUser.username
        });

        $scope.loading = false;
        Restangular.one('chat/messages').get({
            username1: currentUser.username,
            username2: $scope.targetUser.username
        })
        .then(function(data) {
            if (data.chatMessages.length > prevCount) {
                const newMessages = data.chatMessages.slice(prevCount);
                $scope.messages.push(...newMessages);
                prevCount = data.chatMessages.length
            }
            scrollToBottom();
        })
        .catch(function(error) {
            console.error('Error loading messages:', {
                error: error,
                status: error.status,
                data: error.data,
                config: error.config
            });
            $scope.error = '加载消息失败';
        })
        .finally(function() {
        });
    }

    // 开始轮询消息
    function startMessagePolling() {
        console.log('Starting message polling');
        loadMessages(); // 立即加载一次
        messagePollingInterval = $interval(loadMessages, 3000); // 每3秒轮询一次
    }

    // 发送消息
    $scope.sendMessage = function() {
        if (!$scope.newMessage.trim() || !$scope.targetUser || !$scope.targetUser.username) {
            console.log('Message send skipped:', {
                hasContent: !!$scope.newMessage.trim(),
                hasTargetUser: !!$scope.targetUser,
                targetUsername: $scope.targetUser ? $scope.targetUser.username : null
            });
            return;
        }

        if ($scope.targetUser.username === currentUser.username) {
            console.error('Cannot send message - target user is the same as current user');
            $scope.error = '不能给自己发送消息';
            return;
        }

        var message = {
            senderUsername: currentUser.username,
            receiverUsername: $scope.targetUser.username,
            content: $scope.newMessage.trim()
        };

        console.log('Sending message:', message);

        Restangular.one('chat')
            .post('send', message)
            .then(function(response) {
                console.log('Message sent successfully:', response);
                $scope.newMessage = '';
                $scope.error = null;
                loadMessages(); // 立即刷新消息列表
                scrollToBottom();
            })
            .catch(function(error) {
                console.error('Error sending message:', {
                    error: error,
                    status: error.status,
                    data: error.data,
                    config: error.config
                });
                $scope.error = '发送消息失败';
            });
    };
    
    // 删除消息
    $scope.deleteMessage = function(messageId) {
        if (!messageId) {
            console.error('Cannot delete message - messageId is undefined');
            return;
        }
        
        console.log('Deleting message with ID:', messageId);
        
        Restangular.one('chat/delete', messageId)
            .remove({ username: currentUser.username })
            .then(function(response) {
                console.log('Message deleted successfully:', response);
                
                // 从本地消息列表中移除已删除的消息
                $scope.messages = $scope.messages.filter(function(message) {
                    return message.id !== messageId;
                });
                
                // 如果删除后没有消息，减少预计消息数
                prevCount = $scope.messages.length;
                
                $scope.error = null;
            })
            .catch(function(error) {
                console.error('Error deleting message:', {
                    error: error,
                    status: error.status,
                    data: error.data,
                    config: error.config
                });
                
                // 显示错误消息
                if (error.data && error.data.message) {
                    $scope.error = error.data.message;
                } else {
                    $scope.error = '删除消息失败';
                }
            });
    };

    // 滚动到底部
    function scrollToBottom() {
        var chatMessages = document.getElementById('chatMessages');
        if (chatMessages) {
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }
    }

    // 回车发送消息
    $scope.onKeyPress = function($event) {
        if ($event.keyCode === 13 && !$event.shiftKey) {
            $event.preventDefault();
            $scope.sendMessage();
        }
    };

    // 初始化
    loadTargetUser();

    // 清理
    $scope.$on('$destroy', function() {
        console.log('ChatController destroyed, cleaning up...');
        if (messagePollingInterval) {
            $interval.cancel(messagePollingInterval);
        }
    });
}); 