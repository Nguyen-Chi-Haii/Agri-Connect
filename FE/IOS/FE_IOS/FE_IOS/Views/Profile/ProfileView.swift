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
    var body: some View {
        Text("My Posts")
            .navigationTitle("Bài đăng của tôi")
    }
}

struct EditProfileFormView: View {
    var body: some View {
        Text("Edit Profile")
            .navigationTitle("Chỉnh sửa")
    }
}

struct StatisticsDetailView: View {
    var body: some View {
        Text("Statistics")
            .navigationTitle("Thống kê")
    }
}

struct ProfileView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ProfileView()
        }
    }
}
