let stompClient = null;
let currentUserId = null;
let selectedConversationId = null;
let config = {};

function initChatPage(appConfig) {
    console.log("Initializing Chat Page with config:", appConfig);
    config = appConfig;
    currentUserId = config.currentUserId;

    if (!currentUserId) {
        console.error("User not logged in. Chat cannot be initialized.");
        document.getElementById('conversation-list').innerHTML = '<div class="alert alert-danger">Vui lòng đăng nhập để xem tin nhắn.</div>';
        return;
    }

    fetchConversations();
    connectWebSocket();

    const sendButton = document.getElementById('send-chat-message-btn');
    const messageInput = document.getElementById('chat-message-input');

    sendButton.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
}

function fetchConversations() {
    console.log("Fetching conversations from:", config.conversationsApiUrl);
    fetch(config.conversationsApiUrl, {
        headers: {
            [config.csrfHeader]: config.csrfToken,
            'Accept': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(conversations => {
        console.log("Conversations received:", conversations);
        const listElement = document.getElementById('conversation-list');
        listElement.innerHTML = ''; // Clear spinner

        if (conversations.length === 0) {
            listElement.innerHTML = '<div class="text-center p-3 text-muted">Chưa có cuộc trò chuyện nào.</div>';
            return;
        }

        conversations.forEach(conv => {
            const otherParty = conv.user.id === currentUserId ? conv.shop : conv.user;
            const avatar = conv.user.id === currentUserId ? (conv.shop.logoUrl || `https://ui-avatars.com/api/?name=${otherParty.name}&background=random`) : (otherParty.avatarUrl || `https://ui-avatars.com/api/?name=${otherParty.name}&background=random`);
            const name = otherParty.name;

            const convElement = document.createElement('a');
            convElement.href = '#';
            convElement.className = 'list-group-item list-group-item-action';
            convElement.dataset.conversationId = conv.id;
            convElement.innerHTML = `
                <div class="d-flex w-100 justify-content-start align-items-center">
                    <img src="${avatar}" class="rounded-circle me-3" width="50" height="50" alt="avatar">
                    <div class="flex-grow-1">
                        <h6 class="mb-1">${name}</h6>
                        <small class="text-muted">Click to view messages</small>
                    </div>
                </div>`;

            convElement.addEventListener('click', (e) => {
                e.preventDefault();
                openConversation(conv.id, name, avatar);
            });

            listElement.appendChild(convElement);
        });
    })
    .catch(error => {
        console.error('Failed to fetch conversations:', error);
        document.getElementById('conversation-list').innerHTML = '<div class="alert alert-danger">Không thể tải danh sách cuộc trò chuyện.</div>';
    });
}

function openConversation(conversationId, name, avatar) {
    console.log(`Opening conversation ${conversationId}`);
    selectedConversationId = conversationId;

    document.getElementById('chat-welcome-screen').classList.add('d-none');
    const mainScreen = document.getElementById('chat-main-screen');
    mainScreen.classList.remove('d-none');

    document.querySelectorAll('#conversation-list .list-group-item').forEach(item => {
        item.classList.remove('active');
    });
    document.querySelector(`#conversation-list [data-conversation-id='${conversationId}']`).classList.add('active');


    document.getElementById('chat-header-name').textContent = name;
    document.getElementById('chat-header-avatar').src = avatar;

    const messagesContainer = document.getElementById('chat-messages-container');
    messagesContainer.innerHTML = '<div class="text-center p-5"><div class="spinner-border" role="status"><span class="visually-hidden">Loading messages...</span></div></div>';

    // Fetch messages for this conversation
    fetch(`${config.contextPath}api/chat/messages?conversationId=${conversationId}`, {
         headers: {
            [config.csrfHeader]: config.csrfToken,
            'Accept': 'application/json'
        }
    })
    .then(response => response.json())
    .then(messages => {
        messagesContainer.innerHTML = '';
        messages.forEach(addMessageToChatWindow);
    })
    .catch(error => {
        console.error('Failed to fetch messages:', error);
        messagesContainer.innerHTML = '<div class="alert alert-danger">Failed to load messages.</div>';
    });
}

function addMessageToChatWindow(message) {
    const messagesContainer = document.getElementById('chat-messages-container');
    const messageElement = document.createElement('div');
    const isSender = message.sender.id === currentUserId;

    messageElement.className = `chat-message ${isSender ? 'sent' : 'received'}`;

    const bubble = document.createElement('div');
    bubble.className = 'message-bubble';
    bubble.textContent = message.content;

    const timestamp = document.createElement('div');
    timestamp.className = 'message-timestamp';
    timestamp.textContent = new Date(message.createdAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });

    messageElement.appendChild(bubble);
    messageElement.appendChild(timestamp);
    messagesContainer.appendChild(messageElement);

    // Scroll to the bottom
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function connectWebSocket() {
    const socket = new SockJS(`${config.contextPath}ws`);
    stompClient = Stomp.over(socket);

    const headers = {
        [config.csrfHeader]: config.csrfToken
    };

    stompClient.connect(headers, function(frame) {
        console.log('Connected to WebSocket: ' + frame);
        // Subscribe to user-specific topic for private messages
        stompClient.subscribe(`/user/${currentUserId}/queue/messages`, function(message) {
            const receivedMessage = JSON.parse(message.body);
            console.log("Message received:", receivedMessage);

            // If the message belongs to the currently open conversation, display it
            if (receivedMessage.conversationId === selectedConversationId) {
                addMessageToChatWindow(receivedMessage);
            }
            // TODO: Add notification for messages in other conversations
        });
    }, function(error) {
        console.error('WebSocket connection error:', error);
        // TODO: Implement reconnection logic
    });
}

function sendMessage() {
    const messageInput = document.getElementById('chat-message-input');
    const messageContent = messageInput.value.trim();

    if (messageContent && stompClient && selectedConversationId) {
        const chatMessage = {
            conversationId: selectedConversationId,
            senderId: currentUserId,
            content: messageContent,
            // senderType can be determined server-side if needed
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));

        // Optimistically add the message to the UI
        const optimisticMessage = {
            sender: { id: currentUserId },
            content: messageContent,
            createdAt: new Date().toISOString(),
        };
        addMessageToChatWindow(optimisticMessage);

        messageInput.value = '';
    }
}
