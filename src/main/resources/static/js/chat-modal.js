let stompClient = null;
let currentSubscription;
let conversationId;

// Hàm để tải các thư viện bên ngoài
function loadScript(src) {
    return new Promise((resolve, reject) => {
        const script = document.createElement('script');
        script.src = src;
        script.onload = () => resolve(script);
        script.onerror = () => reject(new Error(`Script load error for ${src}`));
        document.head.appendChild(script);
    });
}

// Hàm để khởi tạo modal chat
async function initChatModal() {
    try {
        // Tải đồng thời SockJS và StompJS
        await Promise.all([
            loadScript('https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.1/sockjs.min.js'),
            loadScript('https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js')
        ]);

        const chatModal = new bootstrap.Modal(document.getElementById('chatModal'));

        // Xử lý khi modal được hiển thị
        document.getElementById('chatModal').addEventListener('shown.bs.modal', async (event) => {
            const button = event.relatedTarget;
            const shopId = button.getAttribute('data-shop-id');
            const shopName = button.getAttribute('data-shop-name');

            document.getElementById('chatModalLabel').textContent = `Chat với ${shopName}`;

            await connectAndLoadMessages(shopId);
        });

        // Xử lý khi modal bị đóng
        document.getElementById('chatModal').addEventListener('hidden.bs.modal', () => {
            disconnect();
            document.getElementById('chat-messages').innerHTML = '<div class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';
            document.getElementById('message-input').value = '';
        });

        // Gửi tin nhắn khi nhấn nút
        document.getElementById('send-message-btn').addEventListener('click', sendMessage);
        // Gửi tin nhắn khi nhấn Enter
        document.getElementById('message-input').addEventListener('keypress', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                sendMessage();
            }
        });

    } catch (error) {
        console.error('Không thể tải thư viện chat:', error);
        alert('Đã xảy ra lỗi khi khởi tạo chức năng chat. Vui lòng tải lại trang.');
    }
}

// Kết nối tới WebSocket và tải tin nhắn
async function connectAndLoadMessages(shopId) {
    try {
        // 1. Lấy hoặc tạo cuộc trò chuyện
        const response = await fetch(`/api/chat/conversation/find-or-create?shopId=${shopId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                // Thêm CSRF token nếu cần
            }
        });

        if (!response.ok) throw new Error('Could not find or create conversation.');

        const conversation = await response.json();
        conversationId = conversation.id;

        // 2. Tải tin nhắn cũ
        const messagesResponse = await fetch(`/api/chat/messages/${conversationId}`);
        if (!messagesResponse.ok) throw new Error('Could not load messages.');

        const messages = await messagesResponse.json();
        const chatMessages = document.getElementById('chat-messages');
        chatMessages.innerHTML = ''; // Xóa spinner
        messages.forEach(displayMessage);
        scrollToBottom();

        // 3. Kết nối WebSocket
        const socket = new SockJS('/ws-chat');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, (frame) => {
            console.log('Connected: ' + frame);
            // Hủy đăng ký cũ (nếu có)
            if (currentSubscription) {
                currentSubscription.unsubscribe();
            }
            // Đăng ký nhận tin nhắn cho cuộc trò chuyện mới
            currentSubscription = stompClient.subscribe(`/topic/conversation/${conversationId}`, (message) => {
                displayMessage(JSON.parse(message.body));
                scrollToBottom();
            });
        }, (error) => {
            console.error('STOMP connection error: ' + error);
        });

    } catch (error) {
        console.error('Error in connectAndLoadMessages:', error);
        document.getElementById('chat-messages').innerHTML = '<div class="text-center text-danger">Không thể tải cuộc trò chuyện.</div>';
    }
}

// Ngắt kết nối WebSocket
function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect(() => {
            console.log("Disconnected");
        });
        stompClient = null;
    }
    if (currentSubscription) {
        currentSubscription.unsubscribe();
        currentSubscription = null;
    }
}

// Gửi tin nhắn qua WebSocket
function sendMessage() {
    const messageInput = document.getElementById('message-input');
    const messageContent = messageInput.value.trim();

    if (messageContent && stompClient && conversationId) {
        const chatMessage = {
            conversationId: conversationId,
            messageContent: messageContent,
            senderType: 'USER' // Hoặc lấy từ logic xác thực của bạn
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}

// Hiển thị một tin nhắn trong giao diện
function displayMessage(message) {
    const chatMessages = document.getElementById('chat-messages');
    const messageElement = document.createElement('div');
    // TODO: Cần có logic để xác định tin nhắn này là gửi đi hay nhận được
    // Tạm thời, ta sẽ dựa vào senderType
    const messageType = (message.senderType === 'USER') ? 'sent' : 'received';

    messageElement.classList.add('message', messageType);

    const contentElement = document.createElement('div');
    contentElement.classList.add('message-content');
    contentElement.textContent = message.messageContent;

    const timeElement = document.createElement('div');
    timeElement.classList.add('message-time');
    timeElement.textContent = new Date(message.createdAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute:'2-digit' });

    messageElement.appendChild(contentElement);
    messageElement.appendChild(timeElement);

    chatMessages.appendChild(messageElement);
}

// Cuộn xuống cuối khung chat
function scrollToBottom() {
    const chatMessages = document.getElementById('chat-messages');
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

// Khởi tạo khi DOM đã tải xong
document.addEventListener('DOMContentLoaded', initChatModal);
