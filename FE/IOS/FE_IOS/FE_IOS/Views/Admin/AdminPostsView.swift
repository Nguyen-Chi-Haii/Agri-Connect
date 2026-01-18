import SwiftUI

struct AdminPostsView: View {
    @State private var posts: [Post] = []
    @State private var isLoading = false
    @State private var searchText = ""
    @State private var selectedFilter = "PENDING"
    
    let filters = [
        ("PENDING", "Chờ duyệt"),
        ("APPROVED", "Đã duyệt"),
        ("REJECTED", "Từ chối"),
        ("", "Tất cả")
    ]
    
    var filteredPosts: [Post] {
        var result = posts
        
        if !searchText.isEmpty {
            result = result.filter {
                $0.title.localizedCaseInsensitiveContains(searchText)
            }
        }
        
        if !selectedFilter.isEmpty {
            result = result.filter { $0.status == selectedFilter }
        }
        
        return result
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Search
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.gray)
                TextField("Tìm bài đăng...", text: $searchText)
            }
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(12)
            .padding()
            
            // Filters
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(filters, id: \.0) { filter in
                        Button {
                            selectedFilter = filter.0
                        } label: {
                            Text(filter.1)
                                .font(.subheadline)
                                .padding(.horizontal, 16)
                                .padding(.vertical, 8)
                                .background(
                                    selectedFilter == filter.0
                                    ? Color(hex: "#2E7D32")
                                    : Color(.systemGray6)
                                )
                                .foregroundColor(
                                    selectedFilter == filter.0
                                    ? .white
                                    : .primary
                                )
                                .cornerRadius(20)
                        }
                    }
                }
                .padding(.horizontal)
            }
            
            Divider()
                .padding(.top, 8)
            
            // Posts List
            if isLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else if filteredPosts.isEmpty {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "doc.text")
                        .font(.system(size: 50))
                        .foregroundColor(.gray)
                    Text("Không có bài đăng")
                        .foregroundColor(.gray)
                }
                Spacer()
            } else {
                List(filteredPosts) { post in
                    AdminPostRow(post: post) {
                        loadPosts()
                    }
                }
                .listStyle(PlainListStyle())
            }
        }
        .navigationTitle("Quản lý bài đăng")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            loadPosts()
        }
    }
    
    private func loadPosts() {
        isLoading = true
        
        var endpoint = APIConfig.Posts.list
        if !selectedFilter.isEmpty {
            endpoint += "?status=\(selectedFilter)"
        }
        
        // DEBUG: Trace API call
        print("📡 [AdminPosts] Loading from: \(endpoint)")
        print("📡 [AdminPosts] Filter: '\(selectedFilter)')")
        
        APIClient.shared.request(
            endpoint: endpoint,
            method: .get
        ) { (result: Result<ApiResponse<[Post]>, Error>) in
            isLoading = false
            
            // DEBUG: Trace response
            switch result {
            case .success(let response):
                print("✅ [AdminPosts] Success: \(response.success)")
                if let data = response.data {
                    print("✅ [AdminPosts] Posts count: \(data.count)")
                    posts = data
                } else {
                    print("⚠️ [AdminPosts] Data is nil")
                }
            case .failure(let error):
                print("❌ [AdminPosts] Error: \(error)")
            }
        }
    }
}

// MARK: - Admin Post Row
struct AdminPostRow: View {
    let post: Post
    let onUpdate: () -> Void
    
    @State private var isProcessing = false
    @State private var errorMessage = ""
    @State private var showError = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Header
            HStack {
                if let authorName = post.authorName {
                    HStack(spacing: 8) {
                        Circle()
                            .fill(Color(hex: "#E8F5E9"))
                            .frame(width: 30, height: 30)
                            .overlay(
                                Text(String(authorName.prefix(1)))
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(Color(hex: "#2E7D32"))
                            )
                        
                        Text(authorName)
                            .font(.caption)
                            .foregroundColor(.gray)
                    }
                }
                
                Spacer()
                
                PostStatusBadge(status: post.status ?? "")
            }
            
            // Title
            Text(post.title)
                .font(.headline)
                .lineLimit(2)
            
            // Price
            if let price = post.price, let unit = post.unit {
                Text("\(formatPrice(price)) / \(unit)")
                    .font(.subheadline)
                    .foregroundColor(Color(hex: "#2E7D32"))
            }
            
            // Actions
            if post.status == "PENDING" {
                HStack(spacing: 12) {
                    Button {
                        approvePost()
                    } label: {
                        HStack {
                            Image(systemName: "checkmark")
                            Text("Duyệt")
                        }
                        .font(.caption)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(Color.green.opacity(0.2))
                        .foregroundColor(.green)
                        .cornerRadius(8)
                    }
                    
                    Button {
                        rejectPost()
                    } label: {
                        HStack {
                            Image(systemName: "xmark")
                            Text("Từ chối")
                        }
                        .font(.caption)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(Color.red.opacity(0.2))
                        .foregroundColor(.red)
                        .cornerRadius(8)
                    }
                }
            }
        }
        .padding(.vertical, 8)
        .alert(isPresented: $showError) {
            Alert(title: Text("Lỗi"), message: Text(errorMessage), dismissButton: .default(Text("OK")))
        }
        .disabled(isProcessing)
        .opacity(isProcessing ? 0.6 : 1.0)
    }
    
    private func formatPrice(_ price: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = "."
        return (formatter.string(from: NSNumber(value: price)) ?? "\(price)") + "đ"
    }
    
    private func approvePost() {
        isProcessing = true
        APIClient.shared.request(
            endpoint: "/posts/\(post.id)/approve",
            method: .put,
            body: nil as String?
        ) { (result: Result<ApiResponse<String>, Error>) in
            isProcessing = false
            switch result {
            case .success(let response):
                if response.success {
                    onUpdate()
                } else {
                    errorMessage = response.message ?? "Duyệt bài thất bại"
                    showError = true
                }
            case .failure(let error):
                errorMessage = "Lỗi: \(error.localizedDescription)"
                showError = true
            }
        }
    }
    
    private func rejectPost() {
        isProcessing = true
        APIClient.shared.request(
            endpoint: "/posts/\(post.id)/reject",
            method: .put,
            body: nil as String?
        ) { (result: Result<ApiResponse<String>, Error>) in
            isProcessing = false
            switch result {
            case .success(let response):
                if response.success {
                    onUpdate()
                } else {
                    errorMessage = response.message ?? "Từ chối bài thất bại"
                    showError = true
                }
            case .failure(let error):
                errorMessage = "Lỗi: \(error.localizedDescription)"
                showError = true
            }
        }
    }
}

// MARK: - Post Status Badge
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

struct AdminPostsView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            AdminPostsView()
        }
    }
}
