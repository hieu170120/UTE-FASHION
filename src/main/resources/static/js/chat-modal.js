/**
 * UTE-Fashion - Chat Modal Script (Initialization Pattern with Debug Logs)
 * Encapsulates all chat logic within an initializer function.
 */

function initChatModal(config) {
    console.log("[DEBUG] initChatModal được gọi với config:", config);

    let stompClient = null;
    let currentConversationId = null;
    let chatModalInstance = null;

    const contextPath = config.contextPath;
    const csrfToken = config.csrfToken;
    const csrfHeader = config.csrfHeader;
    const currentUserId = config.currentUserId;

    async function connectAndLoadMessages(shopId) {
        console.log("[DEBUG] Bắt đầu kết nối và tải tin nhắn cho shopId:", shopId);
        const chatMessages = document.getElementById('chat-messages');
        chatMessages.innerHTML = '<div class="text-center p-3"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';

        try {
            const headers = { 'Content-Type': 'application/json' };
            if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

            const response = await fetch(`${contextPath}api/chat/conversation/find-or-create?shopId=${shopId}`, {
                method: 'POST',
                headers: headers
            });

            if (!response.ok) throw new Error('Failed to get conversation.');
            const conversation = await response.json();
            currentConversationId = conversation.id;
            console.log("[DEBUG] Lấy được conversation ID:", currentConversationId);

            chatMessages.innerHTML = '';
            if (conversation.messages && conversation.messages.length > 0) {
                conversation.messages.forEach(displayMessage);
            } else {
                chatMessages.innerHTML = '<div class="text-center text-muted p-4">Chưa có tin nhắn nào. Hãy bắt đầu cuộc trò chuyện!</div>';
            }

            const socket = new SockJS(`${contextPath}ws`);
            stompClient = Stomp.over(socket);
            stompClient.connect({}, (frame) => {
                console.log("[DEBUG] STOMP Connected:", frame);
                stompClient.subscribe(`/topic/conversation/${currentConversationId}`, (message) => displayMessage(JSON.parse(message.body)));
            }, (error) => console.error('STOMP connection error: ', error));

        } catch (error) {
            console.error('Lỗi khi kết nối và tải tin nhắn:', error);
            chatMessages.innerHTML = '<div class="alert alert-danger m-3">Đã xảy ra lỗi khi tải cuộc trò chuyện.</div>';
        }
    }

    function disconnect() {
        console.log("[DEBUG] Ngắt kết nối STOMP.");
        if (stompClient && stompClient.connected) stompClient.disconnect();
    }

    function sendMessage() {
        const messageInput = document.getElementById('message-input');
        const messageContent = messageInput.value.trim();
        if (messageContent && stompClient && currentConversationId) {
            console.log("[DEBUG] Gửi tin nhắn:", messageContent);
            stompClient.send("/app/chat.sendMessage", {}, JSON.stringify({
                content: messageContent,
                conversationId: currentConversationId
            }));
            messageInput.value = '';
        }
    }

    function displayMessage(message) {
        const chatMessages = document.getElementById('chat-messages');
        if (!chatMessages) return;

        const placeholder = chatMessages.querySelector('.text-center.text-muted');
        if (placeholder) placeholder.remove();

        const isSentByCurrentUser = message.sender && message.sender.id === currentUserId;

        const messageRow = document.createElement('div');
        messageRow.className = `chat-message-row d-flex ${isSentByCurrentUser ? 'justify-content-end' : 'justify-content-start'}`;

        const bubble = document.createElement('div');
        bubble.className = `chat-bubble ${isSentByCurrentUser ? 'current-user' : 'other-user'}`;
        bubble.textContent = message.content;

        messageRow.appendChild(bubble);
        chatMessages.appendChild(messageRow);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    console.log("[DEBUG] Tìm kiếm các element: #contact-shop-btn và #chatModal");
    const contactShopBtn = document.getElementById('contact-shop-btn');
    const chatModalEl = document.getElementById('chatModal');

    if (!contactShopBtn || !chatModalEl) {
        console.error("[DEBUG] KHÔNG TÌM THẤY NÚT LIÊN HỆ hoặc MODAL. Script sẽ không hoạt động. Kiểm tra lại ID trong file HTML.", { contactShopBtn, chatModalEl });
        return;
    }

    console.log("[DEBUG] Đã tìm thấy các element. Gán sự kiện click...");
    contactShopBtn.addEventListener('click', () => {
        console.log("[DEBUG] >>> NÚT LIÊN HỆ SHOP ĐÃ ĐƯỢC CLICK!");

        if (!chatModalInstance) {
            chatModalInstance = new bootstrap.Modal(chatModalEl);
        }
        chatModalInstance.show();
    });

    chatModalEl.addEventListener('show.bs.modal', () => {
        const shopId = contactShopBtn.getAttribute('data-shop-id');
        console.log("[DEBUG] Modal đang được hiển thị. Gọi connectAndLoadMessages.");
        connectAndLoadMessages(shopId);
    });

    chatModalEl.addEventListener('hidden.bs.modal', disconnect);

    document.getElementById('send-message-btn')?.addEventListener('click', sendMessage);
    document.getElementById('message-input')?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    console.log("[DEBUG] Script chat-modal.js đã khởi tạo xong.");
}
