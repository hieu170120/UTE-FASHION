let stompClient = null;
let config = {};
let chatWindowState = {}; // To store state for multiple chat windows

// REVERTED: initChatPopup now takes all config, including currentUserId, directly from the view.
function initChatPopup(appConfig) {
    config = appConfig; // config contains contextPath, csrfHeader, csrfToken, and currentUserId

    if (!config.currentUserId) {
        console.log("Chat cannot be initialized. currentUserId is missing from config.");
        return;
    }

    const contactButton = document.getElementById(config.contactButtonId);
    if (contactButton) {
        contactButton.addEventListener('click', () => {
            const shopId = contactButton.dataset.shopId;
            const shopName = contactButton.dataset.shopName;
            openChatPopup(shopId, shopName);
			connectWebSocket();
        });
    }
/*    connectWebSocket();*/
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
    }).catch(error => {
        console.error("Error during chat initialization:", error);
        const messagesContainer = popup.querySelector('.chat-popup-messages');
        messagesContainer.innerHTML = `<p class="error">Error: Could not start chat. ${error.message}</p>`;
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
            <button><i class="fas fa-paper-plane"></i></button>
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
    const url = `${config.contextPath}api/chat/conversation/find-or-create?shopId=${shopId}`;
    
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            [config.csrfHeader]: config.csrfToken,
        },
    });

    if (!response.ok) {
        const errorBody = await response.text();
        console.error('Server responded with error:', response.status, errorBody);
        throw new Error(`Failed to find or create conversation. Server status: ${response.status}`);
    }

    const conversation = await response.json();
    return conversation.id;
}

async function loadMessages(shopId, conversationId) {
    const popup = chatWindowState[shopId].popup;
    const messagesContainer = popup.querySelector('.chat-popup-messages');
    messagesContainer.innerHTML = '<div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div>';

    const url = `${config.contextPath}api/chat/messages/${conversationId}`;
    const response = await fetch(url, {
         headers: { [config.csrfHeader]: config.csrfToken }
    });

    if (!response.ok) {
        messagesContainer.innerHTML = '<p class="error">Failed to load messages.</p>';
        return;
    }
    const messages = await response.json();
    messagesContainer.innerHTML = '';
    messages.forEach(msg => addMessageToPopup(shopId, msg));
    chatWindowState[shopId].messagesLoaded = true;
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

// REVERTED: This function now gets the user ID from the global config object.
function addMessageToPopup(shopId, message) {
    const popup = chatWindowState[shopId]?.popup;
    if (!popup) return;

    const currentUserId = config.currentUserId;
    const messagesContainer = popup.querySelector('.chat-popup-messages');
    const msgElement = document.createElement('div');
    
    // Using == for safe comparison as one might be a string and the other a number.
    msgElement.className = `message ${message.sender.id == currentUserId ? 'sent' : 'received'}`;
    msgElement.textContent = message.content;
    messagesContainer.appendChild(msgElement);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function connectWebSocket() {
    if (stompClient || !config.currentUserId) return;
    
    const socket = new SockJS(`${config.contextPath}ws`);
    stompClient = Stomp.over(socket);
    const headers = { [config.csrfHeader]: config.csrfToken };

    stompClient.connect(headers, () => {
        console.log('WebSocket connected for popup chat.');
        stompClient.subscribe(`/user/${config.currentUserId}/queue/messages`, (message) => {
            const receivedMessage = JSON.parse(message.body);
            
            let targetShopId = null;
            for (const shopId in chatWindowState) {
                if (chatWindowState[shopId].conversationId === receivedMessage.conversationId) {
                    targetShopId = shopId;
                    break;
                }
            }

            if (targetShopId) {
                const popup = chatWindowState[targetShopId].popup;
                if (popup && document.body.contains(popup)) {
                     addMessageToPopup(targetShopId, receivedMessage);
                }
            } 
        });
    }, (error) => {
        console.error('STOMP connection error:', error);
    });
}

function sendMessage(shopId, inputElement) {
    const content = inputElement.value.trim();
    const conversationId = chatWindowState[shopId]?.conversationId;
    const currentUserId = config.currentUserId;

    // THÊM KIỂM TRA ".connected"
    if (content && stompClient && stompClient.connected && conversationId && currentUserId) {
        const message = {
            conversationId: conversationId,
            senderId: currentUserId, 
            content: content
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
        inputElement.value = '';

        // Optimistic UI update
        const optimisticMessage = {
            sender: { id: currentUserId },
            content: content
        };
        addMessageToPopup(shopId, optimisticMessage);
    } else if (!stompClient || !stompClient.connected) {
         console.warn("Hệ thống đang tải! vui lòng chờ giây lát để gửi tin nhắn.");
    }
}
