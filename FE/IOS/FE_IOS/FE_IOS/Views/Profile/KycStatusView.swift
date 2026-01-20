import SwiftUI

struct KycStatusView: View {
    @State private var userProfile: UserProfile?
    @State private var isLoading = false
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Status Header
                VStack(spacing: 16) {
                    ZStack {
                        Circle()
                            .fill(statusColor.opacity(0.1))
                            .frame(width: 100, height: 100)
                        
                        Image(systemName: statusIcon)
                            .font(.system(size: 50))
                            .foregroundColor(statusColor)
                    }
                    
                    Text(statusTitle)
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    Text(statusDescription)
                        .font(.subheadline)
                        .foregroundColor(.gray)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal)
                }
                .padding(.top, 40)
                
                // Details
                if let kyc = userProfile?.kyc {
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Thông tin xác minh")
                            .font(.headline)
                            .padding(.horizontal)
                        
                        VStack(spacing: 0) {
                            DetailInfoRow(title: "Loại xác minh", value: userProfile?.role == "FARMER" ? "Cá nhân (CCCD)" : "Doanh nghiệp (MST)")
                            Divider()
                            if let idNum = kyc.idNumber {
                                DetailInfoRow(title: "Số định danh", value: idNum)
                                Divider()
                            }
                            if let taxCode = kyc.taxCode {
                                DetailInfoRow(title: "Mã số thuế", value: taxCode)
                                Divider()
                            }
                        }
                        .background(Color.white)
                        .cornerRadius(12)
                        .padding(.horizontal)
                        
                        // ID Images
                        if let front = kyc.idFrontImage {
                            VStack(alignment: .leading, spacing: 8) {
                                Text("Hình ảnh xác minh")
                                    .font(.headline)
                                
                                HStack(spacing: 12) {
                                    KycImageView(url: front, title: "Mặt trước")
                                    if let back = kyc.idBackImage {
                                        KycImageView(url: back, title: "Mặt sau")
                                    }
                                }
                            }
                            .padding(.horizontal)
                        }
                        
                        if let reason = kyc.reason {
                            VStack(alignment: .leading, spacing: 8) {
                                Text("Lý do từ chối")
                                    .font(.headline)
                                    .foregroundColor(.red)
                                Text(reason)
                                    .foregroundColor(.secondary)
                            }
                            .padding()
                            .background(Color.red.opacity(0.1))
                            .cornerRadius(12)
                            .padding(.horizontal)
                        }
                    }
                }
                
                Spacer()
                
                if userProfile?.kycStatus == "NONE" || userProfile?.kycStatus == "REJECTED" {
                    NavigationLink(destination: SubmitKycView()) {
                        Text("Bắt đầu xác minh")
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color(hex: "#2E7D32"))
                            .cornerRadius(12)
                    }
                    .padding(.horizontal)
                }
            }
        }
        .background(Color(.systemGray6))
        .navigationTitle("Trạng thái xác minh")
        .onAppear { loadProfile() }
    }
    
    private func loadProfile() {
        isLoading = true
        APIClient.shared.request(
            endpoint: APIConfig.Users.profile,
            method: .get
        ) { (result: Result<ApiResponse<UserProfile>, Error>) in
            isLoading = false
            if case .success(let response) = result, let data = response.data {
                userProfile = data
            }
        }
    }
    
    private var statusTitle: String {
        switch userProfile?.kycStatus {
        case "APPROVED", "VERIFIED": return "Đã xác minh"
        case "PENDING": return "Đang chờ duyệt"
        case "REJECTED": return "Bị từ chối"
        default: return "Chưa xác minh"
        }
    }
    
    private var statusDescription: String {
        switch userProfile?.kycStatus {
        case "APPROVED", "VERIFIED": return "Tài khoản của bạn đã được xác minh chính chủ. Bạn có đầy đủ quyền hạn trên hệ thống."
        case "PENDING": return "Yêu cầu của bạn đang được quản trị viên xem duyệt. Vui lòng đợi trong vòng 24-48h."
        case "REJECTED": return "Yêu cầu của bạn không được chấp nhận. Vui lòng kiểm tra lý do và thực hiện lại."
        default: return "Xác minh danh tính giúp tăng uy tín và bảo mật cho tài khoản của bạn."
        }
    }
    
    private var statusIcon: String {
        switch userProfile?.kycStatus {
        case "APPROVED", "VERIFIED": return "checkmark.seal.fill"
        case "PENDING": return "clock.fill"
        case "REJECTED": return "exclamationmark.shield.fill"
        default: return "person.badge.shield.checkmark"
        }
    }
    
    private var statusColor: Color {
        switch userProfile?.kycStatus {
        case "APPROVED", "VERIFIED": return .green
        case "PENDING": return .orange
        case "REJECTED": return .red
        default: return .blue
        }
    }
}

struct DetailInfoRow: View {
    let title: String
    let value: String
    
    var body: some View {
        HStack {
            Text(title)
                .foregroundColor(.gray)
            Spacer()
            Text(value)
                .fontWeight(.medium)
        }
        .padding()
    }
}

struct KycImageView: View {
    let url: String
    let title: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.caption)
                .foregroundColor(.gray)
            
            if let imageUrl = URL(string: url) {
                AsyncImage(url: imageUrl) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .frame(maxWidth: .infinity)
                            .frame(height: 120)
                            .clipped()
                            .cornerRadius(8)
                    case .failure(_):
                        placeholderView
                    case .empty:
                        ProgressView()
                            .frame(maxWidth: .infinity)
                            .frame(height: 120)
                    @unknown default:
                        EmptyView()
                    }
                }
            } else {
                placeholderView
            }
        }
        .frame(maxWidth: .infinity)
    }
    
    private var placeholderView: some View {
        Rectangle()
            .fill(Color.gray.opacity(0.1))
            .frame(maxWidth: .infinity)
            .frame(height: 120)
            .cornerRadius(8)
            .overlay(
                Image(systemName: "photo")
                    .foregroundColor(.gray)
            )
    }
}

// Placeholder for SubmitKycView
struct SubmitKycView: View {
    var body: some View {
        Text("Form xác minh danh tính")
            .navigationTitle("Xác minh danh tính")
    }
}
