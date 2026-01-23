import Foundation

enum NotificationType: String, Decodable {
    case NEW_MESSAGE
    case POST_APPROVED
    case POST_REJECTED
    case PRICE_UPDATE
    case SYSTEM
}

struct AppNotification: Decodable, Identifiable {
    let id: String
    let userId: String
    let type: NotificationType
    let title: String
    let content: String
    var read: Bool?
    let createdAt: String?
    
    // Helper to check read status (handling null)
    var isRead: Bool {
        return read ?? false
    }
}
