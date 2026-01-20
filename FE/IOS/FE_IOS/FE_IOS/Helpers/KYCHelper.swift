import Foundation
import SwiftUI

class KYCHelper {
    static let shared = KYCHelper()
    
    private init() {}
    
    // MARK: - Verification Checks
    
    /// Check if user is logged in
    func requireLogin(onSuccess: @escaping () -> Void, onFailure: @escaping (String) -> Void) {
        guard TokenManager.shared.isLoggedIn else {
            onFailure("Bạn cần đăng nhập để thực hiện chức năng này")
            return
        }
        onSuccess()
    }
    
    /// Check if user is verified (KYC approved)
    func requireVerified(onSuccess: @escaping () -> Void, onFailure: @escaping (String, String?) -> Void) {
        // First check login
        guard TokenManager.shared.isLoggedIn else {
            onFailure("Yêu cầu đăng nhập", "Bạn cần đăng nhập để thực hiện chức năng này")
            return
        }
        
        // Fetch user profile to check KYC status
        APIClient.shared.request(
            endpoint: "/users/profile",
            method: .get
        ) { (result: Result<ApiResponse<UserProfile>, Error>) in
            if case .success(let response) = result, let profile = response.data {
                let kycStatus = profile.kycStatus ?? "NONE"
                
                switch kycStatus {
                case "VERIFIED":
                    onSuccess()
                    
                case "PENDING":
                    onFailure(
                        "Đang chờ duyệt",
                        "Hồ sơ xác thực của bạn đang được xem xét. Vui lòng chờ admin phê duyệt."
                    )
                    
                case "REJECTED":
                    onFailure(
                        "Xác thực bị từ chối",
                        "Hồ sơ xác thực của bạn đã bị từ chối. Vui lòng cập nhật lại thông tin trong phần Hồ sơ."
                    )
                    
                default: // NONE or missing
                    let role = TokenManager.shared.userRole ?? "FARMER"
                    let message = role == "FARMER"
                        ? "Bạn cần xác thực danh tính bằng CCCD để sử dụng chức năng này.\n\nVui lòng vào Hồ sơ > Xác thực để hoàn tất."
                        : "Bạn cần xác thực danh tính bằng Mã số thuế để sử dụng chức năng này.\n\nVui lòng vào Hồ sơ > Xác thực để hoàn tất."
                    
                    onFailure("Yêu cầu xác thực", message)
                }
            } else {
                onFailure("Lỗi", "Không thể kiểm tra trạng thái xác thực. Vui lòng thử lại.")
            }
        }
    }
    
    // MARK: - SwiftUI View Modifiers
    
    /// Show alert with KYC requirement
    static func showKYCAlert(title: String, message: String, navigateToProfile: Binding<Bool>?) -> Alert {
        if let navigate = navigateToProfile {
            return Alert(
                title: Text(title),
                message: Text(message),
                primaryButton: .default(Text("Xác thực ngay")) {
                    navigate.wrappedValue = true
                },
                secondaryButton: .cancel(Text("Để sau"))
            )
        } else {
            return Alert(
                title: Text(title),
                message: Text(message),
                dismissButton: .default(Text("Đã hiểu"))
            )
        }
    }
}
