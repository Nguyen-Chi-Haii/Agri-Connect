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
                            HStack {
                                if message.senderId == (TokenManager.shared.userId ?? "") {
                                    Spacer()
                                    Text(message.content)
                                        .padding(12)
                                        .background(Color(hex: "#2E7D32"))
                                        .foregroundColor(.white)
                                        .cornerRadius(16)
                                } else {
                                    Text(message.content)
                                        .padding(12)
                                        .background(Color(.systemGray5))
                                        .foregroundColor(.primary)
                                        .cornerRadius(16)
                                    Spacer()
                                }
                            }
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
            markConversationAsRead()
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
    
    private func markConversationAsRead() {
        APIClient.shared.request(
            endpoint: APIConfig.Chat.markRead(conversationId),
            method: .put
        ) { (result: Result<ApiResponse<EmptyResponse>, Error>) in
            // Successfully marked as read
            // ChatListView will update badge count on next load
        }
    }
}
