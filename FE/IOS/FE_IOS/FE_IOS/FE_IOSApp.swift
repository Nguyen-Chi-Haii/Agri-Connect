import SwiftUI

@main
struct FE_IOSApp: App {
    var body: some Scene {
        WindowGroup {
            if TokenManager.shared.isLoggedIn {
                if TokenManager.shared.userRole == "ADMIN" {
                    AdminTabView()
                } else {
                    MainTabView()
                }
            } else {
                LoginView()
            }
        }
    }
}
