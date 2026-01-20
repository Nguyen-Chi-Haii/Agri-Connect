import SwiftUI

@main
struct FE_IOSApp: App {
    @ObservedObject private var tokenManager = TokenManager.shared
    
    var body: some Scene {
        WindowGroup {
            let _ = print("📱 [FE_IOSApp] Body Re-evaluating. isLoggedIn: \(tokenManager.isLoggedIn), Role: \(tokenManager.userRole ?? "Nil")")
            Group {
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
            .id(tokenManager.isLoggedIn) // Force full rebuild when login state changes
        }
    }
}
