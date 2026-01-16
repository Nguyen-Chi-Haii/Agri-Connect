import SwiftUI

@main
struct FE_IOSApp: App {
    @ObservedObject private var tokenManager = TokenManager.shared
    
    var body: some Scene {
        WindowGroup {
            if tokenManager.isLoggedIn {
                if tokenManager.userRole == "ADMIN" {
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
