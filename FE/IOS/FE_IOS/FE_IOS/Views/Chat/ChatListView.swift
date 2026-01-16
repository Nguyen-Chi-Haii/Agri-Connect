import SwiftUI

struct ChatListView: View {
    @State private var conversations: [ChatConversation] = []
    @State private var isLoading = false
    
    var body: some View {
        VStack {
            if isLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else if conversations.isEmpty {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "message")
                        .font(.system(size: 50))
                        .foregroundColor(.gray)
                    Text("Chưa có cuộc trò chuyện nào")
                        .foregroundColor(.gray)
                    Text("Liên hệ với người bán để bắt đầu")
                        .font(.caption)
                        .foregroundColor(.gray)
                }
                Spacer()
            } else {
                List(conversations) { conversation in
                    NavigationLink(destination: ChatRoomView(conversation: conversation)) {
                        ConversationRow(conversation: conversation)
                    }
                }
                .listStyle(PlainListStyle())
            }
        }
        .navigationTitle("Tin nhắn")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            loadConversations()
        }
    }
    
    private func loadConversations() {
        isLoading = true
        
        // Mock data with renamed struct to avoid conflict
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            isLoading = false
            conversations = [
                ChatConversation(id: "1", participantName: "Nguyễn Văn A", participantAvatar: nil, lastMessage: "Còn hàng không bạn?", lastMessageTime: "10:30", unreadCount: 2),
                ChatConversation(id: "2", participantName: "Trần Thị B", participantAvatar: nil, lastMessage: "OK, mai tôi qua lấy", lastMessageTime: "Hôm qua", unreadCount: 0),
                ChatConversation(id: "3", participantName: "Lê Văn C", participantAvatar: nil, lastMessage: "Giá nông sản đợt này thế nào?", lastMessageTime: "2 ngày", unreadCount: 0),
            ]
        }
    }
}

// MARK: - Chat Conversation Model (renamed to avoid conflict with Models.swift)
struct ChatConversation: Identifiable {
    let id: String
    let participantName: String
    let participantAvatar: String?
    let lastMessage: String
    let lastMessageTime: String
    let unreadCount: Int
}

// MARK: - Conversation Row
struct ConversationRow: View {
    let conversation: ChatConversation
    
    var body: some View {
        HStack(spacing: 12) {
            // Avatar
            ZStack {
                Circle()
                    .fill(Color(hex: "#E8F5E9"))
                    .frame(width: 50, height: 50)
                
                Text(String(conversation.participantName.prefix(1)))
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(Color(hex: "#2E7D32"))
            }
            
            // Content
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(conversation.participantName)
                        .font(.headline)
                    
                    Spacer()
                    
                    Text(conversation.lastMessageTime)
                        .font(.caption)
                        .foregroundColor(.gray)
                }
                
                HStack {
                    Text(conversation.lastMessage)
                        .font(.subheadline)
                        .foregroundColor(.gray)
                        .lineLimit(1)
                    
                    Spacer()
                    
                    if conversation.unreadCount > 0 {
                        Text("\(conversation.unreadCount)")
                            .font(.caption2)
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Color(hex: "#2E7D32"))
                            .clipShape(Capsule())
                    }
                }
            }
        }
        .padding(.vertical, 4)
    }
}

// MARK: - Chat Room View
struct ChatRoomView: View {
    let conversation: ChatConversation
    
    @State private var messages: [ChatMessage] = []
    @State private var newMessage = ""
    
    var body: some View {
        VStack(spacing: 0) {
            // Messages
            ScrollView {
                LazyVStack(spacing: 8) {
                    ForEach(messages) { message in
                        MessageBubble(message: message)
                    }
                }
                .padding()
            }
            
            Divider()
            
            // Input
            HStack(spacing: 12) {
                TextField("Nhập tin nhắn...", text: $newMessage)
                    .padding(12)
                    .background(Color(.systemGray6))
                    .cornerRadius(20)
                
                Button {
                    sendMessage()
                } label: {
                    Image(systemName: "arrow.up.circle.fill")
                        .font(.system(size: 35))
                        .foregroundColor(Color(hex: "#2E7D32"))
                }
                .disabled(newMessage.isEmpty)
            }
            .padding()
        }
        .navigationTitle(conversation.participantName)
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            loadMessages()
        }
    }
    
    private func loadMessages() {
        messages = [
            ChatMessage(id: "1", content: "Xin chào, tôi muốn hỏi về sản phẩm", isMine: false, time: "10:00"),
            ChatMessage(id: "2", content: "Vâng, bạn cần hỏi gì ạ?", isMine: true, time: "10:05"),
            ChatMessage(id: "3", content: "Còn hàng không bạn?", isMine: false, time: "10:30"),
        ]
    }
    
    private func sendMessage() {
        let message = ChatMessage(
            id: UUID().uuidString,
            content: newMessage,
            isMine: true,
            time: "Now"
        )
        messages.append(message)
        newMessage = ""
    }
}

// MARK: - Chat Message Model
struct ChatMessage: Identifiable {
    let id: String
    let content: String
    let isMine: Bool
    let time: String
}

// MARK: - Message Bubble
struct MessageBubble: View {
    let message: ChatMessage
    
    var body: some View {
        HStack {
            if message.isMine { Spacer() }
            
            VStack(alignment: message.isMine ? .trailing : .leading, spacing: 4) {
                Text(message.content)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(
                        message.isMine
                        ? Color(hex: "#2E7D32")
                        : Color(.systemGray5)
                    )
                    .foregroundColor(message.isMine ? .white : .primary)
                    .cornerRadius(16)
                
                Text(message.time)
                    .font(.caption2)
                    .foregroundColor(.gray)
            }
            
            if !message.isMine { Spacer() }
        }
    }
}

struct ChatListView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ChatListView()
        }
    }
}
