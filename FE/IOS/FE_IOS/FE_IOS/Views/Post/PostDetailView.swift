import SwiftUI

struct PostDetailView: View {
    let postId: String
    let initialPost: Post?
    
    @State private var post: Post?
    @State private var isLoading: Bool
    @State private var isLiked: Bool
    @State private var likeCount: Int
    @State private var commentCount: Int
    @State private var comments: [Comment] = []
    @State private var commentText = ""
    @State private var isLoadingComments = false
    @State private var statsTimer: Timer?
    
    // Admin Actions
    var showAdminActions: Bool = false
    @State private var isProcessingAdminAction = false
    @State private var showRejectReasonInput = false
    @State private var rejectionReason = ""
    
    @State private var showKYCAlert = false
    @State private var kycAlertTitle = ""
    @State private var kycAlertMessage = ""
    @State private var navigateToVerification = false
    
    init(postId: String, initialPost: Post? = nil, showAdminActions: Bool = false) {
        self.postId = postId
        self.initialPost = initialPost
        self.showAdminActions = showAdminActions
        
        if let p = initialPost {
            _post = State(initialValue: p)
            _isLoading = State(initialValue: false)
            _isLiked = State(initialValue: p.isLiked ?? false)
            _likeCount = State(initialValue: p.likeCount ?? 0)
            _commentCount = State(initialValue: p.commentCount ?? 0)
        } else {
            _post = State(initialValue: nil)
            _isLoading = State(initialValue: true)
            _isLiked = State(initialValue: false)
            _likeCount = State(initialValue: 0)
            _commentCount = State(initialValue: 0)
        }
    }
    
