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
            
            // Profile Tab
            NavigationView {
                ProfileView()
            }
            .navigationViewStyle(StackNavigationViewStyle())
            .tabItem {
                Image(systemName: "person.fill")
                Text("Cá nhân")
            }
            .tag(4)
        }
        .accentColor(Color(hex: "#2E7D32"))
        .onChange(of: selectedTab) { tag in
            if tag == 2 {
                // Check Verification
                if let user = TokenManager.shared.userProfile {
                    if !user.isVerified {
                        selectedTab = 4
                    }
                }
            }
        }
    }
}

struct MainTabView_Previews: PreviewProvider {
    static var previews: some View {
        MainTabView()
    }
}
