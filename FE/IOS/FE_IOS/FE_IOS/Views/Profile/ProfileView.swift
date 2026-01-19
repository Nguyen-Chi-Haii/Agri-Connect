import SwiftUI

struct ProfileView: View {
    @State private var userProfile: UserProfile?
    @State private var isLoading = false
    @State private var showLogoutAlert = false
    @State private var navigateToLogin = false
    
    var body: some View {
        ZStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Profile Header
                    VStack(spacing: 12) {
                        // Avatar
                        ZStack {
                            Circle()
                                .fill(Color(hex: "#E8F5E9"))
                                .frame(width: 100, height: 100)
                            
                            if let avatar = userProfile?.avatar, let url = URL(string: avatar) {
                                AsyncImage(url: url) { phase in
                                    switch phase {
                                    case .success(let image):
                                        image
                                            .resizable()
                                            .aspectRatio(contentMode: .fill)
                                    default:
                                        Image(systemName: "person.fill")
                                            .font(.system(size: 40))
                                            .foregroundColor(Color(hex: "#2E7D32"))
                                    }
                                }
                                .frame(width: 100, height: 100)
                                .clipShape(Circle())
                            } else {
                                Image(systemName: "person.fill")
                                    .font(.system(size: 40))
                                    .foregroundColor(Color(hex: "#2E7D32"))
                            }
                            
                            // Verified badge
                            if userProfile?.verified == true {
                                Image(systemName: "checkmark.seal.fill")
                                    .foregroundColor(.blue)
                                    .font(.title3)
                                    .offset(x: 35, y: 35)
                            }
                        }
                        
                        // Name
                        Text(userProfile?.fullName ?? TokenManager.shared.userName ?? "Đang tải...")
                            .font(.title2)
                            .fontWeight(.bold)
                        
                        // Role
                        Text(roleText)
                            .font(.subheadline)
                            .foregroundColor(.gray)
                        
                        // Phone
                        if let phone = userProfile?.phone {
                            HStack {
                                Image(systemName: "phone.fill")
                                Text(phone)
                            }
                            .font(.subheadline)
                            .foregroundColor(.gray)
                        }
                    }
                    .padding()
                    
                    // Menu Items
                    VStack(spacing: 0) {
                        NavigationLink(destination: MyPostsListView()) {
                            ProfileMenuRow(icon: "doc.text.fill", title: "Bài đăng của tôi", color: .blue)
                        }
                        
                        Divider().padding(.horizontal)
                        
                        NavigationLink(destination: EditProfileFormView()) {
                            ProfileMenuRow(icon: "pencil.circle.fill", title: "Chỉnh sửa thông tin", color: .orange)
                        }
                        
                        Divider().padding(.horizontal)
                        
                        NavigationLink(destination: StatisticsDetailView()) {
                            ProfileMenuRow(icon: "chart.bar.fill", title: "Thống kê", color: .purple)
                        }
                        
                        if TokenManager.shared.userRole == "ADMIN" {
                            Divider().padding(.horizontal)
                            
                            NavigationLink(destination: AdminDashboardView()) {
                                ProfileMenuRow(icon: "shield.fill", title: "Quản trị viên", color: .red)
                            }
                        }
                    }
                    .background(Color.white)
                    .cornerRadius(16)
                    .padding(.horizontal)
                    
                    // Logout Button
                    Button(action: { showLogoutAlert = true }) {
                        HStack {
                            Image(systemName: "rectangle.portrait.and.arrow.right")
                            Text("Đăng xuất")
                        }
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.white)
                        .cornerRadius(12)
                    }
                    .padding(.horizontal)
                    
                    // Hidden navigation for logout
                    NavigationLink(destination: LoginView().navigationBarHidden(true), isActive: $navigateToLogin) {
                        EmptyView()
                    }
                }
                .padding(.vertical)
            }
            .background(Color(.systemGray6))
        }
        .navigationTitle("Hồ sơ")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            loadProfile()
        }
        .alert(isPresented: $showLogoutAlert) {
            Alert(
                title: Text("Đăng xuất"),
                message: Text("Bạn có chắc chắn muốn đăng xuất?"),
                primaryButton: .destructive(Text("Đăng xuất")) {
                    logout()
                },
                secondaryButton: .cancel(Text("Hủy"))
            )
        }
    }
    
    private var roleText: String {
        switch TokenManager.shared.userRole {
        case "FARMER": return "🌾 Nông dân"
        case "TRADER": return "🚛 Thương lái"
        case "ADMIN": return "👨‍💼 Quản trị viên"
        default: return ""
        }
    }
    
    private func loadProfile() {
        isLoading = true
        
        APIClient.shared.request(
            endpoint: APIConfig.Users.profile,
            method: .get
        ) { (result: Result<ApiResponse<UserProfile>, Error>) in
            isLoading = false
            if case .success(let response) = result, let data = response.data {
                userProfile = data
            }
        }
    }
    
    private func logout() {
        TokenManager.shared.clearAll()
        navigateToLogin = true
    }
}

