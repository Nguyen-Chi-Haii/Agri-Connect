import SwiftUI

struct ChatDetailView: View {
    let conversationId: String
    let otherUserName: String
    let recipientId: String
    
    @State private var messages: [Message] = []
    @State private var messageText = ""
    @State private var isLoading = false
    @State private var isSending = false
    
    var body: some View {
        VStack(spacing: 0) {
            // Message List
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(messages) { message in
                            MessageBubble(
                                message: message,
                                isCurrentUser: message.senderId == TokenManager.shared.userId
                            )
                            .id(message.id)
                        }
                    }
                    .padding()
                }
                .onChange(of: messages.count) { _ in
                    if let lastMessage = messages.last {
                        withAnimation {
                            proxy.scrollTo(lastMessage.id, anchor: .bottom)
                        }
                    }
                }
            }
            
            Divider()
            
            // Message Input
            HStack(spacing: 12) {
                TextField("Nhập tin nhắn...", text: $messageText)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                
                Button(action: sendMessage) {
                    if isSending {
                        ProgressView()
                    } else {
                        Image(systemName: "paperplane.fill")
                            .foregroundColor(Color(hex: "#2E7D32"))
                    }
                }
                .disabled(messageText.trimmingCharacters(in: .whitespaces).isEmpty || isSending)
            }
            .padding()
            .background(Color(.systemGray6))
        }
        .navigationTitle(otherUserName)
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            loadMessages()
            startPolling()
        }
        .onDisappear {
            stopPolling()
        }
    }
    
    // MARK: - Polling Timer
    private var pollingTimer: Timer?
    
    private func startPolling() {
        Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { timer in
            loadMessages()
        }
    }
    
    private func stopPolling() {
        pollingTimer?.invalidate()
    }
    
    // MARK: - API Functions
    private func loadMessages() {
        APIClient.shared.request(
            endpoint: "/chat/conversations/\(conversationId)/messages",
            method: .get
        ) { (result: Result<ApiResponse<[Message]>, Error>) in
            if case .success(let response) = result, let messageList = response.data {
                messages = messageList
            }
        }
    }
    
    private func sendMessage() {
        guard !messageText.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        
        isSending = true
        let content = messageText
        messageText = ""
        
        let body: [String: String] = [
            "conversationId": conversationId,
            "recipientId": recipientId,
            "content": content
        ]
        
        APIClient.shared.request(
            endpoint: "/chat/messages",
            method: .post,
            body: body
        ) { (result: Result<ApiResponse<Message>, Error>) in
            isSending = false
            
            if case .success(let response) = result, let newMessage = response.data {
                messages.append(newMessage)
            } else {
                // Restore message on failure
                messageText = content
            }
        }
    }
}

// MARK: - Message Bubble Component
struct MessageBubble: View {
    let message: Message
    let isCurrentUser: Bool
    
    var body: some View {
        HStack {
            if isCurrentUser {
                Spacer()
            }
            
            VStack(alignment: isCurrentUser ? .trailing : .leading, spacing: 4) {
                Text(message.content)
                    .padding(12)
                    .background(isCurrentUser ? Color(hex: "#2E7D32") : Color(.systemGray5))
                    .foregroundColor(isCurrentUser ? .white : .primary)
                    .cornerRadius(16)
                
                Text(formatTime(message.createdAt))
                    .font(.caption2)
                    .foregroundColor(.gray)
            }
            .frame(maxWidth: 250, alignment: isCurrentUser ? .trailing : .leading)
            
            if !isCurrentUser {
                Spacer()
            }
        }
    }
    
    private func formatTime(_ dateString: String) -> String {
        let components = dateString.split(separator: "T")
        if components.count >= 2 {
            let time = components[1].prefix(5)
            return String(time)
        }
        return dateString
    }
}
