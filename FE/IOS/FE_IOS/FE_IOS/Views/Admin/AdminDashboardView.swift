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
                                    Text(greetingMessage)
                                        .font(.title2)
                                        .fontWeight(.bold)
                                    Text(formattedDate)
                                        .font(.subheadline)
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
                            
                            // Quick Actions
                            if let stats = stats {
                                VStack(spacing: 12) {
                                    if (stats.pendingPosts ?? 0) > 0 {
                                        NavigationLink(destination: AdminPostsView()) { // Navigation to pending filter handled in view? Or pass init param
                                           QuickActionRow(
                                                icon: "doc.text.badge.plus",
                                                text: "Có \(stats.pendingPosts ?? 0) bài đăng cần duyệt",
                                                color: .orange
                                           )
                                        }
                                    }
                                    
                                    if (stats.pendingKyc ?? 0) > 0 {
                                        NavigationLink(destination: AdminUsersView()) {
                                            QuickActionRow(
                                                icon: "person.crop.circle.badge.exclamationmark",
                                                text: "Có \(stats.pendingKyc ?? 0) yêu cầu KYC",
                                                color: .purple
                                            )
                                        }
                                    }
                                }
                            }
                            
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
        print("📡 [AdminDashboard] Loading stats from: \(APIConfig.Admin.dashboardStats)")
        
        APIClient.shared.request(
            endpoint: APIConfig.Admin.dashboardStats,
            method: .get
        ) { (result: Result<ApiResponse<DashboardStats>, Error>) in
            isLoading = false
            switch result {
            case .success(let response):
                print("✅ [AdminDashboard] Success: \(response.success)")
                if let data = response.data {
                    print("✅ [AdminDashboard] Data: Users=\(data.totalUsers ?? -1), Posts=\(data.totalPosts ?? -1)")
                    stats = data
                } else {
                    print("⚠️ [AdminDashboard] Response data is nil")
                }
            case .failure(let error):
                print("❌ [AdminDashboard] Error: \(error)")
            }
        }
    }
    

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

struct QuickActionRow: View {
    let icon: String
    let text: String
    let color: Color
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(color)
            Text(text)
                .foregroundColor(.black) // or primary
            Spacer()
            Image(systemName: "chevron.right")
                .foregroundColor(.gray)
                .font(.caption)
        }
        .padding()
        .background(color.opacity(0.1))
        .cornerRadius(12)
    }
}

extension AdminDashboardView {
    var greetingMessage: String {
        let hour = Calendar.current.component(.hour, from: Date())
        if hour < 12 { return "Chào buổi sáng" }
        else if hour < 18 { return "Chào buổi chiều" }
        else { return "Chào buổi tối" }
    }
    
    var formattedDate: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "EEEE, dd/MM/yyyy"
        formatter.locale = Locale(identifier: "vi_VN")
        return formatter.string(from: Date()).capitalized
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
