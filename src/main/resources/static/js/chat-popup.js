let stompClient = null;
let config = {};
let chatWindowState = {}; // To store state for multiple chat windows

function initChatPopup(appConfig) {
    config = appConfig;
    if (!config.currentUserId) {
        console.error("Chat cannot be initialized without a user.");
        return;
    }

    const contactButton = document.getElementById(config.contactButtonId);
    if (contactButton) {
        contactButton.addEventListener('click', () => {
            const shopId = contactButton.dataset.shopId;
            const shopName = contactButton.dataset.shopName;
            openChatPopup(shopId, shopName);
        });
    }
    connectWebSocket();
}

function openChatPopup(shopId, shopName) {
    if (chatWindowState[shopId] && chatWindowState[shopId].popup) {
        chatWindowState[shopId].popup.focus();
        return;
    }

    const popup = createPopupElement(shopId, shopName);
    document.body.appendChild(popup);

    chatWindowState[shopId] = { popup: popup, conversationId: null, messagesLoaded: false };

    findOrCreateConversation(shopId).then(conversationId => {
        chatWindowState[shopId].conversationId = conversationId;
        loadMessages(shopId, conversationId);
    });
}

function createPopupElement(shopId, shopName) {
    const popupId = `chat-popup-${shopId}`;
    const existingPopup = document.getElementById(popupId);
    if (existingPopup) {
        return existingPopup;
    }

    const popup = document.createElement('div');
    popup.id = popupId;
    popup.className = 'chat-popup';
    popup.innerHTML = `
        <div class="chat-popup-header">
            <h6>${shopName}</h6>
            <button class="close-btn">&times;</button>
        </div>
        <div class="chat-popup-messages"></div>
        <div class="chat-popup-input">
            <input type="text" placeholder="Type a message...">
            <button><i class="bi bi-send-fill"></i></button>
        </div>
    `;

    const closeButton = popup.querySelector('.close-btn');
    closeButton.addEventListener('click', () => popup.remove());

    const sendButton = popup.querySelector('.chat-popup-input button');
    const messageInput = popup.querySelector('.chat-popup-input input');

    sendButton.addEventListener('click', () => sendMessage(shopId, messageInput));
    messageInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage(shopId, messageInput);
    });

    return popup;
}

async function findOrCreateConversation(shopId) {
    const url = `${config.contextPath}api/chat/find-or-create`;
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [config.csrfHeader]: config.csrfToken,
        },
        body: JSON.stringify({ shopId: shopId, userId: config.currentUserId })
    });
    if (!response.ok) throw new Error('Failed to find or create conversation');
    const conversation = await response.json();
    return conversation.id;
}

async function loadMessages(shopId, conversationId) {
    const popup = chatWindowState[shopId].popup;
    const messagesContainer = popup.querySelector('.chat-popup-messages');
    messagesContainer.innerHTML = '<div class="spinner"></div>'; // Show loader

    const url = `${config.contextPath}api/chat/messages?conversationId=${conversationId}`;
    const response = await fetch(url, {
         headers: { [config.csrfHeader]: config.csrfToken }
    });
    if (!response.ok) {
        messagesContainer.innerHTML = '<p class="error">Failed to load messages.</p>';
        return;
    }
    const messages = await response.json();
    messagesContainer.innerHTML = ''; // Clear loader
    messages.forEach(msg => addMessageToPopup(shopId, msg));
    chatWindowState[shopId].messagesLoaded = true;
}

function addMessageToPopup(shopId, message) {
    const popup = chatWindowState[shopId].popup;
    if (!popup) return;

    const messagesContainer = popup.querySelector('.chat-popup-messages');
    const msgElement = document.createElement('div');
    msgElement.className = `message ${message.sender.id === config.currentUserId ? 'sent' : 'received'}`;
    msgElement.textContent = message.content;
    messagesContainer.appendChild(msgElement);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function connectWebSocket() {
    if (stompClient) return;
    const socket = new SockJS(`${config.contextPath}ws`);
    stompClient = Stomp.over(socket);
    const headers = { [config.csrfHeader]: config.csrfToken };

    stompClient.connect(headers, () => {
        console.log('WebSocket connected for popup chat.');
        stompClient.subscribe(`/user/${config.currentUserId}/queue/messages`, (message) => {
            const receivedMessage = JSON.parse(message.body);
            // Find the right popup to display the message
            const conversation = Object.values(chatWindowState).find(state => state.conversationId === receivedMessage.conversationId);
            if (conversation && conversation.popup) {
                addMessageToPopup(conversation.popup.id.split('-').pop(), receivedMessage);
            }
        });
    });
}

function sendMessage(shopId, inputElement) {
    const content = inputElement.value.trim();
    const conversationId = chatWindowState[shopId].conversationId;

    if (content && stompClient && conversationId) {
        const message = {
            conversationId: conversationId,
            senderId: config.currentUserId,
            content: content
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
        inputElement.value = '';

        // Optimistic update
        addMessageToPopup(shopId, { sender: { id: config.currentUserId }, content: content });
    }
}
