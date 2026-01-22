import SwiftUI

struct MyPostRow: View {
    let post: Post
    let onDelete: () -> Void
    @State private var showEditSheet = false
    
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
                .clipped()
                .cornerRadius(8)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(post.title)
                    .font(.headline)
                    .lineLimit(2)
                
                PostStatusBadge(status: post.status ?? "PENDING")
                    .padding(.bottom, 2)
                
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
            .layoutPriority(1)
            
            Spacer()
            
            // Actions
            HStack(spacing: 12) {
                Button(action: { showEditSheet = true }) {
                    Image(systemName: "pencil")
                        .foregroundColor(.blue)
                }
                .buttonStyle(BorderlessButtonStyle())
                
                Button(action: onDelete) {
                    Image(systemName: "trash")
                        .foregroundColor(.red)
                }
                .buttonStyle(BorderlessButtonStyle())
            }
            .fixedSize(horizontal: true, vertical: false)
        }
        .padding(.vertical, 8)
        .sheet(isPresented: $showEditSheet) {
            EditPostView(post: post, onUpdate: {
                showEditSheet = false
            })
        }
    }
    
    private func formatPrice(_ price: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = "."
        return (formatter.string(from: NSNumber(value: price)) ?? "\(price)") + "đ"
    }
}