    var body: some View {
        ZStack {
            // Main Content
            contentView
            
            // Hidden Link for Redirect
            if let profile = TokenManager.shared.userProfile {
                NavigationLink(
                    destination: UpdateKycView(userProfile: profile),
                    isActive: $navigateToVerification
                ) { EmptyView() }
                .hidden()
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
        .alert(isPresented: $showKYCAlert) {
            Alert(
                title: Text(kycAlertTitle),
                message: Text(kycAlertMessage),
                primaryButton: .default(Text("Xác thực ngay")) {
                    self.navigateToVerification = true
                },
                secondaryButton: .cancel(Text("Để sau"))
            )
        }
    }
    
    // MARK: - Subviews
    
    @ViewBuilder
    private var contentView: some View {
        if isLoading {
            ProgressView()
                .frame(maxWidth: .infinity, minHeight: 300)
        } else if let post = post {
            mainPostView(post)
        } else {
            EmptyView()
        }
    }
    
    private func mainPostView(_ post: Post) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                PostImageSection(images: post.images)
                
                VStack(alignment: .leading, spacing: 12) {
                    PostInfoSection(post: post)
                    
                    Divider()
                    
                    if !showAdminActions {
                        PostActionSection(
                            isLiked: $isLiked,
                            likeCount: $likeCount,
                            commentCount: $commentCount,
                            onLike: toggleLike,
                            onChat: startChat
                        )
                        .padding(.vertical, 8)
                        
                        Divider()
                    }
                    
                    PostDetailListSection(post: post, formatDate: formatDate)
                    
                    Divider()
                    
                    if let desc = post.description {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Mô tả").font(.headline)
                            Text(desc).foregroundColor(.secondary)
                        }
                    }
                    
                    Divider().padding(.vertical, 8)
                    
                    if showAdminActions {
                        adminApprovalSection(post)
                    } else {
                        PostCommentSection(
                            commentCount: commentCount,
                            commentText: $commentText,
                            comments: comments,
                            isLoadingComments: isLoadingComments,
                            onSend: sendComment
                        )
                    }
                }
                .padding()
            }
        }
    }
    
    private func adminApprovalSection(_ post: Post) -> some View {
        VStack(spacing: 16) {
            Text("Thao tác quản trị")
                .font(.headline)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            HStack(spacing: 16) {
                Button(action: approvePost) {
                    Label("Duyệt bài", systemImage: "checkmark.circle.fill")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.green)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                }
                
                Button(action: { showRejectReasonInput = true }) {
                    Label("Từ chối", systemImage: "xmark.circle.fill")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.red.opacity(0.1))
                        .foregroundColor(.red)
                        .cornerRadius(12)
                }
            }
        }
        .padding(.vertical)
        .disabled(isProcessingAdminAction)
        .opacity(isProcessingAdminAction ? 0.6 : 1.0)
        .alert("Từ chối bài đăng", isPresented: $showRejectReasonInput) {
            TextField("Lý do từ chối", text: $rejectionReason)
            Button("Hủy", role: .cancel) { rejectionReason = "" }
            Button("Xác nhận", role: .destructive) {
                rejectPost()
            }
        } message: {
            Text("Vui lòng nhập lý do từ chối bài viết này.")
        }
    }
    
    // MARK: - Admin Logic
    
    private func approvePost() {
        isProcessingAdminAction = true
        APIClient.shared.request(
            endpoint: APIConfig.Posts.approve(postId),
            method: .put
        ) { (result: Result<ApiResponse<String>, Error>) in
            isProcessingAdminAction = false
            if case .success(let response) = result, response.success {
                loadPost() // Reload to show updated status
            }
        }
    }
    
    private func rejectPost() {
        guard !rejectionReason.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        isProcessingAdminAction = true
        let reasonParam = rejectionReason.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        APIClient.shared.request(
            endpoint: APIConfig.Posts.reject(postId) + "?reason=\(reasonParam)",
            method: .put
        ) { (result: Result<ApiResponse<String>, Error>) in
            isProcessingAdminAction = false
            rejectionReason = ""
            if case .success(let response) = result, response.success {
                loadPost() // Reload
            }
        }
    }
    
    // MARK: - Logic & API Calls
    
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
        KYCHelper.shared.requireVerified(
            onSuccess: { self.performLike() },
            onFailure: { t, m in self.showAlert(t, m) }
        )
    }
    
    private func performLike() {
        isLiked.toggle()
        likeCount += isLiked ? 1 : -1
        
        APIClient.shared.request(
            endpoint: "\(APIConfig.Posts.list)/\(postId)/like",
            method: .post
        ) { (result: Result<ApiResponse<PostInteractionResponse>, Error>) in
            if case .success(let response) = result, let data = response.data {
                isLiked = data.isLiked ?? isLiked
                likeCount = data.likeCount ?? likeCount
                commentCount = data.commentCount ?? commentCount
            } else if case .failure = result {
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
               let pagedData = response.data {
                comments = pagedData.content
            }
        }
    }
    
    private func sendComment() {
        KYCHelper.shared.requireVerified(
            onSuccess: { self.performSendComment() },
            onFailure: { t, m in self.showAlert(t, m) }
        )
    }
    
    private func performSendComment() {
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
        KYCHelper.shared.requireVerified(
            onSuccess: { self.performStartChat() },
            onFailure: { t, m in self.showAlert(t, m) }
        )
    }
    
    private func performStartChat() {
        guard let sellerId = post?.sellerId else { return }
        APIClient.shared.request(
            endpoint: "/chat/conversations/\(sellerId)",
            method: .post
        ) { (result: Result<ApiResponse<Conversation>, Error>) in
            if case .success(let response) = result, let conversation = response.data {
                print("Created conversation: \(conversation.id)")
            }
        }
    }
    
    private func showAlert(_ title: String, _ message: String?) {
        self.kycAlertTitle = title
        self.kycAlertMessage = message ?? ""
        self.showKYCAlert = true
    }
    
    private func formatDate(_ dateString: String) -> String {
        let components = dateString.prefix(10).split(separator: "-")
        if components.count >= 3 {
//...
            return "\(components[2])/\(components[1])/\(components[0])"
        }
        return dateString
    }
}

