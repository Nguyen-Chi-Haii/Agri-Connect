import SwiftUI

struct AdminDashboardView: View {
    @State private var stats: DashboardStats?
    @State private var isLoading = false
    @State private var navigateToLogin = false
    
    var body: some View {
        NavigationView {
            ZStack {
                Color(.systemGray6)
                    .ignoresSafeArea()
                
                if isLoading {
                    ProgressView("Đang tải...")
                } else {
                    ScrollView {
                        VStack(spacing: 20) {
                            // Welcome Header
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("Xin chào, Admin")
                                        .font(.title2)
                                        .fontWeight(.bold)
                                    Text("Bảng điều khiển quản trị")
                                        .foregroundColor(.gray)
                                }
                                Spacer()
                                
                                Image(systemName: "shield.fill")
                                    .font(.title)
                                    .foregroundColor(Color(hex: "#2E7D32"))
                            }
                            .padding()
                            .background(Color.white)
                            .cornerRadius(16)
                            
                            // Stats Cards
                            LazyVGrid(columns: [
                                GridItem(.flexible()),
                                GridItem(.flexible())
                            ], spacing: 16) {
                                StatCard(
                                    icon: "person.3.fill",
                                    title: "Người dùng",
                                    value: "\(stats?.totalUsers ?? 0)",
                                    color: .blue
                                )
                                
                                StatCard(
                                    icon: "doc.text.fill",
                                    title: "Bài đăng",
                                    value: "\(stats?.totalPosts ?? 0)",
                                    color: .green
                                )
                                
                                StatCard(
                                    icon: "clock.fill",
                                    title: "Chờ duyệt",
                                    value: "\(stats?.pendingPosts ?? 0)",
                                    color: .orange
                                )
                                
                                StatCard(
                                    icon: "checkmark.seal.fill",
                                    title: "Chờ KYC",
                                    value: "\(stats?.pendingKyc ?? 0)",
                                    color: .purple
                                )
                            }
                            .padding(.horizontal)
                            
                            // Hidden navigation
                            NavigationLink(destination: LoginView().navigationBarHidden(true), isActive: $navigateToLogin) {
                                EmptyView()
                            }
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Quản trị")
            .navigationBarTitleDisplayMode(.inline)
            .onAppear {
                loadStats()
            }
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
    
    private func loadStats() {
        isLoading = true
        
        APIClient.shared.request(
            endpoint: "/admin/dashboard",
            method: .get
        ) { (result: Result<ApiResponse<DashboardStats>, Error>) in
            isLoading = false
            if case .success(let response) = result, let data = response.data {
                stats = data
            }
        }
    }
    

}

// MARK: - Dashboard Stats Model
struct DashboardStats: Decodable {
    let totalUsers: Int?
    let totalPosts: Int?
    let pendingPosts: Int?
    let pendingKyc: Int?
}

// MARK: - Stat Card
struct StatCard: View {
    let icon: String
    let title: String
    let value: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(color)
            
            Text(value)
                .font(.title)
                .fontWeight(.bold)
            
            Text(title)
                .font(.caption)
                .foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color.white)
        .cornerRadius(16)
    }
}

// MARK: - Admin Menu Row
struct AdminMenuRow: View {
    let icon: String
    let title: String
    let subtitle: String
    let color: Color
    
    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(color)
                .frame(width: 40)
            
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.headline)
                    .foregroundColor(.primary)
                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .foregroundColor(.gray)
        }
        .padding()
    }
}

// Placeholder View
struct AdminStatsView: View {
    var body: some View {
        Text("Admin Statistics")
            .navigationTitle("Thống kê")
    }
}

struct AdminDashboardView_Previews: PreviewProvider {
    static var previews: some View {
        AdminDashboardView()
    }
}
