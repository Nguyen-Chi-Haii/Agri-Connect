import SwiftUI

struct PostStatusBadge: View {
    let status: String
    
    var color: Color {
        switch status {
        case "APPROVED": return .green
        case "PENDING": return .orange
        case "REJECTED": return .red
        case "CLOSED": return .gray
        default: return .gray
        }
    }
    
    var text: String {
        switch status {
        case "APPROVED": return "Đã duyệt"
        case "PENDING": return "Chờ duyệt"
        case "REJECTED": return "Từ chối"
        case "CLOSED": return "Đã đóng"
        default: return status
        }
    }
    
    var body: some View {
        Text(text)
            .font(.caption2)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(color.opacity(0.2))
            .foregroundColor(color)
            .cornerRadius(6)
    }
}
