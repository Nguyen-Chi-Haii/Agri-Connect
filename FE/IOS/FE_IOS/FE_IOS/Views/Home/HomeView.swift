import SwiftUI

struct HomeView: View {
    @State private var posts: [Post] = []
    @State private var categories: [Category] = []
    @State private var isLoading = false
    @State private var searchText = ""
    @State private var selectedCategoryId: String? = nil
    
    var filteredPosts: [Post] {
        if let categoryId = selectedCategoryId {
            return posts.filter { $0.categoryId == categoryId }
        }
        return posts
    }
    
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Search Bar
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.gray)
                    TextField("Tìm kiếm nông sản...", text: $searchText)
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
                                }
                            )
                            
                            ForEach(categories) { category in
                                CategoryChip(
                                    name: category.name,
                                    isSelected: selectedCategoryId == category.id,
                                    onTap: {
                                        selectedCategoryId = category.id
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
                        NavigationLink(destination: AllPostsView()) {
                            Text("Xem tất cả")
                                .font(.subheadline)
                                .foregroundColor(Color(hex: "#2E7D32"))
                        }
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
                            ForEach(filteredPosts) { post in
                                NavigationLink(destination: PostDetailView(postId: post.id)) {
                                    PostCard(post: post)
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
            leading: Image(systemName: "leaf.fill").foregroundColor(Color(hex: "#2E7D32")),
            trailing: NavigationLink(destination: SearchView()) {
                Image(systemName: "line.3.horizontal.decrease.circle")
                    .foregroundColor(Color(hex: "#2E7D32"))
            }
        )
        .onAppear {
            loadData()
        }
    }
    
    private func loadData() {
        isLoading = true
        
        APIClient.shared.request(
            endpoint: APIConfig.Categories.list,
            method: .get
        ) { (result: Result<ApiResponse<[Category]>, Error>) in
            if case .success(let response) = result, let data = response.data {
                categories = data
            }
        }
        
        APIClient.shared.request(
            endpoint: APIConfig.Posts.approved,
            method: .get
        ) { (result: Result<ApiResponse<[Post]>, Error>) in
            isLoading = false
            if case .success(let response) = result, let data = response.data {
                posts = data
            }
        }
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
    let post: Post
    
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
                .frame(height: 150)
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
        }
        .padding()
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
