import SwiftUI

struct PostDetailView: View {
    let postId: String
    
    @State private var post: Post?
    @State private var isLoading = true
    
    var body: some View {
        ScrollView {
            if isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, minHeight: 300)
            } else if let post = post {
                VStack(alignment: .leading, spacing: 16) {
                    // Images
                    if let images = post.images, !images.isEmpty {
                        TabView {
                            ForEach(images, id: \.self) { imageUrl in
                                if let url = URL(string: imageUrl) {
                                    AsyncImage(url: url) { phase in
                                        switch phase {
                                        case .success(let image):
                                            image
                                                .resizable()
                                                .aspectRatio(contentMode: .fill)
                                        default:
                                            Rectangle()
                                                .fill(Color.gray.opacity(0.3))
                                        }
                                    }
                                }
                            }
                        }
                        .frame(height: 250)
                        .tabViewStyle(PageTabViewStyle())
                    } else {
                        Rectangle()
                            .fill(Color.gray.opacity(0.2))
                            .frame(height: 200)
                            .overlay(
                                Image(systemName: "photo")
                                    .font(.system(size: 50))
                                    .foregroundColor(.gray)
                            )
                    }
                    
                    VStack(alignment: .leading, spacing: 12) {
                        // Category
                        if let categoryName = post.categoryName {
                            Text(categoryName)
                                .font(.caption)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 4)
                                .background(Color(hex: "#E8F5E9"))
                                .foregroundColor(Color(hex: "#2E7D32"))
                                .cornerRadius(12)
                        }
                        
                        // Title
                        Text(post.title)
                            .font(.title2)
                            .fontWeight(.bold)
                        
                        // Price
                        if let price = post.price, let unit = post.unit {
                            Text("\(formatPrice(price)) / \(unit)")
                                .font(.title3)
                                .fontWeight(.semibold)
                                .foregroundColor(Color(hex: "#2E7D32"))
                        }
                        
                        Divider()
                        
                        // Details
                        VStack(alignment: .leading, spacing: 8) {
                            if let quantity = post.quantity, let unit = post.unit {
                                DetailRow(icon: "cube.box.fill", title: "Số lượng", value: "\(Int(quantity)) \(unit)")
                            }
                            
                            if let province = post.province {
                                DetailRow(icon: "mappin.circle.fill", title: "Địa điểm", value: province)
                            }
                            
                            if let author = post.authorName {
                                DetailRow(icon: "person.fill", title: "Người đăng", value: author)
                            }
                            
                            if let date = post.createdAt {
                                DetailRow(icon: "calendar", title: "Ngày đăng", value: formatDate(date))
                            }
                        }
                        
                        Divider()
                        
                        // Description
                        if let desc = post.description {
                            VStack(alignment: .leading, spacing: 8) {
                                Text("Mô tả")
                                    .font(.headline)
                                
                                Text(desc)
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                    .padding()
                }
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            loadPost()
        }
    }
    
    private func loadPost() {
        APIClient.shared.request(
            endpoint: "\(APIConfig.Posts.list)/\(postId)",
            method: .get
        ) { (result: Result<ApiResponse<Post>, Error>) in
            isLoading = false
            if case .success(let response) = result, let data = response.data {
                post = data
            }
        }
    }
    
    private func formatPrice(_ price: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = "."
        return (formatter.string(from: NSNumber(value: price)) ?? "\(price)") + "đ"
    }
    
    private func formatDate(_ dateString: String) -> String {
        let components = dateString.prefix(10).split(separator: "-")
        if components.count >= 3 {
            return "\(components[2])/\(components[1])/\(components[0])"
        }
        return dateString
    }
}

// MARK: - Detail Row
struct DetailRow: View {
    let icon: String
    let title: String
    let value: String
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .foregroundColor(Color(hex: "#2E7D32"))
                .frame(width: 24)
            
            Text(title)
                .foregroundColor(.gray)
            
            Spacer()
            
            Text(value)
                .fontWeight(.medium)
        }
    }
}

struct PostDetailView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            PostDetailView(postId: "1")
        }
    }
}
