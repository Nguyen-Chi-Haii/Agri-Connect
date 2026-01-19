import SwiftUI

struct MyPostRow: View {
    let post: Post
    let onDelete: () -> Void
    
    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            // Post thumbnail
            if let imageUrl = post.images?.first, let url = URL(string: imageUrl) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    default:
                        Rectangle().fill(Color.gray.opacity(0.3))
                    }
                }
                .frame(width: 80, height: 80)
                .cornerRadius(8)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(post.title)
                    .font(.headline)
                    .lineLimit(2)
                
                if let price = post.price, let unit = post.unit {
                    Text("\(formatPrice(price)) / \(unit)")
                        .font(.subheadline)
                        .foregroundColor(Color(hex: "#2E7D32"))
                }
                
                HStack(spacing: 12) {
                    Label("\(post.likeCount ?? 0)", systemImage: "heart")
                        .font(.caption)
                        .foregroundColor(.gray)
                    
                    Label("\(post.commentCount ?? 0)", systemImage: "bubble.left")
                        .font(.caption)
                        .foregroundColor(.gray)
                    
                    Label("\(post.viewCount ?? 0)", systemImage: "eye")
                        .font(.caption)
                        .foregroundColor(.gray)
                }
            }
            
            Spacer()
            
            Button(action: onDelete) {
                Image(systemName: "trash")
                    .foregroundColor(.red)
            }
            .buttonStyle(BorderlessButtonStyle())
        }
        .padding(.vertical, 8)
    }
    
    private func formatPrice(_ price: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = "."
        return (formatter.string(from: NSNumber(value: price)) ?? "\(price)") + "đ"
    }
}
