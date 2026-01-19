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
                    NavigationLink(destination: ChatDetailView(
                        conversationId: conversation.id,
                        otherUserName: conversation.participantName ?? "Người dùng",
                        recipientId: getRecipientId(from: conversation)
                    )) {
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
    
    private func getRecipientId(from conversation: Conversation) -> String {
        guard let currentUserId = TokenManager.shared.userId,
              let participants = conversation.participants else {
            return ""
        }
        return participants.first { $0 != currentUserId } ?? ""
    }
}

// MARK: - Conversation Row
struct ConversationRow: View {
    let conversation: Conversation
    
    var body: some View {
        HStack(spacing: 12) {
            avatarView
            contentView
        }
        .padding(.vertical, 4)
    }
    
    private var avatarView: some View {
        ZStack {
            Circle()
                .fill(Color(hex: "#E8F5E9"))
                .frame(width: 50, height: 50)
            
            Text(avatarLetter)
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(Color(hex: "#2E7D32"))
        }
    }
    
    private var contentView: some View {
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
                Text(conversation.lastMessageContent ?? "")
                    .font(.subheadline)
                    .foregroundColor(.gray)
                    .lineLimit(1)
                
                Spacer()
                
                if let unread = conversation.unreadCount, unread > 0 {
                    unreadBadge(count: unread)
                }
            }
        }
    }
    
    private func unreadBadge(count: Int) -> some View {
        Text("\(count)")
            .font(.caption2)
            .fontWeight(.bold)
            .foregroundColor(.white)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(Color(hex: "#2E7D32"))
            .clipShape(Capsule())
    }
    
    private var avatarLetter: String {
        String((conversation.participantName ?? "?").prefix(1))
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

struct ChatListView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ChatListView()
        }
    }
}