// MARK: - Profile Menu Row
struct ProfileMenuRow: View {
    let icon: String
    let title: String
    let color: Color
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .foregroundColor(color)
                .frame(width: 30)
            
            Text(title)
                .foregroundColor(.primary)
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .foregroundColor(.gray)
                .font(.caption)
        }
        .padding()
    }
}

// Placeholder views
struct MyPostsListView: View {
    @State private var posts: [Post] = []
    @State private var isLoading = false
    @State private var showDeleteAlert = false
    @State private var postToDelete: Post?
    
    var body: some View {
        Group {
            if isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if posts.isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: "doc.text")
                        .font(.system(size: 60))
                        .foregroundColor(.gray)
                    Text("Chưa có bài đăng nào")
                        .font(.headline)
                        .foregroundColor(.gray)
                    NavigationLink(destination: CreatePostView()) {
                        Text("Tạo bài đăng mới")
                            .foregroundColor(.white)
                            .padding()
                            .background(Color(hex: "#2E7D32"))
                            .cornerRadius(12)
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                List {
                    ForEach(posts) { post in
                        NavigationLink(destination: PostDetailView(postId: post.id)) {
                            MyPostRow(post: post, onDelete: {
                                postToDelete = post
                                showDeleteAlert = true
                            })
                        }
                    }
                }
            }
        }
        .navigationTitle("Bài đăng của tôi")
        .onAppear { loadMyPosts() }
        .alert(isPresented: $showDeleteAlert) {
            Alert(
                title: Text("Xác nhận xóa"),
                message: Text("Bạn có chắc muốn xóa bài đăng này?"),
                primaryButton: .destructive(Text("Xóa")) {
                    if let post = postToDelete {
                        deletePost(post)
                    }
                },
                secondaryButton: .cancel(Text("Hủy"))
            )
        }
    }
    
    private func loadMyPosts() {
        isLoading = true
        APIClient.shared.request(
            endpoint: APIConfig.Posts.myPosts,
            method: .get
        ) { (result: Result<ApiResponse<[Post]>, Error>) in
            isLoading = false
            if case .success(let response) = result, let postList = response.data {
                posts = postList
            }
        }
    }
    
    private func deletePost(_ post: Post) {
        APIClient.shared.request(
            endpoint: "\(APIConfig.Posts.list)/\(post.id)",
            method: .delete
        ) { (result: Result<ApiResponse<EmptyResponse>, Error>) in
            if case .success = result {
                posts.removeAll { $0.id == post.id }
            }
        }
    }
}

