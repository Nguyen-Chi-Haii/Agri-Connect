import SwiftUI

struct MainTabView: View {
    @State private var selectedTab = 0
    
    var body: some View {
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
            
            // Create Post Tab
            NavigationView {
                CreatePostView(tabSelection: $selectedTab)
            }
            .navigationViewStyle(StackNavigationViewStyle())
            .tabItem {
                Image(systemName: "plus.circle.fill")
                Text("Đăng bài")
            }
            .tag(2)
            
            // Chat Tab
            NavigationView {
                ChatListView()
            }
            .navigationViewStyle(StackNavigationViewStyle())
            .tabItem {
                Image(systemName: "message.fill")
                Text("Tin nhắn")
            }
            .tag(3)
            
            // Notification Tab
            NavigationView {
                NotificationListView()
            }
            .navigationViewStyle(StackNavigationViewStyle())
            .tabItem {
                Image(systemName: "bell.fill")
                Text("Thông báo")
            }
            .tag(4)

            // Profile Tab
            NavigationView {
                ProfileView(tabSelection: $selectedTab)
            }
            .navigationViewStyle(StackNavigationViewStyle())
            .tabItem {
                Image(systemName: "person.fill")
                Text("Cá nhân")
            }
            .tag(5)
        }
        .accentColor(Color(hex: "#2E7D32"))
        .onChange(of: selectedTab) { (tag: Int) in
            checkRedirect(tag)
        }
    }
    
    private func checkRedirect(_ tag: Int) {
        guard tag == 2 else { return }
        guard let user = TokenManager.shared.userProfile else { return }
        
        if !user.isVerified {
            DispatchQueue.main.async {
                self.selectedTab = 4
            }
        }
    }
}

struct MainTabView_Previews: PreviewProvider {
    static var previews: some View {
        MainTabView()
    }
}
