import SwiftUI

struct MainTabView: View {
    @State private var selectedTab = 0
    @State private var showCreatePost = false
    @State private var notificationBadgeCount = 0
    @State private var chatBadgeCount = 0
    
    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            TabView(selection: $selectedTab) {
                // Home Tab
                NavigationView {
                    HomeView()
                }
                .navigationViewStyle(StackNavigationViewStyle())
                .tabItem {
                    Image(systemName: "house.fill")
                    Text("Trang chủ")
                }
                .tag(0)
                
                // Market Tab
                NavigationView {
                    MarketView()
                }
                .navigationViewStyle(StackNavigationViewStyle())
                .tabItem {
                    Image(systemName: "chart.line.uptrend.xyaxis")
                    Text("Thị trường")
                }
                .tag(1)
                
                // Chat Tab
                NavigationView {
                    ChatListView()
                }
                .navigationViewStyle(StackNavigationViewStyle())
                .tabItem {
                    Image(systemName: "message.fill")
                    Text("Tin nhắn")
                }
                .badge(chatBadgeCount)
                .tag(2)
                
                // Notification Tab
                NavigationView {
                    NotificationListView()
                }
                .navigationViewStyle(StackNavigationViewStyle())
                .tabItem {
                    Image(systemName: "bell.fill")
                    Text("Thông báo")
                }
                .badge(notificationBadgeCount)
                .tag(3)

                // Profile Tab
                NavigationView {
                    ProfileView(tabSelection: $selectedTab)
                }
                .navigationViewStyle(StackNavigationViewStyle())
                .tabItem {
                    Image(systemName: "person.fill")
                    Text("Cá nhân")
                }
                .tag(4)
            }
            .accentColor(Color(hex: "#2E7D32"))
            
            // Floating Action Button
            if selectedTab == 0 {
                Button(action: {
                    showCreatePost = true
                }) {
                    Image(systemName: "plus")
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(.white)
                        .frame(width: 56, height: 56)
                        .background(Color(hex: "#2E7D32"))
                        .clipShape(Circle())
                        .shadow(radius: 4)
                }
                .padding(.trailing, 20)
                .padding(.bottom, 80) // Safe padding above tab bar
            }
        }
        .sheet(isPresented: $showCreatePost) {
            NavigationView {
                CreatePostView(tabSelection: $selectedTab)
            }
        }
        .onAppear {
            fetchBadgeCounts()
        }
        .onReceive(NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)) { _ in
            fetchBadgeCounts()
        }
    }
    
    private func fetchBadgeCounts() {
        // Fetch notification count
        APIClient.shared.request(
            endpoint: APIConfig.Notifications.unreadCount,
            method: .get
        ) { (result: Result<ApiResponse<Int>, Error>) in
            switch result {
            case .success(let response):
                DispatchQueue.main.async {
                    let count = response.data ?? 0
                    print("🔔 Notification badge count from API: \(count)")
                    notificationBadgeCount = count
                }
            case .failure(let error):
                print("❌ Failed to fetch notification count: \(error)")
                // Set test value to verify badge works
                DispatchQueue.main.async {
                    notificationBadgeCount = 3  // Test value
                }
            }
        }
        
        // TODO: Fetch chat count when API is available
        // chatBadgeCount will stay at 0 until chat unread count API is implemented
    }
    
    // Removed checkRedirect as it was only for the old CreatePost tab logic
    private func checkRedirect(_ tag: Int) {
        // No longer needed
    }
}

struct MainTabView_Previews: PreviewProvider {
    static var previews: some View {
        MainTabView()
    }
}
