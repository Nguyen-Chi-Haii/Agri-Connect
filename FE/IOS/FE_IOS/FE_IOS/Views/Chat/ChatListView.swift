import SwiftUI

struct ChatListView: View {
    @State private var conversations: [Conversation] = []
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
        
        APIClient.shared.request(
            endpoint: APIConfig.Chat.conversations,
            method: .get
        ) { (result: Result<ApiResponse<[Conversation]>, Error>) in
            isLoading = false
            if case .success(let response) = result, let data = response.data {
                conversations = data
            }
        }
    }
}

// MARK: - Conversation Row
struct ConversationRow: View {
    let conversation: Conversation
    
    var body: some View {
        HStack(spacing: 12) {
            // Avatar
            ZStack {
                Circle()
                    .fill(Color(hex: "#E8F5E9"))
                    .frame(width: 50, height: 50)
                
                Text(String((conversation.participantName ?? "?").prefix(1)))
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(Color(hex: "#2E7D32"))
            }
            
            // Content
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(conversation.participantName ?? "Unknown")
                        .font(.headline)
                    
                    Spacer()
                    
                    Text(formatTime(conversation.updatedAt))
                        .font(.caption)
                        .foregroundColor(.gray)
                }
                
                HStack {
                    Text(conversation.lastMessage ?? "")
                        .font(.subheadline)
                        .foregroundColor(.gray)
                        .lineLimit(1)
                    
                    Spacer()
                    
                    if let unread = conversation.unreadCount, unread > 0 {
                        Text("\(unread)")
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
    
    private func formatTime(_ isoDate: String?) -> String {
        guard let dateString = isoDate else { return "" }
        
        let formatter = ISO8601DateFormatter()
        guard let date = formatter.date(from: dateString) else { return "" }
        
        let now = Date()
        let calendar = Calendar.current
        let components = calendar.dateComponents([.day, .hour, .minute], from: date, to: now)
        
        if let days = components.day, days > 0 {
            return "\(days) ngày"
        } else if let hours = components.hour, hours > 0 {
            return "\(hours)h"
        } else if let minutes = components.minute, minutes > 0 {
            return "\(minutes)m"
        } else {
            return "Vừa xong"
        }
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
