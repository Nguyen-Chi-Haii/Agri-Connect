import SwiftUI

struct AdminPostsView: View {
    @State private var posts: [Post] = []
    @State private var isLoading = false
    @State private var searchText = ""
    @State private var selectedFilter = "PENDING"
    
    // Pagination
    @State private var currentPage = 0
    @State private var totalPages = 1
    private let pageSize = 10
    
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
        
        // Client-side filtering for search text only (since API handles status)
        // If API supports search, we could remove this too, but keeping for safety.
        if !searchText.isEmpty {
            result = result.filter {
                $0.title.localizedCaseInsensitiveContains(searchText)
            }
        }
        
        // Removed client-side status filter as it's handled by API
        
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
                            loadPosts(page: 0) // Reset to page 0 and reload
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
                            loadPosts(page: currentPage)
                        }
                    }
                    .listStyle(PlainListStyle())
                    
                    // Pagination Controls
                    if totalPages > 1 {
                        HStack {
                            Button(action: {
                                if currentPage > 0 {
                                    loadPosts(page: currentPage - 1)
                                }
                            }) {
                                Image(systemName: "chevron.left")
                                    .padding(8)
                                    .background(Color(.systemGray6))
                                    .clipShape(Circle())
                            }
                            .disabled(currentPage == 0)
                            
                            Text("Trang \(currentPage + 1) / \(totalPages)")
                                .font(.caption)
                                .foregroundColor(.gray)
                            
                            Button(action: {
                                if currentPage < totalPages - 1 {
                                    loadPosts(page: currentPage + 1)
                                }
                            }) {
                                Image(systemName: "chevron.right")
                                    .padding(8)
                                    .background(Color(.systemGray6))
                                    .clipShape(Circle())
                            }
                            .disabled(currentPage >= totalPages - 1)
                        }
                        .padding(.vertical, 8)
                    }
                }
        }
        .navigationTitle("Quản lý bài đăng")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            loadPosts(page: 0)
        }
    }
    
    private func loadPosts(page: Int = 0) {
        isLoading = true
        
        var endpoint = APIConfig.Posts.list
        var params = ["page": "\(page)", "size": "\(pageSize)"]
        if !selectedFilter.isEmpty {
            params["status"] = selectedFilter
        }
        
        let queryString = params.map { "\($0.key)=\($0.value)" }.joined(separator: "&")
        endpoint += "?\(queryString)"
        
        // DEBUG: Trace API call
        print("📡 [AdminPosts] Base URL: \(APIConfig.baseURL)")
        print("📡 [AdminPosts] Loading from: \(endpoint)")
        
        APIClient.shared.request(
            endpoint: endpoint,
            method: .get
        ) { (result: Result<ApiResponse<PagedResponse<Post>>, Error>) in
            isLoading = false
            
            switch result {
            case .success(let response):
                print("✅ [AdminPosts] Success: \(response.success)")
                if let data = response.data {
                    print("✅ [AdminPosts] Posts count: \(data.content.count)")
                    posts = data.content
                    currentPage = data.page
                    totalPages = data.totalPages
                }
            case .failure(let error):
                print("❌ [AdminPosts] Error: \(error)")
                // Fallback attempt for array if pagination fails
                loadPostsAsArray()
            }
        }
    }
    
    private func loadPostsAsArray() {
        // Fallback for array response
        var endpoint = APIConfig.Posts.list
        if !selectedFilter.isEmpty {
            endpoint += "?status=\(selectedFilter)"
        }
        
        APIClient.shared.request(
            endpoint: endpoint,
            method: .get
        ) { (result: Result<ApiResponse<[Post]>, Error>) in
            if case .success(let response) = result, let data = response.data {
               self.posts = data
               self.currentPage = 0
               self.totalPages = 1
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
            // Actions
            if post.status == "PENDING" {
                HStack(spacing: 12) {
                    Button(action: approvePost) {
                        Label("Duyệt", systemImage: "checkmark")
                            .font(.caption)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(Color.green.opacity(0.2))
                            .foregroundColor(.green)
                            .cornerRadius(8)
                    }
                    
                    Button(action: showRejectAlert) {
                        Label("Từ chối", systemImage: "xmark")
                            .font(.caption)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(Color.red.opacity(0.2))
                            .foregroundColor(.red)
                            .cornerRadius(8)
                    }
                }
            } else {
                // Actions for other statuses
                HStack {
                    if post.status != "CLOSED" {
                        Button(action: showCloseAlert) {
                            Text("Đóng")
                                .font(.caption)
                                .foregroundColor(.orange)
                        }
                    }
                    
                    Button(action: showDeleteAlert) {
                        Text("Xóa")
                            .font(.caption)
                            .foregroundColor(.red)
                    }
                }
            }
        }
        .padding(.vertical, 8)
        .alert(isPresented: $showError) {
            Alert(title: Text("Lỗi"), message: Text(errorMessage), dismissButton: .default(Text("OK")))
        }
        // Confirmation Alerts
        .alert("Xác nhận", isPresented: $showConfirmation) {
            Button("Hủy", role: .cancel) {}
            Button(confirmationActionTitle, role: .destructive) {
                confirmAction()
            }
        } message: {
            Text(confirmationMessage)
        }
        // Inputs
        .background(
            TextFieldAlert(
                isPresented: $showReasonInput,
                title: "Từ chối bài đăng",
                text: $rejectionReason,
                placeholder: "Nhập lý do từ chối",
                action: rejectPost
            )
        )
        .disabled(isProcessing)
        .opacity(isProcessing ? 0.6 : 1.0)
    }
    
    // Action States
    @State private var showReasonInput = false
    @State private var rejectionReason = ""
    @State private var showConfirmation = false
    @State private var confirmationAction: (() -> Void)?
    @State private var confirmationMessage = ""
    @State private var confirmationActionTitle = ""
    
    private func formatPrice(_ price: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = "."
        return (formatter.string(from: NSNumber(value: price)) ?? "\(price)") + "đ"
    }
    
    // MARK: - Actions
    private func showRejectAlert() {
        rejectionReason = ""
        showReasonInput = true
    }
    
    private func showCloseAlert() {
        confirmationMessage = "Bạn có chắc chắn muốn đóng bài đăng này?"
        confirmationActionTitle = "Đóng"
        confirmationAction = closePost
        showConfirmation = true
    }
    
    private func showDeleteAlert() {
        confirmationMessage = "Bạn có chắc chắn muốn xóa bài đăng này? Hành động không thể hoàn tác."
        confirmationActionTitle = "Xóa"
        confirmationAction = deletePost
        showConfirmation = true
    }
    
    private func confirmAction() {
        confirmationAction?()
    }
    
    private func approvePost() {
        isProcessing = true
        APIClient.shared.request(
            endpoint: APIConfig.Posts.approve(post.id),
            method: .put
        ) { (result: Result<ApiResponse<String>, Error>) in
            isProcessing = false
            handleResult(result)
        }
    }
    
    private func rejectPost() {
        isProcessing = true
        let reasonParam = rejectionReason.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        APIClient.shared.request(
            endpoint: APIConfig.Posts.reject(post.id) + "?reason=\(reasonParam)",
            method: .put
        ) { (result: Result<ApiResponse<String>, Error>) in
            isProcessing = false
            handleResult(result)
        }
    }
    
    private func closePost() {
        isProcessing = true
        APIClient.shared.request(
            endpoint: APIConfig.Posts.close(post.id),
            method: .put
        ) { (result: Result<ApiResponse<String>, Error>) in
            isProcessing = false
            handleResult(result)
        }
    }
    
    private func deletePost() {
        isProcessing = true
        APIClient.shared.request(
            endpoint: APIConfig.Posts.delete(post.id),
            method: .delete
        ) { (result: Result<ApiResponse<String>, Error>) in
            isProcessing = false
            handleResult(result)
        }
    }
    
    private func handleResult(_ result: Result<ApiResponse<String>, Error>) {
        switch result {
        case .success(let response):
            if response.success {
                onUpdate()
            } else {
                errorMessage = response.message ?? "Thao tác thất bại"
                showError = true
            }
        case .failure(let error):
            errorMessage = "Lỗi: \(error.localizedDescription)"
            showError = true
        }
    }
}

// MARK: - Helper Views
struct TextFieldAlert: UIViewControllerRepresentable {
    @Binding var isPresented: Bool
    let title: String
    @Binding var text: String
    let placeholder: String
    let action: () -> Void
    
    func makeUIViewController(context: Context) -> UIViewController {
        return UIViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        guard isPresented && uiViewController.presentedViewController == nil else { return }
        
        let alert = UIAlertController(title: title, message: nil, preferredStyle: .alert)
        alert.addTextField { textField in
            textField.placeholder = placeholder
            textField.text = text
        }
        
        alert.addAction(UIAlertAction(title: "Hủy", style: .cancel) { _ in
            isPresented = false
        })
        
        alert.addAction(UIAlertAction(title: "Xác nhận", style: .destructive) { _ in
            if let textField = alert.textFields?.first {
                text = textField.text ?? ""
                action()
            }
            isPresented = false
        })
        
        DispatchQueue.main.async {
            uiViewController.present(alert, animated: true)
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
