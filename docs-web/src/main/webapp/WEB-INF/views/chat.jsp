<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="header.jsp" %>

<div class="container">
    <div class="row">
        <div class="col-md-4">
            <div class="card">
                <div class="card-header">
                    <h5>用户列表</h5>
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

<script>
let currentUser = '${sessionScope.user.username}';
let selectedUser = null;

// 加载用户列表
function loadUserList() {
    fetch('/api/users')
        .then(response => response.json())
        .then(users => {
            let userList = document.getElementById('userList');
            userList.innerHTML = '';
            users.forEach(user => {
                if (user.username !== currentUser) {
                    let li = document.createElement('li');
                    li.className = 'list-group-item user-item';
                    li.innerHTML = user.username;
                    li.onclick = () => selectUser(user.username);
                    userList.appendChild(li);
                }
            });
        })
        .catch(error => console.error('Error loading users:', error));
}

// 加载消息
function loadMessages() {
    if (!selectedUser) return;
    
    fetch(`/api/chat/messages?username1=${currentUser}&username2=${selectedUser}`)
        .then(response => response.json())
        .then(messages => {
            let chatMessages = document.getElementById('chatMessages');
            chatMessages.innerHTML = '';
            messages.forEach(message => showMessage(message));
        })
        .catch(error => console.error('Error loading messages:', error));
}

// 发送消息
function sendMessage() {
    if (!selectedUser) {
        alert('请先选择一个用户');
        return;
    }
    
    let messageInput = document.getElementById('messageInput');
    let content = messageInput.value.trim();
    
    if (content) {
        fetch('/api/chat/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                senderUsername: currentUser,
                receiverUsername: selectedUser,
                content: content
            })
        })
        .then(response => response.json())
        .then(() => {
        messageInput.value = '';
            loadMessages(); // 重新加载消息
        })
        .catch(error => console.error('Error sending message:', error));
    }
}

function showMessage(message) {
    let chatMessages = document.getElementById('chatMessages');
    let messageElement = document.createElement('div');
    messageElement.className = 'message ' + (message.senderUsername === currentUser ? 'sent' : 'received');
    messageElement.innerHTML = `
        <div class="message-content">
            <strong>${message.senderUsername === currentUser ? '我' : '对方'}</strong>
            <p>${message.content}</p>
            <small>${new Date(message.createDate).toLocaleString()}</small>
        </div>
    `;
    chatMessages.appendChild(messageElement);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function selectUser(username) {
    selectedUser = username;
    document.getElementById('chatMessages').innerHTML = '';
    loadMessages();
}

// 初始化
loadUserList();

// 定期刷新消息
setInterval(loadMessages, 3000);

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