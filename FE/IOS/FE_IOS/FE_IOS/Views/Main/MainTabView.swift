import SwiftUI

struct MainTabView: View {
    @State private var selectedTab = 0
    @State private var showCreatePost = false
    
    var body: some View {
        ZStack(alignment: .bottom) {
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
            .offset(y: -60) // Adjust position above tab bar
        }
        .sheet(isPresented: $showCreatePost) {
            NavigationView {
                CreatePostView(tabSelection: $selectedTab)
            }
        }
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
