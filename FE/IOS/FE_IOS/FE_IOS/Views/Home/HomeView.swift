import SwiftUI

struct HomeView: View {
    @State private var posts: [Post] = []
    @State private var categories: [Category] = []
    @State private var isLoading = false
    @State private var searchText = ""
    @State private var selectedCategoryId: String? = nil
    
    // KYC Alert State
    @State private var showKYCAlert = false
    @State private var kycAlertTitle = ""
    @State private var kycAlertMessage = ""
    @State private var navigateToVerification = false
    
    var filteredPosts: [Post] {
        return posts
    }
    
    var body: some View {
        ScrollView {
            // Hidden Link for Redirect
            if let profile = TokenManager.shared.userProfile {
                NavigationLink(
                    destination: UpdateKycView(userProfile: profile),
                    isActive: $navigateToVerification
                ) { EmptyView() }
                .hidden()
            }

            VStack(spacing: 16) {
                // Search Bar
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.gray)
                    TextField("Tìm kiếm nông sản...", text: $searchText)
                        .onSubmit {
                            loadData()
                        }
                }
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(12)
                .padding(.horizontal)
                
                // Categories
                VStack(alignment: .leading, spacing: 12) {
                    Text("Danh mục")
                        .font(.headline)
                        .padding(.horizontal)
                    
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            // All Categories
                            CategoryChip(
                                name: "Tất cả",
                                isSelected: selectedCategoryId == nil,
                                onTap: {
                                    selectedCategoryId = nil
                                    loadData()
                                }
                            )
                            
                            ForEach(categories) { category in
                                CategoryChip(
                                    name: category.name,
                                    isSelected: selectedCategoryId == category.id,
                                    onTap: {
                                        selectedCategoryId = category.id
                                        loadData()
                                    }
                                )
                            }
                        }
                        .padding(.horizontal)
                    }
                }
                
                // Recent Posts
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Text("Bài đăng mới")
                            .font(.headline)
                        Spacer()
                    }
                    .padding(.horizontal)
                    
                    if isLoading {
                        ProgressView()
                            .frame(maxWidth: .infinity)
                            .padding()
                    } else if posts.isEmpty {
                        Text("Chưa có bài đăng nào")
                            .foregroundColor(.gray)
                            .frame(maxWidth: .infinity)
                            .padding()
                    } else {
                        LazyVStack(spacing: 12) {
                            ForEach($posts) { $post in
                                NavigationLink(destination: PostDetailView(postId: post.id, initialPost: post)) {
                                    PostCard(post: $post, onKYCRequired: { title, message in
                                        self.showAlert(title, message)
                                    })
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                        }
                        .padding(.horizontal)
                    }
                }
            }
            .padding(.vertical)
        }
        .navigationTitle("Agri-Connect")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarItems(
            leading: Image(systemName: "leaf.fill").foregroundColor(Color(hex: "#2E7D32"))
        )
        .onAppear {
            loadData()
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
    
    private func loadData() {
        isLoading = true
        // Load categories once
        if categories.isEmpty {
            APIClient.shared.request(
                endpoint: APIConfig.Categories.list,
                method: .get
            ) { (result: Result<ApiResponse<[Category]>, Error>) in
                if case .success(let response) = result, let data = response.data {
                    categories = data
                }
            }
        }
        
        searchPosts()
    }
    
    private func searchPosts() {
        isLoading = true
        var endpoint = APIConfig.Posts.approved
        
        // If searching or category selected, use main list with query params
        if !searchText.isEmpty || selectedCategoryId != nil {
            var params: [String] = []
            if !searchText.isEmpty {
                params.append("search=\(searchText.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? "")")
            }
            if let catId = selectedCategoryId {
                params.append("categoryId=\(catId)")
            }
            endpoint = "\(APIConfig.Posts.list)?\(params.joined(separator: "&"))"
        }
        
        APIClient.shared.request(
            endpoint: endpoint,
            method: .get
        ) { (result: Result<ApiResponse<[Post]>, Error>) in
            isLoading = false
            if case .success(let response) = result, let data = response.data {
                posts = data
            }
        }
    }
    private func showAlert(_ title: String, _ message: String?) {
        self.kycAlertTitle = title
        self.kycAlertMessage = message ?? ""
        self.showKYCAlert = true
    }
}

// MARK: - Category Card
struct CategoryCard: View {
    let category: Category
    
    var body: some View {
        VStack(spacing: 8) {
            Text(category.icon ?? "🌿")
                .font(.system(size: 30))
            
            Text(category.name)
                .font(.caption)
                .foregroundColor(.primary)
                .lineLimit(1)
        }
        .frame(width: 80, height: 80)
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: .gray.opacity(0.2), radius: 4, x: 0, y: 2)
    }
}

