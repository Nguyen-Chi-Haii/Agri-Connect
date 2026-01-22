import SwiftUI

struct NotificationCell: View {
    let notification: Notification
    
    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            // Icon based on type
            ZStack {
                Circle()
                    .fill(backgroundColor(for: notification.type))
                    .frame(width: 40, height: 40)
                
                Image(systemName: iconName(for: notification.type))
                    .foregroundColor(.white)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(notification.title)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(notification.isRead ? .gray : .primary)
                
                Text(notification.content)
                    .font(.caption)
                    .foregroundColor(.gray)
                    .lineLimit(2)
                
                // Timestamp would go here if formatted
                if let created = notification.createdAt {
                    Text(formatDate(created))
                        .font(.caption2)
                        .foregroundColor(.gray.opacity(0.8))
                }
            }
            
            Spacer()
            
            if !notification.isRead {
                Circle()
                    .fill(Color.blue)
                    .frame(width: 8, height: 8)
            }
        }
        .padding(.vertical, 8)
        .contentShape(Rectangle()) // Make entire cell tappable
    }
    
    private func iconName(for type: NotificationType) -> String {
        switch type {
        case .NEW_MESSAGE: return "message.fill"
        case .POST_APPROVED: return "checkmark.seal.fill"
        case .POST_REJECTED: return "xmark.seal.fill"
        case .PRICE_UPDATE: return "tag.fill"
        case .SYSTEM: return "bell.fill"
        }
    }
    
    private func backgroundColor(for type: NotificationType) -> Color {
        switch type {
        case .NEW_MESSAGE: return .blue
        case .POST_APPROVED: return .green
        case .POST_REJECTED: return .red
        case .PRICE_UPDATE: return .orange
        case .SYSTEM: return .purple
        }
    }
    
    private func formatDate(_ dateString: String) -> String {
        // Simple ISO parser or just return string for now
        // Backend usually sends ISO 8601
        let isoFormatter = ISO8601DateFormatter()
        isoFormatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        
        if let date = isoFormatter.date(from: dateString) {
             let formatter = DateFormatter()
             formatter.dateFormat = "dd/MM HH:mm"
             return formatter.string(from: date)
        }
        return dateString
    }
}