struct EditProfileFormView: View {
    @State private var fullName = ""
    @State private var phone = ""
    @State private var address = ""
    @State  private var selectedImage: UIImage?
    @State private var showImagePicker = false
    @State private var isLoading = false
    @State private var showSuccess = false
    @State private var errorMessage = ""
    @State private var showError = false
    
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        Form {
            Section("Ảnh đại diện") {
                HStack {
                    Spacer()
                    Button(action: { showImagePicker = true }) {
                        if let image = selectedImage {
                            Image(uiImage: image)
                                .resizable()
                                .scaledToFill()
                                .frame(width: 100, height: 100)
                                .clipShape(Circle())
                        } else {
                            ZStack {
                                Circle()
                                    .fill(Color(hex: "#E8F5E9"))
                                    .frame(width: 100, height: 100)
                                Image(systemName: "person.fill")
                                    .font(.system(size: 40))
                                    .foregroundColor(Color(hex: "#2E7D32"))
                            }
                        }
                    }
                    Spacer()
                }
            }
            
            Section("Thông tin cá nhân") {
                ValidatedFormField(
                    title: "Họ tên",
                    placeholder: "Nguyễn Văn A",
                    text: $fullName,
                    error: .constant(nil)
                )
                
                ValidatedFormField(
                    title: "Số điện thoại",
                    placeholder: "0912345678",
                    text: $phone,
                    error: .constant(nil),
                    keyboardType: .phonePad
                )
                
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text("Địa chỉ")
                            .font(.caption)
                            .foregroundColor(.gray)
                        Spacer()
                        LocationFillButton(
                            locationManager: LocationManager.shared,
                            onAddressReceived: { province, district in
                                address = "\(district), \(province)"
                            }
                        )
                    }
                    
                    TextField("Nhập địa chỉ", text: $address)
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(12)
                }
            }
            
            Section {
                Button(action: saveProfile) {
                    if isLoading {
                        ProgressView()
                            .frame(maxWidth: .infinity)
                    } else {
                        Text("Lưu thay đổi")
                            .frame(maxWidth: .infinity)
                            .foregroundColor(.white)
                    }
                }
                .listRowBackground(Color(hex: "#2E7D32"))
                .disabled(isLoading)
            }
        }
        .navigationTitle("Chỉnh sửa hồ sơ")
        .onAppear { loadCurrentProfile() }
        .sheet(isPresented: $showImagePicker) {
            ImagePicker(image: $selectedImage)
        }
        .alert("Thành công", isPresented: $showSuccess) {
            Button("OK") {
                presentationMode.wrappedValue.dismiss()
            }
        } message: {
            Text("Đã cập nhật thông tin")
        }
        .alert("Lỗi", isPresented: $showError) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(errorMessage)
        }
    }
    
    private func loadCurrentProfile() {
        APIClient.shared.request(
            endpoint: "/users/profile",
            method: .get
        ) { (result: Result<ApiResponse<UserProfile>, Error>) in
            if case .success(let response) = result, let profile = response.data {
                fullName = profile.fullName
                phone = profile.phone ?? ""
                address = profile.address ?? ""
            }
        }
    }
    
    private func saveProfile() {
        isLoading = true
        
        let body: [String: String] = [
            "fullName": fullName,
            "phone": phone,
            "address": address
        ]
        
        APIClient.shared.request(
            endpoint: "/users/profile",
            method: .put,
            body: body
        ) { (result: Result<ApiResponse<UserProfile>, Error>) in
            isLoading = false
            
            if case .success = result {
                showSuccess = true
            } else if case .failure(let error) = result {
                errorMessage = error.localizedDescription
                showError = true
            }
        }
    }
}

struct StatisticsDetailView: View {
    @State private var stats: StatisticsData?
    @State private var isLoading = false
    
    var body: some View {
        ScrollView {
            if isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, minHeight: 300)
            } else if let stats = stats {
                VStack(spacing: 20) {
                    // Overview Cards
                    VStack(spacing: 16) {
                        StatCard(icon: "doc.text.fill", title: "Tổng bài đăng", value: "\(stats.totalPosts)", color: .blue)
                        StatCard(icon: "checkmark.circle.fill", title: "Đã duyệt", value: "\(stats.approvedPosts)", color: .green)
                        StatCard(icon: "clock.fill", title: "Chờ duyệt", value: "\(stats.pendingPosts)", color: .orange)
                        StatCard(icon: "hand.thumbsup.fill", title: "Tương tác", value: "\(stats.totalInteractions)", color: .purple)
                    }
                    .padding()
                }
            } else {
                Text("Không có dữ liệu thống kê")
                    .foregroundColor(.gray)
                    .frame(maxWidth: .infinity, minHeight: 300)
            }
        }
        .navigationTitle("Thống kê")
        .onAppear { loadStatistics() }
    }
    
    private func loadStatistics() {
        isLoading = true
        APIClient.shared.request(
            endpoint: "/users/statistics",
            method: .get
        ) { (result: Result<ApiResponse<StatisticsData>, Error>) in
            isLoading = false
            if case .success(let response) = result, let data = response.data {
                stats = data
            }
        }
    }
}

// MARK: - Statistics Data Model
struct StatisticsData: Codable {
    let totalPosts: Int
    let approvedPosts: Int
    let pendingPosts: Int
    let totalInteractions: Int
    
    enum CodingKeys: String, CodingKey {
        case totalPosts, approvedPosts, pendingPosts, totalInteractions
    }
}

struct ProfileView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ProfileView()
        }
    }
}