// MARK: - Post Card
struct PostCard: View {
    @Binding var post: Post
    var onKYCRequired: ((String, String?) -> Void)?
    
    // Remote Image Handling
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Image placeholder
            if let images = post.images, let firstImage = images.first, let url = URL(string: firstImage) {
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
                .frame(maxWidth: .infinity, maxHeight: 200)
                .clipped()
                .cornerRadius(12)
            }
            
            // Content
            VStack(alignment: .leading, spacing: 4) {
                Text(post.title)
                    .font(.headline)
                    .lineLimit(2)
                    .foregroundColor(.primary)
                
                if let price = post.price, let unit = post.unit {
                    Text("\(formatPrice(price)) / \(unit)")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(Color(hex: "#2E7D32"))
                }
                
                HStack {
                    if let location = post.province {
                        HStack(spacing: 4) {
                            Image(systemName: "mappin")
                            Text(location)
                        }
                        .font(.caption)
                        .foregroundColor(.gray)
                    }
                    
                    Spacer()
                    
                    if let author = post.authorName {
                        Text(author)
                            .font(.caption)
                            .foregroundColor(.gray)
                    }
                }
            }
            .padding(.horizontal, 4)
            
            Divider()
                .padding(.vertical, 4)
            
            // Interaction Buttons
            HStack(spacing: 20) {
                // Like
                Button(action: toggleLike) {
                    HStack(spacing: 4) {
                        Image(systemName: (post.isLiked ?? false) ? "heart.fill" : "heart")
                            .foregroundColor((post.isLiked ?? false) ? .red : .gray)
                        Text("\((post.isLiked ?? false) ? max(1, post.likeCount ?? 0) : max(0, post.likeCount ?? 0))")
                            .font(.caption)
                            .foregroundColor(.gray)
                    }
                }
                
                // Comment
                NavigationLink(destination: PostDetailView(postId: post.id, initialPost: post)) {
                    HStack(spacing: 4) {
                        Image(systemName: "bubble.left")
                            .foregroundColor(.gray)
                        Text("\(post.commentCount ?? 0)")
                            .font(.caption)
                            .foregroundColor(.gray)
                    }
                }
                
                // Chat
                Button(action: startChat) {
                    HStack(spacing: 4) {
                        Image(systemName: "message")
                            .foregroundColor(Color(hex: "#2E7D32"))
                        Text("Nhắn tin")
                            .font(.caption)
                            .foregroundColor(Color(hex: "#2E7D32"))
                    }
                }
                
                Spacer()
            }
            .padding(.horizontal, 8)
            .padding(.bottom, 12)
        }
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: .gray.opacity(0.15), radius: 6, x: 0, y: 2)
    }
    
    private func formatPrice(_ price: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = "."
        return (formatter.string(from: NSNumber(value: price)) ?? "\(price)") + "đ"
    }
    
    private func toggleLike() {
        KYCHelper.shared.requireVerified(
            onSuccess: { self.performLike() },
            onFailure: { t, m in self.onKYCRequired?(t, m) }
        )
    }

    private func performLike() {
        // Optimistic update
        let wasLiked = post.isLiked ?? false
        let currentCount = post.likeCount ?? 0
        
        post.isLiked = !wasLiked
        post.likeCount = currentCount + (post.isLiked! ? 1 : -1)
        
        APIClient.shared.request(
            endpoint: "/posts/\(post.id)/like",
            method: .post
        ) { (result: Result<ApiResponse<PostInteractionResponse>, Error>) in
            if case .success(let response) = result, let data = response.data {
                post.likeCount = data.likeCount ?? post.likeCount
                
                if let status = data.isLiked {
                    post.isLiked = status
                } else {
                    // Fallback: If backend missing isLiked, prevents Red Heart + 0 Count
                    if (post.likeCount ?? 0) <= 0 {
                        post.isLiked = false
                    }
                    // If count > 0, we keep optimistic state or previous state
                }
            } else if case .failure = result {
                // Revert
                post.isLiked = wasLiked
                post.likeCount = currentCount
            }
        }
    }
    
    private func startChat() {
        guard let sellerId = post.sellerId else { return }
        
        APIClient.shared.request(
            endpoint: "/chat/conversations/\(sellerId)",
            method: .post
        ) { (result: Result<ApiResponse<Conversation>, Error>) in
            // Navigation handled by parent or deep link
             print("Chat requested for seller: \(sellerId)")
        }
    }
}

// Placeholder view
struct AllPostsView: View {
    var body: some View {
        Text("All Posts")
            .navigationTitle("Tất cả bài đăng")
    }
}

struct HomeView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            HomeView()
        }
    }
}
