import SwiftUI

struct PostDetailView: View {
    let postId: String
    
    @State private var post: Post?
    @State private var isLoading = true
    @State private var isLiked = false
    @State private var likeCount = 0
    @State private var commentCount = 0
    @State private var comments: [Comment] = []
    @State private var commentText = ""
    @State private var isLoadingComments = false
    @State private var showCommentSection = false
    @State private var statsTimer: Timer?
    
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
                        
                        // Action Buttons
                        HStack(spacing: 24) {
                            // Like Button
                            Button(action: toggleLike) {
                                HStack(spacing: 4) {
                                    Image(systemName: isLiked ? "heart.fill" : "heart")
                                        .foregroundColor(isLiked ? .red : .gray)
                                    Text("\(likeCount)")
                                        .foregroundColor(isLiked ? .red : .gray)
                                }
                            }
                            
                            // Comment Button
                            Button(action: { showCommentSection.toggle() }) {
                                HStack(spacing: 4) {
                                    Image(systemName: "bubble.left.fill")
                                        .foregroundColor(.gray)
                                    Text("\(commentCount)")
                                        .foregroundColor(.gray)
                                }
                            }
                            
                            // Chat Button
                            Button(action: startChat) {
                                HStack(spacing: 4) {
                                    Image(systemName: "message.fill")
                                        .foregroundColor(Color(hex: "#2E7D32"))
                                    Text("Nhắn tin")
                                        .foregroundColor(Color(hex: "#2E7D32"))
                                }
                            }
                            
                            Spacer()
                        }
                        .padding(.vertical, 8)
                        
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
                        
                        // Comment Section
                        if showCommentSection {
                            Divider()
                                .padding(.vertical, 8)
                            
                            VStack(alignment: .leading, spacing: 12) {
                                Text("Bình luận (\(commentCount))")
                                    .font(.headline)
                                
                                // Comment Input
                                HStack {
                                    TextField("Viết bình luận...", text: $commentText)
                                        .textFieldStyle(RoundedBorderTextFieldStyle())
                                    
                                    Button(action: sendComment) {
                                        Image(systemName: "paperplane.fill")
                                            .foregroundColor(Color(hex: "#2E7D32"))
                                    }
                                    .disabled(commentText.trimmingCharacters(in: .whitespaces).isEmpty)
                                }
                                
                                // Comment List
                                if isLoadingComments {
                                    ProgressView()
                                        .frame(maxWidth: .infinity)
                                } else if comments.isEmpty {
                                    Text("Chưa có bình luận nào")
                                        .foregroundColor(.gray)
                                        .frame(maxWidth: .infinity)
                                        .padding()
                                } else {
                                    ForEach(comments) { comment in
                                        CommentRow(comment: comment)
                                    }
                                }
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
            startStatsPolling()
        }
        .onDisappear {
            stopStatsPolling()
        }
    }
    
    private func startStatsPolling() {
        statsTimer = Timer.scheduledTimer(withTimeInterval: 10.0, repeats: true) { _ in
            refreshStats()
        }
    }
    
    private func stopStatsPolling() {
        statsTimer?.invalidate()
        statsTimer = nil
    }
    
    private func refreshStats() {
        APIClient.shared.request(
            endpoint: "\(APIConfig.Posts.list)/\(postId)",
            method: .get
        ) { (result: Result<ApiResponse<Post>, Error>) in
            if case .success(let response) = result, let data = response.data {
                likeCount = data.likeCount ?? 0
                commentCount = data.commentCount ?? 0
                isLiked = data.isLiked ?? false
            }
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
                likeCount = data.likeCount ?? 0
                commentCount = data.commentCount ?? 0
                isLiked = data.isLiked ?? false
                loadComments()
            }
        }
    }
    
    private func toggleLike() {
        // Optimistic UI update
        isLiked.toggle()
        likeCount += isLiked ? 1 : -1
        
        APIClient.shared.request(
            endpoint: "\(APIConfig.Posts.list)/\(postId)/like",
            method: .post
        ) { (result: Result<ApiResponse<Void>, Error>) in
            if case .failure = result {
                // Revert on failure
                isLiked.toggle()
                likeCount += isLiked ? 1 : -1
            }
        }
    }
    
    private func loadComments() {
        isLoadingComments = true
        APIClient.shared.request(
            endpoint: "\(APIConfig.Posts.list)/\(postId)/comments",
            method: .get
        ) { (result: Result<ApiResponse<PagedResponse<Comment>>, Error>) in
            isLoadingComments = false
            if case .success(let response) = result,
               let pagedData = response.data,
               let commentList = pagedData.content {
                comments = commentList
            }
        }
    }
    
    private func sendComment() {
        guard !commentText.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        
        let body: [String: String] = ["content": commentText]
        
        APIClient.shared.request(
            endpoint: "\(APIConfig.Posts.list)/\(postId)/comments",
            method: .post,
            body: body
        ) { (result: Result<ApiResponse<Comment>, Error>) in
            if case .success(let response) = result, let newComment = response.data {
                comments.insert(newComment, at: 0)
                commentCount += 1
                commentText = ""
            }
        }
    }
    
    private func startChat() {
        guard let sellerId = post?.sellerId else { return }
        
        let body: [String: String] = ["recipientId": sellerId]
        
        APIClient.shared.request(
            endpoint: "/chat/conversations",
            method: .post,
            body: body
        ) { (result: Result<ApiResponse<Conversation>, Error>) in
            if case .success(let response) = result, let conversation = response.data {
                // TODO: Navigate to ChatDetailView
                print("Created conversation: \(conversation.id)")
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
