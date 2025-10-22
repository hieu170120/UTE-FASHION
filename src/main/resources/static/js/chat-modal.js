/*let stompClient = null;
let currentConversationId = null;


const contextPath = '/UTE_Fashion/';

const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

const currentUserId = window.currentUserId || null;


async function connectAndLoadMessages(shopId) {
    const chatMessages = document.getElementById('chat-messages');
    chatMessages.innerHTML = '<div class="text-center p-3"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';

    try {
        // Find or create a conversation to get its ID.
        const response = await fetch(`${contextPath}api/chat/conversation/find-or-create?shopId=${shopId}`, {
            method: 'POST',
            headers: { [csrfHeader]: csrfToken }
        });

        if (!response.ok) throw new Error('Failed to get conversation.');

        const conversation = await response.json();
        currentConversationId = conversation.id;
        
        // Clear loading spinner and display past messages.
        chatMessages.innerHTML = '';
        conversation.messages.forEach(displayMessage);

        // Connect to the WebSocket.
        const socket = new SockJS(`${contextPath}ws`);
        stompClient = Stomp.over(socket);

        stompClient.connect({}, (frame) => {
            console.log('Connected: ' + frame);
            // Subscribe to the conversation topic to receive new messages.
            stompClient.subscribe(`/topic/conversation/${currentConversationId}`, (message) => {
                displayMessage(JSON.parse(message.body));
            });
        }, (error) => {
            console.error('STOMP connection error: ', error);
            chatMessages.innerHTML = '<div class="alert alert-danger m-3">Không thể kết nối tới server chat.</div>';
        });

    } catch (error) {
        console.error('Error connecting and loading messages:', error);
        chatMessages.innerHTML = '<div class="alert alert-danger m-3">Đã xảy ra lỗi khi tải cuộc trò chuyện.</div>';
    }
}

*
 * Disconnects the WebSocket connection.
 
function disconnect() {
    if (stompClient && stompClient.connected) {
        stompClient.disconnect(() => console.log('Disconnected'));
    }
    stompClient = null;
    currentConversationId = null;
}

*
 * Sends a chat message.
 
function sendMessage() {
    const messageInput = document.getElementById('message-input');
    const messageContent = messageInput.value.trim();

    if (messageContent && stompClient && currentConversationId) {
        const chatMessage = {
            content: messageContent,
            conversationId: currentConversationId
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}

*
 * Displays a single message in the chat window.
 * @param {object} message - The message object to display.
 
function displayMessage(message) {
    const chatMessages = document.getElementById('chat-messages');
    if (!chatMessages) return;

    const isSentByCurrentUser = message.sender.id === currentUserId;
    const messageRow = document.createElement('div');
    messageRow.className = `chat-message-row ${isSentByCurrentUser ? 'current-user' : 'other-user'}`;
    
    const bubble = document.createElement('div');
    bubble.className = 'chat-bubble';
    bubble.textContent = message.content;
    
    messageRow.appendChild(bubble);
    chatMessages.appendChild(messageRow);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

// --- ENTRY POINT --- //
// Wait for the DOM to be fully loaded before adding event listeners.
document.addEventListener('DOMContentLoaded', () => {
    const chatModalEl = document.getElementById('chatModal');

    if (chatModalEl) {
        // Fired when the modal is about to be shown
        chatModalEl.addEventListener('show.bs.modal', async (event) => {
            const button = event.relatedTarget;
            if (!button) return;

            const shopId = button.getAttribute('data-shop-id');
            const shopName = button.getAttribute('data-shop-name');

            document.getElementById('chatModalLabel').textContent = `Chat với ${shopName}`;
            await connectAndLoadMessages(shopId);
        });

        // Fired when the modal has finished being hidden
        chatModalEl.addEventListener('hidden.bs.modal', () => {
            disconnect();
            document.getElementById('chat-messages').innerHTML = '';
        });
    }

    const sendMessageBtn = document.getElementById('send-message-btn');
    if (sendMessageBtn) {
        sendMessageBtn.addEventListener('click', sendMessage);
    }

    const messageInput = document.getElementById('message-input');
    if (messageInput) {
        messageInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                sendMessage();
            }
        });
    }
});
*/