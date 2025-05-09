<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="header.jsp" %>

<div class="container">
    <div class="row">
        <div class="col-md-4">
            <div class="card">
                <div class="card-header">
                    <h5>在线用户</h5>
                </div>
                <div class="card-body">
                    <ul class="list-group" id="userList">
                        <!-- 用户列表将通过JavaScript动态添加 -->
                    </ul>
                </div>
            </div>
        </div>
        <div class="col-md-8">
            <div class="card">
                <div class="card-header">
                    <h5>聊天窗口</h5>
                </div>
                <div class="card-body">
                    <div id="chatMessages" style="height: 400px; overflow-y: auto;">
                        <!-- 消息将通过JavaScript动态添加 -->
                    </div>
                    <div class="input-group mt-3">
                        <input type="text" id="messageInput" class="form-control" placeholder="输入消息...">
                        <div class="input-group-append">
                            <button class="btn btn-primary" id="sendButton">发送</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.0/sockjs.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
<script>
let stompClient = null;
let currentUser = '${sessionScope.user.id}';
let selectedUser = null;

function connect() {
    let socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        
        // 订阅个人消息
        stompClient.subscribe('/user/queue/private', function(message) {
            showMessage(JSON.parse(message.body));
        });
        
        // 订阅用户列表更新
        stompClient.subscribe('/topic/users', function(message) {
            updateUserList(JSON.parse(message.body));
        });
        
        // 获取历史消息
        stompClient.send("/app/chat.history", {}, currentUser);
    });
}

function sendMessage() {
    if (!selectedUser) {
        alert('请先选择一个用户');
        return;
    }
    
    let messageInput = document.getElementById('messageInput');
    let content = messageInput.value.trim();
    
    if (content) {
        stompClient.send("/app/chat.send", {}, JSON.stringify({
            'senderId': currentUser,
            'receiverId': selectedUser,
            'content': content
        }));
        messageInput.value = '';
    }
}

function showMessage(message) {
    let chatMessages = document.getElementById('chatMessages');
    let messageElement = document.createElement('div');
    messageElement.className = 'message ' + (message.senderId === currentUser ? 'sent' : 'received');
    messageElement.innerHTML = `
        <div class="message-content">
            <strong>${message.senderId === currentUser ? '我' : '对方'}</strong>
            <p>${message.content}</p>
            <small>${new Date(message.createDate).toLocaleString()}</small>
        </div>
    `;
    chatMessages.appendChild(messageElement);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function updateUserList(users) {
    let userList = document.getElementById('userList');
    userList.innerHTML = '';
    users.forEach(user => {
        if (user.id !== currentUser) {
            let li = document.createElement('li');
            li.className = 'list-group-item user-item';
            li.innerHTML = user.username;
            li.onclick = () => selectUser(user.id);
            userList.appendChild(li);
        }
    });
}

function selectUser(userId) {
    selectedUser = userId;
    document.getElementById('chatMessages').innerHTML = '';
    stompClient.send("/app/chat.history", {}, currentUser);
}

// 连接WebSocket
connect();

// 绑定发送按钮事件
document.getElementById('sendButton').onclick = sendMessage;
document.getElementById('messageInput').onkeypress = function(e) {
    if (e.key === 'Enter') {
        sendMessage();
    }
};
</script>

<style>
.message {
    margin: 10px;
    padding: 10px;
    border-radius: 5px;
    max-width: 70%;
}

.sent {
    background-color: #007bff;
    color: white;
    margin-left: auto;
}

.received {
    background-color: #f8f9fa;
    margin-right: auto;
}

.user-item {
    cursor: pointer;
}

.user-item:hover {
    background-color: #f8f9fa;
}

.user-item.active {
    background-color: #007bff;
    color: white;
}
</style>

<%@ include file="footer.jsp" %> 