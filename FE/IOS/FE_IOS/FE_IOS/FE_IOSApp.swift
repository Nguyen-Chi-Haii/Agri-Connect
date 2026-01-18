import SwiftUI

@main
struct FE_IOSApp: App {
    @ObservedObject private var tokenManager = TokenManager.shared
    
    var body: some Scene {
        WindowGroup {
            let _ = print("📱 [FE_IOSApp] Body Re-evaluating. isLoggedIn: \(tokenManager.isLoggedIn), Role: \(tokenManager.userRole ?? "Nil")")
            if tokenManager.isLoggedIn {
                if tokenManager.userRole == "ADMIN" {
                    AdminTabView() // Ensure this view is valid
                } else {
                    MainTabView()
                }
            } else {
                LoginView()
            }
            } else {
                LoginView()
            }
        }
    }
}
