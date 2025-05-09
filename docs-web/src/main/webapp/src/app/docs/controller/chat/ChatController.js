'use strict';

angular.module('docs').controller('ChatController', function($scope, $stateParams, Restangular, $timeout) {
    console.log('ChatController initialized with userId:', $stateParams.userId);
    
    $scope.currentUser = null;
    $scope.targetUser = null;
    $scope.messages = [];
    $scope.newMessage = '';

    // 滚动到底部
    function scrollToBottom() {
        $timeout(function() {
            var chatMessages = document.getElementById('chatMessages');
            if (chatMessages) {
                chatMessages.scrollTop = chatMessages.scrollHeight;
            }
        });
    }

    // 获取当前用户信息
    Restangular.one('user', 'me').get().then(function(user) {
        console.log('Current user loaded:', user);
        $scope.currentUser = user;
    }, function(error) {
        console.error('Error loading current user:', error);
    });

    // 获取目标用户信息
    Restangular.one('user', $stateParams.userId).get().then(function(user) {
        console.log('Target user loaded:', user);
        $scope.targetUser = user;
        loadMessages();
    }, function(error) {
        console.error('Error loading target user:', error);
    });

    // 加载历史消息
    function loadMessages() {
        console.log('Loading messages for user:', $stateParams.userId);
        Restangular.one('chat/messages', $stateParams.userId).get().then(function(response) {
            console.log('Messages loaded:', response);
            if (Array.isArray(response)) {
                $scope.messages = response;
            } else if (response.messages) {
                $scope.messages = response.messages;
            } else {
                $scope.messages = [];
            }
            scrollToBottom();
        }, function(error) {
            console.error('Error loading messages:', error);
            $scope.messages = [];
        });
    }

    // 发送消息
    $scope.sendMessage = function() {
        if (!$scope.newMessage.trim()) return;

        var messageContent = $scope.newMessage.trim();
        console.log('Attempting to send message:', {
            to: $stateParams.userId,
            content: messageContent,
            currentUser: $scope.currentUser
        });

        // 创建临时消息对象
        var tempMessage = {
            content: messageContent,
            senderUsername: $scope.currentUser ? $scope.currentUser.username : 'unknown',
            timestamp: new Date().toISOString()
        };

        // 立即显示消息
        $scope.messages.push(tempMessage);
        scrollToBottom();

        // 清空输入框
        $scope.newMessage = '';

        // 发送到服务器
        var messageData = {
            receiverUsername: $stateParams.userId,
            content: messageContent
        };

        console.log('Sending message data:', messageData);

        Restangular.all('chat/send').post(messageData)
            .then(function(response) {
                console.log('Message sent successfully:', response);
                // 重新加载消息以确保消息状态正确
                loadMessages();
            })
            .catch(function(error) {
                console.error('Error sending message:', error);
                // 如果发送失败，从消息列表中移除临时消息
                var index = $scope.messages.indexOf(tempMessage);
                if (index > -1) {
                    $scope.messages.splice(index, 1);
                }
                alert('发送消息失败: ' + (error.data ? error.data.message : '未知错误'));
            });
    };

    // 按回车发送消息
    $scope.onKeyPress = function(event) {
        if (event.keyCode === 13 && !event.shiftKey) {
            event.preventDefault();
            $scope.sendMessage();
        }
    };

    // 监听消息数组变化
    $scope.$watch('messages', function() {
        scrollToBottom();
    }, true);

    // 定期刷新消息
    var messageRefreshInterval = $timeout(function refreshMessages() {
        if ($scope.currentUser && $scope.targetUser) {
            loadMessages();
        }
        messageRefreshInterval = $timeout(refreshMessages, 5000); // 每5秒刷新一次
    }, 5000);

    // 清理定时器
    $scope.$on('$destroy', function() {
        if (messageRefreshInterval) {
            $timeout.cancel(messageRefreshInterval);
        }
    });
}); 