// MARK: - Subviews

struct PostImageSection: View {
    let images: [String]?
    
    var body: some View {
        if let images = images, !images.isEmpty {
            TabView {
                ForEach(images, id: \.self) { imageUrl in
                    if let url = URL(string: imageUrl) {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .success(let image):
                                image.resizable().aspectRatio(contentMode: .fill)
                            default:
                                Rectangle().fill(Color.gray.opacity(0.3))
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
                .overlay(Image(systemName: "photo").font(.system(size: 50)).foregroundColor(.gray))
        }
    }
}

struct PostInfoSection: View {
    let post: Post
    
    var body: some View {
        Group {
            if let categoryName = post.categoryName {
                Text(categoryName)
                    .font(.caption)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(Color(hex: "#E8F5E9"))
                    .foregroundColor(Color(hex: "#2E7D32"))
                    .cornerRadius(12)
            }
            
            Text(post.title).font(.title2).fontWeight(.bold)
            
            if let price = post.price, let unit = post.unit {
                Text("\(formatPrice(price)) / \(unit)")
                    .font(.title3)
                    .fontWeight(.semibold)
                    .foregroundColor(Color(hex: "#2E7D32"))
            }
        }
    }
    
    private func formatPrice(_ price: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = "."
        return (formatter.string(from: NSNumber(value: price)) ?? "\(price)") + "đ"
    }
}

struct PostActionSection: View {
    @Binding var isLiked: Bool
    @Binding var likeCount: Int
    @Binding var commentCount: Int
    var onLike: () -> Void
    var onChat: () -> Void
    
    var body: some View {
        HStack(spacing: 24) {
            Button(action: onLike) {
                HStack(spacing: 4) {
                    Image(systemName: isLiked ? "heart.fill" : "heart")
                        .foregroundColor(isLiked ? .red : .gray)
                    Text("\(isLiked ? max(1, likeCount) : max(0, likeCount))")
                        .foregroundColor(isLiked ? .red : .gray)
                }
            }
            
            HStack(spacing: 4) {
                Image(systemName: "bubble.left.fill").foregroundColor(.gray)
                Text("\(commentCount)").foregroundColor(.gray)
            }
            
            Button(action: onChat) {
                HStack(spacing: 4) {
                    Image(systemName: "message.fill").foregroundColor(Color(hex: "#2E7D32"))
                    Text("Nhắn tin").foregroundColor(Color(hex: "#2E7D32"))
                }
            }
            Spacer()
        }
    }
}

struct PostDetailListSection: View {
    let post: Post
    let formatDate: (String) -> String
    
    var body: some View {
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
    }
}

struct PostCommentSection: View {
    var commentCount: Int
    @Binding var commentText: String
    var comments: [Comment]
    var isLoadingComments: Bool
    var onSend: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Bình luận (\(commentCount))").font(.headline)
            
            HStack {
                TextField("Viết bình luận...", text: $commentText)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                Button(action: onSend) {
                    Image(systemName: "paperplane.fill").foregroundColor(Color(hex: "#2E7D32"))
                }
                .disabled(commentText.trimmingCharacters(in: .whitespaces).isEmpty)
            }
            
            if isLoadingComments {
                ProgressView().frame(maxWidth: .infinity)
            } else if comments.isEmpty {
                Text("Chưa có bình luận nào").foregroundColor(.gray).frame(maxWidth: .infinity).padding()
            } else {
                ForEach(comments) { comment in
                    CommentRow(comment: comment)
                }
            }
        }
    }
}

// Re-use DetailRow from before
struct DetailRow: View {
    let icon: String
    let title: String
    let value: String
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .foregroundColor(Color(hex: "#2E7D32"))
                .frame(width: 24)
            Text(title).foregroundColor(.gray)
            Spacer()
            Text(value).fontWeight(.medium)
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
