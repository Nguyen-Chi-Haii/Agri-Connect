import SwiftUI

@main
struct FE_IOSApp: App {
    var body: some Scene {
        WindowGroup {
            if TokenManager.shared.isLoggedIn {
                if TokenManager.shared.userRole == "ADMIN" {
                    AdminDashboardView()
                } else {
                    MainTabView()
                }
            } else {
                LoginView()
            }
        }
    }
}
