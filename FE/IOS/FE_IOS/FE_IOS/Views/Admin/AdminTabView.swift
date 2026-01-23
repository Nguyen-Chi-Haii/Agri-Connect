import SwiftUI

struct AdminTabView: View {
    @State private var selectedTab = 0
    
    var body: some View {
        TabView(selection: $selectedTab) {
            // Dashboard Tab
            AdminDashboardView()
                .tabItem {
                    Image(systemName: "square.grid.2x2.fill")
                    Text("Tổng quan")
                }
                .tag(0)
            
            // Posts Tab
            NavigationView {
                AdminPostsView()
            }
            .navigationViewStyle(StackNavigationViewStyle())
            .tabItem {
                Image(systemName: "doc.richtext.fill")
                Text("Bài đăng")
            }
            .tag(1)
            
            // Users Tab
            NavigationView {
                AdminUsersView()
            }
            .navigationViewStyle(StackNavigationViewStyle())
            .tabItem {
                Image(systemName: "person.2.fill")
                Text("Người dùng")
            }
            .tag(2)
            
            // Categories Tab
            NavigationView {
                AdminCategoriesView()
            }
            .navigationViewStyle(StackNavigationViewStyle())
            .tabItem {
                Image(systemName: "folder.fill")
                Text("Danh mục")
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

            // Settings Tab
            AdminSettingsView()
                .tabItem {
                    Image(systemName: "gearshape.fill")
                    Text("Cài đặt")
                }
                .tag(5)
        }
        .accentColor(Color(hex: "#2E7D32"))
    }
}

struct AdminTabView_Previews: PreviewProvider {
    static var previews: some View {
        AdminTabView()
    }
}
