import SwiftUI

struct CommentRow: View {
    let comment: Comment
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(comment.userName ?? "Ẩn danh")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                
                Spacer()
                
                Text(formatDate(comment.createdAt))
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            
            Text(comment.content)
                .font(.body)
                .foregroundColor(.primary)
        }
        .padding(.vertical, 8)
        .padding(.horizontal, 4)
    }
    
    private func formatDate(_ dateString: String) -> String {
        let components = dateString.prefix(10).split(separator: "-")
        if components.count >= 3 {
            return "\(components[2])/\(components[1])/\(components[0])"
        }
        return dateString
    }
}
