import SwiftUI

struct NotificationListView: View {
    @State private var notifications: [AppNotification] = []
    @State private var isLoading = false
    @State private var unreadCount = 0
    
    var body: some View {
        Group {
            if isLoading && notifications.isEmpty {
                ProgressView()
            } else if notifications.isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: "bell.slash")
                        .font(.system(size: 50))
                        .foregroundColor(.gray)
                    Text("Chưa có thông báo nào")
                        .foregroundColor(.gray)
                }
            } else {
                List {
                    ForEach(notifications) { notification in
                        NotificationCell(notification: notification)
                            .onTapGesture {
                                markAsRead(notification)
                            }
                    }
                }
                .refreshable {
                    loadData()
                }
            }
        }
        .navigationTitle("Thông báo")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button("Đọc tất cả") {
                    markAllRead()
                }
                .disabled(unreadCount == 0)
            }
        }
        .onAppear {
            loadData()
        }
    }
    
    private func loadData() {
        isLoading = true
        NotificationService.shared.fetchNotifications { result in
            isLoading = false
            switch result {
            case .success(let data):
                self.notifications = data
                self.updateUnreadCount()
            case .failure(let error):
                print("Error loading notifications: \(error)")
            }
        }
    }
    
    private func markAsRead(_ notification: AppNotification) {
        guard !notification.isRead else { return }
        
        // Optimistic update
        if let index = notifications.firstIndex(where: { $0.id == notification.id }) {
            notifications[index].read = true
        }
        
        NotificationService.shared.markAsRead(notification.id) { _ in
            // Silent success or handle error
            updateUnreadCount()
        }
    }
    
    private func markAllRead() {
        // Optimistic
        for i in 0..<notifications.count {
            notifications[i].read = true
        }
        
        NotificationService.shared.markAllAsRead { _ in
            updateUnreadCount()
        }
    }
    
    private func updateUnreadCount() {
        self.unreadCount = notifications.filter { !($0.read ?? false) }.count
        // Could post a notification center event to update badge in Main Tab
    }
}

struct NotificationListView_Previews: PreviewProvider {
    static var previews: some View {
        NotificationListView()
    }
}
